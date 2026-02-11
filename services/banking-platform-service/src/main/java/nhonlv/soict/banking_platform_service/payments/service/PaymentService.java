package nhonlv.soict.banking_platform_service.payments.service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import nhonlv.soict.banking_platform_service.payments.api.dto.CreatePaymentRequest;
import nhonlv.soict.banking_platform_service.payments.api.dto.CreatePaymentResponse;
import nhonlv.soict.banking_platform_service.payments.domain.Payment;
import nhonlv.soict.banking_platform_service.payments.domain.PaymentStatus;
import nhonlv.soict.banking_platform_service.payments.repo.PaymentRepository;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

  private final PaymentRepository paymentRepository;

  public PaymentService(PaymentRepository paymentRepository) {
    this.paymentRepository = paymentRepository;
  }

  public CreatePaymentResponse create(CreatePaymentRequest req) {
      Payment payment = new Payment(UUID.randomUUID(),
              req.clientId(),
              req.amountMinor(),
              req.currency(),
              PaymentStatus.CREATED,
              OffsetDateTime.now());

    Payment savedPayment = paymentRepository.save(payment);

    return new CreatePaymentResponse(
        savedPayment.getId(),
        savedPayment.getStatus(),
        savedPayment.getCreatedAt()
    );
  }

  public Optional<Payment> getById(UUID id) {
    return paymentRepository.findById(id);
  }
}
