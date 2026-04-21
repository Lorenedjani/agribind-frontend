package com.microcredit.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CashLoanDTO {
    private String loanId;
    private String farmerName;
    private Double loanAmount;
    private Double repaidAmount;
    private Double balance;
    private Integer paidInstallments;
    private Integer totalInstallments;
    private Double progress;
    private String purpose;
    private LocalDate dueDate;
    private String status;
    private String riskLevel;

    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public Double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(Double loanAmount) {
        this.loanAmount = loanAmount;
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

    public Integer getPaidInstallments() {
        return paidInstallments;
    }

    public void setPaidInstallments(Integer paidInstallments) {
        this.paidInstallments = paidInstallments;
    }

    public Integer getTotalInstallments() {
        return totalInstallments;
    }

    public void setTotalInstallments(Integer totalInstallments) {
        this.totalInstallments = totalInstallments;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }
}

