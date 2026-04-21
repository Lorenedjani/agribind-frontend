package com.microcredit.repository;

import com.microcredit.model.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MaterialLoanRepository extends MongoRepository<MaterialLoan, String> {

    List<MaterialLoan> findByStatus(String status);

    MaterialLoan findByPackageId(String packageId);

    @Query("{ $or: [ { 'packageId': { $regex: ?0, $options: 'i' } }, " +
            "{ 'farmerName': { $regex: ?1, $options: 'i' } }, " +
            "{ 'packageName': { $regex: ?2, $options: 'i' } } ] }")
    List<MaterialLoan> searchByPackageIdOrFarmerNameOrPackageName(
            String packageId, String farmerName, String packageName);

    List<MaterialLoan> findByRepaymentType(String repaymentType);
}