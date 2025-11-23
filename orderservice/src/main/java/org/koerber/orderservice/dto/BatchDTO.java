package org.koerber.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
