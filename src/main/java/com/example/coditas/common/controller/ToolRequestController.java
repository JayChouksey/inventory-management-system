package com.example.coditas.common.controller;

import com.example.coditas.common.dto.ApiResponseDto;
import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.common.exception.CustomException;
import com.example.coditas.tool.dto.ToolIssuanceResponseDto;
import com.example.coditas.tool.dto.ToolRequestCreateDto;
import com.example.coditas.tool.dto.ToolRequestResponseDto;
import com.example.coditas.tool.service.ToolRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tool-requests")
@RequiredArgsConstructor
public class ToolRequestController {

    private final ToolRequestService toolRequestService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<ToolRequestResponseDto>> createRequest(@Valid @RequestBody ToolRequestCreateDto dto) {
        ToolRequestResponseDto createdRequest = toolRequestService.createToolRequest(dto);
        return new ResponseEntity<>(ApiResponseDto.ok(createdRequest, "Tool request created successfully."), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponseDto<List<ToolIssuanceResponseDto>>> approveRequest(@PathVariable String id) {
        List<ToolIssuanceResponseDto> issuance = toolRequestService.approveAndIssueToolRequest(id);
        return ResponseEntity.ok(ApiResponseDto.ok(issuance, "Request approved and tools issued successfully."));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponseDto<Void>> rejectRequest(@PathVariable String id, @RequestBody Map<String, String> body) {
        String comment = body.get("comment");
        if (comment == null || comment.isBlank()) {
            throw new CustomException("A comment is required when rejecting a request.", HttpStatus.BAD_REQUEST);
        }
        toolRequestService.rejectToolRequest(id, comment);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Request rejected successfully."));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<ToolRequestResponseDto>>> getAllRequests(
            @Valid PageableDto pageReq) {
        Page<ToolRequestResponseDto> page = toolRequestService.getAllToolRequests(pageReq);
        return ResponseEntity.ok(ApiResponseDto.ok(page, "Tool requests fetched"));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponseDto<Page<ToolRequestResponseDto>>> getMyRequests(
            @Valid PageableDto pageReq) {
        Page<ToolRequestResponseDto> page = toolRequestService.getMyToolRequests(pageReq);
        return ResponseEntity.ok(ApiResponseDto.ok(page, "My tool requests"));
    }

    @GetMapping("/{requestNumber}")
    public ResponseEntity<ApiResponseDto<ToolRequestResponseDto>> getRequestByNumber(
            @PathVariable String requestNumber) {
        ToolRequestResponseDto result = toolRequestService.getToolRequestByNumber(requestNumber);
        return ResponseEntity.ok(ApiResponseDto.ok(result, "Request details"));
    }
}
