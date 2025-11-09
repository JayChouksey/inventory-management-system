package com.example.coditas.common.controller;

import com.example.coditas.appuser.dto.*;
import com.example.coditas.appuser.service.AuthService;
import com.example.coditas.appuser.service.UserService;
import com.example.coditas.common.dto.ApiResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> loginUser(@Valid @RequestBody LoginRequestDto request) {

        LoginResponseDto data = authService.loginUser(request);

        ApiResponseDto<LoginResponseDto> responseBody = ApiResponseDto.ok(data, "User logged in successfully");

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signup(
            @Valid @ModelAttribute DistributorSignUpDto dto
    ){

        String data = userService.createDistributor(dto);
        ApiResponseDto<String> responseBody = ApiResponseDto.ok(data, "Success");

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/refresh")
    public ResponseEntity<ApiResponseDto<RefreshTokenDto>> getRefreshToken(@RequestBody @Valid RefreshTokenRequestDto payload) {
        RefreshTokenDto data = authService.getRefreshToken(payload);

        ApiResponseDto<RefreshTokenDto> responseBody = ApiResponseDto.ok(data, "New Access Token fetched successfully");

        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<String>> logoutUser(@RequestBody @Valid RefreshTokenRequestDto payload) {
        String data = authService.logoutUser(payload);

        ApiResponseDto<String> responseBody = ApiResponseDto.ok(data, "User logged out successfully");

        return ResponseEntity.ok(responseBody);
    }
}
