package com.agribind.plant_monitoring.service;

import com.agribind.plant_monitoring.dto.*;
import com.agribind.plant_monitoring.model.DiseaseReport;
import com.agribind.plant_monitoring.repository.DiseaseReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DiseaseReportService {

    @Autowired
    private DiseaseReportRepository diseaseReportRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private FarmerServiceClient farmerServiceClient;

    // Removed: @Autowired PlantPhotoService — was injected but never used

    @Transactional
    public DiseaseReport createDiseaseReport(DiseaseReport report) {
        if (report.getReportDate() == null) {
            report.setReportDate(LocalDateTime.now());
        }
        return diseaseReportRepository.save(report);
    }

    @Transactional
    public List<DiseaseReport> createQuickDiseaseReport(QuickDiseaseReportRequest request) {
        List<DiseaseReport> createdReports = new ArrayList<>();

        try {
            // 1. Save uploaded photo
            String photoPath = null;
            if (request.getPhoto() != null && !request.getPhoto().isEmpty()) {
                String filename = fileStorageService.storeFile(request.getPhoto());
                photoPath = fileStorageService.loadFile(filename).toString();
            }

            // 2. Get all farmers in the specified location
            List<FarmerDTO> farmersInLocation = farmerServiceClient.getFarmersByLocation(request.getLocation());

            if (farmersInLocation.isEmpty()) {
                // If no farmers found in exact location, create a general report
                DiseaseReport generalReport = new DiseaseReport();
                populateReportFromRequest(generalReport, request, photoPath);
                generalReport.setFarmerId(0L); // Set to 0L instead of null to bypass NOT NULL constraint
                generalReport.setFarmerCode("GENERAL");
                generalReport.setReportedVia(DiseaseReport.ReportSource.QUICK_REPORT);
                createdReports.add(diseaseReportRepository.save(generalReport));
            } else {
                // 3. Create a report for each farmer in the location
                for (FarmerDTO farmer : farmersInLocation) {
                    DiseaseReport report = new DiseaseReport();
                    populateReportFromRequest(report, request, photoPath);
                    report.setFarmerId(farmer.getId());
                    report.setFarmerCode(farmer.getCode());
                    report.setReportedVia(DiseaseReport.ReportSource.QUICK_REPORT);
                    createdReports.add(diseaseReportRepository.save(report));
                }
            }

            return createdReports;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create quick disease report: " + e.getMessage(), e);
        }
    }

    private void populateReportFromRequest(DiseaseReport report, QuickDiseaseReportRequest request, String photoPath) {
        report.setCrop(request.getCrop());
        report.setDisease(request.getDisease());
        report.setLocation(request.getLocation());
        report.setAffectedArea(request.getAffectedArea());
        report.setSeverity(DiseaseReport.Severity.valueOf(request.getSeverity().toUpperCase()));
        report.setStatus(DiseaseReport.ReportStatus.INVESTIGATING);
        report.setReportDate(LocalDateTime.now());
        report.setReportedBy("Quick Report System");
        report.setTreatmentNotes(request.getNotes());
        report.setPhotoPath(photoPath);

        if (request.getLatitude() != null && request.getLongitude() != null) {
            report.setCoordinates(request.getLatitude() + "," + request.getLongitude());
        }
    }

    @Transactional
    public DiseaseReportDTO updateReportStatus(Long reportId, String status, String treatmentNotes) {
        DiseaseReport report = diseaseReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));

        report.setStatus(DiseaseReport.ReportStatus.valueOf(status.toUpperCase()));

        if (treatmentNotes != null) {
            report.setTreatmentNotes(treatmentNotes);
        }

        DiseaseReport updatedReport = diseaseReportRepository.save(report);
        return convertToDTO(updatedReport);
    }

    public DiseaseReportDTO getReportById(Long id) {
        DiseaseReport report = diseaseReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
        return convertToDTO(report);
    }

    public DiseaseReportDTO getReportByReportId(String reportId) {
        DiseaseReport report = diseaseReportRepository.findByReportId(reportId);
        if (report == null) {
            throw new RuntimeException("Report not found with reportId: " + reportId);
        }
        return convertToDTO(report);
    }

    public List<DiseaseReportDTO> searchReports(String keyword, String severity, String status, String crop) {
        List<DiseaseReport> reports;

        if (keyword != null && !keyword.isEmpty()) {
            reports = diseaseReportRepository.searchByKeyword(keyword);
        } else {
            reports = diseaseReportRepository.findAll();
        }

        if (severity != null && !severity.isEmpty()) {
            DiseaseReport.Severity severityEnum = DiseaseReport.Severity.valueOf(severity.toUpperCase());
            reports = reports.stream()
                    .filter(r -> r.getSeverity() == severityEnum)
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isEmpty()) {
            DiseaseReport.ReportStatus statusEnum = DiseaseReport.ReportStatus.valueOf(status.toUpperCase());
            reports = reports.stream()
                    .filter(r -> r.getStatus() == statusEnum)
                    .collect(Collectors.toList());
        }

        if (crop != null && !crop.isEmpty() && !crop.equalsIgnoreCase("all")) {
            reports = reports.stream()
                    .filter(r -> r.getCrop().equalsIgnoreCase(crop))
                    .collect(Collectors.toList());
        }

        return reports.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<DiseaseReportDTO> searchReportsPaginated(String keyword, String severity,
                                                         String status, String crop, Pageable pageable) {
        Specification<DiseaseReport> spec = createSearchSpecification(keyword, severity, status, crop);
        Page<DiseaseReport> reports = diseaseReportRepository.findAll(spec, pageable);
        return reports.map(this::convertToDTO);
    }

    private Specification<DiseaseReport> createSearchSpecification(String keyword, String severity,
                                                                   String status, String crop) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                String likeKeyword = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("location")), likeKeyword),
                        cb.like(cb.lower(root.get("disease")), likeKeyword),
                        cb.like(cb.lower(root.get("crop")), likeKeyword)
                ));
            }

            if (severity != null && !severity.isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("severity"),
                            DiseaseReport.Severity.valueOf(severity.toUpperCase())));
                } catch (IllegalArgumentException ignored) {}
            }

            if (status != null && !status.isEmpty()) {
                try {
                    predicates.add(cb.equal(root.get("status"),
                            DiseaseReport.ReportStatus.valueOf(status.toUpperCase())));
                } catch (IllegalArgumentException ignored) {}
            }

            if (crop != null && !crop.isEmpty() && !crop.equalsIgnoreCase("all")) {
                predicates.add(cb.equal(cb.lower(root.get("crop")), crop.toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public DiseaseReportSummaryDTO getReportSummary() {
        DiseaseReportSummaryDTO summary = new DiseaseReportSummaryDTO();

        summary.setTotalReports(diseaseReportRepository.count());

        List<Object[]> statusCounts = diseaseReportRepository.countByStatus();
        Map<String, Long> reportsByStatus = new HashMap<>();
        for (Object[] row : statusCounts) {
            reportsByStatus.put(row[0] != null ? row[0].toString() : "UNKNOWN", ((Number) row[1]).longValue());
        }
        summary.setReportsByStatus(reportsByStatus);

        List<Object[]> severityCounts = diseaseReportRepository.countBySeverity();
        Map<String, Long> reportsBySeverity = new HashMap<>();
        for (Object[] row : severityCounts) {
            reportsBySeverity.put(row[0] != null ? row[0].toString() : "UNKNOWN", ((Number) row[1]).longValue());
        }
        summary.setReportsBySeverity(reportsBySeverity);

        List<Object[]> diseaseCounts = diseaseReportRepository.countByDisease();
        Map<String, Long> reportsByDisease = new HashMap<>();
        for (Object[] row : diseaseCounts) {
            reportsByDisease.put(row[0] != null ? row[0].toString() : "UNKNOWN", ((Number) row[1]).longValue());
        }
        summary.setReportsByDisease(reportsByDisease);

        Long activeCases = reportsByStatus.getOrDefault("INVESTIGATING", 0L)
                + reportsByStatus.getOrDefault("UNDER_TREATMENT", 0L)
                + reportsByStatus.getOrDefault("MONITORING", 0L);
        summary.setActiveCases(activeCases);
        summary.setResolvedCases(reportsByStatus.getOrDefault("RESOLVED", 0L));
        summary.setCriticalAlerts(reportsBySeverity.getOrDefault("CRITICAL", 0L));
        summary.setPendingInvestigations(reportsByStatus.getOrDefault("INVESTIGATING", 0L));

        LocalDateTime startOfWeek = LocalDateTime.now()
                .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        summary.setReportsThisWeek(
                diseaseReportRepository.countByDateRange(startOfWeek, LocalDateTime.now()));

        LocalDateTime startOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
        summary.setReportsThisMonth(
                diseaseReportRepository.countByDateRange(startOfMonth, LocalDateTime.now()));

        return summary;
    }

    public Map<String, Long> getReportsByDisease() {
        List<Object[]> diseaseCounts = diseaseReportRepository.countByDisease();
        Map<String, Long> diseaseMap = new HashMap<>();
        for (Object[] row : diseaseCounts) {
            diseaseMap.put(row[0] != null ? row[0].toString() : "UNKNOWN", ((Number) row[1]).longValue());
        }
        return diseaseMap;
    }

    public Map<String, Long> getReportsByLocation() {
        List<Object[]> locationCounts = diseaseReportRepository.countByLocation();
        Map<String, Long> locationMap = new HashMap<>();
        for (Object[] row : locationCounts) {
            locationMap.put(row[0] != null ? row[0].toString() : "UNKNOWN", ((Number) row[1]).longValue());
        }
        return locationMap;
    }

    public List<DiseaseReportDTO> getReportsByFarmer(Long farmerId) {
        return diseaseReportRepository.findByFarmerId(farmerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DiseaseReportDTO> getCriticalReports() {
        return diseaseReportRepository.findBySeverity(DiseaseReport.Severity.CRITICAL).stream()
                .filter(r -> r.getStatus() != DiseaseReport.ReportStatus.RESOLVED)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteReport(Long id) {
        DiseaseReport report = diseaseReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));

        if (report.getPhotoPath() != null) {
            try {
                String filename = report.getPhotoPath()
                        .substring(report.getPhotoPath().lastIndexOf("/") + 1);
                fileStorageService.deleteFile(filename);
            } catch (Exception e) {
                System.err.println("Failed to delete photo file: " + e.getMessage());
            }
        }

        diseaseReportRepository.delete(report);
    }

    public List<DiseaseReportDTO> getReportsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return diseaseReportRepository.findByDateRange(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Long countByStatus(String status) {
        try {
            DiseaseReport.ReportStatus statusEnum = DiseaseReport.ReportStatus.valueOf(status.toUpperCase());
            return diseaseReportRepository.countByStatusAndDateAfter(
                    statusEnum,
                    LocalDateTime.now().minusDays(30)
            );
        } catch (IllegalArgumentException e) {
            return 0L;
        }
    }

    // Single, authoritative convertToDTO — merges both previous versions
    private DiseaseReportDTO convertToDTO(DiseaseReport report) {
        DiseaseReportDTO dto = new DiseaseReportDTO();
        dto.setId(report.getId());
        dto.setReportId(report.getReportId());
        dto.setFarmerId(report.getFarmerId());

        if (report.getFarmerId() != null) {
            try {
                FarmerDTO farmer = farmerServiceClient.getFarmerById(report.getFarmerId());
                dto.setFarmerName(farmer != null ? farmer.getName() : "Farmer #" + report.getFarmerId());
            } catch (Exception e) {
                System.err.println("Failed to fetch farmer details: " + e.getMessage());
                dto.setFarmerName("Farmer #" + report.getFarmerId());
            }
        } else {
            dto.setFarmerName("General Report");
        }

        dto.setCrop(report.getCrop());
        dto.setDisease(report.getDisease());
        dto.setLocation(report.getLocation());
        dto.setAffectedArea(report.getAffectedArea());
        dto.setSeverity(report.getSeverity() != null ? report.getSeverity().name() : "UNKNOWN");
        dto.setStatus(report.getStatus() != null ? report.getStatus().name() : "UNKNOWN");
        dto.setReportDate(report.getReportDate());
        dto.setReportedBy(report.getReportedBy());
        dto.setTreatmentNotes(report.getTreatmentNotes());
        dto.setPhotoPath(report.getPhotoPath());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setResolvedAt(report.getResolvedAt());

        return dto;
    }
}