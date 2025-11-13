package com.example.coditas.common.controller;

import com.example.coditas.common.dto.ApiResponseDto;
import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.product.dto.ProductRequestCreateDto;
import com.example.coditas.product.dto.ProductRequestResponseDto;
import com.example.coditas.product.service.ProductRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/product-requests")
@RequiredArgsConstructor
public class ProductRequestController {

    private final ProductRequestService productRequestService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<ProductRequestResponseDto>> createProductRequest(@Valid @RequestBody ProductRequestCreateDto dto) {
        ProductRequestResponseDto createdRequest = productRequestService.createProductRequest(dto);
        return new ResponseEntity<>(ApiResponseDto.ok(createdRequest, "Product request created successfully."), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<ProductRequestResponseDto>>> getAllProductRequests(@ModelAttribute PageableDto pageableDto) {
        Page<ProductRequestResponseDto> requests = productRequestService.getAllProductRequests(pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(requests));
    }

    @GetMapping("/factory/{factoryId}")
    public ResponseEntity<ApiResponseDto<Page<ProductRequestResponseDto>>> getProductRequestsByFactory(
            @PathVariable String factoryId,
            @ModelAttribute PageableDto pageableDto) {
        Page<ProductRequestResponseDto> requests = productRequestService.getProductRequestsByFactory(factoryId, pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(requests));
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponseDto<ProductRequestResponseDto>> getProductRequestById(@PathVariable Long requestId) {
        ProductRequestResponseDto request = productRequestService.getProductRequestById(requestId);
        return ResponseEntity.ok(ApiResponseDto.ok(request));
    }

    @PostMapping("/{requestId}/approve")
    public ResponseEntity<ApiResponseDto<ProductRequestResponseDto>> approveRequest(@PathVariable Long requestId) {
        ProductRequestResponseDto approvedRequest = productRequestService.approveRequest(requestId);
        return ResponseEntity.ok(ApiResponseDto.ok(approvedRequest, "Request approved successfully."));
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<ApiResponseDto<ProductRequestResponseDto>> rejectRequest(@PathVariable Long requestId) {
        ProductRequestResponseDto rejectedRequest = productRequestService.rejectRequest(requestId);
        return ResponseEntity.ok(ApiResponseDto.ok(rejectedRequest, "Request rejected successfully."));
    }
}
