package cm.agribind.usermanagement.service.command;

import cm.agribind.usermanagement.dto.command.CreateUserCommand;
import cm.agribind.usermanagement.dto.command.UpdateUserCommand;
import cm.agribind.usermanagement.dto.response.UserResponse;

public interface UserCommandService {

    UserResponse createUser(CreateUserCommand command);
    UserResponse updateUser(String userId, UpdateUserCommand command);
    UserResponse updateUserStatus(String userId, String status);
    void deleteUser(String userId);
    UserResponse generateQRCode(String userId, String purpose);
    UserResponse uploadProfilePicture(String userId, byte[] imageData, String contentType);
}