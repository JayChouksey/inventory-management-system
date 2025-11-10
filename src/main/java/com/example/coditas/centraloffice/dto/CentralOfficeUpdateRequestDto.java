package com.example.coditas.centraloffice.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CentralOfficeUpdateRequestDto {
    private String city;
    private String address;
    private Long headId;
}
