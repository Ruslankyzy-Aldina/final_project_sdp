package org.example.model;

import org.example.booking.Booking;
import org.example.booking.BookingStatus;
import java.math.BigDecimal;
import java.util.Objects;

/** Immutable booking plus its registered participant and price at booking time. */
public record BookingDetails(Booking booking, Participant participant,
                             PricingPlan pricingPlan, BigDecimal price) {
    public BookingDetails {
        Objects.requireNonNull(booking);
        Objects.requireNonNull(participant);
        Objects.requireNonNull(pricingPlan);
        Objects.requireNonNull(price);
    }
    public boolean active() { return booking.status() != BookingStatus.CANCELLED; }
    public BookingDetails withStatus(BookingStatus status) {
        return new BookingDetails(booking.withStatus(status), participant, pricingPlan, price);
    }
}
