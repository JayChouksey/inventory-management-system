package com.example.coditas.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DealerOrderCreateDto {
    @NotEmpty(message = "An order must contain at least one product.")
    @Valid // Ensures the items in the list are validated
    private List<OrderItemDto> products;

    private String comment; // Optional comment from the dealer
}
