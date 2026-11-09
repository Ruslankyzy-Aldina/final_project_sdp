package org.example.option;

import java.math.BigDecimal;

/** Decorator pattern - Concrete Decorator. Hall decoration. */
public final class DecorDecorator extends OptionDecorator {
    public static final BigDecimal PRICE = new BigDecimal("15000.00");

    public DecorDecorator(ServiceOption wrapped) { super(wrapped); }

    @Override protected BigDecimal extraCost() { return PRICE; }
    @Override protected String extraDescription() { return "Decor"; }
}
