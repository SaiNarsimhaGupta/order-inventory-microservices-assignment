package org.koerber.orderservice.service;

import org.koerber.orderservice.dto.OrderRequest;
import org.koerber.orderservice.dto.OrderResponse;

/**
 * Service Interface for order operations.
 */
public interface OrderService {

    /**
     * Place an order based on the provided request.
     * @param request order request payload
     * @return order response
     */
    OrderResponse placeOrder(OrderRequest request);
}

