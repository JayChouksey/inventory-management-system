package com.example.coditas.order.dto;

import com.example.coditas.order.enums.DealerOrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Builder
public class DealerOrderResponseDto {
    private String orderId;
    private String dealerName;
    private String dealerId;
    private BigDecimal totalPrice;
    private DealerOrderStatus status;
    private String comment;
    private ZonedDateTime createdAt;
    private List<OrderItemDto> products;
}
