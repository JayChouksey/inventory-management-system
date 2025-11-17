package com.example.coditas.common.specification;

import com.example.coditas.tool.enums.Expensive;
import com.example.coditas.tool.enums.Perishable;
import org.springframework.util.StringUtils;

public final class EnumHelper {

    private EnumHelper() {}

    // Returns the enum constant or null if the string is empty
    public static Perishable toPerishable(String value) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return Perishable.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static Expensive toExpensive(String value) {
        if (!StringUtils.hasText(value)) return null;
        try {
            return Expensive.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
