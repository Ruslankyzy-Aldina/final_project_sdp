package org.example.option;

import java.math.BigDecimal;

/** Decorator pattern - Component. Anything that can be part of an event service package. */
public interface ServiceOption {
    BigDecimal getCost();
    String getDescription();
}
