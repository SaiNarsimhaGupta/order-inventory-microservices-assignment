package org.koerber.orderservice.exception;

/**
 * Exception thrown when Connecting with Inventory Service.
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}

