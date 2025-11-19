package com.example.coditas.common.util;

public class TempPasswordGenerator {

    private TempPasswordGenerator() {
    }

    public static String generatePassword(String name, String phone) {
        if (name == null || phone == null) {
            throw new IllegalArgumentException("User name and phone are required");
        }

        String[] nameParts = name.trim().split("\\s+");
        StringBuilder nameBuilder = new StringBuilder();

        // Take first & last name (or all if single name)
        if (nameParts.length >= 2) {
            nameBuilder.append(capitalize(nameParts[0]))
                    .append(capitalize(nameParts[nameParts.length - 1]));
        } else if (nameParts.length == 1) {
            nameBuilder.append(capitalize(nameParts[0]));
        }

        // Clean phone: remove non-digits
        String cleanPhone = phone.replaceAll("\\D", "");

        // truncate phone to last 10 digits
        if (cleanPhone.length() > 10) {
            cleanPhone = cleanPhone.substring(cleanPhone.length() - 10);
        }

        return nameBuilder + cleanPhone;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
