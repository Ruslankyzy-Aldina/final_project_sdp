package org.example.ui;

import org.example.validation.ValidationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts raw form text into typed values. Format problems for every field are collected and
 * reported together; business rules (empty name, past date, ranges) stay in EventBuilder.
 */
final class FormInput {
    private final List<String> errors = new ArrayList<>();

    LocalDate date(String label, String raw) {
        if (raw == null || raw.isBlank()) { errors.add(label + " is required"); return null; }
        try { return LocalDate.parse(raw.trim()); }
        catch (DateTimeParseException ex) { errors.add(label + " must be a valid date in YYYY-MM-DD format"); return null; }
    }

    int wholeNumber(String label, String raw) {
        if (raw == null || raw.isBlank()) { errors.add(label + " is required"); return 0; }
        try { return Integer.parseInt(raw.trim()); }
        catch (NumberFormatException ex) { errors.add(label + " must be a whole number"); return 0; }
    }

    BigDecimal money(String label, String raw) {
        if (raw == null || raw.isBlank()) { errors.add(label + " is required"); return null; }
        try { return new BigDecimal(raw.trim().replace(',', '.')); }
        catch (NumberFormatException ex) { errors.add(label + " must be a number, for example 1500.00"); return null; }
    }

    void throwIfInvalid() {
        if (!errors.isEmpty()) throw new ValidationException(errors);
    }
}
