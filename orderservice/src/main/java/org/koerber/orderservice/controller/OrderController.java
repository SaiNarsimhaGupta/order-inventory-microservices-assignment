package org.koerber.orderservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.koerber.orderservice.dto.OrderRequest;
import org.koerber.orderservice.dto.OrderResponse;
import org.koerber.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for order operations
 */
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /order
     * Places an order and updates inventory accordingly
     */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(
            @Valid @RequestBody OrderRequest request) {
        log.info("Placing order for product: {}, quantity: {}",
                request.getProductCode(), request.getQuantity());
        OrderResponse response = orderService.placeOrder(request);
        HttpStatus status = response.getSuccess() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, status);
    }
}
