package com.example.coditas.user.dto;

import com.example.coditas.common.enums.ActiveStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserFilterDto {
    private String name;
    private String email;
    private String phone;
    private String roleId;
    private String factoryId;
    private String bayId;
    private ActiveStatus status;
}
