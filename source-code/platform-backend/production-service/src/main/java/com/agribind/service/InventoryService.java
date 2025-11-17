package com.agribind.service;


import com.agribind.entity.InventoryItem;
import java.util.List;

public interface InventoryService {
    InventoryItem create(InventoryItem item);
    InventoryItem updateQuantity(String itemId, double newQuantity);
    List<InventoryItem> getByCooperative(String coopId);
    List<InventoryItem> getLowStock(String coopId);
}

