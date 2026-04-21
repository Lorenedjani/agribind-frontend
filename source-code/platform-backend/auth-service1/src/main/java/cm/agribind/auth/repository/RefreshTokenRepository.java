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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.userId = :userId")
    void deleteByUserId(String userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.userId = :userId AND rt.deviceId = :deviceId")
    void deleteByUserIdAndDeviceId(String userId, String deviceId);

    /**
     * Safety net: delete any existing row whose token value matches the one
     * we are about to insert, regardless of which user/device it belongs to.
     * This prevents a duplicate-key violation when two users share the same
     * deviceId AND the JwtService generates an identical token string
     * (e.g. same device + same-second issuedAt with no jti claim).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.token = :token")
    void deleteByToken(String token);

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

    boolean existsByToken(String token);

    long deleteByCreatedAtBefore(LocalDateTime thirtyDaysAgo);
}