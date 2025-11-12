package com.example.coditas.tool.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddToolStockDto {

    @NotBlank
    private String toolId;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1.")
    private Long quantity;
}
