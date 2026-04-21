package com.agribind.production_monitoring.security;

import com.agribind.production_monitoring.dto.CropsByRegionDTO;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductionAnalyticsAuthorizationService {

    /**
     * National / scoped cooperative id for analytics & exports.
     * GOVERNMENT: optional query/path cooperativeId (null = all cooperatives).
     * COOPERATIVE_MANAGER / COOPERATIVE: cooperative id from JWT only.
     */
    public String resolveCooperativeId(String requestedCooperativeId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication required");
        }
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");
        if ("GOVERNMENT".equalsIgnoreCase(role) || "GOVERNMENT_OFFICIAL".equalsIgnoreCase(role)) {
            return requestedCooperativeId;
        }
        if ("COOPERATIVE_MANAGER".equalsIgnoreCase(role) || "COOPERATIVE".equalsIgnoreCase(role)) {
            String fromToken = extractCooperativeId(authentication);
            if (fromToken == null || fromToken.isBlank()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cooperative scope missing from token");
            }
            return fromToken;
        }
        throw new AccessDeniedException("Role not allowed for this operation");
    }

    public String resolveCooperativePathOrParam(String pathOrParamCooperativeId, Authentication authentication) {
        return resolveCooperativeId(pathOrParamCooperativeId, authentication);
    }

    private String extractCooperativeId(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof Map<?, ?> map) {
            Object v = map.get("cooperativeId");
            return v != null ? v.toString() : null;
        }
        return null;
    }

    /**
     * COOPERATIVE_MANAGER / COOPERATIVE: only the JWT region (matches {@link CropsByRegionDTO#getRegion()}).
     * Government roles see the full list.
     */
    public List<CropsByRegionDTO> filterCropsByRegionForRole(
            List<CropsByRegionDTO> rows, Authentication authentication) {
        if (rows == null || authentication == null || !authentication.isAuthenticated()) {
            return rows;
        }
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("");
        if (!isCooperativeScopedRole(role)) {
            return rows;
        }
        String jwtRegion = extractRegion(authentication);
        if (jwtRegion == null || jwtRegion.isBlank()) {
            return List.of();
        }
        String norm = jwtRegion.trim();
        return rows.stream()
                .filter(dto -> dto.getRegion() != null && dto.getRegion().trim().equalsIgnoreCase(norm))
                .collect(Collectors.toList());
    }

    private boolean isCooperativeScopedRole(String role) {
        return "COOPERATIVE_MANAGER".equalsIgnoreCase(role) || "COOPERATIVE".equalsIgnoreCase(role);
    }

    private String extractRegion(Authentication authentication) {
        Object details = authentication.getDetails();
        if (details instanceof Map<?, ?> map) {
            Object v = map.get("region");
            return v != null ? v.toString() : null;
        }
        return null;
    }
}
