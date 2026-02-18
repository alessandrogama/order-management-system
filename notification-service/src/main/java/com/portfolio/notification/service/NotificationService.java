package com.portfolio.notification.service;

import com.portfolio.notification.consumer.OrderEventConsumer.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void notifyOrderCreated(OrderEvent event) {
        log.info("=== NOTIFICATION: New order created ===");
        log.info("Order ID: {}", event.orderId());
        log.info("User ID: {}", event.userId());
        log.info("Total: ${}", event.totalAmount());
        log.info("Status: {}", event.status());
        log.info("Timestamp: {}", event.timestamp());
        log.info("Action: Send confirmation email to user");
    }

    public void notifyOrderUpdated(OrderEvent event) {
        log.info("=== NOTIFICATION: Order status updated ===");
        log.info("Order ID: {}", event.orderId());
        log.info("New Status: {}", event.status());
        log.info("Timestamp: {}", event.timestamp());
        log.info("Action: Send status update email to user");
    }
}
