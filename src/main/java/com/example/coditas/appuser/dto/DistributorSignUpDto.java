package com.example.coditas.appuser.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class DistributorSignUpDto {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 30, message = "Name must be between 3 and 30 characters")
    @Pattern(
            regexp = "^[A-Z][a-z]+(?: [A-Z][a-z]+)*$",
            message = "Name must start with a capital letter, each word must begin with an uppercase letter followed by lowercase letters, and words should be separated by a single space."
    )
    private String name;

    @NotBlank(message = "Email is required")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "Invalid email format")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    private String password;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must contain 10–15 digits")
    private String phone;

    // Optional image upload
    private MultipartFile image;
}
