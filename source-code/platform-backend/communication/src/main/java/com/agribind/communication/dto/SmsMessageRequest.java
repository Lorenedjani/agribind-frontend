package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

// ==================== SMS Message DTO ====================
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsMessageRequest {
    @NotNull
    private TargetAudience targetAudience;

    private String specificZone;

    @NotBlank
    @Size(max = 160, message = "SMS content must not exceed 160 characters")
    private String content;

    private MessagePriority priority = MessagePriority.NORMAL;

    private LocalDateTime scheduledAt;

    private Long templateId;
}
