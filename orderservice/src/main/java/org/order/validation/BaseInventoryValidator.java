package org.order.validation;

import org.order.dto.InventoryUpdateRequest;

public interface BaseInventoryValidator {
    String getType();

    void updateInventory(InventoryUpdateRequest inventoryUpdateRequest);
}
