package com.agribind.notification_service.consumer;

import com.agribind.notification_service.model.NotificationRequest;
import com.agribind.notification_service.service.NotificationService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaNotificationConsumer {

    private final NotificationService notificationService;

    public KafkaNotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
        topics = "${app.kafka.topic}",
        groupId = "${spring.kafka.consumer.group-id}",
        concurrency = "3"
    )
    public void consumeNotification(NotificationRequest notification) {
        try {
            log.info("Received notification from Kafka for user: {}", notification.getUserId());
            notificationService.processNotification(notification);
        } catch (Exception e) {
            log.error("Error processing notification from Kafka for user: {}",
                    notification.getUserId(), e);
        }
    }
}