package nhonlv.soict.banking_platform_service.outbox.repo;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import nhonlv.soict.banking_platform_service.outbox.domain.OutboxEvent;
import nhonlv.soict.banking_platform_service.outbox.domain.OutboxEventStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

  List<OutboxEvent> findByStatusAndAvailableAtLessThanEqualOrderByCreatedAtAsc(
      OutboxEventStatus status,
      OffsetDateTime availableAt,
      Pageable pageable
  );

  default List<OutboxEvent> findTopNNewAvailableOrderByCreatedAt(OffsetDateTime now, int limit) {
    return findByStatusAndAvailableAtLessThanEqualOrderByCreatedAtAsc(
        OutboxEventStatus.NEW,
        now,
        PageRequest.of(0, limit)
    );
  }
}
