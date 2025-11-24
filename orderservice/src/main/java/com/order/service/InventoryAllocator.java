package com.order.service;

import com.order.dto.InventoryBatchDto;
import com.order.handlers.InsufficientInventoryException;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class InventoryAllocator {

    /**
     * Select batches to fulfill the requested quantity.
     * Returns a LinkedHashMap of batchNumber -> quantityToDeduct preserving iteration order.
     * Throws InsufficientInventoryException when available quantity < requested quantity.
     */
    public Map<String, Integer> allocate(List<InventoryBatchDto> batches, int requestedQuantity) {
        if (batches == null || batches.isEmpty()) {
            throw new InsufficientInventoryException("Insufficient inventory: no batches available");
        }

        int remaining = requestedQuantity;
        Map<String, Integer> batchQuantityToDeduct = new LinkedHashMap<>();

        for (InventoryBatchDto batch : batches) {
            if (remaining <= 0) break;
            int deduct = Math.min(batch.getQuantity(), remaining);
            if (deduct > 0) {
                batchQuantityToDeduct.put(batch.getBatchNumber(), deduct);
                remaining -= deduct;
            }
        }

        if (remaining > 0) {
            throw new InsufficientInventoryException("Insufficient inventory to fulfill requested quantity");
        }

        return batchQuantityToDeduct;
    }
}

