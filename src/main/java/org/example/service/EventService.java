package org.example.service;

import org.example.model.*;
import org.example.storage.InMemoryEventStore;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public final class EventService {
    private final InMemoryEventStore store;
    private final Clock clock;

    public EventService(InMemoryEventStore store) { this(store, Clock.systemDefaultZone()); }
    public EventService(InMemoryEventStore store, Clock clock) {
        this.store = Objects.requireNonNull(store);
        this.clock = Objects.requireNonNull(clock);
    }

    /** Builder pattern - Director role: the GUI's form values are assembled into an Event here. */
    public Event createEvent(String name, LocalDate date, String venue, int capacity, BigDecimal price) {
        Event event = Event.builder().clock(clock)
                .name(name).date(date).location(venue).capacity(capacity).price(price)
                .build();
        store.addEvent(event);
        return event;
    }
    public Participant addParticipant(String name) {
        Participant participant = new Participant(UUID.randomUUID().toString(), name);
        store.addParticipant(participant);
        return participant;
    }
    public Summary getSummary(String eventId) {
        synchronized (store) { return Summary.of(store.event(eventId), store.bookings(eventId)); }
    }
}
