package com.example.coditas.order.dto;

import lombok.Builder;
import lombok.Data;

import java.time.ZonedDateTime;


@Data
@Builder
public class DealerInvoiceResponseDto {
    private String invoiceId;
    private String orderId;
    private String dealerId;
    private String dealerName;
    private String pdfUrl;
    private ZonedDateTime createdAt;
}
