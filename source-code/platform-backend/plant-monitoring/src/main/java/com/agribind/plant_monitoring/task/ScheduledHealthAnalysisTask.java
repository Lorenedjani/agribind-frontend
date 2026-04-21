package com.agribind.plant_monitoring.task;

import com.agribind.plant_monitoring.model.PlantPhoto;
import com.agribind.plant_monitoring.service.PlantHealthAnalysisService;
import com.agribind.plant_monitoring.service.PlantPhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ScheduledHealthAnalysisTask {
    
    private final PlantPhotoService plantPhotoService;
    private final PlantHealthAnalysisService healthAnalysisService;
    
    @Autowired
    public ScheduledHealthAnalysisTask(
            PlantPhotoService plantPhotoService,
            PlantHealthAnalysisService healthAnalysisService) {
        this.plantPhotoService = plantPhotoService;
        this.healthAnalysisService = healthAnalysisService;
        System.out.println("ScheduledHealthAnalysisTask initialized");
    }
    
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void analyzeUnprocessedPhotos() {
        System.out.println("Starting scheduled health analysis for unprocessed photos");
        
        List<PlantPhoto> unprocessedPhotos = plantPhotoService.getUnprocessedPhotos();
        
        for (PlantPhoto photo : unprocessedPhotos) {
            try {
                healthAnalysisService.analyzePlantHealth(photo);
                System.out.println("Scheduled analysis started for photo: " + photo.getId());
            } catch (Exception e) {
                System.err.println("Failed to analyze photo: " + photo.getId());
                e.printStackTrace();
            }
        }
    }
    
    @Scheduled(cron = "0 0 1 * * MON") // Run every Monday at 1 AM
    public void generateWeeklyHealthReports() {
        System.out.println("Generating weekly health reports");
        // Implement weekly report generation

        // Example: Generate weekly report for all plants
        // healthAnalysisService.generateWeeklyReport();
    }
}