package com.agribind.notification_service.controller;

import com.agribind.notification_service.model.NotificationRequest;
import com.agribind.notification_service.model.NotificationResponse;
import com.agribind.notification_service.model.entity.NotificationHistory;
import com.agribind.notification_service.producer.KafkaNotificationProducer;
import com.agribind.notification_service.producer.RabbitMQNotificationProducer;
import com.agribind.notification_service.repository.NotificationHistoryRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification Service", description = "API for sending notifications via Kafka and RabbitMQ")
public class NotificationController {

    private final Optional<KafkaNotificationProducer> kafkaProducer;
    private final Optional<RabbitMQNotificationProducer> rabbitMQProducer;
    private final NotificationHistoryRepository notificationHistoryRepository;

    public NotificationController(
            Optional<KafkaNotificationProducer> kafkaProducer,
            Optional<RabbitMQNotificationProducer> rabbitMQProducer,
            NotificationHistoryRepository notificationHistoryRepository) {
        this.kafkaProducer = kafkaProducer;
        this.rabbitMQProducer = rabbitMQProducer;
        this.notificationHistoryRepository = notificationHistoryRepository;
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user notification history", description = "Fetch all persistent notifications for a specific user.")
    public ResponseEntity<List<NotificationHistory>> getUserNotifications(@PathVariable String userId) {
        List<NotificationHistory> history = notificationHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(history);
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Updates a notification's isRead status to true.")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Optional<NotificationHistory> historyOpt = notificationHistoryRepository.findById(id);
        if (historyOpt.isPresent()) {
            NotificationHistory history = historyOpt.get();
            history.setRead(true);
            notificationHistoryRepository.save(history);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/user/{userId}/read-all")
    @Operation(summary = "Mark all formatting read", description = "Marks all unread notifications as read for a user.")
    public ResponseEntity<Void> markAllAsRead(@PathVariable String userId) {
        List<NotificationHistory> unread = notificationHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().filter(n -> !n.isRead()).toList();
        
        unread.forEach(n -> n.setRead(true));
        notificationHistoryRepository.saveAll(unread);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Send notification via Kafka", description = "Queue a notification to be sent via Kafka message broker")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification queued successfully",
            content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/kafka")
    public ResponseEntity<NotificationResponse> sendViaKafka(
            @Parameter(description = "Notification request details", required = true)
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

    @Operation(summary = "Send notification via RabbitMQ", description = "Queue a notification to be sent via RabbitMQ message broker")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notification queued successfully",
            content = @Content(schema = @Schema(implementation = NotificationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/rabbitmq")
    public ResponseEntity<NotificationResponse> sendViaRabbitMQ(
            @Parameter(description = "Notification request details", required = true)
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