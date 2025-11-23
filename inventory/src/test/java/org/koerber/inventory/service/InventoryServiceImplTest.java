package org.koerber.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.koerber.inventory.dto.InventoryResponse;
import org.koerber.inventory.dto.InventoryUpdateRequest;
import org.koerber.inventory.dto.InventoryUpdateResponse;
import org.koerber.inventory.enums.BatchStatus;
import org.koerber.inventory.enums.HandlerType;
import org.koerber.inventory.enums.ProductCategory;
import org.koerber.inventory.exception.ProductNotFoundException;
import org.koerber.inventory.factory.InventoryHandlerFactoryImpl;
import org.koerber.inventory.handler.InventoryHandler;
import org.koerber.inventory.model.InventoryBatch;
import org.koerber.inventory.model.Product;
import org.koerber.inventory.repository.InventoryBatchRepository;
import org.koerber.inventory.repository.ProductRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

  @Mock private ProductRepository productRepository;

  @Mock private InventoryBatchRepository batchRepository;

  @Mock private InventoryHandlerFactoryImpl handlerFactory;

  @Mock private InventoryHandler inventoryHandler;

  @InjectMocks private InventoryServiceImpl inventoryService;

  private Product testProduct;
  private InventoryBatch testBatch1;
  private InventoryBatch testBatch2;

  @BeforeEach
  void setUp() {
    testProduct =
        Product.builder()
            .id(1L)
            .productCode("PROD-001")
            .name("Test Product")
            .description("Test Description")
            .category(ProductCategory.ELECTRONICS)
            .minimumStock(50)
            .handlerType(HandlerType.STANDARD)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    testBatch1 =
        InventoryBatch.builder()
            .id(1L)
            .product(testProduct)
            .batchNumber("BATCH-001")
            .quantity(100)
            .expiryDate(LocalDate.now().plusMonths(6))
            .manufacturingDate(LocalDate.now().minusMonths(1))
            .supplierName("Test Supplier")
            .costPerUnit(BigDecimal.valueOf(10.50))
            .status(BatchStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

    testBatch2 =
        InventoryBatch.builder()
            .id(2L)
            .product(testProduct)
            .batchNumber("BATCH-002")
            .quantity(75)
            .expiryDate(LocalDate.now().plusMonths(12))
            .manufacturingDate(LocalDate.now().minusWeeks(2))
            .supplierName("Test Supplier 2")
            .costPerUnit(BigDecimal.valueOf(11.00))
            .status(BatchStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
  }

  @Test
  void getInventoryByProduct_Success() {

    String productCode = "PROD-001";
    List<InventoryBatch> batches = Arrays.asList(testBatch1, testBatch2);
    List<InventoryBatch> sortedBatches = Arrays.asList(testBatch1, testBatch2);

    when(productRepository.findByProductCode(productCode)).thenReturn(Optional.of(testProduct));
    when(batchRepository.findByProductId(testProduct.getId())).thenReturn(batches);
    when(handlerFactory.getHandler("STANDARD")).thenReturn(inventoryHandler);
    when(inventoryHandler.getType()).thenReturn("STANDARD");
    when(inventoryHandler.sortAndFilterBatches(batches)).thenReturn(sortedBatches);
    when(testBatch1.isAvailable()).thenReturn(true);
    when(testBatch2.isAvailable()).thenReturn(true);

    InventoryResponse response = inventoryService.getInventoryByProduct(productCode);

    assertThat(response).isNotNull();
    assertThat(response.getProductId()).isEqualTo(1L);
    assertThat(response.getProductCode()).isEqualTo("PROD-001");
    assertThat(response.getProductName()).isEqualTo("Test Product");
    assertThat(response.getCategory()).isEqualTo(ProductCategory.ELECTRONICS);
    assertThat(response.getTotalQuantity()).isEqualTo(175);
    assertThat(response.getAvailableQuantity()).isEqualTo(175);
    assertThat(response.getHandlerType()).isEqualTo("STANDARD");
    assertThat(response.getMinimumStock()).isEqualTo(50);
    assertThat(response.getBatches()).hasSize(2);

    verify(productRepository).findByProductCode(productCode);
    verify(batchRepository).findByProductId(testProduct.getId());
    verify(handlerFactory).getHandler("STANDARD");
    verify(inventoryHandler).sortAndFilterBatches(batches);
  }

  @Test
  void getInventoryByProduct_ThrowsProductNotFoundException() {

    String productCode = "NONEXISTENT";
    when(productRepository.findByProductCode(productCode)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> inventoryService.getInventoryByProduct(productCode))
        .isInstanceOf(ProductNotFoundException.class)
        .hasMessageContaining("Product not found with productCode: NONEXISTENT");

    verify(productRepository).findByProductCode(productCode);
    verifyNoInteractions(batchRepository, handlerFactory);
  }

  @Test
  void updateInventory_WhenSufficientStock_UpdatesInventorySuccessfully() {
    InventoryUpdateRequest request =
        InventoryUpdateRequest.builder()
            .productCode("PROD-001")
            .quantityToDeduct(50)
            .orderId("ORDER-123")
            .build();

    List<InventoryBatch> availableBatches = Arrays.asList(testBatch1, testBatch2);
    List<InventoryBatch> sortedBatches = Arrays.asList(testBatch1, testBatch2);
    List<InventoryBatch> selectedBatches = Collections.singletonList(testBatch1);

    when(productRepository.findByProductCode("PROD-001")).thenReturn(Optional.of(testProduct));
    when(batchRepository.findAvailableBatches(testProduct.getId())).thenReturn(availableBatches);
    when(handlerFactory.getHandler("STANDARD")).thenReturn(inventoryHandler);
    when(inventoryHandler.sortAndFilterBatches(availableBatches)).thenReturn(sortedBatches);
    when(inventoryHandler.selectBatchesForDeduction(sortedBatches, 50)).thenReturn(selectedBatches);
    when(batchRepository.calculateTotalAvailableQuantity(testProduct.getId())).thenReturn(125);

    InventoryUpdateResponse response = inventoryService.updateInventory(request);

    assertThat(response).isNotNull();
    assertThat(response.getSuccess()).isTrue();
    assertThat(response.getMessage()).isEqualTo("Inventory updated successfully");
    assertThat(response.getProductCode()).isEqualTo("PROD-001");
    assertThat(response.getOrderId()).isEqualTo("ORDER-123");
    assertThat(response.getQuantityDeducted()).isEqualTo(50);
    assertThat(response.getRemainingQuantity()).isEqualTo(125);

    verify(productRepository).findByProductCode("PROD-001");
    verify(batchRepository).findAvailableBatches(testProduct.getId());
    verify(handlerFactory).getHandler("STANDARD");
    verify(inventoryHandler).sortAndFilterBatches(availableBatches);
    verify(inventoryHandler).selectBatchesForDeduction(sortedBatches, 50);
    verify(batchRepository).calculateTotalAvailableQuantity(testProduct.getId());
  }
}
