package cm.agribind.usermanagement.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExportResponse {

    private String exportId;
    private String filename;
    private String format; // CSV, EXCEL, PDF
    private Long recordCount;
    private Long fileSize;
    private String downloadUrl;
    private LocalDateTime generatedAt;
    private ExportStatus status;
    private String message;

    public enum ExportStatus {
        PENDING, COMPLETED, FAILED
    }

    public ExportResponse(String exportId, String format) {
        this.exportId = exportId;
        this.format = format;
        this.status = ExportStatus.PENDING;
        this.generatedAt = LocalDateTime.now();
    }
}