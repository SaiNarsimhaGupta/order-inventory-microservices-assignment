package org.koerber.orderservice.exception;

/**
 * Exception thrown when Connecting with Inventory Service.
 */
public class InventoryServiceException extends RuntimeException {
    public InventoryServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

