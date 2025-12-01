package com.agribind.communication.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

// ==================== Bulk SMS Result ====================
@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class BulkSmsResult {
    private int totalSent;
    private int successCount;
    private int failureCount;
    private List<String> failedNumbers;
    private double totalCost;
}
