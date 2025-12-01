package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

// ==================== Resource Request DTO ====================
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceRequestDto {
    @NotBlank
    private String resourceName;

    @NotNull
    @Min(1)
    private Integer quantity;

    private String unit;

    @NotNull
    private UrgencyLevel urgency;

    @NotBlank
    private String requestedBy;

    private String requestedByZone;
}
