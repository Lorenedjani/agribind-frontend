package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

// ==================== Alert DTO ====================
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertRequest {
    @NotNull
    private AlertType type;

    @NotNull
    private AlertPriority priority;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotEmpty
    private List<String> channels; // SMS, PUSH, AUDIO

    @NotNull
    private TargetAudience targetAudience;

    private String specificZone;
}