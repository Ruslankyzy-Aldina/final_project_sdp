package org.example.option;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Decorator pattern - abstract Decorator. Wraps another ServiceOption and, by default,
 * forwards to it. Concrete decorators add their own cost and description on top.
 */
public abstract class OptionDecorator implements ServiceOption {
    private final ServiceOption wrapped;

    protected OptionDecorator(ServiceOption wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped, "wrapped option");
    }

    protected abstract BigDecimal extraCost();
    protected abstract String extraDescription();

    @Override
    public final BigDecimal getCost() {
        return wrapped.getCost().add(extraCost());
    }

    @Override
    public final String getDescription() {
        return wrapped.getDescription() + " + " + extraDescription();
    }
}
