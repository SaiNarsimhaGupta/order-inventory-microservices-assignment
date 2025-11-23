package org.koerber.inventory.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.koerber.inventory.dto.InventoryResponse;
import org.koerber.inventory.dto.InventoryUpdateRequest;
import org.koerber.inventory.dto.InventoryUpdateResponse;
import org.koerber.inventory.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** REST Controller for inventory operations */
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

  private final InventoryService inventoryService;

  /**
   * GET /inventory/{productCode} Returns a list of inventory batches sorted by expiry date for a
   * given product
   */
  @GetMapping("/{productCode}")
  public ResponseEntity<InventoryResponse> getInventory(@PathVariable String productCode) {
    log.info("Retrieving inventory for product ID/Code {}", productCode);
    InventoryResponse response = inventoryService.getInventoryByProduct(productCode);
    return ResponseEntity.ok(response);
  }

  /** POST /inventory/update Updates inventory after an order is placed */
  @PostMapping("/update")
  public ResponseEntity<InventoryUpdateResponse> updateInventory(
      @Valid @RequestBody InventoryUpdateRequest request) {
    log.info("Updating inventory for product ID/Code: {}", request.getProductCode());
    InventoryUpdateResponse response = inventoryService.updateInventory(request);
    return ResponseEntity.ok(response);
  }
}
