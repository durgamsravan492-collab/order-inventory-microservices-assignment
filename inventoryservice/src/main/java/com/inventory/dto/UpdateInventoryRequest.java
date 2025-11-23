package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class UpdateInventoryRequest {
    private String sku;
    // batchNumber -> qty
    private Map<String, Integer> batchQuantityToDeduct;

    // Default constructor for Jackson
    public UpdateInventoryRequest() {}
}
