package com.example.coditas.factory.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class FactoryPageResponseDto {
    private List<FactoryResponseDto> factories;
    private int currentPage;
    private int totalPages;
    private long totalItems;
    private int pageSize;
}
