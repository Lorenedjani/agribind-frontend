package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AudioMessageResponse {
    private Long id;
    private String title;
    private String language;
    private String audioFileUrl;
    private Integer recipientCount;
    private MessageStatus status;
    private LocalDateTime createdAt;
}

