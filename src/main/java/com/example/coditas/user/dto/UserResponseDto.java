package com.example.coditas.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto {
    String userId;
    String name;
    String email;
    String phone;
    String imageUrl;
    String roleName;
    List<String> factory;
    String bay;
    String isActive;
}
