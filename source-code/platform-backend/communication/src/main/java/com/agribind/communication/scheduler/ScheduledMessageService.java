// ========================================
// FILE: src/main/java/com/agribind/communication/scheduler/ScheduledMessageService.java
// PURPOSE: Process scheduled messages at specified times
// ========================================

package com.agribind.communication.scheduler;

import com.agribind.communication.model.Message;
import com.agribind.communication.model.MessageStatus;
import com.agribind.communication.repository.MessageRepository;
import com.agribind.communication.service.CommunicationService;
import com.agribind.communication.service.MemberService;
import com.agribind.communication.service.TwilioSmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service to handle scheduled message delivery
 * Runs every 5 minutes to check for messages that need to be sent
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledMessageService {

    private final MessageRepository messageRepository;
    private final TwilioSmsService twilioSmsService;
    private final MemberService memberService;
    private final CommunicationService communicationService;

    /**
     * Check and send scheduled messages
     * Runs every 5 minutes (300,000 milliseconds)
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void processScheduledMessages() {
        log.info("Starting scheduled message processing...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinutesAgo = now.minusMinutes(5);

        // Find messages scheduled between 5 minutes ago and now
        List<Message> scheduledMessages = messageRepository
            .findByScheduledAtBetween(fiveMinutesAgo, now);

        log.info("Found {} scheduled messages to process", scheduledMessages.size());

        for (Message message : scheduledMessages) {
            if (message.getStatus() == MessageStatus.SCHEDULED) {
                try {
                    log.info("Processing scheduled message ID: {}", message.getId());
                    sendScheduledMessage(message);
                } catch (Exception e) {
                    log.error("Error processing scheduled message ID {}: {}",
                        message.getId(), e.getMessage(), e);

                    // Mark message as failed
                    message.setStatus(MessageStatus.FAILED);
                    messageRepository.save(message);
                }
            }
        }

        log.info("Scheduled message processing completed");
    }

    /**
     * Send a scheduled message
     */
    private void sendScheduledMessage(Message message) {
        // Update status to SENDING
        message.setStatus(MessageStatus.SENDING);
        messageRepository.save(message);

        // Get recipient phone numbers
        List<String> phoneNumbers = memberService.getMemberPhoneNumbers(
            message.getTargetAudience(),
            message.getSpecificZone()
        );

        if (phoneNumbers.isEmpty()) {
            log.warn("No recipients found for message ID: {}", message.getId());
            message.setStatus(MessageStatus.FAILED);
            messageRepository.save(message);
            return;
        }

        // Send SMS asynchronously
        communicationService.sendSmsAsync(
            message.getId(),
            phoneNumbers,
            message.getContent()
        );

        log.info("Scheduled message ID {} sent to {} recipients",
            message.getId(), phoneNumbers.size());
    }

    /**
     * Clean up old messages
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    @Transactional
    public void cleanupOldMessages() {
        log.info("Starting cleanup of old messages...");

        // Delete messages older than 90 days
        LocalDateTime ninetyDaysAgo = LocalDateTime.now().minusDays(90);

        List<Message> oldMessages = messageRepository.findAll().stream()
            .filter(m -> m.getCreatedAt().isBefore(ninetyDaysAgo))
            .filter(m -> m.getStatus() == MessageStatus.SENT ||
                        m.getStatus() == MessageStatus.FAILED)
            .toList();

        if (!oldMessages.isEmpty()) {
            messageRepository.deleteAll(oldMessages);
            log.info("Cleaned up {} old messages", oldMessages.size());
        } else {
            log.info("No old messages to clean up");
        }
    }

    /**
     * Retry failed messages
     * Runs every 30 minutes
     */
    @Scheduled(fixedRate = 1800000) // Every 30 minutes
    @Transactional
    public void retryFailedMessages() {
        log.info("Checking for failed messages to retry...");

        List<Message> failedMessages = messageRepository
            .findByStatusOrderByCreatedAtDesc(MessageStatus.FAILED)
            .stream()
            .filter(m -> m.getCreatedAt().isAfter(LocalDateTime.now().minusHours(24)))
            .limit(10) // Retry max 10 messages per run
            .toList();

        log.info("Found {} failed messages to retry", failedMessages.size());

        for (Message message : failedMessages) {
            try {
                log.info("Retrying message ID: {}", message.getId());

                // Reset status to SCHEDULED
                message.setStatus(MessageStatus.SCHEDULED);
                messageRepository.save(message);

                // Process the message
                sendScheduledMessage(message);

            } catch (Exception e) {
                log.error("Error retrying message ID {}: {}",
                    message.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Archive sent messages to history table
     * Runs daily at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * *") // Daily at 3 AM
    public void archiveSentMessages() {
        log.info("Archiving sent messages...");

        // Find messages sent more than 30 days ago
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<Message> sentMessages = messageRepository.findAll().stream()
            .filter(m -> m.getSentAt() != null && m.getSentAt().isBefore(thirtyDaysAgo))
            .filter(m -> m.getStatus() == MessageStatus.SENT ||
                        m.getStatus() == MessageStatus.PARTIALLY_SENT)
            .toList();

        log.info("Found {} messages to archive", sentMessages.size());

        // In production, you would move these to an archive table
        // For now, we'll just log them
        for (Message message : sentMessages) {
            log.debug("Archiving message ID: {} sent at {}",
                message.getId(), message.getSentAt());
            // TODO: Implement archiving logic
        }

        log.info("Message archiving completed");
    }
}