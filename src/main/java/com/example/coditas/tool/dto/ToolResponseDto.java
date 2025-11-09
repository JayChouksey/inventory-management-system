package com.example.coditas.tool.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ToolResponseDto {
    private String toolId;
    private String name;
    private String imageUrl;
    private String categoryName;
    private String perishable;
    private String expensive;
    private Integer threshold;
    private Integer totalStock;
    private Integer availableStock;
    private String stockStatus;
    private String createdOn;
}
