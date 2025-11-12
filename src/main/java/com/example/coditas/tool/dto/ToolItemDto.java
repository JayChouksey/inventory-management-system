package com.example.coditas.tool.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ToolItemDto {
    @NotNull
    private Long toolId;
    @NotNull
    @Min(value = 1)
    private Integer quantity;
}
