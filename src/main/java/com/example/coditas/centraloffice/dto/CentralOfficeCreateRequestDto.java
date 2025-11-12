package com.example.coditas.centraloffice.dto;

import com.example.coditas.common.validation.ValidImage;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class CentralOfficeCreateRequestDto {

    @NotBlank
    private String city;
    private String address;

    private Long headId;           // existing user
    private String newHeadName;    // create new
    private String newHeadEmail;
    private String newHeadPhone;

    @ValidImage
    private MultipartFile newHeadImage;
}
