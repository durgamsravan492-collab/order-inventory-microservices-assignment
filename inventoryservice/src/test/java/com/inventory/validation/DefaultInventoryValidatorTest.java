package com.inventory.validation;

import com.inventory.dto.UpdateInventoryRequest;
import com.inventory.entity.InventoryBatch;
import com.inventory.entity.Product;
import com.inventory.handlers.InvalidInventoryOperationException;
import com.inventory.repository.InventoryBatchRepository;
import com.inventory.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefaultInventoryValidatorTest {

    private static final String SKU_1 = "SKU-1";
    private static final String SKU_2 = "SKU-2";
    private static final String BATCH_1 = "B1";
    private static final String BATCH_2 = "B2";

    @Mock
    InventoryBatchRepository inventoryBatchRepository;

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    DefaultInventoryValidator defaultInventoryValidator;

    @Test
    void updateInventory_reducesBatchQuantityWhenValid() {
        Product product = new Product();
        product.setId(10L);
        product.setSku(SKU_1);

        InventoryBatch inventoryBatch = new InventoryBatch();
        inventoryBatch.setId(100L);
        inventoryBatch.setBatchNumber(BATCH_1);
        inventoryBatch.setQuantity(5);
        inventoryBatch.setExpiryDate(LocalDate.now().plusDays(10));
        inventoryBatch.setProduct(product);

        when(productRepository.findBySku(SKU_1)).thenReturn(Optional.of(product));
        when(inventoryBatchRepository.findByProductIdAndBatchNumber(10L, BATCH_1)).thenReturn(Optional.of(inventoryBatch));

        UpdateInventoryRequest req = UpdateInventoryRequest.builder()
                .sku(SKU_1)
                .batchQuantityToDeduct(Map.of(BATCH_1, 3))
                .build();

        defaultInventoryValidator.updateInventory(req);

        ArgumentCaptor<InventoryBatch> captor = ArgumentCaptor.forClass(InventoryBatch.class);
        verify(inventoryBatchRepository, times(1)).save(captor.capture());
        InventoryBatch saved = captor.getValue();
        assertThat(saved.getQuantity()).isEqualTo(2);
    }

    @Test
    void updateInventory_throwsWhenInsufficientQty() {
        Product product = new Product();
        product.setId(11L);
        product.setSku(SKU_2);

        InventoryBatch inventoryBatch = new InventoryBatch();
        inventoryBatch.setId(101L);
        inventoryBatch.setBatchNumber(BATCH_2);
        inventoryBatch.setQuantity(1);
        inventoryBatch.setExpiryDate(LocalDate.now().plusDays(5));
        inventoryBatch.setProduct(product);

        when(productRepository.findBySku(SKU_2)).thenReturn(Optional.of(product));
        when(inventoryBatchRepository.findByProductIdAndBatchNumber(11L, BATCH_2)).thenReturn(Optional.of(inventoryBatch));

        UpdateInventoryRequest updateInventoryRequest = UpdateInventoryRequest.builder()
                .sku(SKU_2)
                .batchQuantityToDeduct(Map.of(BATCH_2, 2))
                .build();

        assertThatThrownBy(() -> defaultInventoryValidator.updateInventory(updateInventoryRequest)).isInstanceOf(InvalidInventoryOperationException.class);
    }
}
