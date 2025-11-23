package org.order.service;


import org.order.dto.OrderRequest;
import org.order.dto.OrderResponse;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest request);
}