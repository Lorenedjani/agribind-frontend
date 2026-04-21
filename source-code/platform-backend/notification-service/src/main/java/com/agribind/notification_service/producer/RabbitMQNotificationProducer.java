package com.agribind.notification_service.producer;

import com.agribind.notification_service.model.NotificationRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMQNotificationProducer {

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key}")
    private String routingKey;

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQNotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        log.info("RabbitMQ Notification Producer initialized");
    }

    public void sendNotification(NotificationRequest notification) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, notification);
            log.info("Sent notification to RabbitMQ successfully for user: {}",
                    notification.getUserId());
        } catch (Exception e) {
            log.error("Failed to send notification to RabbitMQ for user: {}",
                    notification.getUserId(), e);
        }
    }
}