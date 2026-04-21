package com.agribind.plant_monitoring.controller;

import com.agribind.plant_monitoring.model.Plant;
import com.agribind.plant_monitoring.service.PlantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plants")
@Tag(name = "Plant Management", description = "Endpoints for managing plants")
public class PlantController {
    
    @Autowired
    private PlantService plantService;
    
    @PostMapping
    @Operation(summary = "Create a new plant")
    public ResponseEntity<Plant> createPlant(@Valid @RequestBody Plant plant) {
        Plant createdPlant = plantService.createPlant(plant);
        return ResponseEntity.ok(createdPlant);
    }
    
    @GetMapping
    @Operation(summary = "Get all plants")
    public ResponseEntity<List<Plant>> getAllPlants() {
        List<Plant> plants = plantService.getAllPlants();
        return ResponseEntity.ok(plants);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get plant by ID")
    public ResponseEntity<Plant> getPlantById(@PathVariable Long id) {
        Plant plant = plantService.getPlantById(id);
        return ResponseEntity.ok(plant);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update plant information")
    public ResponseEntity<Plant> updatePlant(@PathVariable Long id, @Valid @RequestBody Plant plant) {
        Plant updatedPlant = plantService.updatePlant(id, plant);
        return ResponseEntity.ok(updatedPlant);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a plant")
    public ResponseEntity<Void> deletePlant(@PathVariable Long id) {
        plantService.deletePlant(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search plants by name")
    public ResponseEntity<List<Plant>> searchPlants(@RequestParam String name) {
        // Implementation depends on repository method
        List<Plant> plants = plantService.searchPlantsByName(name);
        return ResponseEntity.ok().build();
    }
}