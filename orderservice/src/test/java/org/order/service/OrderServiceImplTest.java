package org.order.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.order.downstream.InventoryClient;
import org.order.dto.InventoryBatchDto;
import org.order.dto.InventoryUpdateRequest;
import org.order.dto.OrderRequest;
import org.order.entity.Order;
import org.order.repository.OrderRepository;
import org.order.validation.BaseInventoryValidator;
import org.order.validation.InventoryValidationFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceImplTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    InventoryClient inventoryClient;

    @Mock
    InventoryValidationFactory inventoryValidationFactory;

    @Mock
    BaseInventoryValidator baseInventoryValidator;
    private OrderServiceImpl orderService;

    private static final String SKU_SUCCESS = "SKU-2";
    private static final String SKU_FAILURE = "SKU-1";
    private static final String SKU_EXCEPTION = "SKU-EXCEPTION";
    private static final String EXCEPTION_MESSAGE = "Inventory service error";

    @BeforeEach
    void init() {
        when(inventoryValidationFactory.getValidator(anyString())).thenReturn(baseInventoryValidator);
        doNothing().when(baseInventoryValidator).updateInventory(any(InventoryUpdateRequest.class));
        orderService = new OrderServiceImpl(
                orderRepository,
                inventoryClient,
                new org.order.validation.OrderRequestValidator(),
                inventoryValidationFactory,
                new InventoryAllocator()
        );
    }

    @Test
    void placeOrder_successfulFlow_callsInventoryUpdateAndSavesOrder() {
        // Arrange
        InventoryBatchDto inventoryBatchDto = new InventoryBatchDto();
        inventoryBatchDto.setBatchNumber("B1");
        inventoryBatchDto.setQuantity(5);
        inventoryBatchDto.setExpiryDate(java.time.LocalDate.now());
        when(inventoryClient.getBatchesBySku(SKU_SUCCESS)).thenReturn(List.of(inventoryBatchDto));

        OrderRequest orderRequest = new org.order.dto.OrderRequest();
        orderRequest.setSku(SKU_SUCCESS);
        orderRequest.setQuantity(3);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        orderService.placeOrder(orderRequest);

        // Assert
        verify(baseInventoryValidator).updateInventory(any(InventoryUpdateRequest.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void placeOrder_failsWhenInsufficientInventory() {
        // Arrange
        when(inventoryClient.getBatchesBySku(SKU_FAILURE)).thenReturn(List.of());

        OrderRequest orderRequest = new org.order.dto.OrderRequest();
        orderRequest.setSku(SKU_FAILURE);
        orderRequest.setQuantity(5);

        // Act & Assert
        assertThatThrownBy(() -> orderService.placeOrder(orderRequest))
                .isInstanceOf(org.order.handlers.InsufficientInventoryException.class);
        verify(inventoryClient).getBatchesBySku(SKU_FAILURE);
        verifyNoInteractions(orderRepository);
    }

    @Test
    void placeOrder_exceptionFromInventoryClient() {
        // Arrange
        OrderRequest orderRequest = new org.order.dto.OrderRequest();
        orderRequest.setSku(SKU_EXCEPTION);
        orderRequest.setQuantity(1);
        when(inventoryClient.getBatchesBySku(SKU_EXCEPTION)).thenThrow(new RuntimeException(EXCEPTION_MESSAGE));

        // Act & Assert
        assertThatThrownBy(() -> orderService.placeOrder(orderRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage(EXCEPTION_MESSAGE);
        verify(inventoryClient).getBatchesBySku(SKU_EXCEPTION);
        verifyNoInteractions(orderRepository);
    }
}
