package nhonlv.soict.banking_platform_service.payments.domain;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {
  public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
  public static final String STATUS_COMPLETED = "COMPLETED";

  @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;
  
    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;
    
    @Column(name = "request_hash", nullable = false)
    private String requestHash;
    
    @Column(name = "status", nullable = false)
    private String status;
  
    @Column(name = "response_code")
    private Integer responseCode;
    
    @Column(name = "response_body")
    private String responseBody;
  
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected IdempotencyKey() {}

  
    public IdempotencyKey(UUID clientId, String idempotencyKey, String requestHash, String status,
      Integer responseCode, String responseBody, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
      this.id = UUID.randomUUID();
      this.clientId = clientId;
      this.idempotencyKey = idempotencyKey;
      this.requestHash = requestHash;
      this.status = status;
      this.responseCode = responseCode;
      this.responseBody = responseBody;
      this.createdAt = createdAt;
      this.updatedAt = updatedAt;
    }

  public static IdempotencyKey inProgress(UUID clientId, String idempotencyKey, String requestHash,
      OffsetDateTime now) {
    return new IdempotencyKey(
        clientId,
        idempotencyKey,
        requestHash,
        STATUS_IN_PROGRESS,
        null,
        null,
        now,
        now
    );
  }

  public void markCompleted(int responseCode, String responseBody, OffsetDateTime updatedAt) {
    this.status = STATUS_COMPLETED;
    this.responseCode = responseCode;
    this.responseBody = responseBody;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public String getRequestHash() {
    return requestHash;
  }

  public String getStatus() {
    return status;
  }

  public Integer getResponseCode() {
    return responseCode;
  }

  public String getResponseBody() {
    return responseBody;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
}