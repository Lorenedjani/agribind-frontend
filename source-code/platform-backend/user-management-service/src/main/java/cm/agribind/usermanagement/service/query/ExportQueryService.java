package cm.agribind.usermanagement.service.query;

import cm.agribind.usermanagement.dto.query.ExportQuery;
import cm.agribind.usermanagement.dto.response.ExportResponse;

import java.util.List;

public interface ExportQueryService {

    ExportResponse generateExport(ExportQuery query);
    List<String> getSupportedFormats();
    List<String> getAvailableColumns();
    ExportResponse getExportStatus(String exportId);
    byte[] downloadExport(String exportId);
}