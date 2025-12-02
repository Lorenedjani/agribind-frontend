package com.agribind.communication.dto;

import java.util.List;

public class BulkSmsResult {
    private int totalSent;
    private int successCount;
    private int failureCount;
    private List<String> failedNumbers;
    private double totalCost;

    // Default constructor
    public BulkSmsResult() {}

    // All arguments constructor
    public BulkSmsResult(int totalSent, int successCount, int failureCount, List<String> failedNumbers, double totalCost) {
        this.totalSent = totalSent;
        this.successCount = successCount;
        this.failureCount = failureCount;
        this.failedNumbers = failedNumbers;
        this.totalCost = totalCost;
    }

    // Getters and Setters
    public int getTotalSent() { return totalSent; }
    public void setTotalSent(int totalSent) { this.totalSent = totalSent; }

    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }

    public int getFailureCount() { return failureCount; }
    public void setFailureCount(int failureCount) { this.failureCount = failureCount; }

    public List<String> getFailedNumbers() { return failedNumbers; }
    public void setFailedNumbers(List<String> failedNumbers) { this.failedNumbers = failedNumbers; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    // Builder class
    public static class Builder {
        private int totalSent;
        private int successCount;
        private int failureCount;
        private List<String> failedNumbers;
        private double totalCost;

        public Builder totalSent(int totalSent) { this.totalSent = totalSent; return this; }
        public Builder successCount(int successCount) { this.successCount = successCount; return this; }
        public Builder failureCount(int failureCount) { this.failureCount = failureCount; return this; }
        public Builder failedNumbers(List<String> failedNumbers) { this.failedNumbers = failedNumbers; return this; }
        public Builder totalCost(double totalCost) { this.totalCost = totalCost; return this; }

        public BulkSmsResult build() {
            return new BulkSmsResult(totalSent, successCount, failureCount, failedNumbers, totalCost);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}