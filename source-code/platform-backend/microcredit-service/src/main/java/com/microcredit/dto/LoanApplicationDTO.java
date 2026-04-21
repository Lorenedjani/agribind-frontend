package com.microcredit.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LoanApplicationDTO {
    private String farmerId;
    private String loanType; // CASH or MATERIAL
    private Double requestedAmount;
    private String purpose;
    private LocalDate preferredDisbursementDate;
    private Integer repaymentPeriod; // in months
    private MaterialPackageRequest materialPackage; // for material loans

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public Double getRequestedAmount() {
        return requestedAmount;
    }

    public void setRequestedAmount(Double requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public LocalDate getPreferredDisbursementDate() {
        return preferredDisbursementDate;
    }

    public void setPreferredDisbursementDate(LocalDate preferredDisbursementDate) {
        this.preferredDisbursementDate = preferredDisbursementDate;
    }

    public Integer getRepaymentPeriod() {
        return repaymentPeriod;
    }

    public void setRepaymentPeriod(Integer repaymentPeriod) {
        this.repaymentPeriod = repaymentPeriod;
    }

    public MaterialPackageRequest getMaterialPackage() {
        return materialPackage;
    }

    public void setMaterialPackage(MaterialPackageRequest materialPackage) {
        this.materialPackage = materialPackage;
    }
}