package org.koerber.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.koerber.inventory.enums.ProductCategory;

import java.util.List;

/**
 * Response DTO for inventory queries
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

    private Long productId;

    private String productCode;

    private String productName;

    private ProductCategory category;

    private Integer totalQuantity;

    private Integer availableQuantity;

    private String handlerType;

    private List<BatchDTO> batches;

    private Integer minimumStock;

    private String message;
}
