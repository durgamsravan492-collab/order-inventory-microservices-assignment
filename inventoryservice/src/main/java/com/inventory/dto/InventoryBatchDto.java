package com.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryBatchDto {
    private Long id;
    private String batchNumber;
    private Integer quantity;
    private LocalDate expiryDate;
    private Long productId;
}
