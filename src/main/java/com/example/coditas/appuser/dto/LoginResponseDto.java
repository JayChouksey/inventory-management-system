package com.example.coditas.appuser.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {
    private Long userId;
    private String email;
    private String name;
    String phone;
    private String roleName;
    private String refreshToken;
    private String accessToken;
}
