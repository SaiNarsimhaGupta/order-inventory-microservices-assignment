package org.koerber.inventory.handler;

import lombok.extern.slf4j.Slf4j;
import org.koerber.inventory.enums.BatchStatus;
import org.koerber.inventory.model.InventoryBatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FIFO (First In, First Out) inventory handler.
 * Sorts batches by manufacturing date (oldest first).
 * Strategy: Use the oldest inventory first, regardless of expiry date.
 */
@Component
@Slf4j
public class FIFOInventoryHandler implements InventoryHandler {

    public static final String TYPE = "FIFO";

    @Override
    public List<InventoryBatch> sortAndFilterBatches(List<InventoryBatch> batches) {
        log.debug("FIFOHandler: Sorting {} batches by manufacturing/creation date",
                batches.size());

        return batches.stream()
                .filter(batch -> BatchStatus.ACTIVE.equals(batch.getStatus()))
                .filter(batch -> batch.getQuantity() > 0)
                .sorted(Comparator
                        .comparing(
                                InventoryBatch::getManufacturingDate,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                        .thenComparing(
                                InventoryBatch::getCreatedAt,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                )
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryBatch> selectBatchesForDeduction(
            List<InventoryBatch> batches,
            Integer requiredQuantity) {

        log.debug("FIFOHandler: Selecting batches for quantity {}", requiredQuantity);

        List<InventoryBatch> selectedBatches = new ArrayList<>();
        int remainingQuantity = requiredQuantity;
        for (InventoryBatch batch : batches) {
            if (remainingQuantity <= 0) {
                break;
            }

            if (batch.getQuantity() > 0) {
                selectedBatches.add(batch);
                remainingQuantity -= batch.getQuantity();
                log.debug("Selected batch {} (mfg: {}) with quantity {}",
                        batch.getBatchNumber(),
                        batch.getManufacturingDate(),
                        batch.getQuantity());
            }
        }

        return selectedBatches;
    }

    @Override
    public String getType() {
        return TYPE;
    }

}
