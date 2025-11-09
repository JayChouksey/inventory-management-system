package com.example.coditas.tool.dto;

import com.example.coditas.tool.enums.Expensive;
import com.example.coditas.tool.enums.Perishable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class ToolCreateRequestDto {
    @NotBlank
    private String name;
    @NotNull
    private Long categoryId;
    @NotNull private Perishable isPerishable;
    @NotNull private Expensive isExpensive;
    private Integer threshold;
    @NotNull private MultipartFile image;
}
