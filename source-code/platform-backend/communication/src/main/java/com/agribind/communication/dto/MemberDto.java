package com.agribind.communication.dto;

public class MemberDto {
    private Long id;
    private String name;
    private String phoneNumber;
    private String zone;
    private Boolean isActive;
    private String email;

    // Default constructor
    public MemberDto() {}

    // All arguments constructor
    public MemberDto(Long id, String name, String phoneNumber, String zone, Boolean isActive, String email) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.zone = zone;
        this.isActive = isActive;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getZone() { return zone; }
    public void setZone(String zone) { this.zone = zone; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Builder class
    public static class Builder {
        private Long id;
        private String name;
        private String phoneNumber;
        private String zone;
        private Boolean isActive;
        private String email;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder phoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; return this; }
        public Builder zone(String zone) { this.zone = zone; return this; }
        public Builder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public Builder email(String email) { this.email = email; return this; }

        public MemberDto build() {
            return new MemberDto(id, name, phoneNumber, zone, isActive, email);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}