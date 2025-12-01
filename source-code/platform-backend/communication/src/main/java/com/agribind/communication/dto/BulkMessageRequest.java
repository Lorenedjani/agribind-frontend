package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

// ==================== Bulk Composer DTO ====================
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkMessageRequest {
    @NotNull
    private TargetAudience recipientAudience;

    private String specificZone;

    private Long templateId;

    @NotBlank
    private String messageContent;

    private MessagePriority priority = MessagePriority.NORMAL;

    private LocalDateTime scheduledDelivery;
}
