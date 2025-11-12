package com.example.coditas.tool.dto;

import com.example.coditas.tool.enums.Expensive;
import com.example.coditas.tool.enums.Perishable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WorkerToolDto {
    private String toolName;
    private String imageUrl;
    private Integer quantity;
    private String issuedOn;
    private String returnBy; // future
    private Perishable perishable;
    private Expensive expensive;
}
