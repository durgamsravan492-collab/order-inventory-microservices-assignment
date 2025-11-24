package com.order.service;

import com.order.downstream.InventoryClient;
import com.order.dto.InventoryBatchDto;
import com.order.entity.Order;
import com.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class OrderServiceImplIntegrationTest {

    private static final String TEST_SKU = "SKU1";
    private static final int TEST_QUANTITY = 2;
    private static final String TEST_BATCH = "BATCH-1";

    private static final int BATCH_QUANTITY = 5;
    private static final int BATCH_EXPIRY_DAYS = 10;
    private static final String ORDER_STATUS_PLACED = "PLACED";
    private static final String ORDER_METADATA = "auto-picked";
    private static final String ORDER_URI = "/order";
    private static final String REQUEST_FIELD_SKU = "sku";
    private static final String REQUEST_FIELD_QUANTITY = "quantity";
    private static final String EXPECTED_INSUFFICIENT_ERROR = "Insufficient inventory: no batches available";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private InventoryClient inventoryClient;

    @MockBean
    private OrderRepository orderRepository;

    @Test
    void placeOrder_success() {
        // Arrange
        InventoryBatchDto batch = new InventoryBatchDto();
        batch.setBatchNumber(TEST_BATCH);
        batch.setQuantity(BATCH_QUANTITY);
        batch.setExpiryDate(LocalDate.now().plusDays(BATCH_EXPIRY_DAYS));
        when(inventoryClient.getBatchesBySku(TEST_SKU)).thenReturn(List.of(batch));

        // Arrange
        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .sku(TEST_SKU)
                .quantity(TEST_QUANTITY)
                .createdAt(LocalDateTime.now())
                .status(ORDER_STATUS_PLACED)
                .metadata(ORDER_METADATA)
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act & Assert
        webTestClient.post()
                .uri(ORDER_URI)
                .bodyValue(Map.of(REQUEST_FIELD_SKU, TEST_SKU, REQUEST_FIELD_QUANTITY, TEST_QUANTITY))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.order.sku").isEqualTo(TEST_SKU)
                .jsonPath("$.order.quantity").isEqualTo(TEST_QUANTITY);
    }

    @Test
    void placeOrder_failureWhenInsufficientInventory() {
        when(inventoryClient.getBatchesBySku(TEST_SKU)).thenReturn(List.of());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        webTestClient.post()
                .uri(ORDER_URI)
                .bodyValue(Map.of(REQUEST_FIELD_SKU, TEST_SKU, REQUEST_FIELD_QUANTITY, TEST_QUANTITY))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo(EXPECTED_INSUFFICIENT_ERROR);
    }
}