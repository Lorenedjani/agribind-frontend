package cm.agribind.usermanagement.controller;

import cm.agribind.usermanagement.dto.pagination.PageResponse;
import cm.agribind.usermanagement.dto.response.CooperativeResponse;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.enums.CooperativeType;
import cm.agribind.usermanagement.enums.Region;
import cm.agribind.usermanagement.service.query.CooperativeQueryService;
import cm.agribind.usermanagement.service.query.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/cooperatives")
@RequiredArgsConstructor
@Tag(name = "Cooperative Management", description = "APIs specifically for cooperative operations")
public class CooperativeController {

    private final CooperativeQueryService cooperativeQueryService;
    private final UserQueryService userQueryService;

    @GetMapping
    @Operation(summary = "Get all cooperatives", description = "Get paginated list of all cooperatives")
    public ResponseEntity<PageResponse<UserResponse>> getCooperatives(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching cooperatives - page: {}, size: {}", page, size);

        // Using UserQueryService with COOPERATIVE type filter
        cm.agribind.usermanagement.dto.query.UserQuery query = new cm.agribind.usermanagement.dto.query.UserQuery();
        query.setType(cm.agribind.usermanagement.enums.UserType.COOPERATIVE);
        query.setPage(page);
        query.setSize(size);

        PageResponse<UserResponse> response = userQueryService.getUsers(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cooperativeId}")
    @Operation(summary = "Get cooperative by ID", description = "Get cooperative details by ID")
    public ResponseEntity<UserResponse> getCooperativeById(@PathVariable String cooperativeId) {
        UserResponse response = userQueryService.getUserById(cooperativeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{cooperativeType}")
    @Operation(summary = "Get cooperatives by type", description = "Get cooperatives of specific type")
    public ResponseEntity<List<UserResponse>> getCooperativesByType(@PathVariable CooperativeType cooperativeType) {
        // This would need a custom implementation
        List<UserResponse> cooperatives = userQueryService.getUsersByType(cm.agribind.usermanagement.enums.UserType.COOPERATIVE);
        // Filter by type in service layer would be better
        return ResponseEntity.ok(cooperatives);
    }

    @GetMapping("/region/{region}")
    @Operation(summary = "Get cooperatives by region", description = "Get cooperatives in specific region")
    public ResponseEntity<List<UserResponse>> getCooperativesByRegion(@PathVariable Region region) {
        List<UserResponse> response = userQueryService.getUsersByRegion(region.name());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{cooperativeId}/members")
    @Operation(summary = "Get cooperative members", description = "Get all farmers belonging to a cooperative")
    public ResponseEntity<List<UserResponse>> getCooperativeMembers(@PathVariable String cooperativeId) {
        // This would need to find the cooperative entity first, then get members
        // For now, return empty list - implementation would be in service
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{cooperativeId}/members/paginated")
    @Operation(summary = "Get cooperative members (paginated)", description = "Get paginated list of cooperative members")
    public ResponseEntity<PageResponse<UserResponse>> getCooperativeMembersPaginated(
            @PathVariable String cooperativeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Implementation would find cooperative and get paginated members
        return ResponseEntity.ok(new PageResponse<>(List.of(), page, size, 0));
    }

    @GetMapping("/statistics/member-count")
    @Operation(summary = "Get cooperative member statistics", description = "Get statistics on cooperative member counts")
    public ResponseEntity<Map<String, Object>> getCooperativeMemberStatistics() {
        Map<String, Object> stats = Map.of(
                "totalCooperatives", cooperativeQueryService.getTotalCooperatives(),
                "averageMembers", cooperativeQueryService.getAverageMemberCount(),
                "largestCooperative", cooperativeQueryService.getLargestCooperative()
        );
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistics/regional-distribution")
    @Operation(summary = "Get cooperative regional distribution", description = "Get distribution of cooperatives across regions")
    public ResponseEntity<Map<String, Long>> getCooperativeRegionalDistribution() {
        Map<String, Long> distribution = cooperativeQueryService.getCooperativesByRegionDistribution();
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/with-storage-facilities")
    @Operation(summary = "Get cooperatives with storage facilities", description = "Get cooperatives that have storage facilities")
    public ResponseEntity<List<UserResponse>> getCooperativesWithStorageFacilities() {
        List<UserResponse> cooperatives = cooperativeQueryService.getCooperativesWithStorageFacilities();
        return ResponseEntity.ok(cooperatives);
    }

    @GetMapping("/established-after/{year}")
    @Operation(summary = "Get cooperatives established after year", description = "Get cooperatives established after specific year")
    public ResponseEntity<List<UserResponse>> getCooperativesEstablishedAfter(@PathVariable Integer year) {
        List<UserResponse> cooperatives = cooperativeQueryService.getCooperativesEstablishedAfter(year);
        return ResponseEntity.ok(cooperatives);
    }

    @GetMapping("/top-by-members")
    @Operation(summary = "Get top cooperatives by member count", description = "Get cooperatives with highest member counts")
    public ResponseEntity<List<UserResponse>> getTopCooperativesByMembers(
            @RequestParam(defaultValue = "10") int limit) {
        List<UserResponse> cooperatives = cooperativeQueryService.getTopCooperativesByMemberCount(limit);
        return ResponseEntity.ok(cooperatives);
    }
}