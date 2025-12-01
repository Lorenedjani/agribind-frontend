package com.agribind.communication.dto;

import com.agribind.communication.model.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

// ==================== Statistics DTO ====================
@Data
@Builder
public class CommunicationStatsResponse {
    private Integer totalMessagesSent;
    private Integer messagesSentThisWeek;
    private Integer audioMessagesTotal;
    private Integer audioLanguagesSupported;
    private Integer activeAlerts;
    private Integer criticalAlerts;
    private Double deliveryRate;
    private Double deliveryRateChange;
    private Integer activeMembers;
    private Double activeLoansAmount;
    private Integer lowStockAlerts;
    private LocalDateTime lastUpdated;
}