package cm.agribind.auth.dto;

import jakarta.validation.constraints.NotBlank;

public class FarmerRegisterRequest {
    @NotBlank
    private String phone;
    @NotBlank private String address;
    public String getPhone(){return phone;} public void setPhone(String p){this.phone=p;}
    public String getAddress(){return address;} public void setAddress(String a){this.address=a;}
}
