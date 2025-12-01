package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

// ==================== Member Filter DTO ====================
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberFilterRequest {
    private TargetAudience audience;
    private String zone;
    private Boolean activeOnly;
    private List<Long> memberIds;
}
