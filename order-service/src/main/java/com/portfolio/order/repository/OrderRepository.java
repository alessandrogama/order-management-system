package com.portfolio.order.repository;

import com.portfolio.order.domain.enums.OrderStatus;
import com.portfolio.order.domain.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserId(String userId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByUserIdAndStatus(String userId, OrderStatus status);
}
