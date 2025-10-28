package cm.agribind.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class ChangePasswordRequest {
    @NotBlank
    private String newPassword;
    public String getNewPassword(){return newPassword;} public void setNewPassword(String p){this.newPassword=p;}
}
