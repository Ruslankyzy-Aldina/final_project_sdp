package org.example.model;

import org.example.validation.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builder pattern - Builder. Collects event fields step by step (in any order, as the user fills
 * the form) and validates them together in {@link #build()}. All problems are reported at once
 * through a single {@link ValidationException}.
 */
public final class EventBuilder {
    public static final int MAX_NAME_LENGTH = 100;

    String id;
    String name;
    LocalDate date;
    String location;
    int capacity;
    BigDecimal price = BigDecimal.ZERO;
    private Clock clock = Clock.systemDefaultZone();

    EventBuilder() { }

    public EventBuilder id(String id) { this.id = id; return this; }
    public EventBuilder name(String name) { this.name = name; return this; }
    public EventBuilder date(LocalDate date) { this.date = date; return this; }
    public EventBuilder location(String location) { this.location = location; return this; }
    public EventBuilder capacity(int capacity) { this.capacity = capacity; return this; }
    public EventBuilder price(BigDecimal price) { this.price = price; return this; }

    /** Clock used for the "date in the past" rule; tests replace it to get a fixed "today". */
    public EventBuilder clock(Clock clock) { this.clock = clock; return this; }

    public Event build() {
        List<String> errors = new ArrayList<>();
        name = trimToNull(name);
        location = trimToNull(location);
        if (name == null) errors.add("Event name is required");
        else if (name.length() > MAX_NAME_LENGTH)
            errors.add("Event name must be at most " + MAX_NAME_LENGTH + " characters");
        if (location == null) errors.add("Location is required");
        if (date == null) errors.add("Event date is required");
        else if (date.isBefore(LocalDate.now(clock))) errors.add("Event date cannot be in the past");
        if (capacity <= 0) errors.add("Capacity must be greater than zero");
        if (price == null) errors.add("Ticket price is required");
        else if (price.signum() < 0) errors.add("Ticket price cannot be negative");
        if (!errors.isEmpty()) throw new ValidationException(errors);

        id = trimToNull(id) == null ? UUID.randomUUID().toString() : id.trim();
        price = price.setScale(2, RoundingMode.HALF_UP);
        return new Event(this);
    }

    private static String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
