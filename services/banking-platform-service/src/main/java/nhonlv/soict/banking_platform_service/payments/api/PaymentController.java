package nhonlv.soict.banking_platform_service.payments.api;

import jakarta.validation.Valid;
import java.net.URI;
import nhonlv.soict.banking_platform_service.payments.api.dto.ApiResponse;
import java.util.UUID;
import nhonlv.soict.banking_platform_service.payments.api.dto.CreatePaymentRequest;
import nhonlv.soict.banking_platform_service.payments.api.dto.CreatePaymentResponse;
import nhonlv.soict.banking_platform_service.payments.api.dto.PaymentResponse;
import nhonlv.soict.banking_platform_service.payments.service.CreatePaymentResult;
import nhonlv.soict.banking_platform_service.payments.service.PaymentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/payments")
public class PaymentController {

  private final PaymentService paymentService;

  public PaymentController(PaymentService paymentService) {
    this.paymentService = paymentService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse<CreatePaymentResponse>> create(
      @RequestHeader("Idempotency-Key") String idempotencyKey,
      @Valid @RequestBody CreatePaymentRequest req) {
    CreatePaymentResult result = paymentService.create(req, idempotencyKey);

    if (result.type() == CreatePaymentResult.ResultType.HASH_CONFLICT) {
      return ResponseEntity.status(409).body(ApiResponse.failure(
          "IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD",
          "Same Idempotency-Key was used with a different request payload"
      ));
    }

    if (result.type() == CreatePaymentResult.ResultType.IN_PROGRESS) {
      return ResponseEntity.accepted()
          .header(HttpHeaders.RETRY_AFTER, "1")
        .body(ApiResponse.pending(
              "IN_PROGRESS",
              "Payment request is still being processed"
          ));
    }

    CreatePaymentResponse response = result.response();
    URI location = URI.create("/v1/payments/" + response.paymentId());
    if (result.type() == CreatePaymentResult.ResultType.COMPLETED) {
      return ResponseEntity.ok(ApiResponse.success(response));
    }
    return ResponseEntity.created(location).body(ApiResponse.success(response));
  }

  @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(@PathVariable UUID id) {
    return paymentService.getById(id)
      .map(payment -> ResponseEntity.ok(ApiResponse.success(new PaymentResponse(
            payment.getId(),
            payment.getClientId(),
            payment.getAmountMinor(),
            payment.getCurrency(),
            payment.getStatus(),
            payment.getCreatedAt()
      ))))
      .orElseGet(() -> ResponseEntity.status(404)
        .body(ApiResponse.failure(
          "PAYMENT_NOT_FOUND",
          "Payment was not found for the provided id"
        )));
  }
}
