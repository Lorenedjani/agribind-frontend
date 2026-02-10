package com.microcredit.controller;

import com.microcredit.dto.*;
import com.microcredit.model.OverdueLoan;
import com.microcredit.service.MicrocreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/microcredit")
@Tag(name = "Microcredit Service", description = "API for managing cash loans, material loans, repayments, and loan statistics")
public class MicrocreditController {

    private final MicrocreditService microcreditService;

    public MicrocreditController(MicrocreditService microcreditService) {
        this.microcreditService = microcreditService;
    }

    @Operation(summary = "Get dashboard statistics", description = "Retrieve dashboard statistics including total loans, active loans, repayment rates, and overdue loans")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dashboard statistics retrieved successfully",
            content = @Content(schema = @Schema(implementation = DashboardStatsDTO.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(
            @Parameter(description = "Authorization token", required = true)
            @RequestHeader("Authorization") String token) {
        DashboardStatsDTO stats = microcreditService.getDashboardStats(token);
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get cash loans", description = "Retrieve list of cash loans with optional filtering by search term, status, and risk level")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cash loans retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/cash-loans")
    public ResponseEntity<List<CashLoanDTO>> getCashLoans(
            @Parameter(description = "Search term for filtering loans")
            @RequestParam(required = false) String search,
            @Parameter(description = "Filter by loan status")
            @RequestParam(required = false) String status,
            @Parameter(description = "Filter by risk level (LOW, MEDIUM, HIGH)")
            @RequestParam(required = false) String riskLevel,
            @Parameter(description = "Authorization token", required = true)
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

    @Operation(summary = "Apply for cash loan", description = "Submit a new cash loan application")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Loan application submitted successfully",
            content = @Content(schema = @Schema(implementation = CashLoanDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid loan application"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/apply/cash")
    public ResponseEntity<CashLoanDTO> applyForCashLoan(
            @Parameter(description = "Loan application details", required = true)
            @RequestBody LoanApplicationDTO application,
            @Parameter(description = "Authorization token", required = true)
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