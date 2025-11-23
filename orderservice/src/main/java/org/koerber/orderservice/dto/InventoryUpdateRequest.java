package org.koerber.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for inventory update operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdateRequest {

    @NotBlank(message = "productCode is required")
    private String productCode;

    @NotNull(message = "Quantity to deduct is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantityToDeduct;

    @NotBlank(message = "Order ID is required")
    private String orderId;
}
