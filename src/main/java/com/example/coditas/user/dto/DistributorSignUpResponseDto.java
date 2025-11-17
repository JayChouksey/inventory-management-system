package com.example.coditas.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistributorSignUpResponseDto {
    String userId;
    String name;
    String email;
    String phone;
}
