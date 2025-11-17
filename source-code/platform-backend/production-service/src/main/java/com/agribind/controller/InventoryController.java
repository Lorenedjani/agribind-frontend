//package com.agribind.controller;
//
//import com.agribind.dto.requests.InventoryCreateRequest;
//import com.agribind.entity.InventoryItem;
//import com.agribind.service.InventoryService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/cooperatives/{coopId}/inventory")
//@RequiredArgsConstructor
//public class InventoryController {
//    private final InventoryService inventoryService;
//
//    @PostMapping
//    public ResponseEntity<InventoryItem> create(@PathVariable String coopId, @RequestBody InventoryCreateRequest req) {
//        InventoryItem item = InventoryItem.builder()
//                .cooperativeId(coopId)
//                .name(req.getName())
//                .type(req.getType())
//                .quantity(req.getQuantity())
//                .unit(req.getUnit())
//                .lowStockThreshold(req.getLowStockThreshold())
//                .build();
//        return ResponseEntity.ok(inventoryService.create(item));
//    }
//
//    @PutMapping("/{itemId}/quantity")
//    public ResponseEntity<InventoryItem> updateQuantity(@PathVariable String coopId, @PathVariable String itemId,
//                                                        @RequestBody UpdateQuantityRequest rq) {
//        InventoryItem updated = inventoryService.updateQuantity(itemId, rq.getQuantity());
//        return ResponseEntity.ok(updated);
//    }
//
//    @GetMapping("/low-stock")
//    public ResponseEntity<List<InventoryItem>> lowStock(@PathVariable String coopId) {
//        return ResponseEntity.ok(inventoryService.getLowStock(coopId));
//    }
//}
