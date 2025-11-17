package com.example.coditas.common.service;

import com.example.coditas.common.dto.*;
import org.springframework.data.domain.Page;

public interface CategoryService {
    Page<CategoryResponseDto> searchCategories(GenericFilterDto filter, PageableDto pageReq);
    Page<CategoryResponseDto> globalSearch(String q, PageableDto pageReq);
    CategoryResponseDto createCategory(CategoryRequestDto dto);
    CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto);
    String deleteCategory(Long id);
}
