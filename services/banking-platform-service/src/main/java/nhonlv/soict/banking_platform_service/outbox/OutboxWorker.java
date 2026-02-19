package nhonlv.soict.banking_platform_service.outbox;

import java.time.OffsetDateTime;
import java.util.List;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nhonlv.soict.banking_platform_service.payments.domain.Payment;
import nhonlv.soict.banking_platform_service.payments.domain.PaymentStatus;
import nhonlv.soict.banking_platform_service.payments.repo.PaymentRepository;
import nhonlv.soict.banking_platform_service.outbox.domain.OutboxEvent;
import nhonlv.soict.banking_platform_service.outbox.domain.OutboxEventStatus;
import nhonlv.soict.banking_platform_service.outbox.domain.PaymentAttempt;
import nhonlv.soict.banking_platform_service.outbox.repo.OutboxEventRepository;
import nhonlv.soict.banking_platform_service.outbox.repo.PaymentAttemptRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxWorker {

  private static final int BATCH_SIZE = 20;
  private static final int MAX_ATTEMPTS = 5;

  private final OutboxEventRepository outboxEventRepository;
  private final PaymentAttemptRepository paymentAttemptRepository;
  private final PaymentRepository paymentRepository;
  private final ObjectMapper objectMapper;

  public OutboxWorker(OutboxEventRepository outboxEventRepository,
      PaymentAttemptRepository paymentAttemptRepository,
      PaymentRepository paymentRepository,
      ObjectMapper objectMapper) {
    this.outboxEventRepository = outboxEventRepository;
    this.paymentAttemptRepository = paymentAttemptRepository;
    this.paymentRepository = paymentRepository;
    this.objectMapper = objectMapper;
  }

  @Scheduled(fixedDelay = 2000)
  @Transactional
  public void run() {
    List<OutboxEvent> dueEvents = outboxEventRepository.findTopNNewAvailableOrderByCreatedAt(
        OffsetDateTime.now(),
        BATCH_SIZE
    );

    for (OutboxEvent event : dueEvents) {
      processEvent(event);
    }
  }

  @Transactional
  private void processEvent(OutboxEvent event) {
    OffsetDateTime now = OffsetDateTime.now();
    Payment payment = paymentRepository.findById(event.getAggregateId())
        .orElseThrow(() -> new IllegalStateException("Payment not found for outbox event " + event.getId()));

    int attemptNo = paymentAttemptRepository
        .findTopByPaymentIdOrderByAttemptNoDesc(event.getAggregateId())
        .map(previous -> previous.getAttemptNo() + 1)
        .orElse(1);

    event.setStatus(OutboxEventStatus.PROCESSING);
    event.setUpdatedAt(now);
    outboxEventRepository.save(event);

    try {
      process(event);

      event.setStatus(OutboxEventStatus.DONE);
      event.setUpdatedAt(OffsetDateTime.now());
      outboxEventRepository.save(event);

      payment.setStatus(PaymentStatus.SUCCEEDED);
      paymentRepository.save(payment);

      paymentAttemptRepository.save(PaymentAttempt.success(event.getAggregateId(), attemptNo));
    } catch (Exception ex) {
      paymentAttemptRepository.save(PaymentAttempt.failed(
          event.getAggregateId(),
          attemptNo,
          "PROCESSING_ERROR",
          trimMessage(ex.getMessage())
      ));

      if (attemptNo >= MAX_ATTEMPTS) {
        event.setStatus(OutboxEventStatus.DEAD);
        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
      } else {
        event.setStatus(OutboxEventStatus.NEW);
        event.setAvailableAt(OffsetDateTime.now().plusSeconds(backoffSeconds(attemptNo)));
      }

      event.setUpdatedAt(OffsetDateTime.now());
      outboxEventRepository.save(event);
    }
  }

  private void process(OutboxEvent event) throws Exception {
    if (event.getEventType() == null) {
      throw new IllegalStateException("Outbox event type is missing");
    }

    // Demo: deterministic transient error if amountMinor divisible by 5
    String payload = event.getPayload();
    JsonNode node = objectMapper.readTree(payload);
    long amountMinor = node.get("amountMinor").asLong();

    if (amountMinor % 5 == 0) {
      throw new RuntimeException("Transient processing error for demo (amountMinor divisible by 5)");
    }
  }

  private long backoffSeconds(int attemptNo) {
    return Math.min(60L * attemptNo, 300L);
  }

  private String trimMessage(String message) {
    if (message == null || message.isBlank()) {
      return "Unknown error";
    }
    return message.length() <= 500 ? message : message.substring(0, 500);
  }
}
