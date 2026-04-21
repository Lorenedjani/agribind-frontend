package com.microcredit.repository;

import com.microcredit.model.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanRepaymentRepository extends MongoRepository<LoanRepayment, String> {

    List<LoanRepayment> findByLoanId(String loanId);

    List<LoanRepayment> findByPaymentDateBetweenAndStatus(
            LocalDate startDate, LocalDate endDate, String status);
}