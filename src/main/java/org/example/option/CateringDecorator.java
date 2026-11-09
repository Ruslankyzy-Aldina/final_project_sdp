package org.example.option;

import org.example.validation.ValidationException;

import java.math.BigDecimal;

/** Decorator pattern - Concrete Decorator. Catering is charged per guest. */
public final class CateringDecorator extends OptionDecorator {
    public static final BigDecimal PRICE_PER_GUEST = new BigDecimal("2500.00");
    private final int guests;

    public CateringDecorator(ServiceOption wrapped, int guests) {
        super(wrapped);
        if (guests <= 0) throw new ValidationException("Catering needs at least one guest");
        this.guests = guests;
    }

    @Override protected BigDecimal extraCost() { return PRICE_PER_GUEST.multiply(BigDecimal.valueOf(guests)); }
    @Override protected String extraDescription() { return "Catering for " + guests + " guests"; }
}
