package com.microcredit.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class MaterialLoanDTO {
    private String packageId;
    private String farmerName;
    private String packageName;
    private String itemsIncluded;
    private Double totalValue;
    private Double repaidAmount;
    private Double balance;
    private Double progress;
    private String repaymentType;
    private Double utilization;
    private String status;

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getItemsIncluded() {
        return itemsIncluded;
    }

    public void setItemsIncluded(String itemsIncluded) {
        this.itemsIncluded = itemsIncluded;
    }

    public Double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(Double totalValue) {
        this.totalValue = totalValue;
    }

    public Double getRepaidAmount() {
        return repaidAmount;
    }

    public void setRepaidAmount(Double repaidAmount) {
        this.repaidAmount = repaidAmount;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public String getRepaymentType() {
        return repaymentType;
    }

    public void setRepaymentType(String repaymentType) {
        this.repaymentType = repaymentType;
    }

    public Double getUtilization() {
        return utilization;
    }

    public void setUtilization(Double utilization) {
        this.utilization = utilization;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}