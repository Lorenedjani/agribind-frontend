package com.agribind.production_monitoring.service;

import com.agribind.production_monitoring.dto.ReportRowDTO;
import com.agribind.production_monitoring.model.ProductionRecord;
import com.agribind.production_monitoring.repository.ProductionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ProductionRecordRepository productionRecordRepository;

    public byte[] generateFarmerProductionReport(
            String cooperativeId,
            String region,
            LocalDate startDate,
            LocalDate endDate,
            String format) throws Exception {

        log.info("Generating {} report for coop {}, region {}, from {} to {}",
                format, cooperativeId, region, startDate, endDate);

        // Fetch dataset
        List<ProductionRecord> records = productionRecordRepository.findForReport(
                cooperativeId,
                region.equals("All Regions") ? null : region,
                startDate,
                endDate);

        // Map to flat DTOs for Jasper
        List<ReportRowDTO> data = records.stream()
                .map(r -> ReportRowDTO.builder()
                        .farmerId(r.getFarmerId() != null ? r.getFarmerId() : "Unknown")
                        .farmerName(r.getFarmerName() != null ? r.getFarmerName() : "Unknown")
                        .farmerRegion(r.getFarmerRegion() != null ? r.getFarmerRegion() : "Unknown")
                        .cooperativeId(r.getCooperativeId() != null ? r.getCooperativeId() : "Unknown")
                        .productName(r.getProductName() != null ? r.getProductName() : "Unknown")
                        .quantity(r.getQuantity() != null ? r.getQuantity() : java.math.BigDecimal.ZERO)
                        .unit(r.getUnit() != null ? r.getUnit() : "-")
                        .qualityGrade(r.getQualityGrade() != null ? r.getQualityGrade() : "N/A")
                        .unitPrice(r.getUnitPrice() != null ? r.getUnitPrice() : java.math.BigDecimal.ZERO)
                        .valueXaf(r.getValueXaf() != null ? r.getValueXaf() : java.math.BigDecimal.ZERO)
                        .productionDate(r.getProductionDate() != null ? r.getProductionDate() : java.time.LocalDate.now())
                        .maturityStatus(r.getMaturityStatus() != null ? r.getMaturityStatus().name() : "N/A")
                        .build())
                .collect(Collectors.toList());

        // Load Jasper template
        InputStream reportStream = new ClassPathResource("reports/farmer_production_report.jrxml").getInputStream();
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data);

        // Report parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "Farmer Production Report");
        parameters.put("COOPERATIVE_ID", cooperativeId);
        parameters.put("REGION_FILTER", region);
        parameters.put("PERIOD", startDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " - " +
                endDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")));

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // Export Based on format
        if ("excel".equalsIgnoreCase(format)) {
            return exportToExcel(jasperPrint);
        } else {
            return exportToPdf(jasperPrint);
        }
    }

    private byte[] exportToPdf(JasperPrint jasperPrint) throws JRException {
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }

    private byte[] exportToExcel(JasperPrint jasperPrint) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JRXlsxExporter exporter = new JRXlsxExporter();
        
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
        
        SimpleXlsxReportConfiguration configuration = new SimpleXlsxReportConfiguration();
        configuration.setOnePagePerSheet(false);
        configuration.setRemoveEmptySpaceBetweenRows(true);
        configuration.setDetectCellType(true);
        configuration.setWhitePageBackground(false);
        exporter.setConfiguration(configuration);
        
        exporter.exportReport();
        return outputStream.toByteArray();
    }
}
