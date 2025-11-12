package com.example.coditas.tool.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveExtensionDto {

    @NotNull(message = "Approval status cannot be null.")
    private Boolean approved; // true for approve, false for deny

    private String comment; // Optional for denial reason
}
