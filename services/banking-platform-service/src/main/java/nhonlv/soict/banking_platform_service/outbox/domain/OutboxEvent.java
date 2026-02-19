package nhonlv.soict.banking_platform_service.outbox.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type", nullable = false, length = 80)
  private OutboxEventType eventType;

  @Column(name = "aggregate_id", nullable = false)
  private UUID aggregateId;

  @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
  private String payload;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private OutboxEventStatus status;

  @Column(name = "available_at", nullable = false)
  private OffsetDateTime availableAt;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  protected OutboxEvent() {
    // JPA only
  }

  public OutboxEvent(UUID id, OutboxEventType eventType, UUID aggregateId, String payload,
      OutboxEventStatus status, OffsetDateTime availableAt, OffsetDateTime createdAt,
      OffsetDateTime updatedAt) {
    this.id = id;
    this.eventType = eventType;
    this.aggregateId = aggregateId;
    this.payload = payload;
    this.status = status;
    this.availableAt = availableAt;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public static OutboxEvent paymentProcessRequested(UUID aggregateId, String payload) {
    OffsetDateTime eventTime = OffsetDateTime.now();
    return new OutboxEvent(
        UUID.randomUUID(),
        OutboxEventType.PAYMENT_PROCESS_REQUESTED,
        aggregateId,
        payload,
        OutboxEventStatus.NEW,
        eventTime,
        eventTime,
        eventTime
    );
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public OutboxEventType getEventType() {
    return eventType;
  }

  public void setEventType(OutboxEventType eventType) {
    this.eventType = eventType;
  }

  public UUID getAggregateId() {
    return aggregateId;
  }

  public void setAggregateId(UUID aggregateId) {
    this.aggregateId = aggregateId;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public OutboxEventStatus getStatus() {
    return status;
  }

  public void setStatus(OutboxEventStatus status) {
    this.status = status;
  }

  public OffsetDateTime getAvailableAt() {
    return availableAt;
  }

  public void setAvailableAt(OffsetDateTime availableAt) {
    this.availableAt = availableAt;
  }

  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
