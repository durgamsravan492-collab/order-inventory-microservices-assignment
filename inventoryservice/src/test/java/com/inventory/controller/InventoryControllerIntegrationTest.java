package com.inventory.controller;

import com.inventory.entity.InventoryBatch;
import com.inventory.entity.Product;
import com.inventory.repository.InventoryBatchRepository;
import com.inventory.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryControllerIntegrationTest {

    private final int port;

    private final ProductRepository productRepository;
    private final InventoryBatchRepository inventoryBatchRepository;

    @Autowired
    InventoryControllerIntegrationTest(@LocalServerPort int port, ProductRepository productRepository, InventoryBatchRepository inventoryBatchRepository) {
        this.port = port;
        this.productRepository = productRepository;
        this.inventoryBatchRepository = inventoryBatchRepository;
    }

    private Product product;

    private static final String BASE_URL = "http://localhost:";
    private static final String SKU = "SKU-INT-1";
    private static final String PRODUCT_NAME = "Integration Product";
    private static final String BATCH_NUMBER = "BATCH-INT-1";
    private static final String UPDATE_URL = "/inventory/update?handlerType=default";
    private static final String BATCH_MISSING = "MISSING-BATCH";
    private static final String GET_BATCHES_URL = "/inventory/batches";
    private static final String QUERY_PARAM_SKU = "sku";
    private static final String RESPONSE_KEY_MESSAGE = "message";

    @BeforeEach
    void setup() {
        inventoryBatchRepository.deleteAll();
        productRepository.deleteAll();

        product = new Product();
        product.setSku(SKU);
        product.setName(PRODUCT_NAME);
        product = productRepository.save(product);
    }

    @Test
    void createAndUpdateInventory_success() {
        InventoryBatch b1 = new InventoryBatch();
        b1.setBatchNumber(BATCH_NUMBER);
        b1.setQuantity(5);
        b1.setExpiryDate(LocalDate.now().plusDays(10));
        b1.setProduct(product);
        inventoryBatchRepository.save(b1);

        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl(BASE_URL + port)
                .build();

        // verify GET batches by sku
        Object[] resp = client.get()
                .uri(uriBuilder -> uriBuilder.path(GET_BATCHES_URL).queryParam(QUERY_PARAM_SKU, SKU).build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Object[].class)
                .returnResult()
                .getResponseBody();

        assertThat(resp).isNotNull();

        // call update endpoint with valid deduction
        String body = String.format("{\"sku\":\"%s\", \"batchQuantityToDeduct\": { \"%s\": 3 } }", SKU, BATCH_NUMBER);

        client.post()
                .uri(UPDATE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isOk();

        InventoryBatch updated = inventoryBatchRepository.findByProductIdAndBatchNumber(product.getId(), BATCH_NUMBER).orElseThrow();
        assertThat(updated.getQuantity()).isEqualTo(2);
    }

    @Test
    void updateInventory_batchMissing_returns400() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl(BASE_URL + port)
                .build();
        String body = String.format("{\"sku\":\"%s\", \"batchQuantityToDeduct\": { \"%s\": 1 } }", SKU, BATCH_MISSING);

        Map<String, Object> respBody = client.post()
                .uri(UPDATE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(new ParameterizedTypeReference<Map<String, Object>>() {})
                .returnResult()
                .getResponseBody();

        assertThat(respBody).containsKey(RESPONSE_KEY_MESSAGE);
    }
}
