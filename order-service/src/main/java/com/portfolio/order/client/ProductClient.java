package com.portfolio.order.client;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class ProductClient {

    private final RestTemplate restTemplate;
    private final String productServiceUrl;

    public ProductClient(RestTemplate restTemplate,
                         @Value("${product.service.url:http://localhost:8082}") String productServiceUrl) {
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    public ProductInfo getProduct(String productId) {
        String url = productServiceUrl + "/api/v1/products/" + productId;
        return restTemplate.getForObject(url, ProductInfo.class);
    }

    @Getter
    @Setter
    public static class ProductInfo {
        private String id;
        private String name;
        private BigDecimal price;
        private Integer stockQuantity;
        private Boolean active;
    }
}
