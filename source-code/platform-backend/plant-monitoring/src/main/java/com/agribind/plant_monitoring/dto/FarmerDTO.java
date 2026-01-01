package com.agribind.plant_monitoring.dto;

import java.util.List;

public class FarmerDTO {
    private Long id;
    private String code;
    private String name;
    private String phone;
    private String email;
    private String location;
    private String cooperative;
    private String farmSize;
    private List<String> crops;
    private Boolean isActive;
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getCooperative() {
        return cooperative;
    }
    
    public void setCooperative(String cooperative) {
        this.cooperative = cooperative;
    }
    
    public String getFarmSize() {
        return farmSize;
    }
    
    public void setFarmSize(String farmSize) {
        this.farmSize = farmSize;
    }
    
    public List<String> getCrops() {
        return crops;
    }
    
    public void setCrops(List<String> crops) {
        this.crops = crops;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
