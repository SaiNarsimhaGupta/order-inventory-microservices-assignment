package org.koerber.inventory.handler;


import org.koerber.inventory.model.InventoryBatch;

import java.util.List;

/**
 * Interface for inventory handling strategies.
 * Different implementations can provide different sorting and selection logic.
 * This interface is used as Factory Pattern for extensibility.
 */
public interface InventoryHandler {



    /**
     * Sort and optionally filter batches based on the handler's strategy
     *
     * @param batches list of inventory batches
     * @return sorted list of batches
     */
    List<InventoryBatch> sortAndFilterBatches(List<InventoryBatch> batches);

    /**
     * Select batches to fulfill the required quantity
     *
     * @param batches available batches (should be sorted)
     * @param requiredQuantity quantity needed
     * @return list of batches to use for deduction
     */
    List<InventoryBatch> selectBatchesForDeduction(
            List<InventoryBatch> batches,
            Integer requiredQuantity
    );

    /**
     * Get the handler type identifier
     *
     * @return handler type (e.g., "STANDARD", "FIFO")
     */
    String getType();

}