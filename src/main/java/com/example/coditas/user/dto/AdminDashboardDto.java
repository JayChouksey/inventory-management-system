package com.example.coditas.user.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Builder
@Getter
@Setter
public class AdminDashboardDto {
    private long totalFactories;
    private long totalEmployees;
    private long totalDealers;
    private long totalCustomers;

    // Monthly Sales
    private BigDecimal monthlySales;
    private YearMonth salesMonth;

    // Production per factory
    private List<FactoryProductionSummaryDto> factoryProductions;
}
