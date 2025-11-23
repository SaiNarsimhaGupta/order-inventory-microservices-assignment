package org.koerber.inventory.factory;

import lombok.extern.slf4j.Slf4j;
import org.koerber.inventory.handler.InventoryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
@Slf4j
public class InventoryHandlerFactoryImpl implements InventoryHandlerFactory {

    private final Map<String, InventoryHandler> handlerMap;

    /**
     * Constructor with dependency injection.
     * Spring automatically injects all beans implementing InventoryHandler.
     *
     * @param handlers list of all InventoryHandler implementations
     */
    @Autowired
    public InventoryHandlerFactoryImpl(List<InventoryHandler> handlers) {
        // Create a map: handlerType -> handler instance
        this.handlerMap = handlers.stream()
                .collect(Collectors.toMap(
                        InventoryHandler::getType,
                        Function.identity()
                ));
        log.info("InventoryHandlerFactory initialized with {} handlers: {}",
                handlerMap.size(),
                handlerMap.keySet());
    }

    @Override
    public InventoryHandler getHandler(String handlerType) {
        log.debug("Requesting handler for type: {}", handlerType);
        InventoryHandler handler = handlerMap.get(handlerType);
        if (handler == null) {
            log.error("No handler found for type: {}", handlerType);
            throw new IllegalArgumentException(
                    "Unsupported handler type: " + handlerType +
                            ". Available types: " + handlerMap.keySet()
            );
        }
        log.debug("Returning handler: {}", handler.getClass().getSimpleName());
        return handler;
    }

}