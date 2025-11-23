package com.inventory.service;

import com.inventory.dto.UpdateInventoryRequest;
import com.inventory.dto.InventoryBatchDto;
import com.inventory.entity.InventoryBatch;
import com.inventory.entity.Product;

import java.util.List;

public interface InventoryService {
    List<InventoryBatchDto> getBatchesByProductId(Long productId);
    void updateInventory(UpdateInventoryRequest request, String handlerType);
    Long getProductIdBySku(String sku);
    List<InventoryBatchDto> getAllBatches();
    Product createProduct(Product product);
    InventoryBatch createBatch(Long productId, InventoryBatch batch);
}
