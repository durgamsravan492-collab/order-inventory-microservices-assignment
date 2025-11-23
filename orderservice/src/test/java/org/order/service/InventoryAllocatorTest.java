package org.order.service;

import org.junit.jupiter.api.Test;
import org.order.dto.InventoryBatchDto;
import org.order.handlers.InsufficientInventoryException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryAllocatorTest {

    private static final String BATCH_1 = "B1";
    private static final String BATCH_2 = "B2";
    private static final int BATCH_1_QTY = 5;
    private static final int BATCH_2_QTY = 5;
    private static final int ALLOCATE_QTY_SUCCESS = 7;
    private static final int ALLOCATE_QTY_INSUFFICIENT = 5;
    private static final int BATCH_1_QTY_INSUFFICIENT = 2;

    private final InventoryAllocator allocator = new InventoryAllocator();

    @Test
    void allocate_success() {
        InventoryBatchDto b1 = new InventoryBatchDto();
        b1.setBatchNumber(BATCH_1);
        b1.setQuantity(BATCH_1_QTY);
        b1.setExpiryDate(LocalDate.now().plusDays(5));

        InventoryBatchDto b2 = new InventoryBatchDto();
        b2.setBatchNumber(BATCH_2);
        b2.setQuantity(BATCH_2_QTY);
        b2.setExpiryDate(LocalDate.now().plusDays(10));

        Map<String, Integer> map = allocator.allocate(List.of(b1, b2), ALLOCATE_QTY_SUCCESS);
        assertThat(map).containsEntry(BATCH_1, BATCH_1_QTY);
        assertThat(map).containsEntry(BATCH_2, 2);
    }

    @Test
    void allocate_insufficient_throws() {
        InventoryBatchDto b1 = new InventoryBatchDto();
        b1.setBatchNumber(BATCH_1);
        b1.setQuantity(BATCH_1_QTY_INSUFFICIENT);
        b1.setExpiryDate(LocalDate.now().plusDays(5));

        assertThrows(InsufficientInventoryException.class, () -> allocator.allocate(List.of(b1), ALLOCATE_QTY_INSUFFICIENT));
    }
}
