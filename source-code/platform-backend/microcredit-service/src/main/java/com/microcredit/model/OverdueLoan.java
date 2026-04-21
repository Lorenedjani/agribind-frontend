package com.microcredit.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.List;

@Data
@Document(collection = "overdue_loans")
public class OverdueLoan {
    @Id
    private String id;
    private String loanId;
    private String loanType;
    private String farmerId;
    private String farmerName;
    private Double overdueAmount;
    private Integer daysOverdue;
    private LocalDate originalDueDate;
    private LocalDate overdueSince;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public String getFarmerId() {
        return farmerId;
    }

    public void setFarmerId(String farmerId) {
        this.farmerId = farmerId;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public Double getOverdueAmount() {
        return overdueAmount;
    }

    public void setOverdueAmount(Double overdueAmount) {
        this.overdueAmount = overdueAmount;
    }

    public Integer getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(Integer daysOverdue) {
        this.daysOverdue = daysOverdue;
    }

    public LocalDate getOriginalDueDate() {
        return originalDueDate;
    }

    public void setOriginalDueDate(LocalDate originalDueDate) {
        this.originalDueDate = originalDueDate;
    }

    public LocalDate getOverdueSince() {
        return overdueSince;
    }

    public void setOverdueSince(LocalDate overdueSince) {
        this.overdueSince = overdueSince;
    }
}