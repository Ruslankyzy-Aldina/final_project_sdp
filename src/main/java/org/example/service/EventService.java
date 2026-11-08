package org.example.service;

import org.example.model.*;
import org.example.storage.InMemoryEventStore;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class EventService {
    private final InMemoryEventStore store;
    public EventService(InMemoryEventStore store) { this.store = store; }
    public Event createEvent(String name, LocalDate date, String venue, int capacity, BigDecimal price) {
        Event event = new Event(UUID.randomUUID().toString(), name, date, venue, capacity, price);
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
