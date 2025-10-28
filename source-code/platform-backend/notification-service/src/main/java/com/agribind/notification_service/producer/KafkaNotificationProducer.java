package com.agribind.notification_service.producer;

import com.agribind.notification_service.model.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class KafkaNotificationProducer {

    @Value("${app.kafka.topic}")
    private String topic;

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    public KafkaNotificationProducer(KafkaTemplate<String, NotificationRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendNotification(NotificationRequest notification) {
        try {
            CompletableFuture<SendResult<String, NotificationRequest>> future =
                kafkaTemplate.send(topic, notification.getUserId(), notification);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Sent notification to Kafka successfully for user: {}, offset: {}",
                            notification.getUserId(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send notification to Kafka for user: {}",
                            notification.getUserId(), ex);
                }
            });
        } catch (Exception e) {
            log.error("Error sending notification to Kafka: {}", e.getMessage(), e);
        }
    }
}