package com.example.coditas.product.dto;

import com.example.coditas.product.enums.ProductRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
public class ProductRequestResponseDto {
    private Long id;
    private String requestNumber;
    private String centralOfficerName;
    private String factoryId;
    private String factoryName;
    private String status;
    private ZonedDateTime createdAt;
    private List<ProductRequestItemDto> products;
}
