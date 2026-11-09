package org.example.model;

import org.example.validation.ValidationException;

final class Validation {
    private Validation() { }
    static String required(String value, String label) {
        if (value == null || value.isBlank()) throw new ValidationException(label + " is required");
        return value.trim();
    }
}
