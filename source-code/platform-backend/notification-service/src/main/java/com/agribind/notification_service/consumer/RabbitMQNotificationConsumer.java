package com.agribind.notification_service.consumer;

import com.agribind.notification_service.model.NotificationRequest;
import com.agribind.notification_service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMQNotificationConsumer {

    private final NotificationService notificationService;

    public RabbitMQNotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
        log.info("RabbitMQ Notification Consumer initialized");
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