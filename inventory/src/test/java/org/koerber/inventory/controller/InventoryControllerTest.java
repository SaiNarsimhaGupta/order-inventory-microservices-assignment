package org.koerber.inventory.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.koerber.inventory.dto.InventoryResponse;
import org.koerber.inventory.dto.InventoryUpdateRequest;
import org.koerber.inventory.dto.InventoryUpdateResponse;
import org.koerber.inventory.enums.ProductCategory;
import org.koerber.inventory.service.InventoryService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class InventoryControllerTest {

  @LocalServerPort private int port;

  @Mock private RestTemplate restTemplate;

  @InjectMocks private InventoryService inventoryService;

  private String baseUrl;
  private InventoryUpdateRequest validUpdateRequest;
  private InventoryResponse successfulInventoryResponse;
  private InventoryUpdateResponse successfulUpdateResponse;
  private InventoryUpdateResponse failedUpdateResponse;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + port;

    validUpdateRequest =
        InventoryUpdateRequest.builder()
            .productCode("PROD-001")
            .quantityToDeduct(10)
            .orderId("ORD-12345678")
            .build();

    successfulInventoryResponse =
        InventoryResponse.builder()
            .productId(1L)
            .productCode("PROD-001")
            .productName("Aspirin 500mg")
            .category(ProductCategory.HEALTH_AND_BEAUTY)
            .totalQuantity(100)
            .availableQuantity(100)
            .handlerType("STANDARD")
            .batches(new ArrayList<>())
            .minimumStock(50)
            .lowStockWarning(false)
            .message("Stock available")
            .build();

    successfulUpdateResponse =
        InventoryUpdateResponse.builder()
            .success(true)
            .message("Inventory updated successfully")
            .productCode("PROD-001")
            .orderId("ORD-12345678")
            .quantityDeducted(10)
            .remainingQuantity(90)
            .timestamp(LocalDateTime.now())
            .build();

    failedUpdateResponse =
        InventoryUpdateResponse.builder()
            .success(false)
            .message("Failed to update inventory")
            .productCode("PROD-001")
            .orderId("ORD-12345678")
            .quantityDeducted(0)
            .remainingQuantity(100)
            .timestamp(LocalDateTime.now())
            .build();
  }

  @Test
  void getInventory_Successful() {
    when(inventoryService.getInventoryByProduct(any(String.class)))
        .thenReturn(successfulInventoryResponse);

    String productCode = "PROD-001";

    ResponseEntity<InventoryResponse> response =
        restTemplate.getForEntity(baseUrl + "/inventory/" + productCode, InventoryResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getProductCode()).isEqualTo("PROD-001");
    assertThat(response.getBody().getProductName()).isEqualTo("Aspirin 500mg");
    assertThat(response.getBody().getTotalQuantity()).isEqualTo(100);
    assertThat(response.getBody().getAvailableQuantity()).isEqualTo(100);

    verify(inventoryService).getInventoryByProduct(any(String.class));
  }

  @Test
  void updateInventory_Successful() {
    when(inventoryService.updateInventory(any(InventoryUpdateRequest.class)))
        .thenReturn(successfulUpdateResponse);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<InventoryUpdateRequest> request = new HttpEntity<>(validUpdateRequest, headers);

    ResponseEntity<InventoryUpdateResponse> response =
        restTemplate.postForEntity(
            baseUrl + "/inventory/update", request, InventoryUpdateResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess()).isTrue();
    assertThat(response.getBody().getProductCode()).isEqualTo("PROD-001");
    assertThat(response.getBody().getQuantityDeducted()).isEqualTo(10);
    assertThat(response.getBody().getRemainingQuantity()).isEqualTo(90);

    verify(inventoryService).updateInventory(any(InventoryUpdateRequest.class));
  }

  @Test
  void updateInventory_Failed() {
    when(inventoryService.updateInventory(any(InventoryUpdateRequest.class)))
        .thenReturn(failedUpdateResponse);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<InventoryUpdateRequest> request = new HttpEntity<>(validUpdateRequest, headers);

    ResponseEntity<InventoryUpdateResponse> response =
        restTemplate.postForEntity(
            baseUrl + "/inventory/update", request, InventoryUpdateResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess()).isFalse();

    verify(inventoryService).updateInventory(any(InventoryUpdateRequest.class));
  }
}
