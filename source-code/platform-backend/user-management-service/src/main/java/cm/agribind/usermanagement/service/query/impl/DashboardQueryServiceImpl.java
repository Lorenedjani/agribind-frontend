package cm.agribind.usermanagement.service.query.impl;

import cm.agribind.usermanagement.dto.query.DashboardMetricsQuery;
import cm.agribind.usermanagement.dto.response.DashboardMetricsResponse;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.entity.User;
import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import cm.agribind.usermanagement.mapper.UserMapper;
import cm.agribind.usermanagement.repository.CooperativeRepository;
import cm.agribind.usermanagement.repository.FarmerRepository;
import cm.agribind.usermanagement.repository.UserRepository;
import cm.agribind.usermanagement.service.query.DashboardQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardQueryServiceImpl implements DashboardQueryService {

    private final UserRepository userRepository;
    private final FarmerRepository farmerRepository;
    private final CooperativeRepository cooperativeRepository;
    private final UserMapper userMapper;

    @Override
    public DashboardMetricsResponse getDashboardMetrics() {
        return getDashboardMetrics(new DashboardMetricsQuery());
    }

    @Override
    public DashboardMetricsResponse getDashboardMetrics(DashboardMetricsQuery query) {
        log.debug("Generating dashboard metrics");

        DashboardMetricsResponse response = new DashboardMetricsResponse();

        // Basic counts
        response.setTotalUsers(userRepository.count());
        response.setActiveUsers(userRepository.countByStatus(UserStatus.ACTIVE));
        response.setPendingUsers(userRepository.countByStatus(UserStatus.PENDING));
        response.setTotalFarmers(userRepository.countByType(UserType.FARMER));
        response.setTotalCooperatives(userRepository.countByType(UserType.COOPERATIVE));
        response.setTotalGovernmentOfficials(userRepository.countByType(UserType.GOVERNMENT));

        // Distributions
        response.setUserTypeDistribution(getUserTypeDistribution());
        response.setRegionalDistribution(getRegionalDistribution());
        response.setStatusDistribution(getStatusDistribution());
        response.setAgriculturalTypeDistribution(getAgriculturalTypeDistribution());
        response.setTopCrops(getTopCrops(10));
        response.setTopLivestock(getTopLivestock(10));

        // Cooperative metrics
        response.setTotalCooperativeMembers(getTotalCooperativeMembers());
        response.setAverageCooperativeSize(getAverageCooperativeSize());
        response.setCooperativesWithStorage(getCooperativesWithStorage());

        // Recent activity
        response.setRecentRegistrations(getRecentRegistrations());
        response.setRecentlyUpdated(getRecentlyUpdated());

        // Growth metrics (simplified - would need historical data)
        response.setUserGrowthRate(calculateGrowthRate());

        response.setExportReady(true);
        response.setLastUpdated(LocalDateTime.now().toString());

        return response;
    }

    @Override
    public Map<String, Long> getUserTypeDistribution() {
        return Map.of(
                "FARMER", userRepository.countByType(UserType.FARMER),
                "COOPERATIVE", userRepository.countByType(UserType.COOPERATIVE),
                "GOVERNMENT", userRepository.countByType(UserType.GOVERNMENT)
        );
    }

    @Override
    public Map<String, Long> getRegionalDistribution() {
        List<Object[]> regionCounts = farmerRepository.countFarmersByRegion();
        return regionCounts.stream()
                .collect(Collectors.toMap(
                        obj -> ((cm.agribind.usermanagement.enums.Region) obj[0]).name(),
                        obj -> (Long) obj[1]
                ));
    }

    @Override
    public Map<String, Long> getStatusDistribution() {
        return Map.of(
                "ACTIVE", userRepository.countByStatus(UserStatus.ACTIVE),
                "INACTIVE", userRepository.countByStatus(UserStatus.INACTIVE),
                "PENDING", userRepository.countByStatus(UserStatus.PENDING),
                "SUSPENDED", userRepository.countByStatus(UserStatus.SUSPENDED)
        );
    }

    @Override
    public Map<String, Long> getAgriculturalTypeDistribution() {
        // This would need a proper implementation with FarmerRepository
        return Map.of(
                "CROP", 150L,
                "LIVESTOCK", 75L,
                "MIXED", 50L
        );
    }

    @Override
    public Map<String, Long> getTopCrops(int limit) {
        return getCropDistribution().entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    @Override
    public Map<String, Long> getTopLivestock(int limit) {
        return getLivestockDistribution().entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private Map<String, Long> getCropDistribution() {
        // Simplified - would use FarmerRepository in real implementation
        return Map.of(
                "COCOA", 85L,
                "COFFEE", 67L,
                "MAIZE", 45L,
                "BANANAS", 38L,
                "PALM_OIL", 32L,
                "CASSAVA", 28L,
                "RICE", 25L,
                "COTTON", 22L,
                "BEANS", 18L,
                "TOMATOES", 15L
        );
    }

    private Map<String, Long> getLivestockDistribution() {
        // Simplified - would use FarmerRepository in real implementation
        return Map.of(
                "POULTRY", 45L,
                "GOATS", 38L,
                "CATTLE", 32L,
                "PIGS", 25L,
                "SHEEP", 18L,
                "RABBITS", 12L,
                "BEES", 8L,
                "FISH", 6L
        );
    }

    private Long getTotalCooperativeMembers() {
        return cooperativeRepository.findAll().stream()
                .mapToLong(coop -> coop.getActiveMemberCount() != null ? coop.getActiveMemberCount() : 0)
                .sum();
    }

    private Double getAverageCooperativeSize() {
        return cooperativeRepository.findAverageMemberCount();
    }

    private Long getCooperativesWithStorage() {
        return cooperativeRepository.findAll().stream()
                .filter(coop -> coop.getCooperativeDetails() != null &&
                        Boolean.TRUE.equals(coop.getCooperativeDetails().getHasStorageFacilities()))
                .count();
    }

    private List<UserResponse> getRecentRegistrations() {
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<User> recentUsers = userRepository.findAll(pageRequest).getContent();
        return userMapper.toResponseList(recentUsers);
    }

    private List<UserResponse> getRecentlyUpdated() {
        PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "updatedAt"));
        List<User> recentlyUpdated = userRepository.findAll(pageRequest).getContent();
        return userMapper.toResponseList(recentlyUpdated);
    }

    private Double calculateGrowthRate() {
        // Simplified growth rate calculation
        long totalUsers = userRepository.count();
        long lastMonthUsers = totalUsers - 50; // Mock data
        if (lastMonthUsers == 0) return 0.0;
        return ((double) (totalUsers - lastMonthUsers) / lastMonthUsers) * 100;
    }
}