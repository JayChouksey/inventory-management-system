package com.example.coditas.common.controller;

import com.example.coditas.common.dto.ApiResponseDto;
import com.example.coditas.common.dto.GenericFilterDto;
import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.common.exception.CustomException;
import com.example.coditas.tool.dto.*;
import com.example.coditas.tool.service.ToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tools")
@RequiredArgsConstructor
@Slf4j
public class ToolController {

    private final ToolService toolService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<ToolResponseDto>>> getTools(
            @ModelAttribute GenericFilterDto filter,
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

    // --- FACTORY STOCK MANAGEMENT (PLANT_HEAD) ---
    @PostMapping("/stock/factory/{factoryId}")
    public ResponseEntity<ApiResponseDto<ToolStockResponseDto>> addStockToFactory(
            @PathVariable String factoryId,
            @Valid @RequestBody AddToolStockDto dto) {
        ToolStockResponseDto updatedStock = toolService.addStockToFactory(factoryId, dto);
        return ResponseEntity.ok(ApiResponseDto.ok(updatedStock, "Stock added to factory successfully."));
    }

    // --- TOOL RETURN & CONFISCATION (CHIEF_SUPERVISOR) ---
    @PostMapping("/issuance/{issuanceId}/return")
    public ResponseEntity<ApiResponseDto<Void>> returnTool(
            @PathVariable Long issuanceId,
            @RequestBody Map<String, Long> body) {
        Long fitQuantity = body.get("fitQuantity");
        Long unfitQuantity = body.get("unfitQuantity");
        if (fitQuantity == null || unfitQuantity == null) {
            throw new CustomException("Both 'fitQuantity' and 'unfitQuantity' are required.", HttpStatus.BAD_REQUEST);
        }
        toolService.returnTool(issuanceId, fitQuantity, unfitQuantity);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Tool returned successfully."));
    }

    @PostMapping("/issuance/{issuanceId}/confiscate")
    public ResponseEntity<ApiResponseDto<Void>> confiscateTool(@PathVariable Long issuanceId) {
        toolService.confiscateTool(issuanceId);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Tool marked as confiscated successfully."));
    }

    // --- WORKER-CENTRIC VIEWS ---

    @GetMapping("/worker/{workerId}/issued")
    public ResponseEntity<ApiResponseDto<Page<ToolIssuanceResponseDto>>> getToolsByWorker(
            @PathVariable String workerId,
            @ModelAttribute PageableDto pageableDto) {
        Page<ToolIssuanceResponseDto> issuedTools = toolService.getMyIssuedTools(pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(issuedTools));
    }

}
