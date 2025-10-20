package com.agribind.shared.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthRequest {
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    private String password;
    
    private String userType; // FARMER, COOPERATIVE_ADMIN, GOVERNMENT_OFFICIAL
    
    // constructors, getters, setters
}