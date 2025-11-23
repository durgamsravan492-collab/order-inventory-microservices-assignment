package com.inventory.controller;

import com.inventory.dto.InventoryBatchDto;
import com.inventory.dto.UpdateInventoryRequest;
import com.inventory.entity.InventoryBatch;
import com.inventory.entity.Product;
import com.inventory.service.InventoryService;
import com.inventory.validation.BatchQueryValidator;
import com.inventory.validation.InventoryBatchMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@Tag(name = "Inventory", description = "Inventory management APIs")
public class InventoryController {

    private final InventoryService inventoryService;
    private final BatchQueryValidator batchQueryValidator;
    private final InventoryBatchMapper inventoryBatchMapper;

    public InventoryController(InventoryService inventoryService, BatchQueryValidator batchQueryValidator, InventoryBatchMapper inventoryBatchMapper) {
        this.inventoryService = inventoryService;
        this.batchQueryValidator = batchQueryValidator;
        this.inventoryBatchMapper = inventoryBatchMapper;
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get inventory batches by product ID")
    public ResponseEntity<List<InventoryBatchDto>> getBatches(
            @Parameter(description = "Product ID") @PathVariable Long productId) {
        List<InventoryBatchDto> batches = inventoryService.getBatchesByProductId(productId);
        return ResponseEntity.ok(batches);
    }

    @PostMapping("/update")
    @Operation(summary = "Update inventory for a product")
    public ResponseEntity<Void> updateInventory(
            @RequestBody UpdateInventoryRequest request,
            @Parameter(description = "Handler type") @RequestParam(name = "handlerType", defaultValue = "default") String handlerType) {

        inventoryService.updateInventory(request, handlerType);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/batches")
    @Operation(summary = "Get all inventory batches")
    public ResponseEntity<List<InventoryBatchDto>> getAllBatches(@RequestParam(name = "sku", required = false) String sku) {
        List<InventoryBatchDto> batches = batchQueryValidator.getBatchesForSkuOrAll(sku);
        return ResponseEntity.ok(batches);
    }

    @PostMapping("/product")
    @Operation(summary = "Create a new product")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product created = inventoryService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/batch")
    @Operation(summary = "Create a new inventory batch for a product")
    public ResponseEntity<InventoryBatchDto> createBatch(@RequestParam Long productId, @RequestBody InventoryBatch inventoryBatch) {

        InventoryBatch created = inventoryService.createBatch(productId, inventoryBatch);
        InventoryBatchDto inventoryBatchDto = inventoryBatchMapper.toDto(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryBatchDto);
    }
}
