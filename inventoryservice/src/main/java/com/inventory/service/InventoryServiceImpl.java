package com.inventory.service;

import com.inventory.dto.InventoryBatchDto;
import com.inventory.dto.UpdateInventoryRequest;
import com.inventory.entity.InventoryBatch;
import com.inventory.entity.Product;
import com.inventory.handlers.ProductNotFoundException;
import com.inventory.mapper.InventoryBatchMapper;
import com.inventory.repository.InventoryBatchRepository;
import com.inventory.repository.ProductRepository;
import com.inventory.validation.BaseInventoryValidator;
import com.inventory.validation.InventoryValidationFactory;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryBatchRepository inventoryBatchRepository;
    private final InventoryValidationFactory inventoryValidationFactory;
    private final ProductRepository productRepository;
    private final InventoryBatchMapper inventoryBatchMapper;

    public InventoryServiceImpl(InventoryBatchRepository inventoryBatchRepository,
                                InventoryValidationFactory inventoryValidationFactory,
                                ProductRepository productRepository,
                                InventoryBatchMapper inventoryBatchMapper) {
        this.inventoryBatchRepository = inventoryBatchRepository;
        this.inventoryValidationFactory = inventoryValidationFactory;
        this.productRepository = productRepository;
        this.inventoryBatchMapper = inventoryBatchMapper;
    }

    @Override
    public List<InventoryBatchDto> getBatchesByProductId(Long productId) {
        List<InventoryBatch> batches = inventoryBatchRepository.findByProductIdOrderByExpiryDateAsc(productId);
        return batches.stream().map(inventoryBatchMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateInventory(UpdateInventoryRequest request, String validatorType) {
        BaseInventoryValidator baseInventoryValidator = inventoryValidationFactory.getValidator(validatorType);
        baseInventoryValidator.updateInventory(request);
    }

    @Override
    public Long getProductIdBySku(String sku) {
        Optional<Product> product = productRepository.findBySku(sku);
        return product.map(Product::getId).orElse(null);
    }

    @Override
    public List<InventoryBatchDto> getAllBatches() {
        List<InventoryBatch> batches = inventoryBatchRepository.findAll();
        return batches.stream().map(inventoryBatchMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public InventoryBatch createBatch(Long productId, InventoryBatch inventoryBatch) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product with id " + productId + " not found"));
        inventoryBatch.setProduct(product);
        return inventoryBatchRepository.save(inventoryBatch);
    }

}
