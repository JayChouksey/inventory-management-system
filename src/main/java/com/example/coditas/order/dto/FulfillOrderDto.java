package com.example.coditas.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FulfillOrderDto {
    @NotBlank(message = "The fulfilling factory ID is required.")
    private String factoryId;
}
