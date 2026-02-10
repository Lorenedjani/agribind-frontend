package com.agribind.inventory.controller;

import com.agribind.inventory.dto.InventorySummaryDTO;
import com.agribind.inventory.model.InventoryItem;
import com.agribind.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
@CrossOrigin(origins = "*")
@Tag(name = "Inventory Service", description = "API for managing inventory items, farmer products, input supplies, and livestock")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Operation(summary = "Get all inventory items", description = "Retrieve a list of all inventory items")
    @ApiResponse(responseCode = "200", description = "Inventory items retrieved successfully")
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

    @Operation(summary = "Get inventory summary", description = "Retrieve summary statistics of inventory items")
    @ApiResponse(responseCode = "200", description = "Inventory summary retrieved successfully",
        content = @Content(schema = @Schema(implementation = InventorySummaryDTO.class)))
    @GetMapping("/summary")
    public InventorySummaryDTO getInventorySummary() {
        return inventoryService.getInventorySummary();
    }

    @Operation(summary = "Create inventory item", description = "Create a new inventory item")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventory item created successfully",
            content = @Content(schema = @Schema(implementation = InventoryItem.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping
    public InventoryItem createInventoryItem(
            @Parameter(description = "Inventory item details", required = true)
            @RequestBody InventoryItem item) {
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
        return "Inventory Service is running";
    }
}