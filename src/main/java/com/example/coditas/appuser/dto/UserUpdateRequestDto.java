package com.example.coditas.appuser.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class UserUpdateRequestDto {
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone: 10–15 digits only")
    private String phone;

    private MultipartFile image;
}
