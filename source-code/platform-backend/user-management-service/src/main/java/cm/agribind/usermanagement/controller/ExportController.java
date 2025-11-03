package cm.agribind.usermanagement.controller;

import cm.agribind.usermanagement.dto.query.ExportQuery;
import cm.agribind.usermanagement.dto.response.ExportResponse;
import cm.agribind.usermanagement.service.query.ExportQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/exports")
@RequiredArgsConstructor
@Tag(name = "Export", description = "APIs for exporting user data")
public class ExportController {

    private final ExportQueryService exportQueryService;

    @PostMapping("/generate")
    @Operation(summary = "Generate export", description = "Generate export file in CSV, Excel, or PDF format")
    public ResponseEntity<ExportResponse> generateExport(@Valid @RequestBody ExportQuery query) {
        log.info("Generating export in format: {}", query.getFormat());
        ExportResponse response = exportQueryService.generateExport(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/formats")
    @Operation(summary = "Get supported formats", description = "Get list of supported export formats")
    public ResponseEntity<List<String>> getSupportedFormats() {
        List<String> formats = exportQueryService.getSupportedFormats();
        return ResponseEntity.ok(formats);
    }

    @GetMapping("/columns")
    @Operation(summary = "Get available columns", description = "Get list of available columns for export")
    public ResponseEntity<List<String>> getAvailableColumns() {
        List<String> columns = exportQueryService.getAvailableColumns();
        return ResponseEntity.ok(columns);
    }

    @GetMapping("/status/{exportId}")
    @Operation(summary = "Get export status", description = "Get status of export generation")
    public ResponseEntity<ExportResponse> getExportStatus(@PathVariable String exportId) {
        ExportResponse response = exportQueryService.getExportStatus(exportId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{exportId}")
    @Operation(summary = "Download export", description = "Download generated export file")
    public ResponseEntity<byte[]> downloadExport(@PathVariable String exportId) {
        log.info("Downloading export: {}", exportId);

        ExportResponse status = exportQueryService.getExportStatus(exportId);
        byte[] fileContent = exportQueryService.downloadExport(exportId);

        String contentType = getContentType(status.getFormat());
        String filename = status.getFilename();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(fileContent);
    }

    @PostMapping("/quick-export")
    @Operation(summary = "Quick export", description = "Quick export with common filters")
    public ResponseEntity<ExportResponse> quickExport(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "EXCEL") String format) {

        ExportQuery query = new ExportQuery();
        if (type != null) {
            query.setType(cm.agribind.usermanagement.enums.UserType.valueOf(type.toUpperCase()));
        }
        if (status != null) {
            query.setStatus(cm.agribind.usermanagement.enums.UserStatus.valueOf(status.toUpperCase()));
        }
        if (region != null) {
            query.setRegion(cm.agribind.usermanagement.enums.Region.valueOf(region.toUpperCase()));
        }
        query.setFormat(format);

        ExportResponse response = exportQueryService.generateExport(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/quick")
    @Operation(summary = "Quick users export", description = "Quick export of all users")
    public ResponseEntity<ExportResponse> quickUsersExport(
            @RequestParam(defaultValue = "EXCEL") String format) {
        ExportQuery query = new ExportQuery();
        query.setFormat(format);
        query.setIncludeProfile(true);
        query.setIncludeAgriculturalData(true);

        ExportResponse response = exportQueryService.generateExport(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/farmers/quick")
    @Operation(summary = "Quick farmers export", description = "Quick export of all farmers")
    public ResponseEntity<ExportResponse> quickFarmersExport(
            @RequestParam(defaultValue = "EXCEL") String format) {
        ExportQuery query = new ExportQuery();
        query.setType(cm.agribind.usermanagement.enums.UserType.FARMER);
        query.setFormat(format);
        query.setIncludeAgriculturalData(true);

        ExportResponse response = exportQueryService.generateExport(query);
        return ResponseEntity.ok(response);
    }

    private String getContentType(String format) {
        return switch (format.toUpperCase()) {
            case "CSV" -> "text/csv";
            case "EXCEL" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "PDF" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }
}