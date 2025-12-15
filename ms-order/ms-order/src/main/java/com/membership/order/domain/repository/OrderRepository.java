package com.membership.order.domain.repository;

import com.membership.order.domain.entity.Order;
import com.membership.order.domain.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(OrderStatus status);
}
