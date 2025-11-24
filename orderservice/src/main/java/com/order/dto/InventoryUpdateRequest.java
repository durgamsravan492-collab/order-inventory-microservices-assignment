package com.order.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class InventoryUpdateRequest {
    private String sku;
    // batchNumber -> qty
    private Map<String, Integer> batchQuantityToDeduct;
}
