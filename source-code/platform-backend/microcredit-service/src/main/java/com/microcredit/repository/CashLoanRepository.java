package com.microcredit.repository;

import com.microcredit.model.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CashLoanRepository extends JpaRepository<CashLoan, String> {

    List<CashLoan> findByStatus(String status);

    CashLoan findByLoanId(String loanId);

    @Query("{ $or: [ { 'loanId': { $regex: ?0, $options: 'i' } }, { 'farmerName': { $regex: ?1, $options: 'i' } } ] }")
    List<CashLoan> searchByLoanIdOrFarmerName(String loanId, String farmerName);

    List<CashLoan> findByRiskLevel(String riskLevel);
}
