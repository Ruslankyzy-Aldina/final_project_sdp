package org.example.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public record Event(String id, String name, LocalDate date, String venue,
                    int capacity, BigDecimal unitPrice) {
    public Event {
        id = Validation.required(id, "Event id");
        name = Validation.required(name, "Event name");
        venue = Validation.required(venue, "Venue");
        if (date == null) throw new IllegalArgumentException("Event date is required");
        if (capacity <= 0) throw new IllegalArgumentException("Capacity must be positive");
        if (unitPrice == null || unitPrice.signum() < 0)
            throw new IllegalArgumentException("Ticket price must be zero or positive");
        unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
    }

    @Override public String toString() { return name + " · " + date; }
}
