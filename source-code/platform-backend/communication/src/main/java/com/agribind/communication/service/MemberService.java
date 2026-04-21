package com.agribind.communication.service;

import com.agribind.communication.dto.MemberDto; // Add this import
import com.agribind.communication.model.TargetAudience;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to fetch member data from Member Service via API Gateway
 * In production, use Spring Cloud OpenFeign for better integration
 */
@Service
public class MemberService {

    private final RestTemplate restTemplate;

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    // This would typically be injected from configuration
    private static final String MEMBER_SERVICE_URL = "http://localhost:8080/api/members";

    public MemberService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get member phone numbers based on target audience
     */
    public List<String> getMemberPhoneNumbers(TargetAudience audience, String specificZone) {
        try {
            String url = buildMemberQueryUrl(audience, specificZone);
            MemberDto[] members = restTemplate.getForObject(url, MemberDto[].class);

            if (members == null) {
                log.warn("No members found for audience: {} zone: {}", audience, specificZone);
                return new ArrayList<>();
            }

            List<String> phoneNumbers = Arrays.stream(members)
                .map(MemberDto::getPhoneNumber)
                .filter(phone -> phone != null && !phone.isEmpty())
                .collect(Collectors.toList());

            log.info("Retrieved {} phone numbers for audience: {}", phoneNumbers.size(), audience);
            return phoneNumbers;

        } catch (Exception e) {
            log.error("Error fetching member phone numbers: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get member count for estimation
     */
    public int getMemberCount(TargetAudience audience, String specificZone) {
        try {
            String url = buildMemberCountUrl(audience, specificZone);
            Integer count = restTemplate.getForObject(url, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Error fetching member count: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Get active member count
     */
    public int getActiveMemberCount() {
        try {
            String url = MEMBER_SERVICE_URL + "/active/count";
            Integer count = restTemplate.getForObject(url, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Error fetching active member count: {}", e.getMessage());
            return 245; // Default value from dashboard
        }
    }

    /**
     * Build member query URL based on audience
     */
    private String buildMemberQueryUrl(TargetAudience audience, String specificZone) {
        StringBuilder url = new StringBuilder(MEMBER_SERVICE_URL);

        switch (audience) {
            case ALL_MEMBERS:
                url.append("/all");
                break;
            case ACTIVE_MEMBERS:
                url.append("/active");
                break;
            case DOUALA_ZONE:
                url.append("/zone/DOUALA");
                break;
            case YAOUNDE_ZONE:
                url.append("/zone/YAOUNDE");
                break;
            case OTHER_REGIONS:
                url.append("/zone/OTHER_REGIONS");
                break;
            case CUSTOM:
                if (specificZone != null) {
                    url.append("/zone/").append(specificZone);
                }
                break;
        }

        return url.toString();
    }

    /**
     * Build member count URL
     */
    private String buildMemberCountUrl(TargetAudience audience, String specificZone) {
        return buildMemberQueryUrl(audience, specificZone) + "/count";
    }

    /**
     * Mock implementation for testing without actual Member Service
     * Remove this in production
     */
    public List<String> getMockPhoneNumbers(TargetAudience audience) {
        List<String> mockNumbers = new ArrayList<>();

        int count = switch (audience) {
            case ALL_MEMBERS -> 300;
            case ACTIVE_MEMBERS -> 245;
            case DOUALA_ZONE -> 120;
            case YAOUNDE_ZONE -> 100;
            case OTHER_REGIONS -> 80;
            default -> 50;
        };

        // Generate mock Cameroon phone numbers
        for (int i = 0; i < count; i++) {
            mockNumbers.add(String.format("+2376%08d", 70000000 + i));
        }

        return mockNumbers;
    }
}