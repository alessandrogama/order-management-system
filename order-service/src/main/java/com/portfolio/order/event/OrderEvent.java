package com.portfolio.order.event;

import com.portfolio.order.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderEvent(
        String orderId,
        String userId,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime timestamp
) {}
