package com.portfolio.order.service;

import com.portfolio.order.domain.enums.OrderStatus;
import com.portfolio.order.dto.request.CreateOrderRequest;
import com.portfolio.order.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse create(CreateOrderRequest request);
    OrderResponse findById(String id);
    List<OrderResponse> findByUserId(String userId);
    List<OrderResponse> findAll();
    OrderResponse updateStatus(String id, OrderStatus status);
    void cancel(String id);
}
