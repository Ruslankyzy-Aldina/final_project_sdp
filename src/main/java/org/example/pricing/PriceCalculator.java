package org.example.pricing;

import org.example.booking.Booking;

import java.math.BigDecimal;
import java.util.Objects;

public final class PriceCalculator {
    private PricingStrategy strategy;

    public PriceCalculator(PricingStrategy strategy) {
        setStrategy(strategy);
    }

    public void setStrategy(PricingStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy");
    }

    public BigDecimal calculate(Booking booking) {
        return strategy.calculate(booking);
    }
}
