package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Builder pattern - Product. An Event can only be created through {@link EventBuilder}, which
 * validates every field before calling the private constructor. Instances are immutable;
 * editing works by copying an event into a new builder with {@link #toBuilder()}.
 */
public final class Event {
    private final String id;
    private final String name;
    private final LocalDate date;
    private final String venue;
    private final int capacity;
    private final BigDecimal unitPrice;

    Event(EventBuilder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.date = builder.date;
        this.venue = builder.location;
        this.capacity = builder.capacity;
        this.unitPrice = builder.price;
    }

    public static EventBuilder builder() {
        return new EventBuilder();
    }

    /** Starts a builder pre-filled with this event's values, keeping the same id. */
    public EventBuilder toBuilder() {
        return new EventBuilder().id(id).name(name).date(date).location(venue)
                .capacity(capacity).price(unitPrice);
    }

    public String id() { return id; }
    public String name() { return name; }
    public LocalDate date() { return date; }
    public String venue() { return venue; }
    public int capacity() { return capacity; }
    public BigDecimal unitPrice() { return unitPrice; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Event event)) return false;
        return capacity == event.capacity && id.equals(event.id) && name.equals(event.name)
                && date.equals(event.date) && venue.equals(event.venue) && unitPrice.equals(event.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, date, venue, capacity, unitPrice);
    }

    @Override
    public String toString() { return name + " · " + date; }
}
