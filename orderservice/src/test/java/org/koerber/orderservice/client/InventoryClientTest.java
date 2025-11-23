package org.koerber.orderservice.client;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.koerber.orderservice.dto.InventoryResponse;
import org.koerber.orderservice.dto.InventoryUpdateRequest;
import org.koerber.orderservice.dto.InventoryUpdateResponse;
import org.koerber.orderservice.exception.InventoryServiceException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class InventoryClientTest {

  @Mock private RestTemplate restTemplate;

  @InjectMocks private InventoryClient inventoryClient;

  private static final String INVENTORY_SERVICE_URL = "http://inventory-service";
  private static final String PRODUCT_CODE = "PROD-001";

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(inventoryClient, "inventoryServiceUrl", INVENTORY_SERVICE_URL);
  }

  @Test
  void getInventory_SuccessfulResponse() {

    InventoryResponse expectedResponse =
        InventoryResponse.builder().productCode(PRODUCT_CODE).availableQuantity(10).build();

    String expectedUrl = INVENTORY_SERVICE_URL + "/inventory/" + PRODUCT_CODE;
    when(restTemplate.getForObject(expectedUrl, InventoryResponse.class))
        .thenReturn(expectedResponse);

    InventoryResponse actualResponse = inventoryClient.getInventory(PRODUCT_CODE);

    assertThat(actualResponse).isNotNull();
    assertThat(actualResponse.getProductCode()).isEqualTo(PRODUCT_CODE);
    assertThat(actualResponse.getAvailableQuantity()).isEqualTo(10);

    verify(restTemplate).getForObject(expectedUrl, InventoryResponse.class);
  }

  @Test
  void getInventory_ThrowsInventoryServiceException() {

    String expectedUrl = INVENTORY_SERVICE_URL + "/inventory/" + PRODUCT_CODE;
    when(restTemplate.getForObject(expectedUrl, InventoryResponse.class))
        .thenThrow(new RestClientException("Service unavailable"));

    assertThatThrownBy(() -> inventoryClient.getInventory(PRODUCT_CODE))
        .isInstanceOf(InventoryServiceException.class)
        .hasMessageContaining("Failed to fetch inventory details")
        .hasCauseInstanceOf(RestClientException.class);

    verify(restTemplate).getForObject(expectedUrl, InventoryResponse.class);
  }

  @Test
  void updateInventory_SuccessfulResponse() {
    // Arrange
    String orderId = "ORD-12345678";
    Integer quantity = 5;

    InventoryUpdateResponse expectedResponse =
        InventoryUpdateResponse.builder()
            .success(true)
            .message("Inventory updated successfully")
            .remainingQuantity(5)
            .build();

    String expectedUrl = INVENTORY_SERVICE_URL + "/inventory/update";

    when(restTemplate.postForObject(
            eq(expectedUrl), any(InventoryUpdateRequest.class), eq(InventoryUpdateResponse.class)))
        .thenReturn(expectedResponse);


    InventoryUpdateResponse actualResponse =
        inventoryClient.updateInventory(PRODUCT_CODE, quantity, orderId);


    assertThat(actualResponse).isNotNull();
    assertThat(actualResponse.getSuccess()).isTrue();
    assertThat(actualResponse.getMessage()).isEqualTo("Inventory updated successfully");
    assertThat(actualResponse.getRemainingQuantity()).isEqualTo(5);

    verify(restTemplate)
        .postForObject(
            eq(expectedUrl),
            argThat(
                request -> {
                  InventoryUpdateRequest req = (InventoryUpdateRequest) request;
                  return req.getProductCode().equals(PRODUCT_CODE)
                      && req.getQuantityToDeduct().equals(quantity)
                      && req.getOrderId().equals(orderId);
                }),
            eq(InventoryUpdateResponse.class));
  }

  @Test
  void updateInventory_ThrowsInventoryServiceException() {
    String orderId = "ORD-12345678";
    Integer quantity = 5;
    String expectedUrl = INVENTORY_SERVICE_URL + "/inventory/update";

    when(restTemplate.postForObject(
            eq(expectedUrl), any(InventoryUpdateRequest.class), eq(InventoryUpdateResponse.class)))
        .thenThrow(new RestClientException("Connection timeout"));

    assertThatThrownBy(() -> inventoryClient.updateInventory(PRODUCT_CODE, quantity, orderId))
        .isInstanceOf(InventoryServiceException.class)
        .hasMessageContaining("Failed to update inventory")
        .hasCauseInstanceOf(RestClientException.class);

    verify(restTemplate)
        .postForObject(
            eq(expectedUrl), any(InventoryUpdateRequest.class), eq(InventoryUpdateResponse.class));
  }

}
