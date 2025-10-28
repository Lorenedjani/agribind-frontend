package cm.agribind.auth.repository;

import cm.agribind.auth.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, String> {
    Optional<UserAccount> findByUsername(String username);
}
