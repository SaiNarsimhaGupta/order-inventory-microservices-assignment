package org.koerber.inventory.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.koerber.inventory.model.InventoryBatch;

/** DTO representing an inventory batch in API responses */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchDTO {

  private Long batchId;

  private String batchNumber;

  private Integer quantity;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate expiryDate;

  @JsonFormat(pattern = "yyyy-MM-dd")
  private LocalDate manufacturingDate;

  private String status;

  private String supplierName;

  private Integer daysUntilExpiry;

  private Boolean isExpired;

  /** Convert InventoryBatch entity to BatchDTO */
  public static BatchDTO convertToBatchDTO(InventoryBatch batch) {
    BatchDTO dto =
        BatchDTO.builder()
            .batchId(batch.getId())
            .batchNumber(batch.getBatchNumber())
            .quantity(batch.getQuantity())
            .expiryDate(batch.getExpiryDate())
            .manufacturingDate(batch.getManufacturingDate())
            .status(batch.getStatus() != null ? batch.getStatus().name() : null)
            .supplierName(batch.getSupplierName())
            .build();
    dto.calculateExpiryInfo();

    return dto;
  }

  /** Calculate days until expiry and set flags */
  public void calculateExpiryInfo() {
    if (expiryDate != null) {
      LocalDate today = LocalDate.now();
      this.daysUntilExpiry = (int) ChronoUnit.DAYS.between(today, expiryDate);
      this.isExpired = daysUntilExpiry < 0;
    } else {
      this.daysUntilExpiry = null;
      this.isExpired = false;
    }
  }
}
