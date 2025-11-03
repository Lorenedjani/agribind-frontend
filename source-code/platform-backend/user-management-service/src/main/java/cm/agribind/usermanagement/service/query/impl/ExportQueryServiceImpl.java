package cm.agribind.usermanagement.service.query.impl;

import cm.agribind.usermanagement.dto.query.ExportQuery;
import cm.agribind.usermanagement.dto.response.ExportResponse;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.entity.User;
import cm.agribind.usermanagement.enums.UserType;
import cm.agribind.usermanagement.mapper.UserMapper;
import cm.agribind.usermanagement.repository.UserRepository;
import cm.agribind.usermanagement.repository.spec.UserSpecification;
import cm.agribind.usermanagement.service.query.ExportQueryService;
import cm.agribind.usermanagement.util.ExportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportQueryServiceImpl implements ExportQueryService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ExportGenerator exportGenerator;

    @Override
    public ExportResponse generateExport(ExportQuery query) {
        log.info("Generating export in format: {}", query.getFormat());

        String exportId = UUID.randomUUID().toString();
        ExportResponse response = new ExportResponse(exportId, query.getFormat());

        try {
            // Build specification for filtering
            Specification<User> spec = buildExportSpecification(query);

            // Fetch users based on filters
            List<User> users = userRepository.findAll(spec);
            List<UserResponse> userResponses = userMapper.toResponseList(users);

            // Generate export file
            byte[] exportData = exportGenerator.generateExport(userResponses, query);

            // Update response with success details
            response.setStatus(ExportResponse.ExportStatus.COMPLETED);
            response.setRecordCount((long) users.size());
            response.setFileSize((long) exportData.length);
            response.setFilename(generateFilename(query));
            response.setDownloadUrl("/api/exports/download/" + exportId);

            // Store export data (in real implementation, this would be saved to storage)
            storeExportData(exportId, exportData);

            log.info("Export generated successfully: {} records", users.size());

        } catch (Exception e) {
            log.error("Export generation failed", e);
            response.setStatus(ExportResponse.ExportStatus.FAILED);
            response.setMessage("Export generation failed: " + e.getMessage());
        }

        return response;
    }

    @Override
    public List<String> getSupportedFormats() {
        return List.of("CSV", "EXCEL", "PDF");
    }

    @Override
    public List<String> getAvailableColumns() {
        return List.of(
                "userId", "type", "name", "email", "phoneNumber", "status",
                "region", "department", "district", "village", "createdAt",
                "agriculturalType", "cropTypes", "livestockTypes", "landArea",
                "cooperativeName", "cooperativeType", "governmentRole", "department"
        );
    }

    @Override
    public ExportResponse getExportStatus(String exportId) {
        // In real implementation, this would query a storage or database
        // For now, return a mock response
        ExportResponse response = new ExportResponse(exportId, "EXCEL");
        response.setStatus(ExportResponse.ExportStatus.COMPLETED);
        response.setRecordCount(150L);
        response.setFileSize(1024L * 50); // 50KB
        response.setFilename("users_export_" + exportId + ".xlsx");
        response.setDownloadUrl("/api/exports/download/" + exportId);
        return response;
    }

    @Override
    public byte[] downloadExport(String exportId) {
        // In real implementation, this would retrieve from storage
        // For now, return mock data
        log.info("Downloading export: {}", exportId);
        return "Mock export data for: ".getBytes();
    }

    private Specification<User> buildExportSpecification(ExportQuery query) {
        Specification<User> spec = Specification.where(null);

        if (query.getType() != null) {
            spec = spec.and(UserSpecification.hasType(query.getType()));
        }
        if (query.getStatus() != null) {
            spec = spec.and(UserSpecification.hasStatus(query.getStatus()));
        }
        if (query.getRegion() != null) {
            spec = spec.and(UserSpecification.inRegion(query.getRegion()));
        }

        return spec;
    }

    private String generateFilename(ExportQuery query) {
        String timestamp = LocalDateTime.now().toString().replace(":", "-").split("\\.")[0];
        String type = query.getType() != null ? query.getType().name().toLowerCase() : "all";
        return String.format("users_%s_%s.%s", type, timestamp, query.getFormat().toLowerCase());
    }

    private void storeExportData(String exportId, byte[] data) {
        // In real implementation, store in file system, S3, or database
        log.debug("Storing export data for: {}", exportId);
    }
}