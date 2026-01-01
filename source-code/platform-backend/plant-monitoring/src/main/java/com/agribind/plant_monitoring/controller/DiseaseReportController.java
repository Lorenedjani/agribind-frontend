package com.agribind.plant_monitoring.controller;

import com.agribind.plant_monitoring.dto.*;
import com.agribind.plant_monitoring.service.DiseaseReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.agribind.plant_monitoring.model.DiseaseReport;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/disease-reports")
@CrossOrigin(origins = "*")
public class DiseaseReportController {
    
    @Autowired
    private DiseaseReportService diseaseReportService;
    
    //@PostMapping("/quick-report")
    //public ResponseEntity<List<DiseaseReportDTO>> createQuickReport(@ModelAttribute QuickDiseaseReportRequest request) {
    //    List<DiseaseReportDTO> createdReports = diseaseReportService.createQuickDiseaseReport(request)
    //            .stream()
    //            .map(report -> convertToDTO(report))
    //            .toList();
    //    return ResponseEntity.ok(createdReports);
    //}

    @PostMapping("/quick-report")
    public ResponseEntity<List<DiseaseReportDTO>> createQuickReport(@ModelAttribute QuickDiseaseReportRequest request) {
        List<DiseaseReport> createdReports = diseaseReportService.createQuickDiseaseReport(request);
        List<DiseaseReportDTO> dtos = createdReports.stream()
            .map(report -> {
                // Convert each DiseaseReport to DiseaseReportDTO
                DiseaseReportDTO dto = new DiseaseReportDTO();
                dto.setId(report.getId());
                dto.setReportId(report.getReportId());
                dto.setFarmerId(report.getFarmerId());
                dto.setCrop(report.getCrop());
                dto.setDisease(report.getDisease());
                dto.setLocation(report.getLocation());
                dto.setAffectedArea(report.getAffectedArea());
                dto.setSeverity(report.getSeverity().name());
                dto.setStatus(report.getStatus().name());
                dto.setReportDate(report.getReportDate());
                dto.setReportedBy(report.getReportedBy());
                dto.setTreatmentNotes(report.getTreatmentNotes());
                dto.setPhotoPath(report.getPhotoPath());
                dto.setCreatedAt(report.getCreatedAt());
                dto.setResolvedAt(report.getResolvedAt());
                return dto;
            })
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
    
    @GetMapping
    public ResponseEntity<List<DiseaseReportDTO>> getAllReports() {
        List<DiseaseReportDTO> reports = diseaseReportService.searchReports(null, null, null, null);
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/paginated")
    public ResponseEntity<Page<DiseaseReportDTO>> getReportsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "reportDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String crop) {
        
        Sort sort = sortDirection.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<DiseaseReportDTO> reports = diseaseReportService.searchReportsPaginated(
                search, severity, status, crop, pageable);
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/summary")
    public ResponseEntity<DiseaseReportSummaryDTO> getReportSummary() {
        DiseaseReportSummaryDTO summary = diseaseReportService.getReportSummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/by-disease")
    public ResponseEntity<Map<String, Long>> getReportsByDisease() {
        Map<String, Long> diseaseCounts = diseaseReportService.getReportsByDisease();
        return ResponseEntity.ok(diseaseCounts);
    }
    
    @GetMapping("/by-location")
    public ResponseEntity<Map<String, Long>> getReportsByLocation() {
        Map<String, Long> locationCounts = diseaseReportService.getReportsByLocation();
        return ResponseEntity.ok(locationCounts);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DiseaseReportDTO> getReportById(@PathVariable Long id) {
        DiseaseReportDTO report = diseaseReportService.getReportById(id);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/report-id/{reportId}")
    public ResponseEntity<DiseaseReportDTO> getReportByReportId(@PathVariable String reportId) {
        DiseaseReportDTO report = diseaseReportService.getReportByReportId(reportId);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/farmer/{farmerId}")
    public ResponseEntity<List<DiseaseReportDTO>> getReportsByFarmer(@PathVariable Long farmerId) {
        List<DiseaseReportDTO> reports = diseaseReportService.getReportsByFarmer(farmerId);
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/critical")
    public ResponseEntity<List<DiseaseReportDTO>> getCriticalReports() {
        List<DiseaseReportDTO> reports = diseaseReportService.getCriticalReports();
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<DiseaseReportDTO>> searchReports(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String crop) {
        
        List<DiseaseReportDTO> reports = diseaseReportService.searchReports(keyword, severity, status, crop);
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<DiseaseReportDTO>> getReportsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<DiseaseReportDTO> reports = diseaseReportService.getReportsByDateRange(startDate, endDate);
        return ResponseEntity.ok(reports);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<DiseaseReportDTO> updateReportStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String treatmentNotes) {
        
        DiseaseReportDTO updatedReport = diseaseReportService.updateReportStatus(id, status, treatmentNotes);
        return ResponseEntity.ok(updatedReport);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        diseaseReportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
    
    private DiseaseReportDTO convertToDTO(com.agribind.plant_monitoring.model.DiseaseReport report) {
        DiseaseReportDTO dto = new DiseaseReportDTO();
        dto.setId(report.getId());
        dto.setReportId(report.getReportId());
        dto.setCrop(report.getCrop());
        dto.setDisease(report.getDisease());
        dto.setLocation(report.getLocation());
        dto.setAffectedArea(report.getAffectedArea());
        dto.setSeverity(report.getSeverity().name());
        dto.setStatus(report.getStatus().name());
        dto.setReportDate(report.getReportDate());
        dto.setTreatmentNotes(report.getTreatmentNotes());
        dto.setPhotoPath(report.getPhotoPath());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setResolvedAt(report.getResolvedAt());
        return dto;
    }
}
