package nhonlv.soict.banking_platform_service.outbox.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_attempts")
public class PaymentAttempt {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Column(name = "payment_id", nullable = false)
  private UUID paymentId;

  @Column(name = "attempt_no", nullable = false)
  private int attemptNo;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "error_code", length = 50)
  private String errorCode;

  @Column(name = "error_message", length = 500)
  private String errorMessage;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  protected PaymentAttempt() {
    // JPA only
  }

  public PaymentAttempt(UUID id, UUID paymentId, int attemptNo, String status,
      String errorCode, String errorMessage, OffsetDateTime createdAt) {
    this.id = id;
    this.paymentId = paymentId;
    this.attemptNo = attemptNo;
    this.status = status;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.createdAt = createdAt;
  }

  public static PaymentAttempt failed(UUID paymentId, int attemptNo, String errorCode, String errorMessage) {
    return new PaymentAttempt(
        UUID.randomUUID(),
        paymentId,
        attemptNo,
        "FAILED",
        errorCode,
        errorMessage,
        OffsetDateTime.now()
    );
  }

  public static PaymentAttempt success(UUID paymentId, int attemptNo) {
    return new PaymentAttempt(
        UUID.randomUUID(),
        paymentId,
        attemptNo,
        "SUCCESS",
        null,
        null,
        OffsetDateTime.now()
    );
  }

  public int getAttemptNo() {
    return attemptNo;
  }
}
