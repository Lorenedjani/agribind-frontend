package cm.agribind.usermanagement.controller;

import cm.agribind.usermanagement.dto.pagination.PageResponse;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.enums.GovernmentRole;
import cm.agribind.usermanagement.enums.Region;
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
@RequestMapping("/api/v1/government")
@RequiredArgsConstructor
@Tag(name = "Government Management", description = "APIs specifically for government officials")
public class GovernmentController {

    private final GovernmentQueryService governmentQueryService;
    private final UserQueryService userQueryService;

    @GetMapping
    @Operation(summary = "Get all government officials", description = "Get paginated list of all government officials")
    public ResponseEntity<PageResponse<UserResponse>> getGovernmentOfficials(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching government officials - page: {}, size: {}", page, size);

        cm.agribind.usermanagement.dto.query.UserQuery query = new cm.agribind.usermanagement.dto.query.UserQuery();
        query.setType(cm.agribind.usermanagement.enums.UserType.GOVERNMENT);
        query.setPage(page);
        query.setSize(size);

        PageResponse<UserResponse> response = userQueryService.getUsers(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{officialId}")
    @Operation(summary = "Get government official by ID", description = "Get government official details by ID")
    public ResponseEntity<UserResponse> getGovernmentOfficialById(@PathVariable String officialId) {
        UserResponse response = userQueryService.getUserById(officialId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/role/{role}")
    @Operation(summary = "Get officials by role", description = "Get government officials by specific role")
    public ResponseEntity<List<UserResponse>> getOfficialsByRole(@PathVariable GovernmentRole role) {
        List<UserResponse> officials = governmentQueryService.getOfficialsByRole(role);
        return ResponseEntity.ok(officials);
    }

    @GetMapping("/region/{region}")
    @Operation(summary = "Get officials by region", description = "Get government officials assigned to specific region")
    public ResponseEntity<List<UserResponse>> getOfficialsByRegion(@PathVariable Region region) {
        List<UserResponse> officials = governmentQueryService.getOfficialsByRegion(region);
        return ResponseEntity.ok(officials);
    }

    @GetMapping("/department/{department}")
    @Operation(summary = "Get officials by department", description = "Get government officials by department")
    public ResponseEntity<List<UserResponse>> getOfficialsByDepartment(@PathVariable String department) {
        List<UserResponse> officials = governmentQueryService.getOfficialsByDepartment(department);
        return ResponseEntity.ok(officials);
    }

    @GetMapping("/can-approve-loans")
    @Operation(summary = "Get officials who can approve loans", description = "Get government officials with loan approval permissions")
    public ResponseEntity<List<UserResponse>> getOfficialsWithLoanApproval() {
        List<UserResponse> officials = governmentQueryService.getOfficialsWithLoanApproval();
        return ResponseEntity.ok(officials);
    }

    @GetMapping("/field-officers")
    @Operation(summary = "Get field officers", description = "Get government officials who are field officers")
    public ResponseEntity<List<UserResponse>> getFieldOfficers() {
        List<UserResponse> officials = governmentQueryService.getFieldOfficers();
        return ResponseEntity.ok(officials);
    }

    @GetMapping("/statistics/role-distribution")
    @Operation(summary = "Get role distribution", description = "Get distribution of government officials by role")
    public ResponseEntity<Map<String, Long>> getRoleDistribution() {
        Map<String, Long> distribution = governmentQueryService.getRoleDistribution();
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/statistics/regional-coverage")
    @Operation(summary = "Get regional coverage", description = "Get distribution of officials across regions")
    public ResponseEntity<Map<String, Long>> getRegionalCoverage() {
        Map<String, Long> coverage = governmentQueryService.getRegionalCoverage();
        return ResponseEntity.ok(coverage);
    }

    @GetMapping("/coordinators")
    @Operation(summary = "Get regional coordinators", description = "Get all regional coordinators")
    public ResponseEntity<List<UserResponse>> getRegionalCoordinators() {
        List<UserResponse> coordinators = governmentQueryService.getOfficialsByRole(GovernmentRole.REGIONAL_COORDINATOR);
        return ResponseEntity.ok(coordinators);
    }

    @GetMapping("/extension-agents")
    @Operation(summary = "Get agricultural extension agents", description = "Get all agricultural extension agents")
    public ResponseEntity<List<UserResponse>> getExtensionAgents() {
        List<UserResponse> agents = governmentQueryService.getOfficialsByRole(GovernmentRole.AGRICULTURAL_EXTENSION_AGENT);
        return ResponseEntity.ok(agents);
    }
}