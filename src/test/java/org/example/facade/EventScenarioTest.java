package org.example.facade;

import org.example.booking.BookingStatus;
import org.example.model.*;
import org.example.option.ServiceExtra;
import org.example.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Test scenarios 1, 5 and 6 plus the edit/delete/package rules, run through the facade like the GUI does. */
class EventScenarioTest {
    private final EventFacade facade = new EventFacade();
    private final LocalDate nextMonth = LocalDate.now().plusDays(30);

    @Test
    void scenario1MainSuccessfulPath() {
        Event event = facade.createEvent("Spring Gala", nextMonth, "Main Hall", 10, new BigDecimal("1000"));
        assertEquals(1, facade.getEvents().size());

        Event edited = facade.updateEvent(event.id(), "Spring Gala 2027", nextMonth.plusDays(1), "Main Hall", 12, new BigDecimal("1200"));
        assertEquals(event.id(), edited.id());
        assertEquals(12, facade.getEvents().getFirst().capacity());

        ServicePackage servicePackage = facade.configurePackage(event.id(),
                EnumSet.of(ServiceExtra.CATERING, ServiceExtra.PHOTOGRAPHER, ServiceExtra.DECOR));
        // 20000 base + 12 guests * 2500 + 30000 + 15000
        assertEquals(new BigDecimal("95000.00"), servicePackage.cost());
        assertEquals(servicePackage, facade.getPackage(event.id()));

        Participant guest = facade.addParticipant("Aida");
        facade.addBooking(event.id(), guest.id(), 2, PricingPlan.REGULAR);
        Summary summary = facade.getSummary(event.id());
        assertEquals(2, summary.reservedSeats());
        assertEquals(new BigDecimal("2400.00"), summary.totalPrice());
    }

    @Test
    void scenario5EmptyEventNameIsRejected() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> facade.createEvent("   ", nextMonth, "Main Hall", 10, new BigDecimal("1000")));
        assertEquals("Event name is required", error.getMessage());
        assertTrue(facade.getEvents().isEmpty());
    }

    @ParameterizedTest(name = "capacity={0}, price={1}")
    @CsvSource({"0, 1000", "-5, 1000", "10, -1", "0, -1"})
    void scenario6ZeroOrNegativeCapacityOrNegativePriceIsRejected(int capacity, String price) {
        ValidationException error = assertThrows(ValidationException.class,
                () -> facade.createEvent("Gala", nextMonth, "Hall", capacity, new BigDecimal(price)));
        if (capacity <= 0) assertTrue(error.getErrors().contains("Capacity must be greater than zero"));
        if (price.startsWith("-")) assertTrue(error.getErrors().contains("Ticket price cannot be negative"));
        assertTrue(facade.getEvents().isEmpty());
    }

    @Test
    void scenario6ZeroPriceIsAllowedForFreeEvents() {
        Event free = facade.createEvent("Meetup", nextMonth, "Open Space", 5, BigDecimal.ZERO);
        assertEquals(new BigDecimal("0.00"), free.unitPrice());
    }

    @Test
    void pastDateIsRejectedOnCreateAndEdit() {
        assertThrows(ValidationException.class,
                () -> facade.createEvent("Gala", LocalDate.now().minusDays(1), "Hall", 5, BigDecimal.ONE));
        Event event = facade.createEvent("Gala", nextMonth, "Hall", 5, BigDecimal.ONE);
        assertThrows(ValidationException.class,
                () -> facade.updateEvent(event.id(), "Gala", LocalDate.now().minusDays(1), "Hall", 5, BigDecimal.ONE));
        assertEquals(event, facade.getEvents().getFirst());
    }

    @Test
    void capacityCannotDropBelowReservedSeats() {
        Event event = facade.createEvent("Gala", nextMonth, "Hall", 10, BigDecimal.ONE);
        Participant guest = facade.addParticipant("Dana");
        facade.addBooking(event.id(), guest.id(), 6, PricingPlan.GROUP);
        ValidationException error = assertThrows(ValidationException.class,
                () -> facade.updateEvent(event.id(), "Gala", nextMonth, "Hall", 5, BigDecimal.ONE));
        assertTrue(error.getMessage().contains("6 seats already reserved"));
        assertEquals(10, facade.getEvents().getFirst().capacity());
    }

    @Test
    void eventWithActiveBookingsCannotBeDeletedUntilCancelled() {
        Event event = facade.createEvent("Gala", nextMonth, "Hall", 10, BigDecimal.ONE);
        Participant guest = facade.addParticipant("Dana");
        BookingDetails booking = facade.addBooking(event.id(), guest.id(), 1, PricingPlan.REGULAR);
        facade.configurePackage(event.id(), Set.of(ServiceExtra.DECOR));

        assertThrows(ValidationException.class, () -> facade.deleteEvent(event.id()));
        assertEquals(1, facade.getEvents().size());

        facade.changeStatus(booking.booking().id(), BookingStatus.CANCELLED);
        facade.deleteEvent(event.id());
        assertTrue(facade.getEvents().isEmpty());
        assertThrows(ValidationException.class, () -> facade.getSummary(event.id()));
        assertThrows(ValidationException.class, () -> facade.getPackage(event.id()));
    }

    @Test
    void cateringFollowsCapacityWhenEventIsEdited() {
        Event event = facade.createEvent("Gala", nextMonth, "Hall", 10, BigDecimal.ONE);
        facade.configurePackage(event.id(), Set.of(ServiceExtra.CATERING));
        assertEquals(new BigDecimal("45000.00"), facade.getPackage(event.id()).cost());
        facade.updateEvent(event.id(), "Gala", nextMonth, "Hall", 20, BigDecimal.ONE);
        assertEquals(new BigDecimal("70000.00"), facade.getPackage(event.id()).cost());
    }

    @Test
    void unconfiguredEventHasBasePackageAndPreviewIsNotSaved() {
        Event event = facade.createEvent("Gala", nextMonth, "Hall", 10, BigDecimal.ONE);
        assertEquals(new BigDecimal("20000.00"), facade.getPackage(event.id()).cost());
        facade.previewPackage(event.id(), Set.of(ServiceExtra.PHOTOGRAPHER));
        assertTrue(facade.getPackage(event.id()).extras().isEmpty());
    }
}
