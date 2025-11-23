package com.inventory.validation;

import com.inventory.dto.UpdateInventoryRequest;

public interface BaseInventoryValidator {
    String getType();
    void updateInventory(UpdateInventoryRequest request);
}
