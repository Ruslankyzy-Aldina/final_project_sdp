package org.example.option;

import java.math.BigDecimal;

/** Decorator pattern - Concrete Decorator. A photographer for the whole event. */
public final class PhotographerDecorator extends OptionDecorator {
    public static final BigDecimal PRICE = new BigDecimal("30000.00");

    public PhotographerDecorator(ServiceOption wrapped) { super(wrapped); }

    @Override protected BigDecimal extraCost() { return PRICE; }
    @Override protected String extraDescription() { return "Photographer"; }
}
