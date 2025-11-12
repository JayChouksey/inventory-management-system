package com.example.coditas.tool.enums;

import com.example.coditas.common.exception.CustomException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum Perishable {
    PERISHABLE("PERISHABLE"),
    NON_PERISHABLE("NON_PERISHABLE");

    private final String val;

    Perishable(String val) {
        this.val = val;
    }

    public static Perishable getType(String val) {
        return switch (val) {
            case "PERISHABLE" -> PERISHABLE;
            case "NON_PERISHABLE" -> NON_PERISHABLE;
            default -> throw new CustomException("Please enter a valid 'Perishable' type (PERISHABLE or NON_PERISHABLE).", HttpStatus.BAD_REQUEST);
        };
    }
}
