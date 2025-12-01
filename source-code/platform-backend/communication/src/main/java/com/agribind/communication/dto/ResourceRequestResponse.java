package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ResourceRequestResponse {
    private Long id;
    private String requestId;
    private String resourceName;
    private Integer quantity;
    private UrgencyLevel urgency;
    private RequestStatus status;
    private Integer suppliersMatched;
    private LocalDateTime requestDate;
}
