package com.example.coditas.product.service;

import com.example.coditas.common.dto.*;
import com.example.coditas.common.exception.CustomException;
import com.example.coditas.common.service.CategoryService;
import com.example.coditas.common.specification.GenericFilterSpecFactory;
import com.example.coditas.product.entity.ProductCategory;
import com.example.coditas.product.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("PRODUCT")
@RequiredArgsConstructor
@Slf4j
public class ProductCategoryService implements CategoryService {

    private final ProductCategoryRepository categoryRepository;

    public Page<CategoryResponseDto> searchCategories(GenericFilterDto filter, PageableDto pageReq) {
        Specification<ProductCategory> spec = GenericFilterSpecFactory.forProductCategory(filter);
        Pageable pageable = toPageable(pageReq);
        Page<ProductCategory> page = categoryRepository.findAll(spec, pageable);
        return page.map(this::toDto);
    }

    public Page<CategoryResponseDto> globalSearch(String q, PageableDto pageReq) {
        Specification<ProductCategory> spec = GenericFilterSpecFactory.globalSearch(
                new GenericFilterDto(){{setName(q);}},
                "name"
        );
        Pageable pageable = toPageable(pageReq);
        Page<ProductCategory> page = categoryRepository.findAll(spec, pageable);
        return page.map(this::toDto);
    }

    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new CustomException("Category name already exists", HttpStatus.CONFLICT);
        }

        ProductCategory category = ProductCategory.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .build();

        category = categoryRepository.save(category);
        log.info("Product category created: {}", category.getName());
        return toDto(category);
    }

    @Transactional
    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Category not found", HttpStatus.NOT_FOUND));

        if (dto.getName() != null && !dto.getName().trim().isBlank()) {
            if (!dto.getName().trim().equalsIgnoreCase(category.getName())
                    && categoryRepository.existsByNameIgnoreCaseAndIdNot(dto.getName().trim(), id)) {
                throw new CustomException("Category name already exists", HttpStatus.CONFLICT);
            }
            category.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }

        category = categoryRepository.save(category);
        log.info("Product category updated: {}", category.getName());
        return toDto(category);
    }

    @Transactional
    public String deleteCategory(Long id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Category not found", HttpStatus.NOT_FOUND));


        categoryRepository.delete(category);
        log.info("Product category deleted: {}", category.getName());
        return "Category deleted successfully!";
    }

    private Pageable toPageable(PageableDto dto) {
        String field = switch (dto.getSortBy().toLowerCase()) {
            case "name" -> "name";
            case "products", "product_count" -> "id"; // sorted in service
            case "created_on" -> "id";
            default -> "id";
        };
        Sort sort = "desc".equalsIgnoreCase(dto.getSortDir())
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }

    private CategoryResponseDto toDto(ProductCategory c) {
        return CategoryResponseDto.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .createdOn(c.getId() != null ? "Active" : "N/A") // fallback
                .build();
    }
}
