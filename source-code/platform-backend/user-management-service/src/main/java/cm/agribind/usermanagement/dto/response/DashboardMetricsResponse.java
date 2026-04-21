package cm.agribind.usermanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardMetricsResponse {

    // Summary metrics for cards
    private Long totalUsers;
    private Long activeUsers;
    private Long pendingUsers;
    private Long totalFarmers;
    private Long totalCooperatives;
    private Long totalGovernmentOfficials;

    // User type distribution
    private Map<String, Long> userTypeDistribution;

    // Regional distribution
    private Map<String, Long> regionalDistribution;

    // Status distribution
    private Map<String, Long> statusDistribution;

    // Agricultural type distribution
    private Map<String, Long> agriculturalTypeDistribution;

    // Top crops
    private Map<String, Long> topCrops;

    // Top livestock
    private Map<String, Long> topLivestock;

    // Growth metrics
    private Double userGrowthRate;
    private Double farmerGrowthRate;
    private Map<String, Long> monthlyGrowth;

    // Cooperative metrics
    private Long totalCooperativeMembers;
    private Double averageCooperativeSize;
    private Long cooperativesWithStorage;

    // Recent activity
    private List<UserResponse> recentRegistrations;
    private List<UserResponse> recentlyUpdated;

    // Export ready data
    private Boolean exportReady = true;
    private String lastUpdated;
}