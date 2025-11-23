package org.koerber.inventory.exception;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ValidationErrorResponse {
  private LocalDateTime timestamp;
  private Integer status;
  private String error;
  private Map<String, String> validationErrors;
  private String path;
}
