package org.example.validation;

import java.util.List;

/**
 * Raised when user input is empty, out of range, or breaks an application rule.
 * Extends IllegalArgumentException so existing callers that handle invalid arguments keep working,
 * while the GUI can show every collected message at once.
 */
public class ValidationException extends IllegalArgumentException {
    private final List<String> errors;

    public ValidationException(String message) {
        this(List.of(message));
    }

    public ValidationException(List<String> errors) {
        super(String.join("\n", requireMessages(errors)));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }

    private static List<String> requireMessages(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            throw new IllegalStateException("ValidationException needs at least one message");
        }
        return errors;
    }
}
