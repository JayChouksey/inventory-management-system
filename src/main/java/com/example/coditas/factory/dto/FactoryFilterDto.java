package com.example.coditas.factory.dto;

import com.example.coditas.common.enums.ActiveStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class FactoryFilterDto {
    private String name;
    private String city;
    private String plantHeadName;
    private Long centralOfficeId;
    private ActiveStatus status;
    private LocalDate startDate;  // createdAt >=
    private LocalDate endDate;    // createdAt <=
}
