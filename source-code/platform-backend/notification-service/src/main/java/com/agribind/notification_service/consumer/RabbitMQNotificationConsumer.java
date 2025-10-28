package com.agribind.notification_service.consumer;

import com.agribind.notification_service.model.NotificationRequest;
import com.agribind.notification_service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMQNotificationConsumer {

    private final NotificationService notificationService;

    public RabbitMQNotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue}")
    public void consumeNotification(NotificationRequest notification) {
        try {
            log.info("Received notification from RabbitMQ for user: {}", notification.getUserId());
            notificationService.processNotification(notification);
        } catch (Exception e) {
            log.error("Error processing notification from RabbitMQ for user: {}",
                    notification.getUserId(), e);
        }
    }
}
