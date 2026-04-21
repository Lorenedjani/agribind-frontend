package cm.agribind.usermanagement.event;

import cm.agribind.usermanagement.dto.event.*;
import cm.agribind.usermanagement.integration.event.UserEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "agribind.notifications.use-kafka", havingValue = "true")
public class KafkaUserEventPublisher extends UserEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String USER_EVENTS_TOPIC = "user-events";
    private static final String NOTIFICATION_EVENTS_TOPIC = "notification-events";
    private static final String AUDIT_EVENTS_TOPIC = "audit-events";

    @Async
    @Override
    public void publishUserCreated(UserCreatedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(USER_EVENTS_TOPIC, "user.created", message);
            log.info("Published user created event: {}", event.getUserId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize user created event", e);
        }
    }

    @Async
    @Override
    public void publishUserUpdated(UserUpdatedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(USER_EVENTS_TOPIC, "user.updated", message);
            log.info("Published user updated event: {}", event.getUserId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize user updated event", e);
        }
    }

    @Async
    @Override
    public void publishUserStatusChanged(UserStatusChangedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(USER_EVENTS_TOPIC, "user.status.changed", message);

            // Also publish to notification service for status change notifications
            kafkaTemplate.send(NOTIFICATION_EVENTS_TOPIC, "user.status", message);

            log.info("Published user status changed event: {} -> {}",
                    event.getUserId(), event.getNewStatus());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize user status changed event", e);
        }
    }

    @Async
    @Override
    public void publishFarmerRegistered(FarmerRegisteredEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(USER_EVENTS_TOPIC, "farmer.registered", message);

            // Notify cooperative if farmer joined one
            if (event.getCooperativeId() != null) {
                kafkaTemplate.send(NOTIFICATION_EVENTS_TOPIC, "cooperative.member.joined", message);
            }

            log.info("Published farmer registered event: {}", event.getFarmerId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize farmer registered event", e);
        }
    }

    @Async
    @Override
    public void publishProfileUpdated(ProfileUpdatedEvent event) {
        try {
            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(USER_EVENTS_TOPIC, "profile.updated", message);
            log.info("Published profile updated event: {}", event.getUserId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize profile updated event", e);
        }
    }

    @Async
    public void publishAuditEvent(String action, String userId, String details) {
        try {
            AuditEvent event = new AuditEvent();
            event.setEventId(java.util.UUID.randomUUID().toString());
            event.setAction(action);
            event.setUserId(userId);
            event.setDetails(details);
            event.setTimestamp(java.time.LocalDateTime.now());

            String message = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(AUDIT_EVENTS_TOPIC, "audit." + action, message);

            log.debug("Published audit event: {} for user: {}", action, userId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize audit event", e);
        }
    }
}

// Additional event class for audit
class AuditEvent {
    private String eventId;
    private String action;
    private String userId;
    private String details;
    private java.time.LocalDateTime timestamp;

    // Getters and setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public java.time.LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(java.time.LocalDateTime timestamp) { this.timestamp = timestamp; }
}