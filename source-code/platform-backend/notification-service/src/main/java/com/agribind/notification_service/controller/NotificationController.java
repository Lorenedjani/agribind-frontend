package com.agribind.notification_service.controller;

import com.agribind.notification_service.model.NotificationRequest;
import com.agribind.notification_service.model.NotificationResponse;
import com.agribind.notification_service.producer.KafkaNotificationProducer;
import com.agribind.notification_service.producer.RabbitMQNotificationProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final Optional<KafkaNotificationProducer> kafkaProducer;
    private final Optional<RabbitMQNotificationProducer> rabbitMQProducer;

    public NotificationController(
            Optional<KafkaNotificationProducer> kafkaProducer,
            Optional<RabbitMQNotificationProducer> rabbitMQProducer) {
        this.kafkaProducer = kafkaProducer;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    @PostMapping("/kafka")
    public ResponseEntity<NotificationResponse> sendViaKafka(
            @RequestBody NotificationRequest request) {

        request.setTimestamp(LocalDateTime.now());

        log.info("Received notification request for Kafka: {}", request);


          // Send to Kafka if available (will be skipped in dev)
            kafkaProducer.ifPresent(producer -> {
                try {
                    producer.sendNotification(request);
                    System.out.println("Notification sent to Kafka");
                } catch (Exception e) {
                    System.out.println("Failed to send to Kafka: " + e.getMessage());
                }
            });

        NotificationResponse response = new NotificationResponse();
        response.setNotificationId(UUID.randomUUID().toString());
        response.setStatus("PENDING");
        response.setMessage("Notification queued for processing via Kafka");
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/rabbitmq")
    public ResponseEntity<NotificationResponse> sendViaRabbitMQ(
            @RequestBody NotificationRequest request) {

        request.setTimestamp(LocalDateTime.now());

        log.info("Received notification request for RabbitMQ: {}", request);

        rabbitMQProducer.ifPresent(producer -> {
                try {
                    producer.sendNotification(request);
                    System.out.println("Notification sent to Kafka");
                } catch (Exception e) {
                    System.out.println("Failed to send to Kafka: " + e.getMessage());
                }
            });


        NotificationResponse response = new NotificationResponse();
        response.setNotificationId(UUID.randomUUID().toString());
        response.setStatus("PENDING");
        response.setMessage("Notification queued for processing via RabbitMQ");
        response.setTimestamp(LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Notification Service is running");
    }
}