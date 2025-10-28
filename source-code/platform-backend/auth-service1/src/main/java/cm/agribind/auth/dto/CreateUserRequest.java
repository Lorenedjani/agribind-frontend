package cm.agribind.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank private String username;
    @NotBlank private String email;
    @NotBlank private String phone;
    @NotBlank private String password;
}
