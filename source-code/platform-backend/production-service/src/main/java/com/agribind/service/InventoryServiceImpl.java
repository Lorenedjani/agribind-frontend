package com.agribind.service;

import com.agribind.entity.InventoryItem;
import com.agribind.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository repo;
    private final NotificationClient notificationClient; // simple Feign/WebClient to notification-service

    @Override
    public InventoryItem create(InventoryItem item) {
        item.checkAndMarkLow();
        InventoryItem saved = repo.save(item);
        if(saved.isLowStock()) {
            notificationClient.sendLowStockAlert(saved.getCooperativeId(), saved.getName());
        }
        return saved;
    }

    @Override
    public InventoryItem updateQuantity(String itemId, double newQuantity) {
        InventoryItem item = repo.findById(itemId).orElseThrow();
        item.setQuantity(newQuantity);
        boolean low = item.checkAndMarkLow();
        InventoryItem saved = repo.save(item);
        if(low) notificationClient.sendLowStockAlert(item.getCooperativeId(), item.getName());
        return saved;
    }

    @Override
    public List<InventoryItem> getByCooperative(String coopId) {
        return repo.findByCooperativeId(coopId);
    }

    @Override
    public List<InventoryItem> getLowStock(String coopId) {
        return repo.findByCooperativeIdAndIsLowStockTrue(coopId);
    }
}

