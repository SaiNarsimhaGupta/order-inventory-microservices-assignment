package org.koerber.orderservice.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.koerber.orderservice.client.InventoryClient;
import org.koerber.orderservice.dto.InventoryResponse;
import org.koerber.orderservice.dto.InventoryUpdateResponse;
import org.koerber.orderservice.dto.OrderRequest;
import org.koerber.orderservice.dto.OrderResponse;
import org.koerber.orderservice.enums.OrderStatus;
import org.koerber.orderservice.exception.InsufficientStockException;
import org.koerber.orderservice.model.Order;
import org.koerber.orderservice.repository.OrderRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

  @Mock private OrderRepository orderRepository;

  @Mock private InventoryClient inventoryClient;

  @InjectMocks private OrderServiceImpl orderService;

  private OrderRequest orderRequest;
  private InventoryResponse inventoryResponse;
  private InventoryUpdateResponse updateResponse;
  private Order order;

  @BeforeEach
  void setUp() {
    orderRequest = OrderRequest.builder().productCode("PROD-001").quantity(5).build();

    inventoryResponse =
        InventoryResponse.builder().productCode("PROD-001").availableQuantity(10).build();

    updateResponse =
        InventoryUpdateResponse.builder()
            .success(true)
            .message("Inventory updated successfully")
            .remainingQuantity(5)
            .build();

    order =
        Order.builder()
            .id(1L)
            .orderId("ORD-12345678")
            .productCode("PROD-001")
            .quantity(5)
            .status(OrderStatus.PENDING)
            .message("Order placed !")
            .build();
  }

  @Test
  void placeOrder_SuccessResponse() {

    when(inventoryClient.getInventory("PROD-001")).thenReturn(inventoryResponse);
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(inventoryClient.updateInventory(eq("PROD-001"), eq(5), anyString())).thenReturn(updateResponse);

    OrderResponse response = orderService.placeOrder(orderRequest);

    assertThat(response).isNotNull();
    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getProductCode()).isEqualTo("PROD-001");
    assertThat(response.getQuantity()).isEqualTo(5);
    assertThat(response.getRemainingStock()).isEqualTo(5);

    verify(inventoryClient).getInventory("PROD-001");
    verify(orderRepository, times(2)).save(any(Order.class));
    verify(inventoryClient).updateInventory(eq("PROD-001"), eq(5), anyString());
  }

  @Test
  void placeOrder_InsufficientStockException() {
    // Arrange
    inventoryResponse =
        InventoryResponse.builder()
            .productCode("PROD-001")
            .availableQuantity(3)
            .build();

    when(inventoryClient.getInventory("PROD-001")).thenReturn(inventoryResponse);
    when(orderRepository.save(any(Order.class))).thenReturn(order);

    // Act & Assert
    assertThatThrownBy(() -> orderService.placeOrder(orderRequest))
        .isInstanceOf(InsufficientStockException.class)
        .hasMessageContaining("Insufficient stock for product PROD-001");

    verify(inventoryClient).getInventory("PROD-001");
    verify(orderRepository).save(any(Order.class));
    verify(inventoryClient, never()).updateInventory(anyString(), anyInt(), anyString());
  }

  @Test
  void placeOrder_InventoryServiceException() {

    when(inventoryClient.getInventory("PROD-001")).thenReturn(inventoryResponse);
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(inventoryClient.updateInventory(anyString(), anyInt(), anyString())).thenThrow(new RestClientException("Service unavailable"));

    assertThatThrownBy(() -> orderService.placeOrder(orderRequest))
        .isInstanceOf(RestClientException.class)
        .hasMessageContaining("Service unavailable");

    verify(inventoryClient).getInventory("PROD-001");
    verify(orderRepository, times(2)).save(any(Order.class));
    verify(inventoryClient).updateInventory(eq("PROD-001"), eq(5), anyString());
  }

}
