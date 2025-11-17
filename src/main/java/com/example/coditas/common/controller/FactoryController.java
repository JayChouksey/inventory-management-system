package com.example.coditas.common.controller;

import com.example.coditas.common.dto.ApiResponseDto;
import com.example.coditas.common.dto.GenericFilterDto;
import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.factory.dto.FactoryCreateRequestDto;
import com.example.coditas.factory.dto.FactoryResponseDto;
import com.example.coditas.factory.dto.FactoryUpdateRequestDto;
import com.example.coditas.factory.service.FactoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/factories")
@RequiredArgsConstructor
public class FactoryController {

    private final FactoryService factoryService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<FactoryResponseDto>> create(
            @Valid @RequestBody FactoryCreateRequestDto dto) {

        FactoryResponseDto data = factoryService.createFactory(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDto.ok(data, "Factory created successfully"));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseDto<FactoryResponseDto>> update(
            @PathVariable String id,
            @Valid @RequestBody FactoryUpdateRequestDto dto) {

        FactoryResponseDto data = factoryService.updateFactory(id, dto);
        return ResponseEntity.ok(ApiResponseDto.ok(data, "Factory updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> delete(@PathVariable String id) {
        String message = factoryService.softDeleteFactory(id);
        return ResponseEntity.ok(ApiResponseDto.ok(message, "Success"));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<FactoryResponseDto>>> getFactories(
            @ModelAttribute GenericFilterDto filter,
            @ModelAttribute PageableDto page) {

        Page<FactoryResponseDto> data = factoryService.searchFactories(filter, page);
        return ResponseEntity.ok(ApiResponseDto.paged(
                data, page.getPage(), page.getSize(), data.getTotalElements()
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<Page<FactoryResponseDto>>> globalSearch(
            @RequestParam String q,
            @ModelAttribute PageableDto page) {

        Page<FactoryResponseDto> data = factoryService.globalSearch(q, page);
        return ResponseEntity.ok(ApiResponseDto.paged(
                data, page.getPage(), page.getSize(), data.getTotalElements()
        ));
    }
}
