package com.example.coditas.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CategoryResponseDto {
    private Long id;
    private String name;
    private String description;
    private Long toolCount;
    private String createdOn;
}
