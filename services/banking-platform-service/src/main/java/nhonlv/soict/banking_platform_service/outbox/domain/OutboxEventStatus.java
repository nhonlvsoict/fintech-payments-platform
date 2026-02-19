package nhonlv.soict.banking_platform_service.outbox.domain;

public enum OutboxEventStatus {
  NEW,
  PROCESSING,
  DONE,
  DEAD
}
