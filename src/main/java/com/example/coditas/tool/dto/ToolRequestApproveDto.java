package com.example.coditas.tool.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ToolRequestApproveDto {
    private String comment;
    private LocalDateTime returnDate; // required
}
