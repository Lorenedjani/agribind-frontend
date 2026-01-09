package com.agribind.notification_service.controller;

import com.agribind.notification_service.model.NotificationRequest;
import com.agribind.notification_service.model.NotificationResponse;
import com.agribind.notification_service.producer.KafkaNotificationProducer;
import com.agribind.notification_service.producer.RabbitMQNotificationProducer;
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
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification Service", description = "API for sending notifications via Kafka and RabbitMQ")
public class NotificationController {

    private final Optional<KafkaNotificationProducer> kafkaProducer;
    private final Optional<RabbitMQNotificationProducer> rabbitMQProducer;

    public NotificationController(
            Optional<KafkaNotificationProducer> kafkaProducer,
            Optional<RabbitMQNotificationProducer> rabbitMQProducer) {
        this.kafkaProducer = kafkaProducer;
        this.rabbitMQProducer = rabbitMQProducer;
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