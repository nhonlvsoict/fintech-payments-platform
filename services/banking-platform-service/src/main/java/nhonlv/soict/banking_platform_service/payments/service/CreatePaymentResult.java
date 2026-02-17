package nhonlv.soict.banking_platform_service.payments.service;

import nhonlv.soict.banking_platform_service.payments.api.dto.CreatePaymentResponse;

public record CreatePaymentResult(ResultType type, CreatePaymentResponse response) {
  public enum ResultType {
    CREATED,
    COMPLETED,
    IN_PROGRESS,
    HASH_CONFLICT
  }

  public static CreatePaymentResult created(CreatePaymentResponse response) {
    return new CreatePaymentResult(ResultType.CREATED, response);
  }

  public static CreatePaymentResult completed(CreatePaymentResponse response) {
    return new CreatePaymentResult(ResultType.COMPLETED, response);
  }

  public static CreatePaymentResult inProgress() {
    return new CreatePaymentResult(ResultType.IN_PROGRESS, null);
  }

  public static CreatePaymentResult hashConflict() {
    return new CreatePaymentResult(ResultType.HASH_CONFLICT, null);
  }
}
