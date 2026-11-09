package org.example.model;

import org.example.validation.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/** Automated test of the Builder pattern. */
class EventBuilderTest {
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 9);
    private static final Clock CLOCK = Clock.fixed(TODAY.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

    private EventBuilder valid() {
        return Event.builder().clock(CLOCK)
                .name("  Design Patterns Conference ").date(TODAY.plusDays(30))
                .location(" Main Hall ").capacity(12).price(new BigDecimal("5000"));
    }

    @Test
    void buildsEventFromStepByStepCalls() {
        Event event = valid().build();
        assertNotNull(event.id());
        assertEquals("Design Patterns Conference", event.name());
        assertEquals(TODAY.plusDays(30), event.date());
        assertEquals("Main Hall", event.venue());
        assertEquals(12, event.capacity());
        assertEquals(new BigDecimal("5000.00"), event.unitPrice());
    }

    @Test
    void callOrderDoesNotMatterAndTodayIsAllowed() {
        Event event = Event.builder().clock(CLOCK).price(BigDecimal.ZERO).capacity(1)
                .location("Lab").date(TODAY).name("Meetup").build();
        assertEquals(TODAY, event.date());
        assertEquals(new BigDecimal("0.00"), event.unitPrice());
    }

    @Test
    void toBuilderKeepsIdAndChangesOnlyGivenFields() {
        Event original = valid().build();
        Event edited = original.toBuilder().clock(CLOCK).capacity(20).build();
        assertEquals(original.id(), edited.id());
        assertEquals(20, edited.capacity());
        assertEquals(original.name(), edited.name());
        assertNotEquals(original, edited);
    }

    @Test
    void reportsAllProblemsInOneValidationException() {
        ValidationException error = assertThrows(ValidationException.class, () -> Event.builder().clock(CLOCK)
                .name(" ").location("").date(TODAY.minusDays(1)).capacity(0).price(new BigDecimal("-1")).build());
        assertEquals(java.util.List.of(
                "Event name is required",
                "Location is required",
                "Event date cannot be in the past",
                "Capacity must be greater than zero",
                "Ticket price cannot be negative"), error.getErrors());
    }

    @Test
    void rejectsMissingDateAndTooLongName() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> valid().date(null).name("x".repeat(EventBuilder.MAX_NAME_LENGTH + 1)).build());
        assertTrue(error.getErrors().contains("Event date is required"));
        assertTrue(error.getErrors().contains("Event name must be at most 100 characters"));
    }
}
