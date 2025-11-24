package com.inventory.mapper;

import com.inventory.dto.InventoryBatchDto;
import com.inventory.entity.InventoryBatch;
import org.springframework.stereotype.Component;

/**
 * Maps InventoryBatch entity to InventoryBatchDto.
 * <p>
 * This mapper is placed in the validation package to keep controller logic focused.
 * It provides a single method to convert an InventoryBatch entity to its corresponding DTO.
 * <p>
 * Usage of constants for error messages ensures maintainability and consistency.
 */
@Component
public class InventoryBatchMapper {
    private static final String NULL_BATCH_ERROR = "InventoryBatch is null";

    public InventoryBatchDto toDto(InventoryBatch inventoryBatch) {
        if (inventoryBatch == null) {
            throw new IllegalArgumentException(NULL_BATCH_ERROR);
        }
        InventoryBatchDto inventoryBatchDto = new InventoryBatchDto();
        inventoryBatchDto.setId(inventoryBatch.getId());
        inventoryBatchDto.setBatchNumber(inventoryBatch.getBatchNumber());
        inventoryBatchDto.setQuantity(inventoryBatch.getQuantity());
        inventoryBatchDto.setExpiryDate(inventoryBatch.getExpiryDate());
        inventoryBatchDto.setProductId(inventoryBatch.getProduct() != null ? inventoryBatch.getProduct().getId() : null);
        return inventoryBatchDto;
    }
}

