package cm.agribind.usermanagement.integration.event;

import cm.agribind.usermanagement.dto.event.*;
import org.springframework.scheduling.annotation.Async;

public abstract class UserEventPublisher {
    @Async
    public abstract void publishUserCreated(UserCreatedEvent event);

    @Async
    public abstract void publishUserUpdated(UserUpdatedEvent event);

    @Async
    public abstract void publishUserStatusChanged(UserStatusChangedEvent event);

    @Async
    public abstract void publishFarmerRegistered(FarmerRegisteredEvent event);

    @Async
    public abstract void publishProfileUpdated(ProfileUpdatedEvent event);
}
