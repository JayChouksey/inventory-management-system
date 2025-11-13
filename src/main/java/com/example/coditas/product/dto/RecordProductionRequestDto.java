package com.example.coditas.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RecordProductionRequestDto {
    @NotBlank(message = "Product ID is required.")
    private String productId;

    @NotNull(message = "Production quantity is required.")
    @Min(value = 1, message = "Production quantity must be at least 1.")
    private Long productionQuantity;

    @NotNull(message = "Production date is required.")
    private LocalDate productionDate;
}
