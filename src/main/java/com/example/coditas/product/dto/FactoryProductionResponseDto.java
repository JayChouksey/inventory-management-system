package com.example.coditas.product.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class FactoryProductionResponseDto {
    private Long id;
    private String productId;
    private String productName;
    private String factoryId;
    private String factoryName;
    private Long productionQuantity;
    private LocalDate productionDate;
}
