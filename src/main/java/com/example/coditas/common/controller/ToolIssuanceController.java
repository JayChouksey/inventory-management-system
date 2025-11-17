package com.example.coditas.common.controller;

import com.example.coditas.common.dto.ApiResponseDto;
import com.example.coditas.common.dto.PageableDto;
import com.example.coditas.tool.dto.ApproveExtensionDto;
import com.example.coditas.tool.dto.ToolIssuanceResponseDto;
import com.example.coditas.tool.service.ToolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tool-issuance")
@RequiredArgsConstructor
public class ToolIssuanceController {

    private final ToolService toolService;

    @PostMapping("/{issuanceId}/request-extension")
    public ResponseEntity<ApiResponseDto<Void>> requestExtension(@PathVariable Long issuanceId) {
        toolService.requestExtension(issuanceId);
        return ResponseEntity.ok(ApiResponseDto.ok(null, "Extension requested successfully. Awaiting supervisor approval."));
    }

    @PostMapping("/{issuanceId}/process-extension")
    public ResponseEntity<ApiResponseDto<ToolIssuanceResponseDto>> processExtension(
            @PathVariable Long issuanceId,
            @Valid @RequestBody ApproveExtensionDto dto) {
        ToolIssuanceResponseDto updatedIssuance = toolService.processExtensionRequest(issuanceId, dto);
        String message = Boolean.TRUE.equals(dto.getApproved()) ? "Extension approved successfully." : "Extension denied.";
        return ResponseEntity.ok(ApiResponseDto.ok(updatedIssuance, message));
    }

    @GetMapping("/overdue")
    public ResponseEntity<ApiResponseDto<Page<ToolIssuanceResponseDto>>> getOverdueTools(@ModelAttribute PageableDto pageableDto) {
        Page<ToolIssuanceResponseDto> overdueTools = toolService.getOverdueTools(pageableDto);
        return ResponseEntity.ok(ApiResponseDto.ok(overdueTools));
    }
}
