package cm.agribind.usermanagement.repository;

import cm.agribind.usermanagement.entity.User;
import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUserId(String userId);

    Optional<User> findByRegistrationNumber(String registrationNumber);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);

    List<User> findByType(UserType type);

    Page<User> findByType(UserType type, Pageable pageable);

    List<User> findByStatus(UserStatus status);

    long countByType(UserType type);

    long countByStatus(UserStatus status);

    @Query("SELECT u FROM User u WHERE u.profile.preferredLanguage = :language")
    List<User> findByPreferredLanguage(String language);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByRegistrationNumber(String registrationNumber);
}