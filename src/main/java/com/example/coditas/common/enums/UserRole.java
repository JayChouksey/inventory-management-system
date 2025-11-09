package com.example.coditas.common.enums;

import com.example.coditas.common.exception.CustomException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserRole {
    ADMIN("ADMIN"),
    PLANT_HEAD("PLANT_HEAD"),
    CHIEF_SUPERVISOR("CHIEF_SUPERVISOR"),
    WORKER("WORKER"),
    CENTRAL_OFFICE_HEAD("CENTRAL_OFFICE_HEAD"),
    DEALER("DEALER"),
    CUSTOMER("CUSTOMER");

    final String val;

    UserRole(String val) {
        this.val = val;
    }

    public static UserRole getRole(String val){
        return switch (val){
            case "PLANT_HEAD" -> PLANT_HEAD;
            case "CHIEF_SUPERVISOR" -> CHIEF_SUPERVISOR;
            case "WORKER" -> WORKER;
            case "CENTRAL_OFFICE_HEAD" -> CENTRAL_OFFICE_HEAD;
            case "CUSTOMER" -> CUSTOMER;
            default -> throw new CustomException("Please enter valid role.", HttpStatus.BAD_REQUEST);
        };
    }
}
