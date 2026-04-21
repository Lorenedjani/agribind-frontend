package com.agribind.production_monitoring.inventory.controller;

import com.agribind.production_monitoring.inventory.dto.InventorySummaryDTO;
import com.agribind.production_monitoring.inventory.model.InventoryItem;
import com.agribind.production_monitoring.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public List<InventoryItem> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    @GetMapping("/search")
    public List<InventoryItem> searchInventory(@RequestParam String q) {
        return inventoryService.searchInventory(q);
    }

    @GetMapping("/category/{category}")
    public List<InventoryItem> getInventoryByCategory(@PathVariable String category) {
        return inventoryService.getInventoryByCategory(category);
    }

    @GetMapping("/status/{status}")
    public List<InventoryItem> getInventoryByStatus(@PathVariable String status) {
        return inventoryService.getInventoryByStatus(status);
    }

    @GetMapping("/location/{location}")
    public List<InventoryItem> getInventoryByLocation(@PathVariable String location) {
        return inventoryService.getInventoryByLocation(location);
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<InventoryItem> getInventoryItemByItemId(@PathVariable String itemId) {
        Optional<InventoryItem> item = inventoryService.getInventoryItemByItemId(itemId);
        return item.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/summary")
    public InventorySummaryDTO getInventorySummary() {
        return inventoryService.getInventorySummary();
    }

    @PostMapping
    public InventoryItem createInventoryItem(@RequestBody InventoryItem item) {
        return inventoryService.createInventoryItem(item);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryItem> updateInventoryItem(@PathVariable String id, @RequestBody InventoryItem itemDetails) {
        InventoryItem updatedItem = inventoryService.updateInventoryItem(id, itemDetails);
        return updatedItem != null ? ResponseEntity.ok(updatedItem) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventoryItem(@PathVariable String id) {
        boolean deleted = inventoryService.deleteInventoryItem(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "Inventory Service is running (merged into Production Monitoring)";
    }
}
