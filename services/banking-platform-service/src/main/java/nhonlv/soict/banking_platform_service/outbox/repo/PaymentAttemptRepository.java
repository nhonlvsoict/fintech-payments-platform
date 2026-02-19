package nhonlv.soict.banking_platform_service.outbox.repo;

import java.util.Optional;
import java.util.UUID;
import nhonlv.soict.banking_platform_service.outbox.domain.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, UUID> {

  Optional<PaymentAttempt> findTopByPaymentIdOrderByAttemptNoDesc(UUID paymentId);
}
