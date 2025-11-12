package com.example.coditas.tool.dto;

import com.example.coditas.tool.enums.ToolIssuanceStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ToolIssuanceResponseDto {
    private Long issuanceId;
    private String toolName;
    private String workerName;
    private String issuerName;
    private ToolIssuanceStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime returnDate;
}
