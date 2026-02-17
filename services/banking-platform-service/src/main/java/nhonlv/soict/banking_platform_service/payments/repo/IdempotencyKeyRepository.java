package nhonlv.soict.banking_platform_service.payments.repo;

import java.util.Optional;
import java.util.UUID;

import nhonlv.soict.banking_platform_service.payments.domain.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, UUID> {
  Optional<IdempotencyKey> findByClientIdAndIdempotencyKey(UUID client_id, String idempotencyKey);
}
