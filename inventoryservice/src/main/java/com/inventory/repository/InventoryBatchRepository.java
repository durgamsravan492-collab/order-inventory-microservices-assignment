package com.inventory.repository;

import com.inventory.entity.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {
    List<InventoryBatch> findByProduct_IdOrderByExpiryDateAsc(Long productId);
    Optional<InventoryBatch> findByProductIdAndBatchNumber(Long id, String batchNumber);
}
