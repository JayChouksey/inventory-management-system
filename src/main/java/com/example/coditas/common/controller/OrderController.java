package com.example.coditas.common.controller;

import com.example.coditas.common.dto.ApiResponseDto;
import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.order.dto.DealerInvoiceResponseDto;
import com.example.coditas.order.dto.DealerOrderCreateDto;
import com.example.coditas.order.dto.DealerOrderResponseDto;
import com.example.coditas.order.dto.FulfillOrderDto;
import com.example.coditas.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<DealerOrderResponseDto>> createDealerOrder(@Valid @RequestBody DealerOrderCreateDto dto) {
        DealerOrderResponseDto createdOrder = orderService.createDealerOrder(dto);
        return new ResponseEntity<>(ApiResponseDto.ok(createdOrder, "Order placed successfully."), HttpStatus.CREATED);
    }

    @PostMapping("/{orderId}/fulfill")
    public ResponseEntity<ApiResponseDto<DealerInvoiceResponseDto>> fulfillDealerOrder(
            @PathVariable String orderId,
            @Valid @RequestBody FulfillOrderDto dto) {
        DealerInvoiceResponseDto invoiceDto = orderService.fulfillDealerOrder(orderId, dto);
        return ResponseEntity.ok(ApiResponseDto.ok(invoiceDto, "Order fulfilled and invoice generated successfully."));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<DealerOrderResponseDto>>> getAllDealerOrders(@ModelAttribute PageableDto pageableDto) {
        Page<DealerOrderResponseDto> orders = orderService.getAllDealerOrders(pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(orders));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponseDto<DealerOrderResponseDto>> getDealerOrderById(@PathVariable String orderId) {
        DealerOrderResponseDto order = orderService.getDealerOrderById(orderId);
        return ResponseEntity.ok(ApiResponseDto.ok(order));
    }
}
