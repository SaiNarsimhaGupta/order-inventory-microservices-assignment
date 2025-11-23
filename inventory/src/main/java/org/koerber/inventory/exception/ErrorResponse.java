package org.koerber.inventory.exception;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
  private LocalDateTime timestamp;
  private Integer status;
  private String error;
  private String message;
  private String path;
}
