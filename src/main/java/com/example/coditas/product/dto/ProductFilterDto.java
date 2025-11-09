package com.example.coditas.product.dto;

import com.example.coditas.common.enums.ActiveStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ProductFilterDto {
    private String name;
    private Integer categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String stockStatus; // "IN_STOCK", "LOW", "OUT_OF_STOCK"
    private ActiveStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
}