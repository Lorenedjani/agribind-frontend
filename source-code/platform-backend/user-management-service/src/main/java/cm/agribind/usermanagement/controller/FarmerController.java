package cm.agribind.usermanagement.controller;

import cm.agribind.usermanagement.dto.command.RegisterFarmerCommand;
import cm.agribind.usermanagement.dto.pagination.PageResponse;
import cm.agribind.usermanagement.dto.response.FarmerResponse;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.enums.AgriculturalType;
import cm.agribind.usermanagement.enums.CropType;
import cm.agribind.usermanagement.enums.LivestockType;
import cm.agribind.usermanagement.service.command.UserCommandService;
import cm.agribind.usermanagement.service.query.FarmerQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/farmers")
@RequiredArgsConstructor
@Tag(name = "Farmer Management", description = "APIs specifically for farmer operations")
public class FarmerController {

    private final UserCommandService userCommandService;
    private final FarmerQueryService farmerQueryService;

    @PostMapping("/register")
    @Operation(summary = "Register a new farmer", description = "Register a new farmer with agricultural details")
    public ResponseEntity<UserResponse> registerFarmer(@Valid @RequestBody RegisterFarmerCommand command) {
        log.info("Registering new farmer: {}", command.getName());

        // Convert to CreateUserCommand for the general user service
        cm.agribind.usermanagement.dto.command.CreateUserCommand createCommand = new cm.agribind.usermanagement.dto.command.CreateUserCommand();
        createCommand.setType(cm.agribind.usermanagement.enums.UserType.FARMER);
        createCommand.setName(command.getName());
        createCommand.setPhoneNumber(command.getPhoneNumber());
        createCommand.setRegion(command.getRegion());
        createCommand.setDepartment(command.getDepartment());
        createCommand.setDistrict(command.getDistrict());
        createCommand.setVillage(command.getVillage());
        createCommand.setGpsCoordinates(command.getGpsCoordinates());
        createCommand.setPreferredLanguage(command.getPreferredLanguage());
        createCommand.setAgriculturalType(command.getAgriculturalType().name());

        if (command.getCropTypes() != null) {
            createCommand.setCropTypes(command.getCropTypes().stream()
                    .map(Enum::name)
                    .toArray(String[]::new));
        }

        if (command.getLivestockTypes() != null) {
            createCommand.setLivestockTypes(command.getLivestockTypes().stream()
                    .map(Enum::name)
                    .toArray(String[]::new));
        }

        createCommand.setLandArea(command.getLandArea());
        createCommand.setCooperativeId(command.getCooperativeId());

        UserResponse response = userCommandService.createUser(createCommand);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/agricultural-type/{type}")
    @Operation(summary = "Get farmers by agricultural type", description = "Get farmers by CROP, LIVESTOCK, or MIXED type")
    public ResponseEntity<List<FarmerResponse>> getFarmersByAgriculturalType(@PathVariable AgriculturalType type) {
        List<FarmerResponse> response = farmerQueryService.getFarmersByAgriculturalType(type);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/crop/{cropType}")
    @Operation(summary = "Get farmers by crop type", description = "Get farmers who grow specific crop type")
    public ResponseEntity<List<FarmerResponse>> getFarmersByCropType(@PathVariable CropType cropType) {
        List<FarmerResponse> response = farmerQueryService.getFarmersByCropType(cropType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/livestock/{livestockType}")
    @Operation(summary = "Get farmers by livestock type", description = "Get farmers who raise specific livestock type")
    public ResponseEntity<List<FarmerResponse>> getFarmersByLivestockType(@PathVariable LivestockType livestockType) {
        List<FarmerResponse> response = farmerQueryService.getFarmersByLivestockType(livestockType);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cooperative/{cooperativeId}")
    @Operation(summary = "Get farmers by cooperative", description = "Get all farmers belonging to a cooperative")
    public ResponseEntity<List<FarmerResponse>> getFarmersByCooperative(@PathVariable Long cooperativeId) {
        List<FarmerResponse> response = farmerQueryService.getFarmersByCooperative(cooperativeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cooperative/{cooperativeId}/paginated")
    @Operation(summary = "Get farmers by cooperative (paginated)", description = "Get paginated list of farmers in a cooperative")
    public ResponseEntity<PageResponse<UserResponse>> getFarmersByCooperativePaginated(
            @PathVariable Long cooperativeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<UserResponse> response = farmerQueryService.getFarmersByCooperative(cooperativeId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/land-area")
    @Operation(summary = "Get farmers by land area range", description = "Get farmers with land area between min and max")
    public ResponseEntity<List<FarmerResponse>> getFarmersByLandArea(
            @RequestParam(required = false) Double minArea,
            @RequestParam(required = false) Double maxArea) {
        List<FarmerResponse> response = farmerQueryService.getFarmersByLandAreaRange(minArea, maxArea);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/crops")
    @Operation(summary = "Get crop distribution", description = "Get statistics of crop types among farmers")
    public ResponseEntity<Map<String, Long>> getCropDistribution() {
        Map<String, Long> response = farmerQueryService.getCropDistribution();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/livestock")
    @Operation(summary = "Get livestock distribution", description = "Get statistics of livestock types among farmers")
    public ResponseEntity<Map<String, Long>> getLivestockDistribution() {
        Map<String, Long> response = farmerQueryService.getLivestockDistribution();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/statistics/agricultural-types")
    @Operation(summary = "Get agricultural type distribution", description = "Get distribution of CROP, LIVESTOCK, MIXED types")
    public ResponseEntity<Map<String, Long>> getAgriculturalTypeDistribution() {
        Map<String, Long> response = farmerQueryService.getAgriculturalTypeDistribution();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count/crop/{cropType}")
    @Operation(summary = "Count farmers by crop type", description = "Get count of farmers growing specific crop")
    public ResponseEntity<Long> countFarmersByCropType(@PathVariable CropType cropType) {
        long count = farmerQueryService.countFarmersByCropType(cropType);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/livestock/{livestockType}")
    @Operation(summary = "Count farmers by livestock type", description = "Get count of farmers raising specific livestock")
    public ResponseEntity<Long> countFarmersByLivestockType(@PathVariable LivestockType livestockType) {
        long count = farmerQueryService.countFarmersByLivestockType(livestockType);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/without-cooperative")
    @Operation(summary = "Get farmers without cooperative", description = "Get farmers not belonging to any cooperative")
    public ResponseEntity<List<FarmerResponse>> getFarmersWithoutCooperative() {
        // This would need a custom service method
        List<FarmerResponse> response = farmerQueryService.getFarmersByCooperative(null);
        return ResponseEntity.ok(response);
    }
}