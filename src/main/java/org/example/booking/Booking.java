package org.example.booking;

import org.example.validation.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Booking(
        String id,
        String eventId,
        String customerName,
        int seatCount,
        BigDecimal unitPrice,
        BookingStatus status
) {
    public Booking {
        if (id == null || id.isBlank()) {
            throw new ValidationException("Booking id is required");
        }
        if (eventId == null || eventId.isBlank()) {
            throw new ValidationException("Event id is required");
        }
        if (customerName == null || customerName.isBlank()) {
            throw new ValidationException("Customer name is required");
        }
        if (seatCount <= 0) {
            throw new ValidationException("Seat count must be positive");
        }
        Objects.requireNonNull(unitPrice, "unitPrice");
        if (unitPrice.signum() < 0) {
            throw new ValidationException("Unit price cannot be negative");
        }
        unitPrice = unitPrice.setScale(2, RoundingMode.HALF_UP);
        Objects.requireNonNull(status, "status");
    }

    public Booking withStatus(BookingStatus newStatus) {
        return new Booking(id, eventId, customerName, seatCount, unitPrice, newStatus);
    }

    public BigDecimal basePrice() {
        return unitPrice.multiply(BigDecimal.valueOf(seatCount));
    }
}
