package org.order.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class InventoryBatchDto {
    private String batchNumber;
    private Integer quantity;
    private LocalDate expiryDate;
}
