package org.koerber.inventory.exception;

/** Exception thrown when attempting to reduce stock below available quantity. */
public class InsufficientStockException extends RuntimeException {
  public InsufficientStockException(String message) {
    super(message);
  }
}
