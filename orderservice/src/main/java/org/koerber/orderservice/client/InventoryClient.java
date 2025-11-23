package org.koerber.orderservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.koerber.orderservice.dto.InventoryResponse;
import org.koerber.orderservice.dto.InventoryUpdateRequest;
import org.koerber.orderservice.dto.InventoryUpdateResponse;
import org.koerber.orderservice.exception.InventoryServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Client for communicating with Inventory Service */
@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryClient {

  private final RestTemplate restTemplate;

  @Value("${inventory.service.url}")
  private String inventoryServiceUrl;

  /** Get inventory details for a product */
  public InventoryResponse getInventory(String productCode) {
    try {
      String url = String.format("%s/inventory/%s", inventoryServiceUrl, productCode);

      log.info("Fetching inventory details from: {}", url);

      InventoryResponse response = restTemplate.getForObject(url, InventoryResponse.class);

      log.info("Inventory response: {}", response);

      return response;

    } catch (RestClientException e) {
      log.error("Error fetching inventory for product {}: {}", productCode, e.getMessage());
      throw new InventoryServiceException(
          "Failed to fetch inventory details: " + e.getMessage(), e);
    }
  }

  /** Update inventory after order placement */
  public InventoryUpdateResponse updateInventory(
      String productCode, Integer quantity, String orderId) {
    try {
      String url = String.format("%s/inventory/update", inventoryServiceUrl);

      InventoryUpdateRequest request =
          InventoryUpdateRequest.builder()
              .productCode(productCode)
              .quantityToDeduct(quantity)
              .orderId(orderId)
              .build();

      log.info("Updating inventory at: {} with request: {}", url, request);

      InventoryUpdateResponse response =
          restTemplate.postForObject(url, request, InventoryUpdateResponse.class);

      log.info("Inventory update response: {}", response);

      return response;

    } catch (RestClientException e) {
      log.error("Error updating inventory for product {}: {}", productCode, e.getMessage());
      throw new InventoryServiceException("Failed to update inventory: " + e.getMessage(), e);
    }
  }
}
