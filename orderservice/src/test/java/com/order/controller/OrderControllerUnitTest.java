package com.order.controller;

import com.order.dto.OrderRequest;
import com.order.dto.OrderResponse;
import com.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrderControllerUnitTest {

    private static final String SKU_SUCCESS = "SKU-1";
    private static final String SKU_FAILURE = "SKU-FAIL";
    private static final String SKU_EXCEPTION = "SKU-EXCEPTION";
    private static final String FAILURE_MESSAGE = "Failure";
    private static final String EXCEPTION_MESSAGE = "Service error";

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this).close(); // Use try-with-resources to avoid warning
    }

    @Test
    void placeOrder_success() {
        OrderRequest request = new OrderRequest();
        request.setSku(SKU_SUCCESS);
        request.setQuantity(1);
        OrderResponse expected = new OrderResponse(true, null, null);
        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(expected);

        ResponseEntity<OrderResponse> response = orderController.placeOrder(request);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(orderService).placeOrder(request);
    }

    @Test
    void placeOrder_failure() {
        OrderRequest request = new OrderRequest();
        request.setSku(SKU_FAILURE);
        request.setQuantity(1);
        OrderResponse expected = new OrderResponse(false, null, FAILURE_MESSAGE);
        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(expected);

        ResponseEntity<OrderResponse> response = orderController.placeOrder(request);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(expected);
        verify(orderService).placeOrder(request);
    }

    @Test
    void placeOrder_exception() {
        OrderRequest request = new OrderRequest();
        request.setSku(SKU_EXCEPTION);
        request.setQuantity(1);
        when(orderService.placeOrder(any(OrderRequest.class))).thenThrow(new RuntimeException(EXCEPTION_MESSAGE));

        assertThatThrownBy(() -> orderController.placeOrder(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage(EXCEPTION_MESSAGE);
        verify(orderService).placeOrder(request);
    }
}
