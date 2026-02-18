package com.portfolio.product.service;

import com.portfolio.product.dto.request.CreateProductRequest;
import com.portfolio.product.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse create(CreateProductRequest request);
    ProductResponse findById(String id);
    List<ProductResponse> findAll();
    List<ProductResponse> findByCategory(String category);
    ProductResponse updateStock(String id, int quantity);
    void delete(String id);
}
