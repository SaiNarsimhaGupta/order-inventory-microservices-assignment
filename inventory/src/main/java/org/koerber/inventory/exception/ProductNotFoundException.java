package org.koerber.inventory.exception;

/** Exception thrown when a requested product is not found. */
public class ProductNotFoundException extends RuntimeException {
  public ProductNotFoundException(String message) {
    super(message);
  }
}
