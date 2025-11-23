package org.koerber.inventory.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.koerber.inventory.dto.BatchDTO;
import org.koerber.inventory.dto.InventoryResponse;
import org.koerber.inventory.dto.InventoryUpdateRequest;
import org.koerber.inventory.dto.InventoryUpdateResponse;
import org.koerber.inventory.enums.BatchStatus;
import org.koerber.inventory.exception.InsufficientStockException;
import org.koerber.inventory.exception.ProductNotFoundException;
import org.koerber.inventory.factory.InventoryHandlerFactoryImpl;
import org.koerber.inventory.handler.InventoryHandler;
import org.koerber.inventory.model.InventoryBatch;
import org.koerber.inventory.model.Product;
import org.koerber.inventory.repository.InventoryBatchRepository;
import org.koerber.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementation of InventoryService */
@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

  private final ProductRepository productRepository;
  private final InventoryBatchRepository batchRepository;
  private final InventoryHandlerFactoryImpl handlerFactory;

  @Override
  @Transactional(readOnly = true)
  public InventoryResponse getInventoryByProduct(String productCode) {
    log.info("Getting inventory for product ID: {}", productCode);

    Product product =
        productRepository
            .findByProductCode(productCode)
            .orElseThrow(
                () ->
                    new ProductNotFoundException(
                        "Product not found with productCode: " + productCode));

    List<InventoryBatch> batches = batchRepository.findByProductId(product.getId());
    log.debug("Found {} batches for product {}", batches.size(), productCode);

    InventoryHandler handler = handlerFactory.getHandler(product.getHandlerType().name());
    log.debug("Using handler: {} for product {}", handler.getType(), product.getProductCode());

    List<InventoryBatch> sortedBatches = handler.sortAndFilterBatches(batches);

    List<BatchDTO> batchDTOs =
        sortedBatches.stream().map(BatchDTO::convertToBatchDTO).collect(Collectors.toList());

    int totalQuantity = batches.stream().mapToInt(InventoryBatch::getQuantity).sum();

    int availableQuantity =
        sortedBatches.stream()
            .filter(InventoryBatch::isAvailable)
            .mapToInt(InventoryBatch::getQuantity)
            .sum();

    // Check low stock warning
    boolean lowStockWarning =
        product.getMinimumStock() != null && availableQuantity < product.getMinimumStock();

    return InventoryResponse.builder()
        .productId(product.getId())
        .productCode(product.getProductCode())
        .productName(product.getName())
        .category(product.getCategory())
        .totalQuantity(totalQuantity)
        .availableQuantity(availableQuantity)
        .handlerType(product.getHandlerType().name())
        .batches(batchDTOs)
        .minimumStock(product.getMinimumStock())
        .lowStockWarning(lowStockWarning)
        .message(lowStockWarning ? "Low stock warning!" : "Stock available")
        .build();
  }

  @Override
  @Transactional
  public InventoryUpdateResponse updateInventory(InventoryUpdateRequest request) {
    log.info(
        "Updating inventory for product ID/Code: {}, quantity: {}, order: {}",
        request.getProductCode(),
        request.getQuantityToDeduct(),
        request.getOrderId());

    Product product =
        productRepository
            .findByProductCode(request.getProductCode())
            .orElseThrow(
                () ->
                    new ProductNotFoundException(
                        "Product not found with ID: " + request.getProductCode()));

    List<InventoryBatch> availableBatches = batchRepository.findAvailableBatches(product.getId());

    InventoryHandler handler = handlerFactory.getHandler(product.getHandlerType().name());

    List<InventoryBatch> sortedBatches = handler.sortAndFilterBatches(availableBatches);

    int totalAvailable =
        sortedBatches.stream()
            .filter(InventoryBatch::isAvailable)
            .mapToInt(InventoryBatch::getQuantity)
            .sum();

    if (totalAvailable < request.getQuantityToDeduct()) {
      String message =
          String.format(
              "Insufficient stock for product %s: requested=%d, available=%d",
              request.getProductCode(), request.getQuantityToDeduct(), totalAvailable);
      throw new InsufficientStockException(message);
    }

    List<InventoryBatch> selectedBatches =
        handler.selectBatchesForDeduction(sortedBatches, request.getQuantityToDeduct());

    List<InventoryUpdateResponse.BatchDeduction> batchDeductions =
        deductFromBatches(selectedBatches, request.getQuantityToDeduct());

    int remainingQuantity = batchRepository.calculateTotalAvailableQuantity(product.getId());

    log.info("Inventory updated successfully. Remaining quantity: {}", remainingQuantity);
    return InventoryUpdateResponse.builder()
        .success(true)
        .message("Inventory updated successfully")
        .productCode(request.getProductCode())
        .orderId(request.getOrderId())
        .quantityDeducted(request.getQuantityToDeduct())
        .remainingQuantity(remainingQuantity)
        .batchDeductions(batchDeductions)
        .timestamp(LocalDateTime.now())
        .build();
  }

  /** Deduct quantities from selected batches */
  private List<InventoryUpdateResponse.BatchDeduction> deductFromBatches(
      List<InventoryBatch> batches, Integer totalQuantityToDeduct) {

    List<InventoryUpdateResponse.BatchDeduction> deductions = new ArrayList<>();
    int remainingToDeduct = totalQuantityToDeduct;

    for (InventoryBatch batch : batches) {
      if (remainingToDeduct <= 0) {
        break;
      }

      int batchQuantity = batch.getQuantity();
      int deductFromThisBatch = Math.min(batchQuantity, remainingToDeduct);

      batch.setQuantity(batchQuantity - deductFromThisBatch);

      BatchStatus newStatus = batch.getQuantity() == 0 ? BatchStatus.INACTIVE : BatchStatus.ACTIVE;
      batch.setStatus(newStatus);

      batchRepository.save(batch);
      deductions.add(
          InventoryUpdateResponse.BatchDeduction.builder()
              .batchId(batch.getId())
              .batchNumber(batch.getBatchNumber())
              .quantityDeducted(deductFromThisBatch)
              .remainingQuantity(batch.getQuantity())
              .newStatus(newStatus.name())
              .build());

      remainingToDeduct -= deductFromThisBatch;
      log.debug(
          "Deducted {} from batch {}. Remaining in batch: {}",
          deductFromThisBatch,
          batch.getBatchNumber(),
          batch.getQuantity());
    }

    return deductions;
  }
}
