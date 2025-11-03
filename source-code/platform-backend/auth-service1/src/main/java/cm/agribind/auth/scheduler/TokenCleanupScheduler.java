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
     * Clean up expired and used tokens daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting token cleanup job");

        LocalDateTime now = LocalDateTime.now();

        try {
            // Cleanup refresh tokens
            refreshTokenRepository.deleteExpiredAndRevoked(now);
            log.info("Cleaned up expired refresh tokens");

            // Cleanup password reset tokens
            passwordResetRepository.deleteExpiredAndUsed(now);
            log.info("Cleaned up expired password reset tokens");


            log.info("Token cleanup job completed successfully");
        } catch (Exception e) {
            log.error("Error during token cleanup: {}", e.getMessage(), e);
        }
    }
}