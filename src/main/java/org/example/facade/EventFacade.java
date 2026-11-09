package org.example.facade;

import org.example.booking.BookingStatus;
import org.example.export.*;
import org.example.model.*;
import org.example.option.ServiceExtra;
import org.example.service.*;
import org.example.storage.InMemoryEventStore;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** The only application entry point used by GUI actions. */
public final class EventFacade {
    private final InMemoryEventStore store;
    private final EventService events;
    private final BookingService bookings;
    private final ExporterFactory exporters;

    public EventFacade() { this(new InMemoryEventStore(), new DefaultExporterFactory()); }
    public EventFacade(InMemoryEventStore store, ExporterFactory exporters) {
        this.store = Objects.requireNonNull(store);
        this.exporters = Objects.requireNonNull(exporters);
        events = new EventService(store);
        bookings = new BookingService(store);
    }
    public Event createEvent(String name, LocalDate date, String venue, int capacity, BigDecimal price) {
        return events.createEvent(name, date, venue, capacity, price);
    }
    public Event updateEvent(String id, String name, LocalDate date, String venue, int capacity, BigDecimal price) {
        return events.updateEvent(id, name, date, venue, capacity, price);
    }
    public void deleteEvent(String id) { events.deleteEvent(id); }
    public ServicePackage previewPackage(String eventId, Set<ServiceExtra> extras) {
        return events.previewPackage(eventId, extras);
    }
    public ServicePackage configurePackage(String eventId, Set<ServiceExtra> extras) {
        return events.configurePackage(eventId, extras);
    }
    public ServicePackage getPackage(String eventId) { return events.getPackage(eventId); }
    public Participant addParticipant(String name) { return events.addParticipant(name); }
    public List<Event> getEvents() { return store.events(); }
    public List<Participant> getParticipants() { return store.participants(); }
    public BookingDetails addBooking(String eventId, String participantId, int seats, PricingPlan plan) {
        return bookings.addBooking(eventId, participantId, seats, plan);
    }
    public BookingDetails changeStatus(String bookingId, BookingStatus status) {
        return bookings.changeStatus(bookingId, status);
    }
    public Summary getSummary(String eventId) { return events.getSummary(eventId); }
    public void exportSummary(String eventId, String format, Path path) throws IOException {
        exporters.export(getSummary(eventId), path, format);
    }
}
