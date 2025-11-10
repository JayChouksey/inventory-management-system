package com.example.coditas.common.service;

import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.common.dto.CategoryFilterDto;
import com.example.coditas.common.dto.CategoryRequestDto;
import com.example.coditas.common.dto.CategoryResponseDto;
import org.springframework.data.domain.Page;

public interface CategoryService {
    Page<CategoryResponseDto> searchCategories(CategoryFilterDto filter, PageableDto pageReq);
    Page<CategoryResponseDto> globalSearch(String q, PageableDto pageReq);
    CategoryResponseDto createCategory(CategoryRequestDto dto);
    CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto);
    String deleteCategory(Long id);
}
