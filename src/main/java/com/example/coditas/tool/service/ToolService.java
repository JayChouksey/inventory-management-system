package com.example.coditas.tool.service;

import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.common.exception.CustomException;
import com.example.coditas.common.util.CloudinaryService;
import com.example.coditas.tool.dto.ToolCreateRequestDto;
import com.example.coditas.tool.dto.ToolFilterDto;
import com.example.coditas.tool.dto.ToolResponseDto;
import com.example.coditas.tool.dto.ToolUpdateRequestDto;
import com.example.coditas.tool.entity.Tool;
import com.example.coditas.tool.entity.ToolCategory;
import com.example.coditas.tool.enums.Perishable;
import com.example.coditas.tool.repository.ToolCategoryRepository;
import com.example.coditas.tool.repository.ToolRepository;
import com.example.coditas.tool.repository.ToolSpecifications;
import com.example.coditas.tool.repository.ToolStockRepository;
import jakarta.persistence.EntityManager;
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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ToolService {

    private final ToolRepository toolRepository;
    private final ToolCategoryRepository categoryRepository;
    private final ToolStockRepository toolStockRepository;
    private final CloudinaryService cloudinaryService;
    private final EntityManager entityManager;

    public Page<ToolResponseDto> searchTools(ToolFilterDto filter, PageableDto pageReq) {
        Specification<Tool> spec = ToolSpecifications.withFilters(filter);
        Pageable pageable = toPageableForTool(pageReq);
        Page<Tool> page = toolRepository.findAll(spec, pageable);
        return page.map(this::toDto);
    }

    public Page<ToolResponseDto> globalSearch(String q, PageableDto pageReq) {
        Specification<Tool> spec = ToolSpecifications.globalSearch(q);
        Pageable pageable = toPageableForTool(pageReq);
        Page<Tool> page = toolRepository.findAll(spec, pageable);
        return page.map(this::toDto);
    }

    public ToolResponseDto getToolDetail(String id) {
        Tool tool = toolRepository.findByToolId(id)
                .orElseThrow(() -> new CustomException("Tool not found", HttpStatus.NOT_FOUND));
        return toDto(tool);
    }

    @Transactional
    public ToolResponseDto createTool(ToolCreateRequestDto dto) {
        if (toolRepository.existsByNameIgnoreCase(dto.getName().trim())) {
            throw new CustomException("Tool name already exists", HttpStatus.CONFLICT);
        }

        ToolCategory category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new CustomException("Category not found", HttpStatus.NOT_FOUND));

        String imageUrl = cloudinaryService.uploadFile(dto.getImage());
        String toolId = generateUniqueToolId();

        Tool tool = Tool.builder()
                .toolId(toolId)
                .name(dto.getName().trim())
                .category(category)
                .imageUrl(imageUrl)
                .isPerishable(dto.getIsPerishable())
                .isExpensive(dto.getIsExpensive())
                .threshold(dto.getThreshold() != null ? dto.getThreshold() : 0)
                .build();

        tool = toolRepository.saveAndFlush(tool);
        entityManager.refresh(tool);

        log.info("Tool created: {} ({})", tool.getName(), tool.getToolId());
        return toDto(tool);
    }

    @Transactional
    public ToolResponseDto updateTool(String id, ToolUpdateRequestDto dto) {
        Tool tool = toolRepository.findByToolId(id)
                .orElseThrow(() -> new CustomException("Tool not found", HttpStatus.NOT_FOUND));

        if (dto.getName() != null && !dto.getName().trim().isBlank()) {
            if (!dto.getName().trim().equalsIgnoreCase(tool.getName())
                    && toolRepository.existsByNameIgnoreCase(dto.getName().trim())) {
                throw new CustomException("Tool name already exists", HttpStatus.CONFLICT);
            }
            tool.setName(dto.getName().trim());
        }
        if (dto.getCategoryId() != null) {
            ToolCategory cat = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new CustomException("Category not found", HttpStatus.NOT_FOUND));
            tool.setCategory(cat);
        }
        if (dto.getIsPerishable() != null) tool.setIsPerishable(dto.getIsPerishable());
        if (dto.getIsExpensive() != null) tool.setIsExpensive(dto.getIsExpensive());
        if (dto.getThreshold() != null) tool.setThreshold(dto.getThreshold());
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            if (tool.getImageUrl() != null) {
                String publicId = cloudinaryService.extractPublicIdFromUrl(tool.getImageUrl());
                cloudinaryService.deleteFile(publicId);
            }
            tool.setImageUrl(cloudinaryService.uploadFile(dto.getImage()));
        }

        tool.setUpdatedAt(ZonedDateTime.now());
        tool = toolRepository.save(tool);

        return toDto(tool);
    }

    @Transactional
    public String deleteTool(String id) {
        Tool tool = toolRepository.findByToolId(id)
                .orElseThrow(() -> new CustomException("Tool not found", HttpStatus.NOT_FOUND));
        toolRepository.delete(tool);
        log.info("Tool deleted: {} ({})", tool.getName(), tool.getToolId());
        return "Tool deleted successfully!";
    }

    private String generateUniqueToolId() {
        long count = toolRepository.count();
        return String.format("TOOL-%04d", count + 1);
    }

    private Pageable toPageableForTool(PageableDto dto) {
        String field = switch (dto.getSortBy().toLowerCase()) {
            case "name" -> "name";
            case "category" -> "category.name";
            case "perishable" -> "isPerishable";
            case "expensive" -> "isExpensive";
            case "threshold" -> "threshold";
            case "created_on", "createdat" -> "createdAt";
            default -> "createdAt";
        };
        Sort sort = "desc".equalsIgnoreCase(dto.getSortDir())
                ? Sort.by(field).descending()
                : Sort.by(field).ascending();
        return PageRequest.of(dto.getPage(), dto.getSize(), sort);
    }

    private ToolResponseDto toDto(Tool t) {
        Integer total = toolStockRepository.sumTotalQuantityByToolId(t.getId());
        Integer available = toolStockRepository.sumAvailableQuantityByToolId(t.getId());
        if (total == null) total = 0;
        if (available == null) available = 0;

        String stockStatus = available >= t.getThreshold() ? "IN_STOCK"
                : available > 0 ? "LOW"
                : "OUT_OF_STOCK";

        if (t.getIsPerishable() == Perishable.PERISHABLE && available <= 5) {
            stockStatus = "CRITICAL";
        }

        return ToolResponseDto.builder()
                .toolId(t.getToolId())
                .name(t.getName())
                .imageUrl(t.getImageUrl())
                .categoryName(t.getCategory().getName())
                .perishable(t.getIsPerishable().name())
                .expensive(t.getIsExpensive().name())
                .threshold(t.getThreshold())
                .totalStock(total)
                .availableStock(available)
                .stockStatus(stockStatus)
                .createdOn(t.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                .build();
    }
}