package cm.agribind.usermanagement.util;

import cm.agribind.usermanagement.dto.query.ExportQuery;
import cm.agribind.usermanagement.dto.response.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class ExportGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] generateExport(List<UserResponse> users, ExportQuery query) {
        return switch (query.getFormat().toUpperCase()) {
            case "CSV" -> generateCSV(users, query);
            case "EXCEL" -> generateExcel(users, query);
            case "PDF" -> generatePDF(users, query);
            default -> throw new IllegalArgumentException("Unsupported export format: " + query.getFormat());
        };
    }

    private byte[] generateCSV(List<UserResponse> users, ExportQuery query) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            StringBuilder csv = new StringBuilder();

            // Header
            List<String> headers = getHeaders(query);
            csv.append(String.join(",", headers)).append("\n");

            // Data
            for (UserResponse user : users) {
                List<String> row = getRowData(user, query, headers);
                csv.append(String.join(",", row)).append("\n");
            }

            return csv.toString().getBytes();

        } catch (Exception e) {
            log.error("Error generating CSV export", e);
            throw new RuntimeException("Failed to generate CSV export", e);
        }
    }

    private byte[] generateExcel(List<UserResponse> users, ExportQuery query) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Users");

            // Create header row
            List<String> headers = getHeaders(query);
            Row headerRow = sheet.createRow(0);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            int rowNum = 1;
            for (UserResponse user : users) {
                Row row = sheet.createRow(rowNum++);
                List<String> rowData = getRowData(user, query, headers);

                for (int i = 0; i < rowData.size(); i++) {
                    row.createCell(i).setCellValue(rowData.get(i));
                }
            }

            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error generating Excel export", e);
            throw new RuntimeException("Failed to generate Excel export", e);
        }
    }

    private byte[] generatePDF(List<UserResponse> users, ExportQuery query) {
        // In a real implementation, use a PDF library like Apache PDFBox or iText
        log.info("PDF export not yet implemented, returning placeholder");

        StringBuilder pdfContent = new StringBuilder();
        pdfContent.append("AgriBind User Export\n");
        pdfContent.append("Generated: ").append(java.time.LocalDateTime.now().format(DATE_FORMATTER)).append("\n\n");

        for (UserResponse user : users) {
            pdfContent.append(String.format("ID: %s, Name: %s, Type: %s, Status: %s\n",
                    user.getUserId(), user.getName(), user.getType(), user.getStatus()));
        }

        return pdfContent.toString().getBytes();
    }

    private List<String> getHeaders(ExportQuery query) {
        if (query.getColumns() != null && query.getColumns().length > 0) {
            return List.of(query.getColumns());
        }

        // Default columns
        return List.of(
                "User ID", "Type", "Name", "Email", "Phone", "Status",
                "Region", "Department", "District", "Village", "Created At"
        );
    }

    private List<String> getRowData(UserResponse user, ExportQuery query, List<String> headers) {
        return headers.stream()
                .map(header -> getFieldValue(user, header))
                .map(value -> value != null ? escapeCsv(value) : "")
                .toList();
    }

    private String getFieldValue(UserResponse user, String fieldName) {
        try {
            Field field = user.getClass().getDeclaredField(getFieldName(fieldName));
            field.setAccessible(true);
            Object value = field.get(user);

            if (value == null) return null;

            // Handle special types
            if (value instanceof java.time.LocalDateTime) {
                return ((java.time.LocalDateTime) value).format(DATE_FORMATTER);
            }

            return value.toString();

        } catch (Exception e) {
            log.debug("Field not found: {}", fieldName);
            return null;
        }
    }

    private String getFieldName(String header) {
        return header.toLowerCase().replace(" ", "");
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}