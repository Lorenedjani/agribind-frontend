package com.agribind.communication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ==================== Member DTO ====================
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class MemberDto {
    private Long id;
    private String name;
    private String phoneNumber;
    private String zone;
    private Boolean isActive;
    private String email;
}