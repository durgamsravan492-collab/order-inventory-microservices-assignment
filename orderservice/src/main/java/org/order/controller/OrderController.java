package org.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.order.dto.OrderRequest;
import org.order.dto.OrderResponse;
import org.order.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
@Tag(name = "Order", description = "Order management APIs")
public class OrderController {

    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }

    @PostMapping
    @Operation(summary = "Place a new order")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderRequest request) {
        OrderResponse response = service.placeOrder(request);
        return ResponseEntity.ok(response);
    }
}
