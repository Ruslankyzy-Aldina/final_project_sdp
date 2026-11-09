package org.example.service;

import org.example.model.*;
import org.example.option.ServiceExtra;
import org.example.storage.InMemoryEventStore;
import org.example.validation.ValidationException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
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

    /** Rebuilds the event through its builder; capacity may not drop below seats already reserved. */
    public Event updateEvent(String id, String name, LocalDate date, String venue, int capacity, BigDecimal price) {
        synchronized (store) {
            Event updated = store.event(id).toBuilder().clock(clock)
                    .name(name).date(date).location(venue).capacity(capacity).price(price)
                    .build();
            long reserved = Summary.of(updated, store.bookings(id)).reservedSeats();
            if (updated.capacity() < reserved)
                throw new ValidationException("Capacity cannot be lower than the " + reserved
                        + " seats already reserved. Cancel bookings first.");
            store.updateEvent(updated);
            // Catering is priced per guest, so a saved package is recalculated for the new capacity.
            store.servicePackage(id).ifPresent(saved ->
                    store.savePackage(ServicePackage.configure(updated, Set.copyOf(saved.extras()))));
            return updated;
        }
    }

    /** Only events without active bookings can be deleted, so no participant loses a seat silently. */
    public void deleteEvent(String id) {
        synchronized (store) {
            store.event(id);
            boolean hasActive = store.bookings(id).stream().anyMatch(BookingDetails::active);
            if (hasActive)
                throw new ValidationException("This event has active bookings. Cancel them before deleting the event.");
            store.removeEvent(id);
        }
    }

    public ServicePackage previewPackage(String eventId, Set<ServiceExtra> extras) {
        return ServicePackage.configure(store.event(eventId), extras);
    }

    public ServicePackage configurePackage(String eventId, Set<ServiceExtra> extras) {
        synchronized (store) {
            ServicePackage servicePackage = previewPackage(eventId, extras);
            store.savePackage(servicePackage);
            return servicePackage;
        }
    }

    /** The saved package, or the base package without extras if nothing was configured yet. */
    public ServicePackage getPackage(String eventId) {
        synchronized (store) {
            return store.servicePackage(eventId).orElseGet(() -> previewPackage(eventId, Set.of()));
        }
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
