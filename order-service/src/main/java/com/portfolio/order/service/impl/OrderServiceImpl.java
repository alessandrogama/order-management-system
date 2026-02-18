package com.portfolio.order.service.impl;

import com.portfolio.order.client.ProductClient;
import com.portfolio.order.config.RabbitMQConfig;
import com.portfolio.order.domain.enums.OrderStatus;
import com.portfolio.order.domain.model.Order;
import com.portfolio.order.domain.model.OrderItem;
import com.portfolio.order.dto.request.CreateOrderRequest;
import com.portfolio.order.dto.response.OrderResponse;
import com.portfolio.order.event.OrderEvent;
import com.portfolio.order.exception.OrderNotFoundException;
import com.portfolio.order.mapper.OrderMapper;
import com.portfolio.order.repository.OrderRepository;
import com.portfolio.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final RabbitTemplate rabbitTemplate;
    private final ProductClient productClient;

    @Override
    public OrderResponse create(CreateOrderRequest request) {
        log.info("Creating order for user: {}", request.userId());

        Order order = Order.builder()
                .userId(request.userId())
                .notes(request.notes())
                .status(OrderStatus.PENDING)
                .build();

        List<OrderItem> items = request.items().stream().map(itemRequest -> {
            ProductClient.ProductInfo product = productClient.getProduct(itemRequest.productId());

            if (product == null || !product.getActive()) {
                throw new IllegalStateException("Product not available: " + itemRequest.productId());
            }

            if (product.getStockQuantity() < itemRequest.quantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.quantity())
                    .unitPrice(product.getPrice())
                    .build();

            item.calcSubtotal();
            return item;
        }).toList();

        order.setItems(items);
        order.calculateTotal();

        Order saved = orderRepository.save(order);

        OrderEvent event = new OrderEvent(saved.getId(), saved.getUserId(),
                saved.getStatus(), saved.getTotalAmount(), LocalDateTime.now());

        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_CREATED_ROUTING_KEY, event);

        log.info("Order created: {} for user: {}", saved.getId(), request.userId());
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(String id) {
        return orderRepository.findById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByUserId(String userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    public OrderResponse updateStatus(String id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update a cancelled order");
        }

        order.setStatus(status);
        Order saved = orderRepository.save(order);

        OrderEvent event = new OrderEvent(saved.getId(), saved.getUserId(),
                saved.getStatus(), saved.getTotalAmount(), LocalDateTime.now());
        rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_UPDATED_ROUTING_KEY, event);

        return orderMapper.toResponse(saved);
    }

    @Override
    public void cancel(String id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel an order that has been shipped or delivered");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order {} cancelled", id);
    }
}
