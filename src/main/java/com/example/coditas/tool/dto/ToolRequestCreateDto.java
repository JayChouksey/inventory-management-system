package com.example.coditas.tool.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ToolRequestCreateDto {

    @NotEmpty(message = "Add at least one tool")
    @Valid
    private List<ToolRequestItemDto> tools;
    private String comment;
}
