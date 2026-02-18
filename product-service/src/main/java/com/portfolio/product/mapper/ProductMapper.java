package com.portfolio.product.mapper;

import com.portfolio.product.domain.model.Product;
import com.portfolio.product.dto.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {
    ProductResponse toResponse(Product product);
}
