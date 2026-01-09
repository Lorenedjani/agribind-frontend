package com.microcredit.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DashboardStatsDTO {
    private Double totalCashLoans;
    private Integer activeCashLoans;
    private Double totalMaterialLoans;
    private Integer activeMaterialLoans;
    private Double repaymentRate;
    private Double repaymentRateChange;
    private Integer overdueLoansCount;
    private Double overdueAmount;

    public Double getTotalCashLoans() {
        return totalCashLoans;
    }

    public void setTotalCashLoans(Double totalCashLoans) {
        this.totalCashLoans = totalCashLoans;
    }

    public Integer getActiveCashLoans() {
        return activeCashLoans;
    }

    public void setActiveCashLoans(Integer activeCashLoans) {
        this.activeCashLoans = activeCashLoans;
    }

    public Double getTotalMaterialLoans() {
        return totalMaterialLoans;
    }

    public void setTotalMaterialLoans(Double totalMaterialLoans) {
        this.totalMaterialLoans = totalMaterialLoans;
    }

    public Integer getActiveMaterialLoans() {
        return activeMaterialLoans;
    }

    public void setActiveMaterialLoans(Integer activeMaterialLoans) {
        this.activeMaterialLoans = activeMaterialLoans;
    }

    public Double getRepaymentRate() {
        return repaymentRate;
    }

    public void setRepaymentRate(Double repaymentRate) {
        this.repaymentRate = repaymentRate;
    }

    public Double getRepaymentRateChange() {
        return repaymentRateChange;
    }

    public void setRepaymentRateChange(Double repaymentRateChange) {
        this.repaymentRateChange = repaymentRateChange;
    }

    public Integer getOverdueLoansCount() {
        return overdueLoansCount;
    }

    public void setOverdueLoansCount(Integer overdueLoansCount) {
        this.overdueLoansCount = overdueLoansCount;
    }

    public Double getOverdueAmount() {
        return overdueAmount;
    }

    public void setOverdueAmount(Double overdueAmount) {
        this.overdueAmount = overdueAmount;
    }
}
