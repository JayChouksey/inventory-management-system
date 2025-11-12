package com.example.coditas.tool.dto;

import com.example.coditas.tool.enums.ToolIssuanceStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolIssuanceDto {
    private Long issuanceId;
    private String toolName;
    private String workerName;
    private String issuerName;
    private ToolIssuanceStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime returnDate; // We need to add this field to the ToolIssuance entity
}
