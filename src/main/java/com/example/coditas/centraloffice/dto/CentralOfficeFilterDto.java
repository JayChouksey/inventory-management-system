package com.example.coditas.centraloffice.dto;

import com.example.coditas.common.enums.ActiveStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CentralOfficeFilterDto {
    private String city;
    private String headName;
    private ActiveStatus status;
}