package com.example.coditas.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class GenericFilterDto {
    private String name;
    private String city;
    private String plantHeadName;
    private Long centralOfficeId;
    private Integer categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String stockStatus;
    private String perishable;
    private String expensive;
    private String roleId;
    private String factoryId;
    private String bayId;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
}
