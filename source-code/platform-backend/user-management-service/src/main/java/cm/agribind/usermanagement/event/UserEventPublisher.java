package cm.agribind.usermanagement.event;

import cm.agribind.usermanagement.dto.event.*;

public interface UserEventPublisher {

    void publishUserCreated(UserCreatedEvent event);
    void publishUserUpdated(UserUpdatedEvent event);
    void publishUserStatusChanged(UserStatusChangedEvent event);
    void publishFarmerRegistered(FarmerRegisteredEvent event);
    void publishProfileUpdated(ProfileUpdatedEvent event);
}