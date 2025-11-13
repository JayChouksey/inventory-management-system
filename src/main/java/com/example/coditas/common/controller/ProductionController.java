package com.example.coditas.common.controller;

import com.example.coditas.common.dto.ApiResponseDto;
import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.product.dto.FactoryProductionResponseDto;
import com.example.coditas.product.dto.ProductStockResponseDto;
import com.example.coditas.product.dto.RecordProductionRequestDto;
import com.example.coditas.product.service.FactoryProductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProductionController {

    private final FactoryProductionService productionService;

    @PostMapping("/production")
    public ResponseEntity<ApiResponseDto<FactoryProductionResponseDto>> recordProduction(@Valid @RequestBody RecordProductionRequestDto dto) {
        FactoryProductionResponseDto recordedProduction = productionService.recordProduction(dto);
        return new ResponseEntity<>(ApiResponseDto.ok(recordedProduction, "Production recorded successfully."), HttpStatus.CREATED);
    }

    @GetMapping("/factories/{factoryId}/stock")
    public ResponseEntity<ApiResponseDto<Page<ProductStockResponseDto>>> getStockByFactory(
            @PathVariable String factoryId,
            @ModelAttribute PageableDto pageableDto) {
        Page<ProductStockResponseDto> stockPage = productionService.getStockByFactory(factoryId, pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(stockPage));
    }

    @GetMapping("/factories/{factoryId}/production-records")
    public ResponseEntity<ApiResponseDto<Page<FactoryProductionResponseDto>>> getProductionRecordsByFactory(
            @PathVariable String factoryId,
            @ModelAttribute PageableDto pageableDto) {
        Page<FactoryProductionResponseDto> recordsPage = productionService.getProductionRecordsByFactory(factoryId, pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(recordsPage));
    }

    @GetMapping("/products/{productId}/stock-levels")
    public ResponseEntity<ApiResponseDto<Page<ProductStockResponseDto>>> getStockLevelsForProduct(@PathVariable String productId,
                                                                                          @ModelAttribute PageableDto pageableDto) {
        Page<ProductStockResponseDto> stockLevels = productionService.getStockLevelsForProduct(productId, pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(stockLevels));
    }
}
