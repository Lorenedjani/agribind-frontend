package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BulkMessageResponse {
    private Long messageId;
    private Integer recipientCount;
    private Double estimatedCost;
    private MessageStatus status;
    private String message;
}
