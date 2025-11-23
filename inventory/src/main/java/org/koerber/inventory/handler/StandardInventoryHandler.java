package org.koerber.inventory.handler;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.koerber.inventory.enums.BatchStatus;
import org.koerber.inventory.model.InventoryBatch;
import org.springframework.stereotype.Component;

/**
 * Standard inventory handler that sorts batches by expiry date (earliest first) and excludes
 * expired batches. Strategy: Use batches that expire soonest to minimize waste.
 */
@Component
@Slf4j
public class StandardInventoryHandler implements InventoryHandler {

  public static final String TYPE = "STANDARD";

  @Override
  public List<InventoryBatch> sortAndFilterBatches(List<InventoryBatch> batches) {
    log.debug("StandardHandler: Sorting {} batches by expiry date", batches.size());

    LocalDate today = LocalDate.now();

    return batches.stream()
        .filter(batch -> BatchStatus.ACTIVE.equals(batch.getStatus()))
        .filter(batch -> batch.getExpiryDate() == null || !batch.getExpiryDate().isBefore(today))
        .filter(batch -> batch.getQuantity() > 0)
        .sorted(
            Comparator.comparing(
                InventoryBatch::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder())))
        .collect(Collectors.toList());
  }

  @Override
  public List<InventoryBatch> selectBatchesForDeduction(
      List<InventoryBatch> batches, Integer requiredQuantity) {

    log.debug("StandardHandler: Selecting batches for quantity {}", requiredQuantity);

    List<InventoryBatch> selectedBatches = new ArrayList<>();
    int remainingQuantity = requiredQuantity;

    for (InventoryBatch batch : batches) {
      if (remainingQuantity <= 0) {
        break;
      }

      if (batch.getQuantity() > 0) {
        selectedBatches.add(batch);
        remainingQuantity -= batch.getQuantity();
        log.debug(
            "Selected batch {} with quantity {}", batch.getBatchNumber(), batch.getQuantity());
      }
    }

    return selectedBatches;
  }

  @Override
  public String getType() {
    return TYPE;
  }
}
