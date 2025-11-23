package org.order.service;



import jakarta.transaction.Transactional;
import org.order.downstream.InventoryClient;
import org.order.dto.InventoryBatchDto;
import org.order.dto.InventoryUpdateRequest;
import org.order.dto.OrderRequest;
import org.order.dto.OrderResponse;
import org.order.entity.Order;
import org.order.repository.OrderRepository;
import org.order.validation.DefaultInventoryValidator;
import org.order.validation.InventoryValidationFactory;
import org.order.validation.OrderRequestValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final OrderRequestValidator orderRequestValidator;
    private final InventoryValidationFactory inventoryValidationFactory;
    private final InventoryAllocator inventoryAllocator;

    private static final String ORDER_PLACED_STATUS = "PLACED";
    private static final String ORDER_METADATA_AUTO_PICKED = "auto-picked";

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository,
                            InventoryClient inventoryClient,
                            OrderRequestValidator orderRequestValidator,
                            InventoryValidationFactory inventoryValidationFactory,
                            InventoryAllocator inventoryAllocator) {
        this.orderRepository = orderRepository;
        this.inventoryClient = inventoryClient;
        this.orderRequestValidator = orderRequestValidator;
        this.inventoryValidationFactory = inventoryValidationFactory;
        this.inventoryAllocator = inventoryAllocator;
    }

    // Convenience constructor kept for unit tests and simple usage where DI is not available
    public OrderServiceImpl(OrderRepository orderRepository, InventoryClient inventoryClient) {
        this(orderRepository,
             inventoryClient,
             new OrderRequestValidator(),
             new InventoryValidationFactory(List.of(new DefaultInventoryValidator(inventoryClient))),
             new InventoryAllocator());
    }

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        // Delegate validation
        orderRequestValidator.validate(request);

        // Fetch available batches for the SKU
        List<InventoryBatchDto> batches = inventoryClient.getBatchesBySku(request.getSku());
        if (batches == null || batches.isEmpty()) {
            throw new org.order.handlers.InsufficientInventoryException("Insufficient inventory: no batches available");
        }

        // Allocate batches delegated to InventoryAllocator
        Map<String, Integer> batchQuantityToDeduct = inventoryAllocator.allocate(batches, request.getQuantity());

        // Update inventory using validator obtained from factory (defaults to default validator)
        InventoryUpdateRequest inventoryUpdateRequest = InventoryUpdateRequest.builder()
                .sku(request.getSku())
                .batchQuantityToDeduct(batchQuantityToDeduct)
                .build();

        inventoryValidationFactory.getValidator("default").updateInventory(inventoryUpdateRequest);

        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .sku(request.getSku())
                .quantity(request.getQuantity())
                .createdAt(LocalDateTime.now())
                .status(ORDER_PLACED_STATUS)
                .metadata(ORDER_METADATA_AUTO_PICKED)
                .build();
        Order savedOrder = orderRepository.save(order);
        return new OrderResponse(true, savedOrder, null);
    }
}
