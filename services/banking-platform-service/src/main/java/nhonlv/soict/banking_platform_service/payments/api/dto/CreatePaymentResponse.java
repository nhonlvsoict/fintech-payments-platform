package nhonlv.soict.banking_platform_service.payments.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import nhonlv.soict.banking_platform_service.payments.domain.Payment;
import nhonlv.soict.banking_platform_service.payments.domain.PaymentStatus;

public record CreatePaymentResponse(
    UUID paymentId,
    PaymentStatus status,
    OffsetDateTime createdAt
) {
    static public CreatePaymentResponse from(Payment payment) {
        return new CreatePaymentResponse(
            payment.getId(),
            payment.getStatus(),
            payment.getCreatedAt()
        );
    }
}
