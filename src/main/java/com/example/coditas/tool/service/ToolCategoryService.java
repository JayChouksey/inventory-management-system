package com.example.coditas.tool.service;

import com.example.coditas.common.dto.*;
import com.example.coditas.common.exception.CustomException;
import com.example.coditas.common.service.CategoryService;
import com.example.coditas.common.specification.GenericFilterSpecFactory;
import com.example.coditas.tool.entity.ToolCategory;
import com.example.coditas.tool.repository.ToolCategoryRepository;
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

@Service("TOOL")
@RequiredArgsConstructor
@Slf4j
public class ToolCategoryService implements CategoryService {

    private final ToolCategoryRepository categoryRepository;

    public Page<CategoryResponseDto> searchCategories(GenericFilterDto filter, PageableDto pageReq) {
        Specification<ToolCategory> spec = GenericFilterSpecFactory.forToolCategory(filter);
        Pageable pageable = toPageable(pageReq);
        Page<ToolCategory> page = categoryRepository.findAll(spec, pageable);
        return page.map(this::toDto);
    }

    public Page<CategoryResponseDto> globalSearch(String q, PageableDto pageReq) {
        Specification<ToolCategory> spec = GenericFilterSpecFactory.globalSearch(
                new GenericFilterDto(){{setName(q);}},
                "name"
        );
        Pageable pageable = toPageable(pageReq);
        Page<ToolCategory> page = categoryRepository.findAll(spec, pageable);
        return page.map(this::toDto);
    }

    @Transactional
    public CategoryResponseDto createCategory(CategoryRequestDto dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new CustomException("Category name already exists", HttpStatus.CONFLICT);
        }

        ToolCategory category = ToolCategory.builder()
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .build();

        category = categoryRepository.save(category);
        log.info("Tool category created: {}", category.getName());
        return toDto(category);
    }

    @Transactional
    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto dto) {
        ToolCategory category = categoryRepository.findById(id)
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
        log.info("Tool category updated: {}", category.getName());
        return toDto(category);
    }

    @Transactional
    public String deleteCategory(Long id) {
        ToolCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new CustomException("Category not found", HttpStatus.NOT_FOUND));

        Long toolCount = categoryRepository.countToolsByCategoryId(id);
        if (toolCount > 0) {
            throw new CustomException(
                    String.format("Cannot delete category. %d tools are using it.", toolCount),
                    HttpStatus.BAD_REQUEST
            );
        }

        categoryRepository.delete(category);
        log.info("Tool category deleted: {}", category.getName());
        return "Category deleted successfully!";
    }

    private Pageable toPageable(PageableDto dto) {
        String field = switch (dto.getSortBy().toLowerCase()) {
            case "name" -> "name";
            case "tools", "tool_count" -> "id"; // sorted in service
            case "created_on" -> "id";
            default -> "id";
        };
        Sort sort = "desc".equalsIgnoreCase(dto.getSortDir())
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }

    private CategoryResponseDto toDto(ToolCategory c) {
        Long toolCount = categoryRepository.countToolsByCategoryId(c.getId());
        return CategoryResponseDto.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .toolCount(toolCount)
                .createdOn(c.getId() != null ? "Active" : "N/A") // fallback
                .build();
    }
}
