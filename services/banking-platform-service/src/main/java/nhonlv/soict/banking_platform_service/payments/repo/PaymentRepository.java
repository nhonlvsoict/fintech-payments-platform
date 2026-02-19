package nhonlv.soict.banking_platform_service.payments.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import nhonlv.soict.banking_platform_service.payments.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
  Optional<Payment> findByIdAndClientId(UUID id, UUID clientId);
}
