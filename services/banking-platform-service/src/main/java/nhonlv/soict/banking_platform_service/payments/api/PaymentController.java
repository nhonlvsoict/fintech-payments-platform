package nhonlv.soict.banking_platform_service.payments.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import nhonlv.soict.banking_platform_service.payments.api.dto.CreatePaymentRequest;
import nhonlv.soict.banking_platform_service.payments.api.dto.CreatePaymentResponse;
import nhonlv.soict.banking_platform_service.payments.api.dto.PaymentResponse;
import nhonlv.soict.banking_platform_service.payments.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
  public ResponseEntity<CreatePaymentResponse> create(@Valid @RequestBody CreatePaymentRequest req) {
    CreatePaymentResponse response = paymentService.create(req);
    URI location = URI.create("/v1/payments/" + response.paymentId());
    return ResponseEntity.created(location).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<PaymentResponse> getById(@PathVariable UUID id) {
    return paymentService.getById(id)
        .map(payment -> ResponseEntity.ok(new PaymentResponse(
            payment.getId(),
            payment.getClientId(),
            payment.getAmountMinor(),
            payment.getCurrency(),
            payment.getStatus(),
            payment.getCreatedAt()
        )))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
