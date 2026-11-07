package org.example.pricing;

import org.example.booking.Booking;
import org.example.booking.BookingStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriceCalculatorTest {
    private final Booking booking = new Booking(
            "booking-1", "event-1", "Aida", 5, new BigDecimal("100.00"), BookingStatus.CREATED);

    @Test
    void sameBookingHasThreePricesWhenStrategyChanges() {
        PriceCalculator calculator = new PriceCalculator(new RegularPricing());
        assertEquals(new BigDecimal("500.00"), calculator.calculate(booking));

        calculator.setStrategy(new StudentDiscountPricing());
        assertEquals(new BigDecimal("450.00"), calculator.calculate(booking));

        calculator.setStrategy(new GroupDiscountPricing());
        assertEquals(new BigDecimal("425.00"), calculator.calculate(booking));
    }

    @Test
    void groupDiscountRequiresFiveSeats() {
        Booking smallBooking = new Booking(
                "booking-2", "event-1", "Dana", 4, new BigDecimal("100.00"), BookingStatus.CREATED);
        assertThrows(IllegalArgumentException.class,
                () -> new GroupDiscountPricing().calculate(smallBooking));
    }
}
