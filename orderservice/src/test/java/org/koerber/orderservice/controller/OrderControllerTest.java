package org.koerber.orderservice.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.koerber.orderservice.dto.OrderRequest;
import org.koerber.orderservice.dto.OrderResponse;
import org.koerber.orderservice.enums.OrderStatus;
import org.koerber.orderservice.service.OrderService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OrderControllerTest {

  @LocalServerPort private int port;

  @Mock
  private RestTemplate restTemplate;

  @InjectMocks
  private OrderService orderService;

  private String baseUrl;
  private OrderRequest validOrderRequest;
  private OrderResponse successfulOrderResponse;
  private OrderResponse failedOrderResponse;

  @BeforeEach
  void setUp() {
    baseUrl = "http://localhost:" + port;

    validOrderRequest = OrderRequest.builder().productCode("PROD-001").quantity(5).build();

    successfulOrderResponse =
        OrderResponse.builder()
            .id(1L)
            .orderId("ORD-12345678")
            .productCode("PROD-001")
            .quantity(5)
            .status(OrderStatus.CONFIRMED)
            .orderDate(LocalDateTime.now())
            .message("Order confirmed. Remaining stock: 5")
            .success(true)
            .remainingStock(5)
            .build();

    failedOrderResponse =
        OrderResponse.builder()
            .id(2L)
            .orderId("ORD-87654321")
            .productCode("PROD-001")
            .quantity(5)
            .status(OrderStatus.FAILED)
            .orderDate(LocalDateTime.now())
            .message("Failed to update inventory")
            .success(false)
            .build();
  }

  @Test
  void placeOrder_Successful() {
    when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(successfulOrderResponse);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<OrderRequest> request = new HttpEntity<>(validOrderRequest, headers);

    ResponseEntity<OrderResponse> response =
        restTemplate.postForEntity(baseUrl + "/order", request, OrderResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess()).isTrue();
    assertThat(response.getBody().getOrderId()).isEqualTo("ORD-12345678");
    assertThat(response.getBody().getProductCode()).isEqualTo("PROD-001");
    assertThat(response.getBody().getQuantity()).isEqualTo(5);

    verify(orderService).placeOrder(any(OrderRequest.class));
  }

  @Test
  void placeOrder_Failed() {
    when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(failedOrderResponse);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<OrderRequest> request = new HttpEntity<>(validOrderRequest, headers);

    ResponseEntity<OrderResponse> response =
        restTemplate.postForEntity(baseUrl + "/order", request, OrderResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getSuccess()).isFalse();

    verify(orderService).placeOrder(any(OrderRequest.class));
  }
}
