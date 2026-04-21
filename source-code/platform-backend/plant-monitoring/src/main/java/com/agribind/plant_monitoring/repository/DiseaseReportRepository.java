package com.agribind.plant_monitoring.repository;

import com.agribind.plant_monitoring.model.DiseaseReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DiseaseReportRepository extends JpaRepository<DiseaseReport, Long>, 
                                                  JpaSpecificationExecutor<DiseaseReport> { // Add this
    
    DiseaseReport findByReportId(String reportId);
    
    List<DiseaseReport> findByFarmerId(Long farmerId);
    
    List<DiseaseReport> findByLocationContainingIgnoreCase(String location);
    
    List<DiseaseReport> findByDiseaseContainingIgnoreCase(String disease);
    
    List<DiseaseReport> findBySeverity(DiseaseReport.Severity severity);
    
    List<DiseaseReport> findByStatus(DiseaseReport.ReportStatus status);
    
    List<DiseaseReport> findByCropContainingIgnoreCase(String crop);
    
    @Query("SELECT d FROM DiseaseReport d WHERE " +
           "LOWER(d.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.disease) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.crop) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DiseaseReport> searchByKeyword(@Param("keyword") String keyword);
    
    @Query("SELECT d.status, COUNT(d) FROM DiseaseReport d GROUP BY d.status")
    List<Object[]> countByStatus();
    
    @Query("SELECT d.severity, COUNT(d) FROM DiseaseReport d GROUP BY d.severity")
    List<Object[]> countBySeverity();
    
    @Query("SELECT d.disease, COUNT(d) FROM DiseaseReport d GROUP BY d.disease")
    List<Object[]> countByDisease();
    
    @Query("SELECT d.crop, COUNT(d) FROM DiseaseReport d GROUP BY d.crop")
    List<Object[]> countByCrop();
    
    @Query("SELECT d.location, COUNT(d) FROM DiseaseReport d GROUP BY d.location")
    List<Object[]> countByLocation();
    
    @Query("SELECT COUNT(d) FROM DiseaseReport d WHERE d.reportDate >= :startDate AND d.reportDate <= :endDate")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate, 
                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT d FROM DiseaseReport d WHERE d.reportDate >= :startDate AND d.reportDate <= :endDate")
    List<DiseaseReport> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(d) FROM DiseaseReport d WHERE d.status = :status AND d.reportDate >= :date")
    Long countByStatusAndDateAfter(@Param("status") DiseaseReport.ReportStatus status, 
                                   @Param("date") LocalDateTime date);
}