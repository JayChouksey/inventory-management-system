package com.example.coditas.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDto {

    @NotBlank(message = "Refresh token is required.")
    String refreshToken;
}
