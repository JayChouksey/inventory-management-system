package com.example.coditas.common.controller;

import com.example.coditas.common.dto.*;
import com.example.coditas.common.factorydesign.CategoryFactory;
import com.example.coditas.common.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryFactory categoryFactory;

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<CategoryResponseDto>>> getCategories(
            @ModelAttribute GenericFilterDto filter,
            @RequestParam String type,
            @ModelAttribute PageableDto page) {

        CategoryService service = categoryFactory.getCategoryService(type);
        Page<CategoryResponseDto> data = service.searchCategories(filter, page);
        return ResponseEntity.ok(ApiResponseDto.paged(data, page.getPage(), page.getSize(), data.getTotalElements()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<Page<CategoryResponseDto>>> search(
            @RequestParam String q, @RequestParam String type, @ModelAttribute PageableDto page) {

        CategoryService service = categoryFactory.getCategoryService(type);
        Page<CategoryResponseDto> data = service.globalSearch(q, page);
        return ResponseEntity.ok(ApiResponseDto.paged(data, page.getPage(), page.getSize(), data.getTotalElements()));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<CategoryResponseDto>> create(
            @Valid @ModelAttribute CategoryRequestDto dto, @RequestParam String type) {

        CategoryService service = categoryFactory.getCategoryService(type);
        CategoryResponseDto data = service.createCategory(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.ok(data, "Category created successfully"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CategoryResponseDto>> update(
            @PathVariable Long id,
            @Valid @ModelAttribute CategoryRequestDto dto,
            @RequestParam String type) {

        CategoryService service = categoryFactory.getCategoryService(type);
        CategoryResponseDto data = service.updateCategory(id, dto);
        return ResponseEntity.ok(ApiResponseDto.ok(data, "Category updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> delete(@PathVariable Long id, @RequestParam String type) {

        CategoryService service = categoryFactory.getCategoryService(type);
        String msg = service.deleteCategory(id);
        return ResponseEntity.ok(ApiResponseDto.ok(msg, "Success"));
    }
}
