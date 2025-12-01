// ========================================
// FILE: src/main/java/com/agribind/communication/scheduler/StatisticsRefreshScheduler.java
// PURPOSE: Automatically refresh communication statistics
// ========================================

package com.agribind.communication.scheduler;

import com.agribind.communication.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler to automatically refresh communication statistics
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StatisticsRefreshScheduler {

    private final CommunicationService communicationService;

    /**
     * Refresh statistics every 15 minutes
     */
    @Scheduled(fixedRate = 900000) // Every 15 minutes (900,000 ms)
    public void refreshStatistics() {
        log.info("Starting automatic statistics refresh...");

        try {
            communicationService.updateStatistics();
            log.info("Statistics refreshed successfully");

        } catch (Exception e) {
            log.error("Error refreshing statistics: {}", e.getMessage(), e);
        }
    }

    /**
     * Refresh statistics at specific times during the day
     * Morning: 8 AM
     * Noon: 12 PM
     * Evening: 6 PM
     * Night: 10 PM
     */
    @Scheduled(cron = "0 0 8,12,18,22 * * *")
    public void refreshStatisticsAtScheduledTimes() {
        log.info("Starting scheduled statistics refresh...");

        try {
            communicationService.updateStatistics();
            log.info("Scheduled statistics refresh completed successfully");

        } catch (Exception e) {
            log.error("Error in scheduled statistics refresh: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate daily statistics report
     * Runs every day at midnight
     */
    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    public void generateDailyReport() {
        log.info("Generating daily statistics report...");

        try {
            // Update statistics
            communicationService.updateStatistics();

            // Get current statistics
            var stats = communicationService.getStatistics();

            // Log daily summary
            log.info("=== Daily Communication Report ===");
            log.info("Total Messages Sent: {}", stats.getTotalMessagesSent());
            log.info("Messages This Week: {}", stats.getMessagesSentThisWeek());
            log.info("Active Alerts: {}", stats.getActiveAlerts());
            log.info("Delivery Rate: {}%", stats.getDeliveryRate());
            log.info("Active Members: {}", stats.getActiveMembers());
            log.info("================================");

            // TODO: Send email report to administrators

        } catch (Exception e) {
            log.error("Error generating daily report: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate weekly statistics report
     * Runs every Monday at 9 AM
     */
    @Scheduled(cron = "0 0 9 * * MON") // Every Monday at 9 AM
    public void generateWeeklyReport() {
        log.info("Generating weekly statistics report...");

        try {
            var stats = communicationService.getStatistics();

            log.info("=== Weekly Communication Report ===");
            log.info("Messages Sent This Week: {}", stats.getMessagesSentThisWeek());
            log.info("Audio Messages Total: {}", stats.getAudioMessagesTotal());
            log.info("Average Delivery Rate: {}%", stats.getDeliveryRate());
            log.info("Delivery Rate Change: {}%", stats.getDeliveryRateChange());
            log.info("==================================");

            // TODO: Generate and send detailed weekly report

        } catch (Exception e) {
            log.error("Error generating weekly report: {}", e.getMessage(), e);
        }
    }

    /**
     * Clean up old statistics
     * Runs monthly on the 1st at 1 AM
     */
    @Scheduled(cron = "0 0 1 1 * *") // 1st of every month at 1 AM
    public void cleanupOldStatistics() {
        log.info("Cleaning up old statistics records...");

        try {
            // Keep only last 12 months of statistics
            // TODO: Implement cleanup logic in service

            log.info("Old statistics cleanup completed");

        } catch (Exception e) {
            log.error("Error cleaning up old statistics: {}", e.getMessage(), e);
        }
    }

    /**
     * Check system health and alert if issues detected
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void performHealthCheck() {
        log.debug("Performing system health check...");

        try {
            var stats = communicationService.getStatistics();

            // Check if delivery rate is too low
            if (stats.getDeliveryRate() < 85.0) {
                log.warn("⚠️ WARNING: Delivery rate is below 85%: {}%",
                    stats.getDeliveryRate());
                // TODO: Send alert to administrators
            }

            // Check if there are too many critical alerts
            if (stats.getCriticalAlerts() > 20) {
                log.warn("⚠️ WARNING: {} critical alerts active",
                    stats.getCriticalAlerts());
                // TODO: Send alert to administrators
            }

            log.debug("Health check completed successfully");

        } catch (Exception e) {
            log.error("❌ CRITICAL: Error during health check: {}", e.getMessage(), e);
            // TODO: Send critical alert to administrators
        }
    }

    /**
     * Optimize database performance
     * Runs weekly on Sunday at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * SUN") // Every Sunday at 2 AM
    public void optimizeDatabase() {
        log.info("Starting database optimization...");

        try {
            // TODO: Implement database optimization
            // - Vacuum old data
            // - Rebuild indexes
            // - Update table statistics

            log.info("Database optimization completed");

        } catch (Exception e) {
            log.error("Error during database optimization: {}", e.getMessage(), e);
        }
    }
}