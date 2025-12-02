package com.agribind.inventory.config;

import com.agribind.inventory.model.*;
import com.agribind.inventory.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize with sample data from the dashboard
        if (inventoryRepository.count() == 0) {
            // Farmer Products
            inventoryRepository.save(new FarmerProduct("PRO001", "Cocoa Beans (Grade A)", "Cocoa",
                168.3, 30.0, "MT", new BigDecimal("353430000"),
                "Donald Warehouse", InventoryItem.StockStatus.IN_STOCK, InventoryItem.Trend.UP, "Grade A"));

            inventoryRepository.save(new FarmerProduct("PRO002", "Coffee Beans (Arabica A)", "Coffee",
                82.5, 30.0, "MT", new BigDecimal("181500000"),
                "Yaoundé Warehouse", InventoryItem.StockStatus.IN_STOCK, InventoryItem.Trend.UP, "Arabica A"));

            // Input Supplies
            inventoryRepository.save(new InputSupply("INP001", "Cocoa Seedlings (Hybrid)", "Seeds",
                5000.0, 2000.0, "units", new BigDecimal("2500000"),
                "Central Depot", InventoryItem.StockStatus.IN_STOCK, InventoryItem.Trend.STABLE, "AgriSupply Co."));

            // Livestock
            inventoryRepository.save(new Livestock("LIV001", "Cattle (Adult)", "Cattle",
                125.0, 50.0, "heads", new BigDecimal("187500000"),
                "Ranch A - Adamawa", InventoryItem.StockStatus.IN_STOCK, InventoryItem.Trend.STABLE, "Mixed Breed"));
        }
    }
}