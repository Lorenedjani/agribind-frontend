package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

// ==================== Audio Message DTO ====================
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioMessageRequest {
    @NotNull
    private TargetAudience targetAudience;

    private String specificZone;

    @NotBlank
    private String title;

    @NotBlank
    private String language; // French, English, Fulfulde, Ewondo, Duala

    private Boolean autoPlay = false;

    private MessagePriority priority = MessagePriority.NORMAL;
}
