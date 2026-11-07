package org.example.pricing;

import org.example.booking.Booking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class StudentDiscountPricing implements PricingStrategy {
    private static final BigDecimal RATE = new BigDecimal("0.90");

    @Override
    public BigDecimal calculate(Booking booking) {
        return Objects.requireNonNull(booking, "booking")
                .basePrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
