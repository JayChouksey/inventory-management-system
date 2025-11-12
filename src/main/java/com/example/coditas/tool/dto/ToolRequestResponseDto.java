package com.example.coditas.tool.dto;

import com.example.coditas.tool.enums.RequestNature;
import com.example.coditas.tool.enums.ToolRequestStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ToolRequestResponseDto {
    private Long requestId;
    private String requestNumber;
    private String workerName;
    private String factoryId;
    private ToolRequestStatus status;
    private String comment;
    private LocalDateTime createdAt;
    private List<ToolRequestItemDto> tools;
}
