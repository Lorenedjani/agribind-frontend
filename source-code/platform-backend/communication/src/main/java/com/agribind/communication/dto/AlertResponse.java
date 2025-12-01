package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AlertResponse {
    private Long id;
    private String alertId;
    private AlertType type;
    private AlertPriority priority;
    private String title;
    private Integer recipientCount;
    private List<String> channels;
    private Double deliveryRate;
    private AlertStatus status;
    private LocalDateTime createdAt;
}
