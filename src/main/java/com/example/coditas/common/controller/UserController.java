package com.example.coditas.common.controller;

import com.example.coditas.user.dto.*;
import com.example.coditas.user.service.UserService;
import com.example.coditas.common.dto.ApiResponseDto;
import com.example.coditas.common.dto.PageableDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<UserResponseDto>> create(
            @Valid @ModelAttribute UserCreateRequestDto dto
    ){

        UserResponseDto data = userService.createUser(dto);
        ApiResponseDto<UserResponseDto> responseBody = ApiResponseDto.ok(data, "Success");

        return ResponseEntity.ok(responseBody);
    }

    @PatchMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateUser(
            @PathVariable String userId,
            @Valid @ModelAttribute UserUpdateRequestDto dto) {

        UserResponseDto data = userService.updateUser(userId, dto);
        ApiResponseDto<UserResponseDto> responseBody = ApiResponseDto.ok(data, "User updated successfully");

        return ResponseEntity.ok(responseBody);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<String>> softDeleteUser(
            @PathVariable String userId) {  // assuming you have auth

        String data = userService.softDeleteUser(userId);
        ApiResponseDto<String> responseBody = ApiResponseDto.ok(data, "Success");

        return ResponseEntity.ok(responseBody);
    }

    // Owner – Full employee list (filter + search + page + sort)
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<UserResponseDto>>> getEmployees(
            @ModelAttribute UserFilterDto filter, @RequestParam(value = "q", required = false) String query,
            @ModelAttribute PageableDto page) {

        Page<UserResponseDto> data = userService.searchUsers(filter, query, page);
        return ResponseEntity.ok(ApiResponseDto.paged(
                data, page.getPage(), page.getSize(), data.getTotalElements()
        ));
    }

    // Global text search
    @GetMapping("/search")
    public ResponseEntity<ApiResponseDto<Page<UserResponseDto>>> globalSearch(
            @RequestParam String q,
            @ModelAttribute PageableDto page) {

        Page<UserResponseDto> data = userService.globalSearch(q, page);
        return ResponseEntity.ok(ApiResponseDto.paged(
                data, page.getPage(), page.getSize(), data.getTotalElements()
        ));
    }

    // Plant Head – My factory workers
    @GetMapping("/factory/{factoryId}/workers")
    public ResponseEntity<ApiResponseDto<Page<UserResponseDto>>> getFactoryWorkers(
            @PathVariable String factoryId,
            @ModelAttribute PageableDto page) {

        Page<UserResponseDto> data = userService.getFactoryWorkers(factoryId, page);
        return ResponseEntity.ok(ApiResponseDto.paged(
                data, page.getPage(), page.getSize(), data.getTotalElements()
        ));
    }

    // Chief Supervisor – My bay workers
    @GetMapping("/bay/{bayId}/workers")
    public ResponseEntity<ApiResponseDto<Page<UserResponseDto>>> getBayWorkers(
            @PathVariable String bayId,
            @ModelAttribute PageableDto page) {

        Page<UserResponseDto> data = userService.getBayWorkers(bayId, page);
        return ResponseEntity.ok(ApiResponseDto.paged(
                data, page.getPage(), page.getSize(), data.getTotalElements()
        ));
    }

    // Central Office – All dealers
    @GetMapping("/dealers")
    public ResponseEntity<ApiResponseDto<Page<UserResponseDto>>> getDealers(
            @ModelAttribute PageableDto page) {

        Page<UserResponseDto> data = userService.getAllDealers(page);
        return ResponseEntity.ok(ApiResponseDto.paged(
                data, page.getPage(), page.getSize(), data.getTotalElements()
        ));
    }

    // Dashboard – Summary counts
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponseDto<AdminDashboardDto>> getDashboardDashboard() {
        AdminDashboardDto data = userService.getAdminDashboardData();
        return ResponseEntity.ok(ApiResponseDto.ok(data, "Admin dashboard data fetched successfully"));
    }
}
