package com.order.validation;

import com.order.dto.OrderRequest;
import org.springframework.stereotype.Component;

@Component
public class OrderRequestValidator {

    private static final String ORDER_REQUEST_REQUIRED_MSG = "Order request must be provided";
    private static final String SKU_REQUIRED_MSG = "SKU must be provided";
    private static final String QUANTITY_INVALID_MSG = "Quantity must be greater than 0";

    public void validate(OrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(ORDER_REQUEST_REQUIRED_MSG);
        }
        if (request.getSku() == null || request.getSku().isBlank()) {
            throw new IllegalArgumentException(SKU_REQUIRED_MSG);
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new IllegalArgumentException(QUANTITY_INVALID_MSG);
        }
    }
}
