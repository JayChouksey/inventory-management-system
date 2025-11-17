package com.example.coditas.common.enums;

import com.example.coditas.common.exception.CustomException;
import org.springframework.http.HttpStatus;

public enum ActiveStatus {
    ACTIVE(), INACTIVE();

    public static ActiveStatus getType(String val) {
        return switch (val) {
            case "ACTIVE" -> ACTIVE;
            case "INACTIVE" -> INACTIVE;
            default -> throw new CustomException("Please enter a valid 'Active Status' type (ACTIVE or INACTIVE).", HttpStatus.BAD_REQUEST);
        };
    }
}
