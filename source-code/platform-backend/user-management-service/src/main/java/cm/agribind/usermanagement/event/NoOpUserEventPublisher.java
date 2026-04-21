package cm.agribind.usermanagement.event;

import cm.agribind.usermanagement.dto.event.*;
import cm.agribind.usermanagement.integration.event.UserEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Used when Kafka is disabled so domain code still has a {@link UserEventPublisher} bean.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "agribind.notifications.use-kafka", havingValue = "false", matchIfMissing = true)
public class NoOpUserEventPublisher extends UserEventPublisher {

    @Async
    @Override
    public void publishUserCreated(UserCreatedEvent event) {
        log.debug("Kafka disabled — skip publishUserCreated for {}", event.getUserId());
    }

    @Async
    @Override
    public void publishUserUpdated(UserUpdatedEvent event) {
        log.debug("Kafka disabled — skip publishUserUpdated for {}", event.getUserId());
    }

    @Async
    @Override
    public void publishUserStatusChanged(UserStatusChangedEvent event) {
        log.debug("Kafka disabled — skip publishUserStatusChanged for {}", event.getUserId());
    }

    @Async
    @Override
    public void publishFarmerRegistered(FarmerRegisteredEvent event) {
        log.debug("Kafka disabled — skip publishFarmerRegistered for {}", event.getFarmerId());
    }

    @Async
    @Override
    public void publishProfileUpdated(ProfileUpdatedEvent event) {
        log.debug("Kafka disabled — skip publishProfileUpdated for {}", event.getUserId());
    }
}
