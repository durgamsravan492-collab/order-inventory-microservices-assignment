package com.order.validation;

import com.order.dto.InventoryUpdateRequest;

public interface BaseInventoryValidator {
    String getType();

    void updateInventory(InventoryUpdateRequest inventoryUpdateRequest);
}
