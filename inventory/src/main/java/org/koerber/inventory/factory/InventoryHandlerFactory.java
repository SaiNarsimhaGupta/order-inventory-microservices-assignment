package org.koerber.inventory.factory;


import org.koerber.inventory.handler.InventoryHandler;

/**
 * Factory interface for creating inventory handlers.
 * This enables the Factory Design Pattern for extensibility.
 * New handler types can be added without modifying existing code.
 */
public interface InventoryHandlerFactory {

    /**
     * Get an inventory handler for the specified type
     *
     * @param handlerType type of handler (e.g., "STANDARD", "FIFO")
     * @return the appropriate inventory handler
     * @throws IllegalArgumentException if a handler type is not supported
     */
    InventoryHandler getHandler(String handlerType);

}