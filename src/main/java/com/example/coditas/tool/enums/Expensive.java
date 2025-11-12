package com.example.coditas.tool.enums;

import com.example.coditas.common.exception.CustomException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum Expensive {
    EXPENSIVE("EXPENSIVE"),
    INEXPENSIVE("INEXPENSIVE");

    private final String val;

    Expensive(String val) {
        this.val = val;
    }

    public static Expensive getType(String val) {
        return switch (val) {
            case "EXPENSIVE" -> EXPENSIVE;
            case "INEXPENSIVE" -> INEXPENSIVE;
            default -> throw new CustomException("Please enter a valid 'Expensive' type (EXPENSIVE or INEXPENSIVE).", HttpStatus.BAD_REQUEST);
        };
    }
}
