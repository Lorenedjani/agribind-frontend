package com.agribind.plant_monitoring.task;

import com.agribind.plant_monitoring.model.PlantPhoto;
import com.agribind.plant_monitoring.service.PlantHealthAnalysisService;
import com.agribind.plant_monitoring.service.PlantPhotoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class ScheduledHealthAnalysisTask {
    
    @Autowired
    private PlantPhotoService plantPhotoService;
    
    @Autowired
    private PlantHealthAnalysisService healthAnalysisService;
    
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void analyzeUnprocessedPhotos() {
        log.info("Starting scheduled health analysis for unprocessed photos");
        
        List<PlantPhoto> unprocessedPhotos = plantPhotoService.getUnprocessedPhotos();
        
        for (PlantPhoto photo : unprocessedPhotos) {
            try {
                healthAnalysisService.analyzePlantHealth(photo);
                log.info("Scheduled analysis started for photo: {}", photo.getId());
            } catch (Exception e) {
                log.error("Failed to analyze photo: {}", photo.getId(), e);
            }
        }
    }
    
    @Scheduled(cron = "0 0 1 * * MON") // Run every Monday at 1 AM
    public void generateWeeklyHealthReports() {
        log.info("Generating weekly health reports");
        // Implement weekly report generation
    }
}