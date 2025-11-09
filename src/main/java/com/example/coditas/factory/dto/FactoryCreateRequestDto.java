package com.example.coditas.factory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// FactoryCreateRequestDto.java
@Getter
@Setter
@NoArgsConstructor
public class FactoryCreateRequestDto {

    @NotBlank(message = "Factory name is required")
    private String name;

    @NotBlank(message = "City is required")
    private String city;

    private String address;

    @NotNull(message = "Central office is required")
    private String centralOfficeId;

    // Plant Head: either existing or new
    private String plantHeadId;
    private String newPlantHeadName;
    private String newPlantHeadEmail;
    private String newPlantHeadPhone;
}
