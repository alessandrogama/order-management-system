package com.portfolio.order.mapper;

import com.portfolio.order.domain.model.Order;
import com.portfolio.order.domain.model.OrderItem;
import com.portfolio.order.dto.response.OrderItemResponse;
import com.portfolio.order.dto.response.OrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    OrderResponse toResponse(Order order);
    OrderItemResponse toItemResponse(OrderItem item);
}
