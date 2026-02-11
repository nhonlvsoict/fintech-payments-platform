package nhonlv.soict.banking_platform_service.payments.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record CreatePaymentRequest(
    @NotNull UUID clientId,
    @Positive long amountMinor,
    @NotBlank String currency,
    String rail,
    String reference
) {}
