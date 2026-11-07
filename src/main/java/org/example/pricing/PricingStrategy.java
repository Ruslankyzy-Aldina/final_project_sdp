package org.example.pricing;

import org.example.booking.Booking;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculate(Booking booking);
}
