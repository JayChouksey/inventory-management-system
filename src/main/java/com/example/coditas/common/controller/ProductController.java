package com.example.coditas.common.controller;

import com.example.coditas.common.dto.ApiResponseDto;
import com.example.coditas.common.dto.GenericFilterDto;
import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.product.dto.ProductCreateRequestDto;
import com.example.coditas.product.dto.ProductResponseDto;
import com.example.coditas.product.dto.ProductUpdateRequestDto;
import com.example.coditas.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<ProductResponseDto>>> getProducts(
            @ModelAttribute GenericFilterDto filter,
            @ModelAttribute PageableDto page) {
        Page<ProductResponseDto> data = productService.searchProducts(filter, page);
        return ResponseEntity.ok(ApiResponseDto.paged(data, page.getPage(), page.getSize(), data.getTotalElements()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<Page<ProductResponseDto>>> globalSearch(
            @RequestParam String q,
            @ModelAttribute PageableDto page) {
        Page<ProductResponseDto> data = productService.globalSearch(q, page);
        return ResponseEntity.ok(ApiResponseDto.paged(data, page.getPage(), page.getSize(), data.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> getDetail(@PathVariable String id) {
        ProductResponseDto data = productService.getProductDetail(id);
        return ResponseEntity.ok(ApiResponseDto.ok(data, "Product details"));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> create(
            @Valid @ModelAttribute ProductCreateRequestDto dto) {
        ProductResponseDto data = productService.createProduct(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.ok(data, "Product created successfully"));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<ProductResponseDto>> update(
            @PathVariable String id,
            @Valid @ModelAttribute ProductUpdateRequestDto dto) {
        ProductResponseDto data = productService.updateProduct(id, dto);
        return ResponseEntity.ok(ApiResponseDto.ok(data, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> delete(@PathVariable String id) {
        String msg = productService.softDeleteProduct(id);
        return ResponseEntity.ok(ApiResponseDto.ok(msg, "Success"));
    }
}
