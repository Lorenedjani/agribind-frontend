// Update TokenCleanupScheduler.java with better cleanup logic

package cm.agribind.auth.scheduler;

import cm.agribind.auth.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetRepository;

    /**
     * Clean up expired and revoked tokens every hour
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("🧹 Starting hourly token cleanup...");

        LocalDateTime now = LocalDateTime.now();

        try {
            // Count before cleanup
            long totalBefore = refreshTokenRepository.count();

            // Delete expired and revoked tokens
            refreshTokenRepository.deleteExpiredAndRevoked(now);

            // Count after cleanup
            long totalAfter = refreshTokenRepository.count();
            long deleted = totalBefore - totalAfter;

            log.info("✅ Cleaned up {} refresh tokens (Before: {}, After: {})",
                    deleted, totalBefore, totalAfter);

            // Cleanup password reset tokens
            passwordResetRepository.deleteExpiredAndUsed(now);
            log.info("✅ Cleaned up expired password reset tokens");

        } catch (Exception e) {
            log.error("❌ Error during token cleanup", e);
        }
    }

    /**
     * Deep cleanup - runs daily at 2 AM
     * Removes old tokens even if not marked as revoked
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void deepCleanup() {
        log.info("🧹 Starting deep token cleanup...");

        try {
            // Delete tokens older than 30 days regardless of status
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

            long deleted = refreshTokenRepository.deleteByCreatedAtBefore(thirtyDaysAgo);
            log.info("✅ Deep cleanup removed {} old tokens", deleted);

        } catch (Exception e) {
            log.error("❌ Error during deep cleanup", e);
        }
    }
}

// Add this method to RefreshTokenRepository:
// @Query("DELETE FROM RefreshToken rt WHERE rt.createdAt < :date")
// long deleteByCreatedAtBefore(LocalDateTime date);