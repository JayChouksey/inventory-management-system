package com.example.coditas.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProductRequestCreateDto {
    @NotBlank(message = "Target Factory ID is required.")
    private String factoryId;

    @NotEmpty(message = "Request must contain at least one product.")
    @Valid // Ensures validation rules on ProductRequestItemDto are checked
    private List<ProductRequestItemDto> products;
}
