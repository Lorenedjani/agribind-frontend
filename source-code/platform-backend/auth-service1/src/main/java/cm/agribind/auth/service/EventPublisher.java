package cm.agribind.auth.service;

import cm.agribind.auth.entity.UserAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final AmqpTemplate amqp;

    public void publishUserCreated(UserAccount user, String rawPassword) {
        amqp.convertAndSend("notification.exchange", "notification.email",
                Map.of("type", "USER_CREATED",
                        "email", user.getEmail(),
                        "username", user.getUsername(),
                        "password", rawPassword));
    }

    public void publishFarmerRegistered(UserAccount user, String regNum) {
        amqp.convertAndSend("notification.exchange", "notification.sms",
                Map.of("type", "FARMER_REGISTERED",
                        "phone", user.getPhone(),
                        "registrationNumber", regNum));
    }

    public void publishEmail(UserAccount saved, String rawPassword) {

    }

    public void publishSms(UserAccount saved, String regNum) {
    }
}
