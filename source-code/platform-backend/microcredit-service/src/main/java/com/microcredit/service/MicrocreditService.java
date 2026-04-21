package com.microcredit.service;

import com.microcredit.dto.*;
import com.microcredit.feign.UserServiceClient;
import com.microcredit.model.*;
import com.microcredit.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MicrocreditService {

    private static final Logger log = LoggerFactory.getLogger(MicrocreditService.class);

    private final CashLoanRepository cashLoanRepository;
    private final MaterialLoanRepository materialLoanRepository;
    private final LoanRepaymentRepository repaymentRepository;
    private final OverdueLoanRepository overdueLoanRepository;
    private final UserServiceClient userServiceClient;

    public MicrocreditService(
            CashLoanRepository cashLoanRepository,
            MaterialLoanRepository materialLoanRepository,
            LoanRepaymentRepository repaymentRepository,
            OverdueLoanRepository overdueLoanRepository,
            UserServiceClient userServiceClient) {
        this.cashLoanRepository = cashLoanRepository;
        this.materialLoanRepository = materialLoanRepository;
        this.repaymentRepository = repaymentRepository;
        this.overdueLoanRepository = overdueLoanRepository;
        this.userServiceClient = userServiceClient;
    }

    public DashboardStatsDTO getDashboardStats(String token) {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // Calculate total cash loans
        List<CashLoan> activeCashLoans = cashLoanRepository.findByStatus("Active");
        stats.setTotalCashLoans(activeCashLoans.stream()
                .mapToDouble(CashLoan::getLoanAmount)
                .sum());
        stats.setActiveCashLoans(activeCashLoans.size());

        // Calculate total material loans
        List<MaterialLoan> activeMaterialLoans = materialLoanRepository.findByStatus("Active");
        stats.setTotalMaterialLoans(activeMaterialLoans.stream()
                .mapToDouble(MaterialLoan::getTotalValue)
                .sum());
        stats.setActiveMaterialLoans(activeMaterialLoans.size());

        // Calculate repayment rate
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        List<LoanRepayment> thisMonthRepayments = repaymentRepository
                .findByPaymentDateBetweenAndStatus(startOfMonth, LocalDate.now(), "Completed");

        double totalDueThisMonth = calculateTotalDueThisMonth();
        double repaidThisMonth = thisMonthRepayments.stream()
                .mapToDouble(LoanRepayment::getAmount)
                .sum();

        double repaymentRate = totalDueThisMonth > 0 ? (repaidThisMonth / totalDueThisMonth) * 100 : 0;
        stats.setRepaymentRate(Math.round(repaymentRate * 10.0) / 10.0);

        // Calculate overdue loans
        List<OverdueLoan> overdueLoans = overdueLoanRepository.findAll();
        stats.setOverdueLoansCount(overdueLoans.size());
        stats.setOverdueAmount(overdueLoans.stream()
                .mapToDouble(OverdueLoan::getOverdueAmount)
                .sum());

        return stats;
    }

    public List<CashLoanDTO> getCashLoans(String search, String status, String riskLevel, String token) {
        List<CashLoan> loans;

        if (search != null && !search.isEmpty()) {
            loans = cashLoanRepository.searchByLoanIdOrFarmerName(search, search);
        } else {
            loans = cashLoanRepository.findAll();
        }

        // Filter by status if provided
        if (status != null && !status.equals("All Status")) {
            loans = loans.stream()
                    .filter(loan -> loan.getStatus().equals(status))
                    .collect(Collectors.toList());
        }

        // Filter by risk level if provided
        if (riskLevel != null && !riskLevel.equals("All Risk Levels")) {
            loans = loans.stream()
                    .filter(loan -> loan.getRiskLevel().equals(riskLevel))
                    .collect(Collectors.toList());
        }

        return loans.stream()
                .map(this::convertToCashLoanDTO)
                .collect(Collectors.toList());
    }

    public List<OverdueLoan> getOverdueLoans() {
        return overdueLoanRepository.findAll();
    }

    public List<MaterialLoanDTO> getMaterialLoans(String search, String repaymentType, String status, String token) {
        List<MaterialLoan> loans;

        if (search != null && !search.isEmpty()) {
            loans = materialLoanRepository.searchByPackageIdOrFarmerNameOrPackageName(search, search, search);
        } else {
            loans = materialLoanRepository.findAll();
        }

        // Filter by repayment type if provided
        if (repaymentType != null && !repaymentType.equals("All Repayment Types")) {
            loans = loans.stream()
                    .filter(loan -> loan.getRepaymentType().equals(repaymentType))
                    .collect(Collectors.toList());
        }

        // Filter by status if provided
        if (status != null && !status.equals("All Status")) {
            loans = loans.stream()
                    .filter(loan -> loan.getStatus().equals(status))
                    .collect(Collectors.toList());
        }

        return loans.stream()
                .map(this::convertToMaterialLoanDTO)
                .collect(Collectors.toList());
    }

    public CashLoanDTO createCashLoan(LoanApplicationDTO application, String token) {
        // Get farmer info from user service
        FarmerDTO farmer = userServiceClient.getFarmerById(application.getFarmerId(), token);

        // Perform risk assessment
        String riskLevel = assessRisk(farmer, application.getRequestedAmount());

        CashLoan loan = new CashLoan();
        loan.setLoanId(generateCashLoanId());
        loan.setFarmerId(farmer.getId());
        loan.setFarmerName(farmer.getFirstName() + " " + farmer.getLastName());
        loan.setLoanAmount(application.getRequestedAmount());
        loan.setRepaidAmount(0.0);
        loan.setBalance(application.getRequestedAmount());
        loan.setTotalInstallments(application.getRepaymentPeriod());
        loan.setPaidInstallments(0);
        loan.setProgressPercentage(0.0);
        loan.setPurpose(application.getPurpose());
        loan.setDisbursementDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusMonths(application.getRepaymentPeriod()));
        loan.setStatus("Active");
        loan.setRiskLevel(riskLevel);
        loan.setCreatedAt(LocalDate.now());
        loan.setUpdatedAt(LocalDate.now());

        CashLoan savedLoan = cashLoanRepository.save(loan);
        return convertToCashLoanDTO(savedLoan);
    }

    public MaterialLoanDTO createMaterialLoan(LoanApplicationDTO application, String token) {
        FarmerDTO farmer = userServiceClient.getFarmerById(application.getFarmerId(), token);

        MaterialLoan loan = new MaterialLoan();
        loan.setPackageId(generateMaterialLoanId());
        loan.setFarmerId(farmer.getId());
        loan.setFarmerName(farmer.getFirstName() + " " + farmer.getLastName());
        loan.setPackageName(application.getMaterialPackage().getPackageName());

        List<MaterialItem> items = application.getMaterialPackage().getItems().stream()
                .map(itemDto -> {
                    MaterialItem item = new MaterialItem();
                    item.setName(itemDto.getName());
                    item.setQuantity(itemDto.getQuantity());
                    item.setUnit(itemDto.getUnit());
                    return item;
                })
                .collect(Collectors.toList());

        loan.setItems(items);
        loan.setTotalValue(calculatePackageValue(items));
        loan.setRepaidAmount(0.0);
        loan.setBalance(loan.getTotalValue());
        loan.setProgressPercentage(0.0);
        loan.setRepaymentType("Cash"); // Default, can be configured
        loan.setUtilizationPercentage(0.0);
        loan.setStatus("Active");
        loan.setDisbursementDate(LocalDate.now());
        loan.setCreatedAt(LocalDate.now());
        loan.setUpdatedAt(LocalDate.now());

        MaterialLoan savedLoan = materialLoanRepository.save(loan);
        return convertToMaterialLoanDTO(savedLoan);
    }

    public void recordRepayment(String loanId, Double amount, String paymentMethod, String token) {
        // Find loan (could be cash or material)
        CashLoan cashLoan = cashLoanRepository.findByLoanId(loanId);
        MaterialLoan materialLoan = materialLoanRepository.findByPackageId(loanId);

        LoanRepayment repayment = new LoanRepayment();
        repayment.setLoanId(loanId);
        repayment.setAmount(amount);
        repayment.setPaymentDate(LocalDate.now());
        repayment.setPaymentMethod(paymentMethod);
        repayment.setStatus("Completed");

        if (cashLoan != null) {
            repayment.setLoanType("CASH");
            cashLoan.setRepaidAmount(cashLoan.getRepaidAmount() + amount);
            cashLoan.setBalance(cashLoan.getBalance() - amount);
            cashLoan.setPaidInstallments(cashLoan.getPaidInstallments() + 1);
            cashLoan.setProgressPercentage((cashLoan.getRepaidAmount() / cashLoan.getLoanAmount()) * 100);
            cashLoan.setUpdatedAt(LocalDate.now());

            cashLoanRepository.save(cashLoan);
        } else if (materialLoan != null) {
            repayment.setLoanType("MATERIAL");
            materialLoan.setRepaidAmount(materialLoan.getRepaidAmount() + amount);
            materialLoan.setBalance(materialLoan.getBalance() - amount);
            materialLoan.setProgressPercentage((materialLoan.getRepaidAmount() / materialLoan.getTotalValue()) * 100);
            materialLoan.setUpdatedAt(LocalDate.now());

            materialLoanRepository.save(materialLoan);
        }

        repaymentRepository.save(repayment);

        // Check and update overdue status
        updateOverdueLoans();
    }

    private String assessRisk(FarmerDTO farmer, Double requestedAmount) {
        // Simple risk assessment based on credit score and requested amount
        if (farmer.getCreditScore() >= 700 && requestedAmount <= 5000000) {
            return "Low";
        } else if (farmer.getCreditScore() >= 500 && requestedAmount <= 10000000) {
            return "Medium";
        } else {
            return "High";
        }
    }

    private String generateCashLoanId() {
        Long count = cashLoanRepository.count();
        return String.format("CASH-%03d", count + 1);
    }

    private String generateMaterialLoanId() {
        Long count = materialLoanRepository.count();
        return String.format("MAT-%03d", count + 1);
    }

    private Double calculatePackageValue(List<MaterialItem> items) {
        // This would typically involve looking up item prices from inventory
        // For now, return a placeholder value
        return 1500000.0; // Example value
    }

    private Double calculateTotalDueThisMonth() {
        // Calculate total payments due this month
        // Implementation depends on your repayment schedule logic
        return 10000000.0; // Placeholder
    }

    private void updateOverdueLoans() {
        LocalDate today = LocalDate.now();

        // Check cash loans
        List<CashLoan> cashLoans = cashLoanRepository.findByStatus("Active");
        for (CashLoan loan : cashLoans) {
            if (loan.getDueDate().isBefore(today) && loan.getBalance() > 0) {
                OverdueLoan overdue = new OverdueLoan();
                overdue.setLoanId(loan.getLoanId());
                overdue.setLoanType("CASH");
                overdue.setFarmerId(loan.getFarmerId());
                overdue.setFarmerName(loan.getFarmerName());
                overdue.setOverdueAmount(loan.getBalance());
                overdue.setDaysOverdue((int) java.time.temporal.ChronoUnit.DAYS.between(loan.getDueDate(), today));
                overdue.setOriginalDueDate(loan.getDueDate());
                overdue.setOverdueSince(loan.getDueDate());

                overdueLoanRepository.save(overdue);
            }
        }
    }

    private CashLoanDTO convertToCashLoanDTO(CashLoan loan) {
        CashLoanDTO dto = new CashLoanDTO();
        dto.setLoanId(loan.getLoanId());
        dto.setFarmerName(loan.getFarmerName());
        dto.setLoanAmount(loan.getLoanAmount());
        dto.setRepaidAmount(loan.getRepaidAmount());
        dto.setBalance(loan.getBalance());
        dto.setPaidInstallments(loan.getPaidInstallments());
        dto.setTotalInstallments(loan.getTotalInstallments());
        dto.setProgress(Math.round((loan.getRepaidAmount() / loan.getLoanAmount()) * 100.0) / 100.0);
        dto.setPurpose(loan.getPurpose());
        dto.setDueDate(loan.getDueDate());
        dto.setStatus(loan.getStatus());
        dto.setRiskLevel(loan.getRiskLevel());
        return dto;
    }

    private MaterialLoanDTO convertToMaterialLoanDTO(MaterialLoan loan) {
        MaterialLoanDTO dto = new MaterialLoanDTO();
        dto.setPackageId(loan.getPackageId());
        dto.setFarmerName(loan.getFarmerName());
        dto.setPackageName(loan.getPackageName());

        // Format items as string for display
        String itemsString = loan.getItems().stream()
                .map(item -> item.getName() + " (" + item.getQuantity() + item.getUnit() + ")")
                .collect(Collectors.joining(", "));
        dto.setItemsIncluded(itemsString);

        dto.setTotalValue(loan.getTotalValue());
        dto.setRepaidAmount(loan.getRepaidAmount());
        dto.setBalance(loan.getBalance());
        dto.setProgress(Math.round((loan.getRepaidAmount() / loan.getTotalValue()) * 100.0) / 100.0);
        dto.setRepaymentType(loan.getRepaymentType());
        dto.setUtilization(loan.getUtilizationPercentage());
        dto.setStatus(loan.getStatus());
        return dto;
    }
}