package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

// ==================== Message Template DTO ====================
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageTemplateDto {
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String content;

    private List<String> variables;
}
