package com.agribind.plant_monitoring.dto;

import java.util.Map;

public class DiseaseReportSummaryDTO {
    private Long totalReports;
    private Map<String, Long> reportsByStatus;
    private Map<String, Long> reportsBySeverity;
    private Map<String, Long> reportsByDisease;
    private Long activeCases;
    private Long resolvedCases;
    private Long criticalAlerts;
    private Long pendingInvestigations;
    private Long reportsThisWeek;
    private Long reportsThisMonth;
    
    // Getters and Setters
    public Long getTotalReports() {
        return totalReports;
    }
    
    public void setTotalReports(Long totalReports) {
        this.totalReports = totalReports;
    }
    
    public Map<String, Long> getReportsByStatus() {
        return reportsByStatus;
    }
    
    public void setReportsByStatus(Map<String, Long> reportsByStatus) {
        this.reportsByStatus = reportsByStatus;
    }
    
    public Map<String, Long> getReportsBySeverity() {
        return reportsBySeverity;
    }
    
    public void setReportsBySeverity(Map<String, Long> reportsBySeverity) {
        this.reportsBySeverity = reportsBySeverity;
    }
    
    public Map<String, Long> getReportsByDisease() {
        return reportsByDisease;
    }
    
    public void setReportsByDisease(Map<String, Long> reportsByDisease) {
        this.reportsByDisease = reportsByDisease;
    }
    
    public Long getActiveCases() {
        return activeCases;
    }
    
    public void setActiveCases(Long activeCases) {
        this.activeCases = activeCases;
    }
    
    public Long getResolvedCases() {
        return resolvedCases;
    }
    
    public void setResolvedCases(Long resolvedCases) {
        this.resolvedCases = resolvedCases;
    }
    
    public Long getCriticalAlerts() {
        return criticalAlerts;
    }
    
    public void setCriticalAlerts(Long criticalAlerts) {
        this.criticalAlerts = criticalAlerts;
    }
    
    public Long getPendingInvestigations() {
        return pendingInvestigations;
    }
    
    public void setPendingInvestigations(Long pendingInvestigations) {
        this.pendingInvestigations = pendingInvestigations;
    }
    
    public Long getReportsThisWeek() {
        return reportsThisWeek;
    }
    
    public void setReportsThisWeek(Long reportsThisWeek) {
        this.reportsThisWeek = reportsThisWeek;
    }
    
    public Long getReportsThisMonth() {
        return reportsThisMonth;
    }
    
    public void setReportsThisMonth(Long reportsThisMonth) {
        this.reportsThisMonth = reportsThisMonth;
    }
}