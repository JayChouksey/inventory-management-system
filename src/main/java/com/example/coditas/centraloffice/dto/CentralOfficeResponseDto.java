package com.example.coditas.centraloffice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CentralOfficeResponseDto {
    private String centralOfficeId;
    private String city;
    private String address;
    private String headName;
    private String headEmail;
    private String headImageUrl;
    private String status;
    private Long totalOrdersProcessed;
    private String createdOn;
}
