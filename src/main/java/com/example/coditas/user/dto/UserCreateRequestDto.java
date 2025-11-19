package com.example.coditas.user.dto;

import com.example.coditas.common.validation.ValidImage;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class UserCreateRequestDto {

    @NotBlank(message = "Name is required")
    @Size(max = 255, message = "Name must be less than 255 characters")
    private String name;

    // TODO: Add a regex
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String email;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must contain 10–15 digits")
    private String phone;

    @ValidImage
    private MultipartFile image;

    @NotBlank(message = "Role is required")
    private String role;

    // TODO: Add factory and bay kinda thing
}
