package nhonlv.soict.banking_platform_service.payments.repo;

import java.util.Optional;
import java.util.UUID;
import nhonlv.soict.banking_platform_service.payments.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
  Optional<Payment> findByIdAndClientId(UUID id, UUID clientId);
}
