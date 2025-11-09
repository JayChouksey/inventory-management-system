package com.example.coditas.factory.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// FactoryUpdateRequestDto.java
@Getter
@Setter
@NoArgsConstructor
public class FactoryUpdateRequestDto {

    private String name;
    private String centralOfficeId;
    private String city;
    private String address;
    private String plantHeadId;
}
