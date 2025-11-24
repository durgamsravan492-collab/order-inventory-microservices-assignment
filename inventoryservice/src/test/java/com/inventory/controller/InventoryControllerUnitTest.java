package com.inventory.controller;

import com.inventory.dto.InventoryBatchDto;
import com.inventory.dto.UpdateInventoryRequest;
import com.inventory.mapper.InventoryBatchMapper;
import com.inventory.service.InventoryService;
import com.inventory.validation.BatchQueryValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class InventoryControllerUnitTest {

    private InventoryService inventoryService;
    private BatchQueryValidator batchQueryValidator;
    private InventoryBatchMapper inventoryBatchMapper;
    private InventoryController inventoryController;

    private static final String BATCH_NUMBER = "B1";
    private static final String SKU = "SKU";
    private static final String DEFAULT = "default";
    private static final Long PRODUCT_ID = 1L;
    private static final int QUANTITY = 5;
    private static final String DB_ERROR = "DB error";
    private static final String SERVICE_ERROR = "Service error";
    private static final String INVALID_TYPE = "Invalid type";
    private static final String INVALID = "invalid";

    @BeforeEach
    void setUp() {
        inventoryService = mock(InventoryService.class);
        batchQueryValidator = mock(BatchQueryValidator.class);
        inventoryBatchMapper = mock(InventoryBatchMapper.class);
        inventoryController = new InventoryController(inventoryService, batchQueryValidator, inventoryBatchMapper);
    }

    @Test
    void getBatches_returnsOkAndCorrectResponse() {
        InventoryBatchDto inventoryBatchDto = new InventoryBatchDto();
        inventoryBatchDto.setBatchNumber(BATCH_NUMBER);
        inventoryBatchDto.setQuantity(QUANTITY);
        inventoryBatchDto.setExpiryDate(LocalDate.now());
        when(inventoryService.getBatchesByProductId(PRODUCT_ID)).thenReturn(List.of(inventoryBatchDto));

        var response = inventoryController.getBatches(PRODUCT_ID);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().size()).isEqualTo(1);
        assertThat(response.getBody().get(0).getBatchNumber()).isEqualTo(BATCH_NUMBER);
        verify(inventoryService).getBatchesByProductId(PRODUCT_ID);
    }

    @Test
    void getBatches_returnsEmptyList() {
        when(inventoryService.getBatchesByProductId(PRODUCT_ID)).thenReturn(List.of());
        var response = inventoryController.getBatches(PRODUCT_ID);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isEmpty()).isTrue();
        verify(inventoryService).getBatchesByProductId(PRODUCT_ID);
    }

    @Test
    void getBatches_serviceThrowsException() {
        when(inventoryService.getBatchesByProductId(PRODUCT_ID)).thenThrow(new RuntimeException(DB_ERROR));
        assertThatThrownBy(() -> inventoryController.getBatches(PRODUCT_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(DB_ERROR);
        verify(inventoryService).getBatchesByProductId(PRODUCT_ID);
    }

    @Test
    void updateInventory_returnsOkAndVerifiesCall() {
        UpdateInventoryRequest request = UpdateInventoryRequest.builder()
                .sku(SKU)
                .batchQuantityToDeduct(java.util.Map.of(BATCH_NUMBER, 1))
                .build();
        doNothing().when(inventoryService).updateInventory(request, DEFAULT);

        var response = inventoryController.updateInventory(request, DEFAULT);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(inventoryService).updateInventory(request, DEFAULT);
    }

    @Test
    void updateInventory_invalidTypeThrowsException() {
        UpdateInventoryRequest updateInventoryRequest = UpdateInventoryRequest.builder()
                .sku(SKU)
                .batchQuantityToDeduct(java.util.Map.of(BATCH_NUMBER, 1))
                .build();
        doThrow(new IllegalArgumentException(INVALID_TYPE)).when(inventoryService)
                .updateInventory(updateInventoryRequest, INVALID);
        assertThatThrownBy(() -> inventoryController.updateInventory(updateInventoryRequest, INVALID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(INVALID_TYPE);
        verify(inventoryService).updateInventory(updateInventoryRequest, INVALID);
    }

    @Test
    void updateInventory_serviceThrowsException() {
        UpdateInventoryRequest updateInventoryRequest = UpdateInventoryRequest.builder()
                .sku(SKU)
                .batchQuantityToDeduct(java.util.Map.of(BATCH_NUMBER, 1))
                .build();
        doThrow(new RuntimeException(SERVICE_ERROR)).when(inventoryService)
                .updateInventory(updateInventoryRequest, DEFAULT);
        assertThatThrownBy(() -> inventoryController.updateInventory(updateInventoryRequest, DEFAULT))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(SERVICE_ERROR);
        verify(inventoryService).updateInventory(updateInventoryRequest, DEFAULT);
    }
}