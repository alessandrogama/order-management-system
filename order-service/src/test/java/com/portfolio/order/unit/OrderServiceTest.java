package com.portfolio.order.unit;

import com.portfolio.order.client.ProductClient;
import com.portfolio.order.config.RabbitMQConfig;
import com.portfolio.order.domain.enums.OrderStatus;
import com.portfolio.order.domain.model.Order;
import com.portfolio.order.domain.model.OrderItem;
import com.portfolio.order.dto.request.CreateOrderRequest;
import com.portfolio.order.dto.request.OrderItemRequest;
import com.portfolio.order.dto.response.OrderResponse;
import com.portfolio.order.exception.OrderNotFoundException;
import com.portfolio.order.mapper.OrderMapper;
import com.portfolio.order.repository.OrderRepository;
import com.portfolio.order.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order buildOrder() {
        Order order = new Order();
        order.setId("order-123");
        order.setUserId("user-123");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(100.00));
        order.setItems(new ArrayList<>());
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }

    private OrderResponse buildOrderResponse(Order order) {
        return new OrderResponse(order.getId(), order.getUserId(), order.getStatus(),
                order.getTotalAmount(), null, List.of(), order.getCreatedAt(), null);
    }

    @Test
    @DisplayName("Should create order successfully")
    void shouldCreateOrder() {
        var itemRequest = new OrderItemRequest("product-1", 2);
        var request = new CreateOrderRequest("user-123", List.of(itemRequest), null);
        var order = buildOrder();
        var response = buildOrderResponse(order);

        var productInfo = new ProductClient.ProductInfo();
        productInfo.setId("product-1");
        productInfo.setName("Test Product");
        productInfo.setPrice(BigDecimal.valueOf(50.00));
        productInfo.setStockQuantity(10);
        productInfo.setActive(true);

        when(productClient.getProduct("product-1")).thenReturn(productInfo);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(response);

        OrderResponse result = orderService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo("user-123");
        verify(orderRepository).save(any(Order.class));
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.ORDER_EXCHANGE),
                eq(RabbitMQConfig.ORDER_CREATED_ROUTING_KEY), Optional.ofNullable(any()));
    }

    @Test
    @DisplayName("Should throw when product is out of stock")
    void shouldThrowWhenOutOfStock() {
        var itemRequest = new OrderItemRequest("product-1", 10);
        var request = new CreateOrderRequest("user-123", List.of(itemRequest), null);

        var productInfo = new ProductClient.ProductInfo();
        productInfo.setId("product-1");
        productInfo.setName("Test Product");
        productInfo.setPrice(BigDecimal.valueOf(50.00));
        productInfo.setStockQuantity(5);
        productInfo.setActive(true);

        when(productClient.getProduct("product-1")).thenReturn(productInfo);

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order not found")
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById("non-existent"))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @DisplayName("Should update order status")
    void shouldUpdateStatus() {
        var order = buildOrder();
        var response = buildOrderResponse(order);

        when(orderRepository.findById("order-123")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(response);

        orderService.updateStatus("order-123", OrderStatus.CONFIRMED);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
    }

    @Test
    @DisplayName("Should not allow updating a cancelled order")
    void shouldNotUpdateCancelledOrder() {
        var order = buildOrder();
        order.setStatus(OrderStatus.CANCELLED);

        when(orderRepository.findById("order-123")).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus("order-123", OrderStatus.CONFIRMED))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cancelled");
    }

    @Test
    @DisplayName("Should cancel order")
    void shouldCancelOrder() {
        var order = buildOrder();
        when(orderRepository.findById("order-123")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.cancel("order-123");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }
}
