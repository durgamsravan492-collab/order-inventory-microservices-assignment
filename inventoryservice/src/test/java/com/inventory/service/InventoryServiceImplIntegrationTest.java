package com.inventory.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryServiceImplIntegrationTest {

    private final int port;

    @Autowired
    InventoryServiceImplIntegrationTest(@LocalServerPort int port) {
        this.port = port;
    }

    @Test
    void getBatches_returnsOk() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        List<?> resp = client.get()
                .uri("/inventory/1")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class)
                .returnResult()
                .getResponseBody();

        assertThat(resp).isNotNull();
    }
}
