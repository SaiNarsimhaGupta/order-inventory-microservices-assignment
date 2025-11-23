package org.koerber.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.koerber.orderservice.enums.OrderStatus;
import org.koerber.orderservice.model.Order;

import java.time.LocalDateTime;

/**
 * Response DTO for order operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private String orderId;
    private String productCode;
    private Integer quantity;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private String message;
    private Boolean success;
    private Integer remainingStock;

    /**
     * Build OrderResponse DTO
     */
    public static OrderResponse buildOrderResponse(Order order, Boolean success) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .productCode(order.getProductCode())
                .quantity(order.getQuantity())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .message(order.getMessage())
                .success(success)
                .remainingStock(order.getRemainingStock())
                .build();
    }
}
