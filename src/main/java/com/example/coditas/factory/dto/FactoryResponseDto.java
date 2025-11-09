package com.example.coditas.factory.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FactoryResponseDto {
    private String factoryId;
    private String name;
    private String city;
    private String address;
    private String plantHeadName;
    private String status;
    private String createdOn;
}
