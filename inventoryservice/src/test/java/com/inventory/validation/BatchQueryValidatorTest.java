package com.inventory.validation;

import com.inventory.dto.InventoryBatchDto;
import com.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

class BatchQueryValidatorTest {

    private InventoryService inventoryService;
    private BatchQueryValidator batchQueryValidator;

    private static final String SKU_B1 = "B1";
    private static final String SKU_B2 = "B2";
    private static final String SKU_B3 = "B3";
    private static final String SKU_NON = "NON";
    private static final String SKU_EXIST = "EXIST";
    private static final String BLANK = "   ";

    @BeforeEach
    void setUp() {
        inventoryService = Mockito.mock(InventoryService.class);
        batchQueryValidator = new BatchQueryValidator(inventoryService);
    }

    @Test
    void whenSkuIsNull_returnsAllBatches() {
        InventoryBatchDto dto = InventoryBatchDto.builder()
                .id(1L)
                .batchNumber(SKU_B1)
                .quantity(5)
                .expiryDate(LocalDate.now())
                .productId(1L)
                .build();
        given(inventoryService.getAllBatches()).willReturn(List.of(dto));

        List<InventoryBatchDto> result = batchQueryValidator.getBatchesForSkuOrAll(null);

        assertEquals(1, result.size());
        assertEquals(dto.getBatchNumber(), result.get(0).getBatchNumber());
    }

    @Test
    void whenSkuIsBlank_returnsAllBatches() {
        InventoryBatchDto dto = InventoryBatchDto.builder()
                .id(2L)
                .batchNumber(SKU_B2)
                .quantity(10)
                .expiryDate(LocalDate.now())
                .productId(2L)
                .build();
        given(inventoryService.getAllBatches()).willReturn(List.of(dto));

        List<InventoryBatchDto> result = batchQueryValidator.getBatchesForSkuOrAll(BLANK);

        assertEquals(1, result.size());
        assertEquals(dto.getBatchNumber(), result.get(0).getBatchNumber());
    }

    @Test
    void whenSkuDoesNotExist_returnsEmptyList() {
        given(inventoryService.getProductIdBySku(SKU_NON)).willReturn(null);

        List<InventoryBatchDto> result = batchQueryValidator.getBatchesForSkuOrAll(SKU_NON);

        assertEquals(0, result.size());
    }

    @Test
    void whenSkuExists_returnsBatchesForProduct() {
        InventoryBatchDto dto = InventoryBatchDto.builder()
                .id(3L)
                .batchNumber(SKU_B3)
                .quantity(20)
                .expiryDate(LocalDate.now())
                .productId(3L)
                .build();

        given(inventoryService.getProductIdBySku(SKU_EXIST))
                .willReturn(3L);
        given(inventoryService.getBatchesByProductId(3L))
                .willReturn(List.of(dto));

        List<InventoryBatchDto> result = batchQueryValidator.getBatchesForSkuOrAll(SKU_EXIST);

        assertEquals(1, result.size());
        assertEquals(dto.getProductId(), result.get(0).getProductId());
    }
}
