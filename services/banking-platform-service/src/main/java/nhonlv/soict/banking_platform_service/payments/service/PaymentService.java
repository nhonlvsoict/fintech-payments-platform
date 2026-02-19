package nhonlv.soict.banking_platform_service.payments.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import nhonlv.soict.banking_platform_service.outbox.domain.OutboxEvent;
import nhonlv.soict.banking_platform_service.outbox.repo.OutboxEventRepository;
import nhonlv.soict.banking_platform_service.payments.api.dto.CreatePaymentRequest;
import nhonlv.soict.banking_platform_service.payments.api.dto.CreatePaymentResponse;
import nhonlv.soict.banking_platform_service.payments.domain.IdempotencyKey;
import nhonlv.soict.banking_platform_service.payments.domain.Payment;
import nhonlv.soict.banking_platform_service.payments.domain.PaymentStatus;
import nhonlv.soict.banking_platform_service.payments.repo.IdempotencyKeyRepository;
import nhonlv.soict.banking_platform_service.payments.repo.PaymentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final IdempotencyKeyRepository idempotencyKeyRepository;
  private final OutboxEventRepository outboxEventRepository;
  private final ObjectMapper objectMapper;

  public PaymentService(PaymentRepository paymentRepository,
      IdempotencyKeyRepository idempotencyKeyRepository,
      OutboxEventRepository outboxEventRepository,
      ObjectMapper objectMapper) {
    this.paymentRepository = paymentRepository;
    this.idempotencyKeyRepository = idempotencyKeyRepository;
    this.outboxEventRepository = outboxEventRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public CreatePaymentResult create(CreatePaymentRequest req, String idempotencyKey) {
    String requestHash = computeRequestHash(req);
    OffsetDateTime now = OffsetDateTime.now();
    IdempotencyKey newRecord = IdempotencyKey.inProgress(req.clientId(), idempotencyKey, requestHash, now);

    try {
      idempotencyKeyRepository.saveAndFlush(newRecord);
    } catch (DataIntegrityViolationException ex) {
      IdempotencyKey existing = idempotencyKeyRepository
          .findByClientIdAndIdempotencyKey(req.clientId(), idempotencyKey)
          .orElseThrow(() -> ex);

      if (!existing.getRequestHash().equals(requestHash)) {
        return CreatePaymentResult.hashConflict();
      }

      if (IdempotencyKey.STATUS_COMPLETED.equals(existing.getStatus())) {
        return CreatePaymentResult.completed(parseResponse(existing.getResponseBody()));
      }

      return CreatePaymentResult.inProgress();
    }

    Payment payment = new Payment(UUID.randomUUID(),
        req.clientId(),
        req.amountMinor(),
        req.currency(),
        PaymentStatus.CREATED,
        OffsetDateTime.now());

    Payment savedPayment = paymentRepository.save(payment);
  
    OutboxEvent outboxEvent = OutboxEvent.paymentProcessRequested(
      savedPayment.getId(), 
      buildOutboxPayload(savedPayment.getId(), req.clientId(), req.rail())
    );
 
    outboxEventRepository.save(outboxEvent);

    CreatePaymentResponse response = CreatePaymentResponse.from(savedPayment);

    newRecord.markCompleted(201, toJson(response), OffsetDateTime.now());
    idempotencyKeyRepository.save(newRecord);

    return CreatePaymentResult.created(response);
  }

  public Optional<Payment> getById(UUID id) {
    return paymentRepository.findById(id);
  }

  private String computeRequestHash(CreatePaymentRequest req) {
    String payload = req.clientId() + "|"
        + req.amountMinor() + "|"
        + normalize(req.currency()) + "|"
        + normalize(req.rail()) + "|"
        + normalize(req.reference());
    return sha256Hex(payload);
  }

  private String normalize(String value) {
    return value == null ? "" : value.trim().toUpperCase();
  }

  private String sha256Hex(String payload) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
      StringBuilder builder = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        builder.append(String.format("%02x", b));
      }
      return builder.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  private String toJson(CreatePaymentResponse response) {
    try {
      return objectMapper.writeValueAsString(response);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize create payment response", e);
    }
  }

  private CreatePaymentResponse parseResponse(String responseBody) {
    try {
      return objectMapper.readValue(responseBody, CreatePaymentResponse.class);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to deserialize stored create payment response", e);
    }
  }

  private String buildOutboxPayload(UUID paymentId, UUID clientId, String rail) {
    Map<String, String> payload = new LinkedHashMap<>();
    payload.put("paymentId", paymentId.toString());
    payload.put("clientId", clientId.toString());
    payload.put("rail", rail);
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Unable to serialize outbox payload", e);
    }
  }
}
