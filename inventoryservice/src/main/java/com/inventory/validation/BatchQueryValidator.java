package com.inventory.validation;

import com.inventory.dto.InventoryBatchDto;
import com.inventory.service.InventoryService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Helper/validator to encapsulate SKU-based batch lookup logic.
 */
@Component
public class BatchQueryValidator {
    private final InventoryService inventoryService;

    public BatchQueryValidator(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Return batches depending on the sku parameter:
     * - if sku is null or blank => return all batches
     * - if sku is provided but no product found => return empty list
     * - if sku is provided and product exists => return batches for that product
     */
    public List<InventoryBatchDto> getBatchesForSkuOrAll(String sku) {
        if (sku != null && !sku.isBlank()) {
            Long productId = inventoryService.getProductIdBySku(sku);
            if (productId == null) {
                return List.of();
            }
            return inventoryService.getBatchesByProductId(productId);
        }
        return inventoryService.getAllBatches();
    }
}

