package com.example.coditas.product.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductUpdateRequestDto {
    private String name;
    private String description;
    private BigDecimal unitPrice;
    private Long categoryId;
    private MultipartFile image; // optional
}
