package org.koerber.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for inventory update operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryUpdateResponse {

    private Boolean success;

    private String message;

    private String productCode;

    private String orderId;

    private Integer quantityDeducted;

    private Integer remainingQuantity;

    private List<BatchDeduction> batchDeductions;

    private LocalDateTime timestamp;

    /**
     * Inner class representing batch-wise deduction details
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BatchDeduction {
        private Long batchId;
        private String batchNumber;
        private Integer quantityDeducted;
        private Integer remainingQuantity;
        private String newStatus;
    }
}
