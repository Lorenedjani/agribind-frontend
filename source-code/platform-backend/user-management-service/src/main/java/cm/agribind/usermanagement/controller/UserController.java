package cm.agribind.usermanagement.controller;

import cm.agribind.usermanagement.dto.command.CreateUserCommand;
import cm.agribind.usermanagement.dto.command.UpdateUserCommand;
import cm.agribind.usermanagement.dto.pagination.PageResponse;
import cm.agribind.usermanagement.dto.query.UserFilterQuery;
import cm.agribind.usermanagement.dto.query.UserQuery;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.enums.Region;
import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import cm.agribind.usermanagement.service.command.UserCommandService;
import cm.agribind.usermanagement.service.query.UserQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users, farmers, cooperatives, and government officials")
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Create a new user (farmer, cooperative, or government official)")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserCommand command) {
        log.info("Creating user of type: {}", command.getType());
        UserResponse response = userCommandService.createUser(command);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/with-file")
    @Operation(summary = "Create user with profile picture", description = "Create user with profile picture upload")
    public ResponseEntity<UserResponse> createUserWithFile(
            @RequestPart("userData") @Valid CreateUserCommand command,
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
        log.info("Creating user with file - Type: {}, Name: {}", command.getType(), command.getName());

        if (profilePicture != null && !profilePicture.isEmpty()) {
            // This would be handled in the service layer
            log.info("Profile picture provided: {}", profilePicture.getOriginalFilename());
        }

        UserResponse response = userCommandService.createUser(command);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get users with filtering", description = "Get paginated list of users with filtering options")
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            @RequestParam(required = false) UserType type,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) Region region,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        UserQuery query = new UserQuery();
        query.setType(type);
        query.setStatus(status);
        query.setRegion(region);
        query.setSearchTerm(search);
        query.setPage(page);
        query.setSize(size);
        query.setSortBy(sortBy);
        query.setDirection(Sort.Direction.fromString(sortDirection));

        PageResponse<UserResponse> response = userQueryService.getUsers(query);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    @Operation(summary = "Search users with advanced filters", description = "Search users with comprehensive filtering options")
    public ResponseEntity<PageResponse<UserResponse>> searchUsers(@Valid @RequestBody UserFilterQuery query) {
        log.info("Searching users with filters: {}", query);
        PageResponse<UserResponse> response = userQueryService.searchUsers(query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Get user details by user ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String userId) {
        UserResponse response = userQueryService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/phone/{phoneNumber}")
    @Operation(summary = "Get user by phone number", description = "Get user details by phone number")
    public ResponseEntity<UserResponse> getUserByPhone(@PathVariable String phoneNumber) {
        UserResponse response = userQueryService.getUserByPhone(phoneNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/registration/{registrationNumber}")
    @Operation(summary = "Get user by registration number", description = "Get user details by registration number")
    public ResponseEntity<UserResponse> getUserByRegistration(@PathVariable String registrationNumber) {
        UserResponse response = userQueryService.getUserByRegistrationNumber(registrationNumber);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update user", description = "Update user information")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserCommand command) {
        log.info("Updating user: {}", userId);
        UserResponse response = userCommandService.updateUser(userId, command);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}/status")
    @Operation(summary = "Update user status", description = "Update user status (ACTIVE, INACTIVE, SUSPENDED)")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable String userId,
            @RequestParam UserStatus status) {
        log.info("Updating status for user: {} to {}", userId, status);
        UserResponse response = userCommandService.updateUserStatus(userId, status.name());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete user", description = "Soft delete user by setting status to DELETED")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        log.info("Deleting user: {}", userId);
        userCommandService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/types/{type}")
    @Operation(summary = "Get users by type", description = "Get all users of specific type")
    public ResponseEntity<List<UserResponse>> getUsersByType(@PathVariable UserType type) {
        List<UserResponse> response = userQueryService.getUsersByType(type);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get users by status", description = "Get all users with specific status")
    public ResponseEntity<List<UserResponse>> getUsersByStatus(@PathVariable String status) {
        List<UserResponse> response = userQueryService.getUsersByStatus(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/region/{region}")
    @Operation(summary = "Get users by region", description = "Get all users in specific region")
    public ResponseEntity<List<UserResponse>> getUsersByRegion(@PathVariable Region region) {
        List<UserResponse> response = userQueryService.getUsersByRegion(region.name());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/exists/phone/{phoneNumber}")
    @Operation(summary = "Check phone number exists", description = "Check if phone number is already registered")
    public ResponseEntity<Boolean> checkPhoneExists(@PathVariable String phoneNumber) {
        boolean exists = userQueryService.existsByPhoneNumber(phoneNumber);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/email/{email}")
    @Operation(summary = "Check email exists", description = "Check if email is already registered")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        boolean exists = userQueryService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/{userId}/profile-picture")
    @Operation(summary = "Upload profile picture", description = "Upload or update user profile picture")
    public ResponseEntity<UserResponse> uploadProfilePicture(
            @PathVariable String userId,
            @RequestParam("file") MultipartFile file) {
        log.info("Uploading profile picture for user: {}", userId);
        try {
            UserResponse response = userCommandService.uploadProfilePicture(userId, file.getBytes(), file.getContentType());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to upload profile picture", e);
            return ResponseEntity.badRequest().build();
        }
    }
}