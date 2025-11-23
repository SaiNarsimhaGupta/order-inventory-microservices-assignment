package org.koerber.orderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.koerber.orderservice.client.InventoryClient;
import org.koerber.orderservice.dto.InventoryUpdateResponse;
import org.koerber.orderservice.dto.OrderRequest;
import org.koerber.orderservice.dto.OrderResponse;
import org.koerber.orderservice.enums.OrderStatus;
import org.koerber.orderservice.exception.InsufficientStockException;
import org.koerber.orderservice.model.Order;
import org.koerber.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service layer for order operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;

    /**
     * Place order
     * 1. Check stock availability
     * 2. Create order with PENDING status
     * 3. Update inventory
     * 4. Update order status based on a result
     */
    @Transactional
    @Override
    public OrderResponse placeOrder(OrderRequest request) {
        log.info("Processing order for product: {}, quantity: {}",
                request.getProductCode(), request.getQuantity());

        String orderId = generateOrderId();
        log.info("Generated order ID: {}", orderId);

        int availableQuantity = inventoryClient.getInventory(request.getProductCode()).getAvailableQuantity();

        if (availableQuantity > 0 && availableQuantity < request.getQuantity()) {
            log.warn("Insufficient stock for product: {}", request.getProductCode());

            Order order = createOrder(orderId, request, OrderStatus.FAILED,
                    "Insufficient stock available", null);
            orderRepository.save(order);

            throw new InsufficientStockException(
                    String.format("Insufficient stock for product %s. Requested: %d",
                            request.getProductCode(), request.getQuantity()));
        }

        Order order = createOrder(orderId, request, OrderStatus.PENDING,
                "Order placed, awaiting inventory update", null);
        order = orderRepository.save(order);
        log.info("Order created with ID: {} (DB ID: {})", order.getOrderId(), order.getId());

        try {
            InventoryUpdateResponse updateResponse = inventoryClient.updateInventory(
                    request.getProductCode(), request.getQuantity(), orderId);

            if (updateResponse.getSuccess()) {
                order.setStatus(OrderStatus.CONFIRMED);
                order.setMessage(String.format("Order confirmed. Remaining stock: %d",
                        updateResponse.getRemainingQuantity()));
                order.setRemainingStock(updateResponse.getRemainingQuantity());
                log.info("Order {} confirmed successfully", order.getOrderId());
            } else {
                order.setStatus(OrderStatus.FAILED);
                order.setMessage("Failed to update inventory: " + updateResponse.getMessage());
                log.error("Failed to update inventory for order {}", order.getOrderId());
            }

            order = orderRepository.save(order);

            return OrderResponse.buildOrderResponse(order, updateResponse.getSuccess());

        } catch (Exception e) {
            log.error("Error processing order {}: {}", order.getOrderId(), e.getMessage());

            order.setStatus(OrderStatus.FAILED);
            order.setMessage("Error: " + e.getMessage());
            orderRepository.save(order);

            throw e;
        }
    }

    /**
     * Generate unique order ID
     */
    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Create an Order entity from request
     */
    private Order createOrder(String orderId, OrderRequest request,
                              OrderStatus status, String message, Integer remainingStock) {
        return Order.builder()
                .orderId(orderId)
                .productCode(request.getProductCode())
                .quantity(request.getQuantity())
                .status(status)
                .orderDate(LocalDateTime.now())
                .message(message)
                .remainingStock(remainingStock)
                .build();
    }


}
