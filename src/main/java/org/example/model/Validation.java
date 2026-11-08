package org.example.model;

final class Validation {
    private Validation() { }
    static String required(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " is required");
        return value.trim();
    }
}
