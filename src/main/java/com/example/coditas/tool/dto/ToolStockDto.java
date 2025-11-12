package com.example.coditas.tool.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Builder
public class ToolStockDto {
    private String toolId;
    private String toolName;
    private String factoryId;
    private Long totalQuantity;
    private Long availableQuantity;
    private Long issuedQuantity;
    private ZonedDateTime lastUpdatedAt;
}
