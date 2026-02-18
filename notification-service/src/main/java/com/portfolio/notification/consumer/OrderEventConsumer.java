package com.portfolio.notification.consumer;

import com.portfolio.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreated(OrderEvent event) {
        log.info("Received order.created event for order: {}", event.orderId());
        notificationService.notifyOrderCreated(event);
    }

    @RabbitListener(queues = "order.updated.queue")
    public void handleOrderUpdated(OrderEvent event) {
        log.info("Received order.updated event for order: {}", event.orderId());
        notificationService.notifyOrderUpdated(event);
    }

    public record OrderEvent(
            String orderId,
            String userId,
            String status,
            BigDecimal totalAmount,
            LocalDateTime timestamp
    ) {}
}
