package org.koerber.orderservice.repository;

import org.koerber.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for Order entity */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {}
