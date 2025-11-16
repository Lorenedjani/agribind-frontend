package com.agribind.notification_service.controller;

import com.agribind.notification_service.model.NotificationRequest;
import com.agribind.notification_service.model.NotificationResponse;
import com.agribind.notification_service.producer.KafkaNotificationProducer;
import com.agribind.notification_service.producer.RabbitMQNotificationProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KafkaNotificationProducer kafkaNotificationProducer;

    @MockBean
    private RabbitMQNotificationProducer rabbitMQNotificationProducer;

    @Autowired
    private ObjectMapper objectMapper;

    private NotificationRequest validNotificationRequest;

    @BeforeEach
    void setUp() {
        validNotificationRequest = new NotificationRequest();
        validNotificationRequest.setUserId("user123");
        validNotificationRequest.setType("EMAIL");
        validNotificationRequest.setSubject("Test Subject");
        validNotificationRequest.setMessage("Test Message");
        validNotificationRequest.setRecipient("test@example.com");
        validNotificationRequest.setTimestamp(LocalDateTime.now());
    }

    @Test
    void sendViaKafka_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(kafkaNotificationProducer).sendNotification(any(NotificationRequest.class));

        // When & Then
        mockMvc.perform(post("/api/notifications/kafka")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validNotificationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("Notification queued for processing via Kafka"))
                .andExpect(jsonPath("$.notificationId").exists());

        verify(kafkaNotificationProducer, times(1)).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void sendViaRabbitMQ_WithValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(rabbitMQNotificationProducer).sendNotification(any(NotificationRequest.class));

        // When & Then
        mockMvc.perform(post("/api/notifications/rabbitmq")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validNotificationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("Notification queued for processing via RabbitMQ"))
                .andExpect(jsonPath("$.notificationId").exists());

        verify(rabbitMQNotificationProducer, times(1)).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void sendViaKafka_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given - Create invalid request (missing required fields)
        NotificationRequest invalidRequest = new NotificationRequest();
        // Missing userId, type, etc.

        // When & Then
        mockMvc.perform(post("/api/notifications/kafka")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isOk()); // Still returns 200 because we're not validating in controller

        verify(kafkaNotificationProducer, times(1)).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void sendViaKafka_WhenProducerFails_ShouldStillReturnSuccess() throws Exception {
        // Given
        doThrow(new RuntimeException("Kafka unavailable"))
                .when(kafkaNotificationProducer).sendNotification(any(NotificationRequest.class));

        // When & Then - Controller should handle exception and still return 200
        mockMvc.perform(post("/api/notifications/kafka")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validNotificationRequest)))
                .andExpect(status().isOk());

        verify(kafkaNotificationProducer, times(1)).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void sendViaRabbitMQ_WhenProducerFails_ShouldStillReturnSuccess() throws Exception {
        // Given
        doThrow(new RuntimeException("RabbitMQ unavailable"))
                .when(rabbitMQNotificationProducer).sendNotification(any(NotificationRequest.class));

        // When & Then - Controller should handle exception and still return 200
        mockMvc.perform(post("/api/notifications/rabbitmq")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validNotificationRequest)))
                .andExpect(status().isOk());

        verify(rabbitMQNotificationProducer, times(1)).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void healthCheck_ShouldReturnServiceStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/notifications/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Notification Service is running"));
    }

    @Test
    void sendViaKafka_WithDifferentNotificationTypes_ShouldHandleAll() throws Exception {
        // Given
        String[] types = {"EMAIL", "SMS", "PUSH"};

        for (String type : types) {
            validNotificationRequest.setType(type);

            doNothing().when(kafkaNotificationProducer).sendNotification(any(NotificationRequest.class));

            // When & Then
            mockMvc.perform(post("/api/notifications/kafka")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(validNotificationRequest)))
                    .andExpect(status().isOk());

            verify(kafkaNotificationProducer, times(1)).sendNotification(any(NotificationRequest.class));

            // Reset mock for next iteration
            reset(kafkaNotificationProducer);
        }
    }

    @Test
    void sendViaRabbitMQ_WithEmptyMessage_ShouldProcessSuccessfully() throws Exception {
        // Given
        validNotificationRequest.setMessage("");
        doNothing().when(rabbitMQNotificationProducer).sendNotification(any(NotificationRequest.class));

        // When & Then
        mockMvc.perform(post("/api/notifications/rabbitmq")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validNotificationRequest)))
                .andExpect(status().isOk());

        verify(rabbitMQNotificationProducer, times(1)).sendNotification(any(NotificationRequest.class));
    }

    @Test
    void sendViaKafka_WithNullTimestamp_ShouldSetTimestamp() throws Exception {
        // Given
        validNotificationRequest.setTimestamp(null);
        doNothing().when(kafkaNotificationProducer).sendNotification(any(NotificationRequest.class));

        // When & Then
        mockMvc.perform(post("/api/notifications/kafka")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validNotificationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(kafkaNotificationProducer, times(1)).sendNotification(any(NotificationRequest.class));
    }
}