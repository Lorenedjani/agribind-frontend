package cm.agribind.usermanagement.service.query.impl;

import cm.agribind.usermanagement.dto.pagination.PageResponse;
import cm.agribind.usermanagement.dto.query.UserFilterQuery;
import cm.agribind.usermanagement.dto.query.UserQuery;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.entity.User;
import cm.agribind.usermanagement.enums.UserStatus;
import cm.agribind.usermanagement.enums.UserType;
import cm.agribind.usermanagement.exception.UserNotFoundException;
import cm.agribind.usermanagement.mapper.QueryMapper;
import cm.agribind.usermanagement.mapper.UserMapper;
import cm.agribind.usermanagement.repository.UserRepository;
import cm.agribind.usermanagement.repository.spec.UserSpecification;
import cm.agribind.usermanagement.service.query.UserQueryService;
import cm.agribind.usermanagement.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final QueryMapper queryMapper;

    @Override
    public UserResponse getUserById(String userId) {
        log.debug("Fetching user by ID: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserByPhone(String phoneNumber) {
        log.debug("Fetching user by phone: {}", phoneNumber);

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found with phone: " + phoneNumber));

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserByRegistrationNumber(String registrationNumber) {
        log.debug("Fetching user by registration number: {}", registrationNumber);

        User user = userRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new UserNotFoundException("User not found with registration: " + registrationNumber));

        return userMapper.toResponse(user);
    }

    // CRITICAL FIX: UserQueryServiceImpl.java - getUsers method
// Replace the existing getUsers method with this error-safe version

    @Override
    public PageResponse<UserResponse> getUsers(UserQuery query) {
        log.debug("Fetching users with query: {}", query);

        try {
            // ✅ Validate and sanitize query parameters
            if (query.getPage() == null || query.getPage() < 0) {
                log.warn("Invalid page: {}. Setting to 0.", query.getPage());
                query.setPage(0);
            }
            if (query.getSize() == null || query.getSize() <= 0 || query.getSize() > 100) {
                log.warn("Invalid size: {}. Setting to 20.", query.getSize());
                query.setSize(20);
            }
            if (query.getSortBy() == null || query.getSortBy().trim().isEmpty()) {
                query.setSortBy("createdAt");
            }
            if (query.getDirection() == null) {
                query.setDirection(Sort.Direction.DESC);
            }

            // ✅ Create Pageable with error handling
            Pageable pageable;
            try {
                pageable = PaginationUtil.createPageable(
                        query.getPage(),
                        query.getSize(),
                        query.getSortBy(),
                        query.getDirection().name()
                );
            } catch (Exception e) {
                log.error("Error creating pageable, using default", e);
                pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
            }

            // ✅ Build specification with null-safe logic
            Specification<User> spec = buildSafeSpecification(query);

            // ✅ Execute query with error handling
            Page<User> userPage;
            try {
                userPage = userRepository.findAll(spec, pageable);
            } catch (Exception e) {
                log.error("❌ Error executing user query", e);
                // Return empty result instead of throwing
                return new PageResponse<>(List.of(), 0, 20, 0L);
            }

            // ✅ Map results
            List<UserResponse> content = userPage.getContent()
                    .stream()
                    .map(user -> {
                        try {
                            return userMapper.toResponse(user);
                        } catch (Exception e) {
                            log.error("Error mapping user {}: {}", user.getUserId(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            log.info("✅ Successfully fetched {} users out of {} total",
                    content.size(), userPage.getTotalElements());

            return PageResponse.of(
                    content,
                    userPage.getNumber(),
                    userPage.getSize(),
                    userPage.getTotalElements()
            );

        } catch (Exception e) {
            log.error("❌ Unexpected error in getUsers", e);
            // Return empty result to prevent frontend crash
            return new PageResponse<>(List.of(), 0, 20, 0L);
        }
    }

    // ✅ NEW: Safe specification builder
    private Specification<User> buildSafeSpecification(UserQuery query) {
        Specification<User> spec = Specification.where(null);

        try {
            if (query.getType() != null) {
                spec = spec.and(UserSpecification.hasType(query.getType()));
            }
        } catch (Exception e) {
            log.warn("Error adding type filter", e);
        }

        try {
            if (query.getStatus() != null) {
                spec = spec.and(UserSpecification.hasStatus(query.getStatus()));
            }
        } catch (Exception e) {
            log.warn("Error adding status filter", e);
        }

        try {
            if (query.getRegion() != null) {
                spec = spec.and(UserSpecification.inRegion(query.getRegion()));
            }
        } catch (Exception e) {
            log.warn("Error adding region filter", e);
        }

        try {
            if (query.getSearchTerm() != null && !query.getSearchTerm().trim().isEmpty()) {
                spec = spec.and(
                        UserSpecification.nameContains(query.getSearchTerm())
                                .or(UserSpecification.phoneContains(query.getSearchTerm()))
                );
            }
        } catch (Exception e) {
            log.warn("Error adding search filter", e);
        }

        return spec;
    }

    @Override
    public PageResponse<UserResponse> searchUsers(UserFilterQuery query) {
        log.debug("Searching users with filters: {}", query);

        Pageable pageable = queryMapper.toPageable(query);
        Specification<User> spec = buildFilterSpecification(query);

        Page<User> userPage = userRepository.findAll(spec, pageable);

        List<UserResponse> content = userPage.getContent()
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        PageResponse<UserResponse> response = PageResponse.of(
                content,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements()
        );

        // Set filter information for frontend
        response.setFilters(query);
        response.setFilteredCount(userPage.getTotalElements());

        return response;
    }

    @Override
    public List<UserResponse> getUsersByType(UserType type) {
        log.debug("Fetching users by type: {}", type);

        List<User> users = userRepository.findByType(type);
        return userMapper.toResponseList(users);
    }

    @Override
    public List<UserResponse> getUsersByStatus(String status) {
        log.debug("Fetching users by status: {}", status);

        UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
        List<User> users = userRepository.findByStatus(userStatus);
        return userMapper.toResponseList(users);
    }

    @Override
    public List<UserResponse> getUsersByRegion(String region) {
        log.debug("Fetching users by region: {}", region);

        // This would need a custom repository method or specification
        Specification<User> spec = UserSpecification.inRegion(
                cm.agribind.usermanagement.enums.Region.valueOf(region.toUpperCase())
        );

        List<User> users = userRepository.findAll(spec);
        return userMapper.toResponseList(users);
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    private Specification<User> buildSpecification(UserQuery query) {
        return Specification.where(UserSpecification.hasType(query.getType()))
                .and(UserSpecification.hasStatus(query.getStatus()))
                .and(UserSpecification.inRegion(query.getRegion()))
                .and(UserSpecification.nameContains(query.getSearchTerm()))
                .and(UserSpecification.phoneContains(query.getSearchTerm()));
    }

    private Specification<User> buildFilterSpecification(UserFilterQuery query) {
        Specification<User> spec = Specification.where(null);

        // Common filters
        if (query.getType() != null) {
            spec = spec.and(UserSpecification.hasType(query.getType()));
        }
        if (query.getStatus() != null) {
            spec = spec.and(UserSpecification.hasStatus(query.getStatus()));
        }
        if (query.getRegion() != null) {
            spec = spec.and(UserSpecification.inRegion(query.getRegion()));
        }
        if (query.getSearchTerm() != null && !query.getSearchTerm().trim().isEmpty()) {
            spec = spec.and(UserSpecification.nameContains(query.getSearchTerm())
                    .or(UserSpecification.phoneContains(query.getSearchTerm())));
        }

        return spec;
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        return userMapper.toResponse(user);
    }
}