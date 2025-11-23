package org.koerber.orderservice.exception;

/**
 * Exception thrown when Connecting with Inventory Service.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}

