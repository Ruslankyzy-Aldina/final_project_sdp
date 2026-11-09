package org.example.option;

import org.example.validation.ValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Decorator pattern - Concrete Component. The mandatory package every event starts with. */
public final class BasePackage implements ServiceOption {
    public static final String DEFAULT_NAME = "Base package (venue setup, registration desk)";
    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("20000.00");

    private final String name;
    private final BigDecimal price;

    public BasePackage() {
        this(DEFAULT_NAME, DEFAULT_PRICE);
    }

    public BasePackage(String name, BigDecimal price) {
        if (name == null || name.isBlank()) throw new ValidationException("Package name is required");
        if (price == null || price.signum() < 0) throw new ValidationException("Package price cannot be negative");
        this.name = name.trim();
        this.price = price.setScale(2, RoundingMode.HALF_UP);
    }

    @Override public BigDecimal getCost() { return price; }
    @Override public String getDescription() { return name; }
}
