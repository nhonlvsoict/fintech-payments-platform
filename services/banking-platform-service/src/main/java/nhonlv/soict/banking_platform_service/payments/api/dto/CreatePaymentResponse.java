package nhonlv.soict.banking_platform_service.payments.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import nhonlv.soict.banking_platform_service.payments.domain.PaymentStatus;

public record CreatePaymentResponse(
    UUID paymentId,
    PaymentStatus status,
    OffsetDateTime createdAt
) {}
