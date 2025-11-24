package com.order.controller;

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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class OrderControllerIntegrationTest {

    private static final String TEST_SKU = "SKU1";
    private static final int TEST_QUANTITY = 2;
    private static final String TEST_BATCH = "BATCH-1";
    private static final String ORDER_URI = "/order";

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
        batch.setQuantity(5);
        batch.setExpiryDate(LocalDate.now().plusDays(10));
        when(inventoryClient.getBatchesBySku(TEST_SKU)).thenReturn(List.of(batch));

        Order saved = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .sku(TEST_SKU)
                .quantity(TEST_QUANTITY)
                .createdAt(LocalDateTime.now())
                .status("PLACED")
                .metadata("auto-picked")
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(saved);

        // Act & Assert
        webTestClient.post()
                .uri(ORDER_URI)
                .bodyValue(java.util.Map.of("sku", TEST_SKU, "quantity", TEST_QUANTITY))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.order.sku").isEqualTo(TEST_SKU)
                .jsonPath("$.order.quantity").isEqualTo(TEST_QUANTITY);
    }
}
