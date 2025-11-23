package org.koerber.inventory.repository;


import org.koerber.inventory.model.InventoryBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for InventoryBatch entity
 */
@Repository
public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {

    /**
     * Find all batches for a product
     * @param productId product ID
     * @return list of batches
     */
    List<InventoryBatch> findByProductId(Long productId);


    /**
     * Find active non-expired batches for a product
     * @param productId product ID
     * @return list of active non-expired batches
     */
    @Query("SELECT b FROM InventoryBatch b WHERE b.product.id = :productId " +
            "AND b.status = 'ACTIVE' " +
            "AND (b.expiryDate IS NULL OR b.expiryDate >= CURRENT_DATE) " +
            "AND b.quantity > 0")
    List<InventoryBatch> findAvailableBatches(@Param("productId") Long productId);

    /**
     * Calculate total available quantity for a product
     * @param productId product ID
     * @return total quantity
     */
    @Query("SELECT COALESCE(SUM(b.quantity), 0) FROM InventoryBatch b " +
            "WHERE b.product.id = :productId " +
            "AND b.status = 'ACTIVE' " +
            "AND (b.expiryDate IS NULL OR b.expiryDate >= CURRENT_DATE) " +
            "AND b.quantity > 0")
    Integer calculateTotalAvailableQuantity(@Param("productId") Long productId);

}
