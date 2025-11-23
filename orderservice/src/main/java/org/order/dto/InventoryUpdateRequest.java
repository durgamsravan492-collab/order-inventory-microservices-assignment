package org.order.dto;

import lombok.*;
import java.util.Map;

@Data
@Builder
public class InventoryUpdateRequest {
    private String sku;
    private Map<String, Integer> batchQuantityToDeduct; // batchNumber -> qty
}
