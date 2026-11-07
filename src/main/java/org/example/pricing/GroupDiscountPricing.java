package org.example.pricing;

import org.example.booking.Booking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class GroupDiscountPricing implements PricingStrategy {
    public static final int MIN_GROUP_SIZE = 5;
    private static final BigDecimal RATE = new BigDecimal("0.85");

    @Override
    public BigDecimal calculate(Booking booking) {
        Objects.requireNonNull(booking, "booking");
        if (booking.seatCount() < MIN_GROUP_SIZE) {
            throw new IllegalArgumentException("Group discount requires at least five seats");
        }
        return booking.basePrice().multiply(RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
