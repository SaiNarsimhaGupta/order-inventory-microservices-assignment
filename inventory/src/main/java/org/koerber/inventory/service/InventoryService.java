package org.koerber.inventory.service;

import org.koerber.inventory.dto.InventoryResponse;
import org.koerber.inventory.dto.InventoryUpdateRequest;
import org.koerber.inventory.dto.InventoryUpdateResponse;

/**
 * Service interface for inventory operations
 */
public interface InventoryService {

    /**
     * Get inventory details for a product with batches sorted by handler strategy
     *
     * @param productCode product ID
     * @return inventory response with sorted batches
     */
    InventoryResponse getInventoryByProduct(String productCode);

    /**
     * Update inventory by deducting quantity after an order is placed
     *
     * @param request update request containing product ID/Code, quantity, and order ID
     * @return update response with deduction details
     */
    InventoryUpdateResponse updateInventory(InventoryUpdateRequest request);

}
