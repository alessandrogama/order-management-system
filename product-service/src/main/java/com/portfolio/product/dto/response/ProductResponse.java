package com.portfolio.product.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        String id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String category,
        Boolean active,
        LocalDateTime createdAt
) {}
