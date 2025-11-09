package com.example.coditas.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductCreateRequestDto {

    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Unit price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal unitPrice;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Image is required")
    private MultipartFile image;
}
