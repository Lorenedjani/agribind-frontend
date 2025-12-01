// ========================================
// FILE: src/main/java/com/agribind/communication/scheduler/AlertExpirationScheduler.java
// PURPOSE: Check and expire old alerts
// ========================================

package com.agribind.communication.scheduler;

import com.agribind.communication.model.Alert;
import com.agribind.communication.model.AlertStatus;
import com.agribind.communication.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler to handle alert expiration
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertExpirationScheduler {

    private final AlertRepository alertRepository;

    /**
     * Expire old alerts
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    @Transactional
    public void expireOldAlerts() {
        log.info("Checking for expired alerts...");

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<Alert> activeAlerts = alertRepository
            .findByStatusOrderByCreatedAtDesc(AlertStatus.ACTIVE)
            .stream()
            .filter(alert -> alert.getCreatedAt().isBefore(sevenDaysAgo))
            .toList();

        if (!activeAlerts.isEmpty()) {
            log.info("Found {} alerts to expire", activeAlerts.size());

            for (Alert alert : activeAlerts) {
                alert.setStatus(AlertStatus.EXPIRED);
                alertRepository.save(alert);

                log.info("Expired alert: {} ({})", alert.getAlertId(), alert.getTitle());
            }

            log.info("Alert expiration completed");
        } else {
            log.debug("No alerts to expire");
        }
    }

    /**
     * Archive expired alerts
     * Runs daily at 4 AM
     */
    @Scheduled(cron = "0 0 4 * * *") // Daily at 4 AM
    @Transactional
    public void archiveExpiredAlerts() {
        log.info("Archiving expired alerts...");

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<Alert> expiredAlerts = alertRepository
            .findByStatusOrderByCreatedAtDesc(AlertStatus.EXPIRED)
            .stream()
            .filter(alert -> alert.getCreatedAt().isBefore(thirtyDaysAgo))
            .toList();

        if (!expiredAlerts.isEmpty()) {
            log.info("Found {} expired alerts to archive", expiredAlerts.size());

            // In production, move to archive table before deletion
            // For now, we'll keep them in the main table

            log.info("Expired alerts archiving completed");
        } else {
            log.debug("No expired alerts to archive");
        }
    }
}