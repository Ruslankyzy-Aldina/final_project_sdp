package org.example.pricing;

import org.example.booking.Booking;

import java.math.BigDecimal;
import java.util.Objects;

public final class RegularPricing implements PricingStrategy {
    @Override
    public BigDecimal calculate(Booking booking) {
        return Objects.requireNonNull(booking, "booking").basePrice();
    }
}
