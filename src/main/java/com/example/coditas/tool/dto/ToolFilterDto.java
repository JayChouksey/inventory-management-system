package com.example.coditas.tool.dto;

import com.example.coditas.tool.enums.Expensive;
import com.example.coditas.tool.enums.Perishable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ToolFilterDto {
    private String name;
    private Integer categoryId;
    private String perishable;
    private String expensive;
    private String stockStatus; // "IN_STOCK", "LOW", "CRITICAL", "OUT_OF_STOCK"
    private LocalDate startDate;
    private LocalDate endDate;
}
