package com.example.coditas.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CategoryRequestDto {

    @NotBlank(message = "Category name is required")
    @Size(max = 150, message = "Name must be less than 150 characters")
    private String name;

    private String description;
}
