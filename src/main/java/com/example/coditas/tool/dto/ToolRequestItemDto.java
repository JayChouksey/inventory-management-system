package com.example.coditas.tool.dto;

import com.example.coditas.tool.enums.Expensive;
import com.example.coditas.tool.enums.Perishable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ToolRequestItemDto {
    @NotBlank(message = "Tool ID is required.")
    private String toolId;

    @NotNull(message = "Quantity is required.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    private Long quantity;
}
