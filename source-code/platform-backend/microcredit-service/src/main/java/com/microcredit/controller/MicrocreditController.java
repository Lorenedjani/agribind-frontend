package com.microcredit.controller;

import com.microcredit.dto.*;
import com.microcredit.model.OverdueLoan;
import com.microcredit.service.MicrocreditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/microcredit")
public class MicrocreditController {

    private final MicrocreditService microcreditService;

    public MicrocreditController(MicrocreditService microcreditService) {
        this.microcreditService = microcreditService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(
            @RequestHeader("Authorization") String token) {
        DashboardStatsDTO stats = microcreditService.getDashboardStats(token);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/cash-loans")
    public ResponseEntity<List<CashLoanDTO>> getCashLoans(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String riskLevel,
            @RequestHeader("Authorization") String token) {
        List<CashLoanDTO> loans = microcreditService.getCashLoans(search, status, riskLevel, token);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/material-loans")
    public ResponseEntity<List<MaterialLoanDTO>> getMaterialLoans(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String repaymentType,
            @RequestParam(required = false) String status,
            @RequestHeader("Authorization") String token) {
        List<MaterialLoanDTO> loans = microcreditService.getMaterialLoans(
                search, repaymentType, status, token);
        return ResponseEntity.ok(loans);
    }

    @PostMapping("/apply/cash")
    public ResponseEntity<CashLoanDTO> applyForCashLoan(
            @RequestBody LoanApplicationDTO application,
            @RequestHeader("Authorization") String token) {
        CashLoanDTO loan = microcreditService.createCashLoan(application, token);
        return ResponseEntity.ok(loan);
    }

    @PostMapping("/apply/material")
    public ResponseEntity<MaterialLoanDTO> applyForMaterialLoan(
            @RequestBody LoanApplicationDTO application,
            @RequestHeader("Authorization") String token) {
        MaterialLoanDTO loan = microcreditService.createMaterialLoan(application, token);
        return ResponseEntity.ok(loan);
    }

    @PostMapping("/repayments")
    public ResponseEntity<Void> recordRepayment(
            @RequestBody RepaymentRequest request,
            @RequestHeader("Authorization") String token) {
        microcreditService.recordRepayment(
                request.getLoanId(),
                request.getAmount(),
                request.getPaymentMethod(),
                token
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping("/overdue-loans")
    public ResponseEntity<List<OverdueLoan>> getOverdueLoans(
            @RequestHeader("Authorization") String token) {
        // This should be implemented in service
        return ResponseEntity.ok(microcreditService.getOverdueLoans());
    }
}

class RepaymentRequest {
    private String loanId;
    private Double amount;
    private String paymentMethod;

    public RepaymentRequest() {
    }

    public RepaymentRequest(String loanId, Double amount, String paymentMethod) {
        this.loanId = loanId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Override
    public String toString() {
        return "RepaymentRequest{" +
                "loanId='" + loanId + '\'' +
                ", amount=" + amount +
                ", paymentMethod='" + paymentMethod + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RepaymentRequest that = (RepaymentRequest) o;

        if (loanId != null ? !loanId.equals(that.loanId) : that.loanId != null) return false;
        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        return paymentMethod != null ? paymentMethod.equals(that.paymentMethod) : that.paymentMethod == null;
    }

    @Override
    public int hashCode() {
        int result = loanId != null ? loanId.hashCode() : 0;
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (paymentMethod != null ? paymentMethod.hashCode() : 0);
        return result;
    }
}