package com.example.coditas.appuser.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// FactoryProductionSummaryDto.java
@Builder
@Getter
@Setter
public class FactoryProductionSummaryDto {
    private Long factoryId;
    private String factoryName;
    private String city;
    private String plantHeadName;
    private Long totalProducedThisMonth;
}