package com.inventory.validation;


import com.inventory.dto.UpdateInventoryRequest;
import com.inventory.entity.InventoryBatch;
import com.inventory.handlers.InvalidInventoryOperationException;
import com.inventory.handlers.ProductNotFoundException;
import com.inventory.repository.InventoryBatchRepository;
import com.inventory.repository.ProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class DefaultInventoryValidator implements BaseInventoryValidator {

    private final InventoryBatchRepository batchRepository;
    private final ProductRepository productRepository;

    private static final String ERROR_REQUEST_BODY_MISSING = "Request body is missing";
    private static final String ERROR_SKU_REQUIRED = "SKU must be provided";
    private static final String ERROR_BATCH_QUANTITY_REQUIRED = "batchQuantityToDeduct must be provided and non-empty";
    private static final String ERROR_QUANTITY_REQUIRED = "Quantity for batch '%s' must be provided";
    private static final String ERROR_QUANTITY_POSITIVE = "Quantity to deduct for batch '%s' must be greater than 0";
    private static final String ERROR_PRODUCT_NOT_FOUND = "Product with SKU '%s' not found";
    private static final String ERROR_BATCH_NOT_FOUND = "Batch '%s' not found for product SKU '%s'";
    private static final String ERROR_INSUFFICIENT_QTY = "Insufficient qty in batch: %s";

    public DefaultInventoryValidator(InventoryBatchRepository batchRepository, ProductRepository productRepository) {
        this.batchRepository = batchRepository;
        this.productRepository = productRepository;
    }

    @Override
    public String getType() {
        return "default";
    }

    @Override
    @Transactional
    public void updateInventory(UpdateInventoryRequest request) {
        // Validate request
        if (request == null) {
            throw new InvalidInventoryOperationException(ERROR_REQUEST_BODY_MISSING);
        }
        if (request.getSku() == null || request.getSku().isBlank()) {
            throw new InvalidInventoryOperationException(ERROR_SKU_REQUIRED);
        }
        if (request.getBatchQuantityToDeduct() == null || request.getBatchQuantityToDeduct().isEmpty()) {
            throw new InvalidInventoryOperationException(ERROR_BATCH_QUANTITY_REQUIRED);
        }

        // Check if product exists by SKU
        var productOpt = productRepository.findBySku(request.getSku());
        if (productOpt.isEmpty()) {
            throw new ProductNotFoundException(String.format(ERROR_PRODUCT_NOT_FOUND, request.getSku()));
        }
        var product = productOpt.get();
        request.getBatchQuantityToDeduct().forEach((batchNumber, qty) -> {
            if (qty == null) {
                throw new InvalidInventoryOperationException(String.format(ERROR_QUANTITY_REQUIRED, batchNumber));
            }
            if (qty <= 0) {
                throw new InvalidInventoryOperationException(String.format(ERROR_QUANTITY_POSITIVE, batchNumber));
            }
            Optional<InventoryBatch> batchOpt = batchRepository.findByProductIdAndBatchNumber(product.getId(), batchNumber);
            if (batchOpt.isEmpty()) {
                throw new InvalidInventoryOperationException(String.format(ERROR_BATCH_NOT_FOUND, batchNumber, request.getSku()));
            }
            InventoryBatch b = batchOpt.get();
            if (b.getQuantity() < qty) {
                throw new InvalidInventoryOperationException(String.format(ERROR_INSUFFICIENT_QTY, batchNumber));
            }
            b.setQuantity(b.getQuantity() - qty);
            batchRepository.save(b);
        });
    }
}
