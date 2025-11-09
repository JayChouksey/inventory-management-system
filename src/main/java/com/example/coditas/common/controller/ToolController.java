package com.example.coditas.common.controller;

import com.example.coditas.common.dto.ApiResponseDto;
import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.tool.dto.ToolCreateRequestDto;
import com.example.coditas.tool.dto.ToolFilterDto;
import com.example.coditas.tool.dto.ToolResponseDto;
import com.example.coditas.tool.dto.ToolUpdateRequestDto;
import com.example.coditas.tool.service.ToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/tools")
@RequiredArgsConstructor
@Slf4j
public class ToolController {

    private final ToolService toolService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<ToolResponseDto>>> getTools(
            @ModelAttribute ToolFilterDto filter,
            @ModelAttribute PageableDto page) {
        Page<ToolResponseDto> data = toolService.searchTools(filter, page);
        return ResponseEntity.ok(ApiResponseDto.paged(data, page.getPage(), page.getSize(), data.getTotalElements()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<Page<ToolResponseDto>>> globalSearch(
            @RequestParam String q,
            @ModelAttribute PageableDto page) {
        Page<ToolResponseDto> data = toolService.globalSearch(q, page);
        return ResponseEntity.ok(ApiResponseDto.paged(data, page.getPage(), page.getSize(), data.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ToolResponseDto>> getDetail(@PathVariable String id) {
        ToolResponseDto data = toolService.getToolDetail(id);
        return ResponseEntity.ok(ApiResponseDto.ok(data, "Tool details"));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<ToolResponseDto>> create(
            @Valid @ModelAttribute ToolCreateRequestDto dto) {
        ToolResponseDto data = toolService.createTool(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.ok(data, "Tool created successfully"));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<ToolResponseDto>> update(
            @PathVariable String id,
            @Valid @ModelAttribute ToolUpdateRequestDto dto) {
        ToolResponseDto data = toolService.updateTool(id, dto);
        return ResponseEntity.ok(ApiResponseDto.ok(data, "Tool updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> delete(@PathVariable String id) {
        String msg = toolService.deleteTool(id);
        return ResponseEntity.ok(ApiResponseDto.ok(msg, "Success"));
    }
}
