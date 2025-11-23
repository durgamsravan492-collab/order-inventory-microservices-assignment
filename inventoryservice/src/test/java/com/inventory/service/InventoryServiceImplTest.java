package com.inventory.service;

import com.inventory.dto.InventoryBatchDto;
import com.inventory.dto.UpdateInventoryRequest;
import com.inventory.entity.InventoryBatch;
import com.inventory.entity.Product;
import com.inventory.repository.ProductRepository;
import com.inventory.validation.BaseInventoryValidator;
import com.inventory.validation.InventoryBatchMapper;
import com.inventory.validation.InventoryValidationFactory;
import com.inventory.repository.InventoryBatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    private static final String BATCH_NUMBER_1 = "inventoryBatch";
    private static final String BATCH_NUMBER_2 = "B2";
    private static final String DEFAULT = "default";
    private static final long PRODUCT_ID = 42L;
    private static final long BATCH_ID_1 = 1L;
    private static final String SKU_1 = "SKU-1";
    private static final String MAP_BATCH_NUMBER_1 = "inventoryBatch";
    private static final int MAP_BATCH_QUANTITY_2 = 2;
    private static final String ERROR_MSG = "error";
    private static final String INVALID_MSG = "invalid";
    private static final String INVALID_SKU = "INVALID-SKU";

    @Mock
    InventoryBatchRepository inventoryBatchRepository;

    @Mock
    InventoryValidationFactory inventoryValidationFactory;

    @Mock
    InventoryBatchMapper inventoryBatchMapper;

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    InventoryServiceImpl inventoryService;

    @Test
    void getBatchesByProductId_success() {
        Product product = new Product();
        product.setId(PRODUCT_ID);

        InventoryBatch inventoryBatch = new InventoryBatch();
        inventoryBatch.setId(BATCH_ID_1);
        inventoryBatch.setBatchNumber(BATCH_NUMBER_1);
        inventoryBatch.setQuantity(10);
        inventoryBatch.setExpiryDate(LocalDate.of(2025,1,1));
        inventoryBatch.setProduct(product);

        when(inventoryBatchRepository.findByProduct_IdOrderByExpiryDateAsc(PRODUCT_ID)).thenReturn(List.of(inventoryBatch));
        InventoryBatchDto dto = new InventoryBatchDto();
        when(inventoryBatchMapper.toDto(inventoryBatch)).thenReturn(dto);

        List<InventoryBatchDto> dtos = inventoryService.getBatchesByProductId(PRODUCT_ID);
        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0)).isSameAs(dto);
        verify(inventoryBatchRepository).findByProduct_IdOrderByExpiryDateAsc(PRODUCT_ID);
        verify(inventoryBatchMapper).toDto(inventoryBatch);
    }

    @Test
    void getBatchesByProductId_exception() {
        when(inventoryBatchRepository.findByProduct_IdOrderByExpiryDateAsc(PRODUCT_ID)).thenThrow(new RuntimeException(ERROR_MSG));
        assertThatThrownBy(() -> inventoryService.getBatchesByProductId(PRODUCT_ID))
            .isInstanceOf(RuntimeException.class)
            .hasMessage(ERROR_MSG);
        verify(inventoryBatchRepository).findByProduct_IdOrderByExpiryDateAsc(PRODUCT_ID);
    }

    @Test
    void updateInventory_success() {
        BaseInventoryValidator baseInventoryValidator = mock(BaseInventoryValidator.class);
        when(inventoryValidationFactory.getValidator(DEFAULT)).thenReturn(baseInventoryValidator);
        UpdateInventoryRequest updateInventoryRequest = UpdateInventoryRequest.builder()
                .sku(SKU_1)
                .batchQuantityToDeduct(Map.of(MAP_BATCH_NUMBER_1, MAP_BATCH_QUANTITY_2))
                .build();
        inventoryService.updateInventory(updateInventoryRequest, DEFAULT);
        verify(inventoryValidationFactory).getValidator(DEFAULT);
        verify(baseInventoryValidator).updateInventory(updateInventoryRequest);
    }

    @Test
    void updateInventory_invalidRequest() {
        when(inventoryValidationFactory.getValidator(DEFAULT)).thenThrow(new com.inventory.handlers.InvalidInventoryOperationException(INVALID_MSG));
        UpdateInventoryRequest updateInventoryRequest = UpdateInventoryRequest.builder()
                .sku(INVALID_SKU)
                .batchQuantityToDeduct(Map.of())
                .build();
        assertThatThrownBy(() -> inventoryService.updateInventory(updateInventoryRequest, DEFAULT))
            .isInstanceOf(com.inventory.handlers.InvalidInventoryOperationException.class);
        verify(inventoryValidationFactory).getValidator(DEFAULT);
    }

    @Test
    void updateInventory_exception() {
        when(inventoryValidationFactory.getValidator(DEFAULT)).thenThrow(new RuntimeException(ERROR_MSG));
        UpdateInventoryRequest updateInventoryRequest = UpdateInventoryRequest.builder()
                .sku(SKU_1)
                .batchQuantityToDeduct(Map.of(MAP_BATCH_NUMBER_1, MAP_BATCH_QUANTITY_2))
                .build();
        assertThatThrownBy(() -> inventoryService.updateInventory(updateInventoryRequest, DEFAULT))
            .isInstanceOf(RuntimeException.class)
            .hasMessage(ERROR_MSG);
        verify(inventoryValidationFactory).getValidator(DEFAULT);
    }
}
