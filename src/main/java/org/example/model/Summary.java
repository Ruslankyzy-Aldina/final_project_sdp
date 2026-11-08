package org.example.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/** Snapshot: lists and their record elements cannot change after export starts. */
public record Summary(Event event, List<Participant> participants, List<BookingDetails> bookings,
                      long reservedSeats, BigDecimal totalPrice) {
    public Summary {
        Objects.requireNonNull(event);
        participants = List.copyOf(participants);
        bookings = List.copyOf(bookings);
        Objects.requireNonNull(totalPrice);
    }
    public static Summary of(Event event, List<BookingDetails> bookings) {
        List<BookingDetails> snapshot = List.copyOf(bookings);
        return new Summary(event, snapshot.stream().map(BookingDetails::participant).distinct().toList(),
                snapshot, snapshot.stream().filter(BookingDetails::active)
                .mapToLong(b -> b.booking().seatCount()).sum(),
                snapshot.stream().filter(BookingDetails::active).map(BookingDetails::price)
                        .reduce(new BigDecimal("0.00"), BigDecimal::add));
    }
    public long availableSeats() { return event.capacity() - reservedSeats; }
}
