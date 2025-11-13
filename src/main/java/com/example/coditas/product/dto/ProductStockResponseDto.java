package com.example.coditas.product.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductStockResponseDto {
    private String productId;
    private String productName;
    private String factoryId;
    private String factoryName;
    private Long quantity;
}
