package cm.agribind.usermanagement.service.query;

import cm.agribind.usermanagement.dto.pagination.PageResponse;
import cm.agribind.usermanagement.dto.query.UserFilterQuery;
import cm.agribind.usermanagement.dto.query.UserQuery;
import cm.agribind.usermanagement.dto.response.UserResponse;
import cm.agribind.usermanagement.enums.UserType;

import java.util.List;

public interface UserQueryService {

    UserResponse getUserById(String userId);
    UserResponse getUserByPhone(String phoneNumber);
    UserResponse getUserByRegistrationNumber(String registrationNumber);
    PageResponse<UserResponse> getUsers(UserQuery query);
    PageResponse<UserResponse> searchUsers(UserFilterQuery query);
    List<UserResponse> getUsersByType(UserType type);
    List<UserResponse> getUsersByStatus(String status);
    List<UserResponse> getUsersByRegion(String region);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);

    UserResponse getUserByEmail(String email);
}