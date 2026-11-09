package org.example.service;

import org.example.validation.ValidationException;
import org.example.booking.*;
import org.example.model.*;
import org.example.pricing.*;
import org.example.storage.InMemoryEventStore;
import java.math.BigDecimal;
import java.util.UUID;

public final class BookingService {
    private final InMemoryEventStore store;
    public BookingService(InMemoryEventStore store) { this.store = store; }

    public BookingDetails addBooking(String eventId, String participantId, int seats, PricingPlan plan) {
        if (plan == null) throw new ValidationException("Pricing plan is required");
        synchronized (store) {
            Event event = store.event(eventId);
            Participant participant = store.participant(participantId);
            Booking booking = new Booking(UUID.randomUUID().toString(), eventId, participant.name(),
                    seats, event.unitPrice(), BookingStatus.CREATED);
            checkCapacity(event, seats);
            PricingStrategy strategy = switch (plan) {
                case REGULAR -> new RegularPricing();
                case STUDENT -> new StudentDiscountPricing();
                case GROUP -> new GroupDiscountPricing();
            };
            BigDecimal price = new PriceCalculator(strategy).calculate(booking);
            BookingDetails details = new BookingDetails(booking, participant, plan, price);
            store.saveBooking(details);
            return details;
        }
    }

    public BookingDetails changeStatus(String bookingId, BookingStatus status) {
        if (status == null) throw new ValidationException("Booking status is required");
        synchronized (store) {
            BookingDetails old = store.booking(bookingId);
            if (!old.active() && status != BookingStatus.CANCELLED)
                checkCapacity(store.event(old.booking().eventId()), old.booking().seatCount());
            BookingDetails updated = old.withStatus(status);
            store.saveBooking(updated);
            return updated;
        }
    }

    private void checkCapacity(Event event, int additionalSeats) {
        long occupied = store.bookings(event.id()).stream().filter(BookingDetails::active)
                .mapToLong(b -> b.booking().seatCount()).sum();
        long available = event.capacity() - occupied;
        if (additionalSeats > available)
            throw new ValidationException("Not enough seats: requested " + additionalSeats
                    + ", available " + available + ". Booking was not changed.");
    }
}
