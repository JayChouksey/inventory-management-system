package com.example.coditas.common.controller;

import com.example.coditas.centraloffice.dto.CentralOfficeCreateRequestDto;
import com.example.coditas.centraloffice.dto.CentralOfficeFilterDto;
import com.example.coditas.centraloffice.dto.CentralOfficeResponseDto;
import com.example.coditas.centraloffice.dto.CentralOfficeUpdateRequestDto;
import com.example.coditas.centraloffice.service.CentralOfficeService;
import com.example.coditas.common.dto.ApiResponseDto;
import com.example.coditas.common.dto.PageableDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/central-offices")
@RequiredArgsConstructor
@Slf4j
public class CentralOfficeController {

    private final CentralOfficeService service;

    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<CentralOfficeResponseDto>>> getOffices(
            @ModelAttribute CentralOfficeFilterDto filter,
            @ModelAttribute PageableDto page) {
        Page<CentralOfficeResponseDto> data = service.searchOffices(filter, page);
        return ResponseEntity.ok(ApiResponseDto.paged(data, page.getPage(), page.getSize(), data.getTotalElements()));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<Page<CentralOfficeResponseDto>>> search(
            @RequestParam String q, @ModelAttribute PageableDto page) {
        Page<CentralOfficeResponseDto> data = service.globalSearch(q, page);
        return ResponseEntity.ok(ApiResponseDto.paged(data, page.getPage(), page.getSize(), data.getTotalElements()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CentralOfficeResponseDto>> getDetail(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponseDto.ok(service.getOfficeDetail(id), "Office details"));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<CentralOfficeResponseDto>> create(
            @Valid @ModelAttribute CentralOfficeCreateRequestDto dto) {
        CentralOfficeResponseDto data = service.createOffice(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.ok(data, "Central Office Created Successfully"));
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<CentralOfficeResponseDto>> update(
            @PathVariable String id,
            @Valid @ModelAttribute CentralOfficeUpdateRequestDto dto) {
        CentralOfficeResponseDto data = service.updateOffice(id, dto);
        return ResponseEntity.ok(ApiResponseDto.ok(data, "Central Office updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> delete(@PathVariable String id) {
        String msg = service.deleteOffice(id);
        return ResponseEntity.ok(ApiResponseDto.ok(msg, "Success"));
    }
}
