package cm.agribind.auth.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "${services.notification}")
public interface NotificationEventPublisher {

    @PostMapping("/api/notifications/sms/send")
    void sendSms(@RequestBody SmsRequest request);

    @PostMapping("/push/send")
    void sendPushNotification(@RequestBody PushNotificationRequest request);
}
