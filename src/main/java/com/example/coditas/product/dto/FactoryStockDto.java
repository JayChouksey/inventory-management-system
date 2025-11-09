package com.example.coditas.product.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FactoryStockDto {
    private String factoryName;
    private String city;
    private Integer quantity;
}
