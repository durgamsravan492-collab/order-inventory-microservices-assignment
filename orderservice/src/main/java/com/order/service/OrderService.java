package com.order.service;


import com.order.dto.OrderRequest;
import com.order.dto.OrderResponse;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest request);
}