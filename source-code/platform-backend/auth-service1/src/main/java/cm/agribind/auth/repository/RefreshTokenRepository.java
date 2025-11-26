// Update RefreshTokenRepository.java with these additional methods

package cm.agribind.auth.repository;

import cm.agribind.auth.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserId(String userId);

    Optional<RefreshToken> findByUserIdAndDeviceId(String userId, String deviceId);

    // ✅ CHANGE: Use DELETE instead of UPDATE for immediate removal
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.userId = :userId")
    void deleteByUserId(String userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.userId = :userId AND rt.deviceId = :deviceId")
    void deleteByUserIdAndDeviceId(String userId, String deviceId);

    // ✅ KEEP: These are still useful for soft-delete scenarios
    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.userId = :userId")
    void revokeAllByUserId(String userId);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revoked = true WHERE rt.userId = :userId AND rt.deviceId = :deviceId")
    void revokeByUserIdAndDeviceId(String userId, String deviceId);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.revoked = true")
    void deleteExpiredAndRevoked(LocalDateTime now);

    boolean existsByTokenAndRevokedFalse(String token);

    // ✅ NEW: Check if token exists (useful for debugging)
    boolean existsByToken(String token);

    long deleteByCreatedAtBefore(LocalDateTime thirtyDaysAgo);
}