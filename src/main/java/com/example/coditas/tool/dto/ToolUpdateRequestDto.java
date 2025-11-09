package com.example.coditas.tool.dto;

import com.example.coditas.tool.enums.Expensive;
import com.example.coditas.tool.enums.Perishable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class ToolUpdateRequestDto {
    private String name;
    private Long categoryId;
    private Perishable isPerishable;
    private Expensive isExpensive;
    private Integer threshold;
    private MultipartFile image;
}
