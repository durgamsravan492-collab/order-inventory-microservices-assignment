package org.order.downstream;


import org.springframework.beans.factory.annotation.Value;

import org.order.dto.InventoryUpdateRequest;
import org.order.dto.InventoryBatchDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

@Component
public class InventoryClient {

    private final WebClient client;

    private static final String FETCH_BATCHES_ERROR_MSG = "Failed to fetch inventory batches from inventory service.";
    private static final String UPDATE_INVENTORY_ERROR_MSG = "Failed to update inventory in inventory service.";

    public InventoryClient(WebClient.Builder builder,
                           @Value("${inventory.service.url:http://localhost:8081}") String baseUrl) {
        this.client = builder.baseUrl(baseUrl).build();
    }

    // call update inventory
    public void updateInventory(InventoryUpdateRequest request) {
        try {
            client.post()
                    .uri("/inventory/update")
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("request", request);
            logMap.put("errorStatus", e.getStatusCode().value());
            logMap.put("errorBody", e.getResponseBodyAsString());
            throw new RuntimeException(UPDATE_INVENTORY_ERROR_MSG + " " + logMap, e);
        }
    }

    // Fetch inventory batches for a given SKU
    public List<InventoryBatchDto> getBatchesBySku(String sku) {
        try {
            InventoryBatchDto[] batches = client.get()
                    .uri(uriBuilder -> uriBuilder.path("/inventory/batches").queryParam("sku", sku).build())
                    .retrieve()
                    .bodyToMono(InventoryBatchDto[].class)
                    .block();
            return batches != null ? Arrays.asList(batches) : List.of();
        } catch (WebClientResponseException e) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("sku", sku);
            logMap.put("errorStatus", e.getStatusCode().value());
            logMap.put("errorBody", e.getResponseBodyAsString());
            throw new RuntimeException(FETCH_BATCHES_ERROR_MSG + " " + logMap, e);
        }
    }
}
