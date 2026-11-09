package org.example.facade;

import org.example.booking.BookingStatus;
import org.example.model.*;
import org.example.sample.SampleData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class EventFacadeTest {
    @TempDir Path directory;
    private final EventFacade facade = new EventFacade();
    private final Event event = facade.createEvent("Java <&> \"Клуб\"", LocalDate.now().plusDays(20), "Hall", 5, new BigDecimal("100"));
    private final Participant participant = facade.addParticipant("Айда & Dana");

    @ParameterizedTest @ValueSource(strings = {"txt", "json", "html"})
    void scenario7ExportsSummaryInSelectedFormat(String format) throws Exception {
        var booking = facade.addBooking(event.id(), participant.id(), 5, PricingPlan.GROUP);
        Path path = directory.resolve("summary." + format);
        facade.exportSummary(event.id(), format, path);
        String text = Files.readString(path);
        assertTrue(text.contains("425.00")); assertTrue(text.contains(booking.booking().id()));
        assertTrue(text.contains("GROUP")); assertTrue(text.contains("CREATED"));
        assertTrue(text.contains(format.equals("html") ? "Айда &amp; Dana" : "Айда & Dana"));
        if (format.equals("json")) {
            assertTrue(text.startsWith("{\n")); assertTrue(text.contains("\"seatCount\":5"));
            assertTrue(text.contains("\\\"Клуб\\\""));
        }
        if (format.equals("html")) { assertTrue(text.startsWith("<!doctype html>")); assertFalse(text.contains("<script>")); }
        try (var files = Files.list(directory)) { assertEquals(1, files.count()); }
    }
    @Test void scenario8RejectsOverCapacityWithoutChangingState() {
        facade.addBooking(event.id(), participant.id(), 5, PricingPlan.REGULAR);
        Summary before = facade.getSummary(event.id());
        var failure = assertThrows(IllegalArgumentException.class,
                () -> facade.addBooking(event.id(), participant.id(), 1, PricingPlan.REGULAR));
        assertTrue(failure.getMessage().contains("available 0"));
        assertEquals(before, facade.getSummary(event.id()));
    }
    @Test void cancellationReleasesSeatsAndReactivationChecksCapacity() {
        var first = facade.addBooking(event.id(), participant.id(), 5, PricingPlan.STUDENT);
        facade.changeStatus(first.booking().id(), BookingStatus.CANCELLED);
        assertEquals(new BigDecimal("0.00"), facade.getSummary(event.id()).totalPrice());
        assertEquals(5, facade.getSummary(event.id()).availableSeats());
        var second = facade.addBooking(event.id(), participant.id(), 5, PricingPlan.REGULAR);
        assertThrows(IllegalArgumentException.class, () -> facade.changeStatus(first.booking().id(), BookingStatus.CONFIRMED));
        assertEquals(BookingStatus.CANCELLED, facade.getSummary(event.id()).bookings().getFirst().booking().status());
        facade.changeStatus(second.booking().id(), BookingStatus.CANCELLED);
        facade.changeStatus(first.booking().id(), BookingStatus.CONFIRMED);
        facade.changeStatus(first.booking().id(), BookingStatus.CONFIRMED);
        assertEquals(5, facade.getSummary(event.id()).reservedSeats());
        assertEquals(new BigDecimal("450.00"), facade.getSummary(event.id()).totalPrice());
    }
    @Test void invalidBookingsDoNotMutateStore() {
        assertThrows(IllegalArgumentException.class, () -> facade.addBooking(event.id(), participant.id(), 4, PricingPlan.GROUP));
        assertThrows(IllegalArgumentException.class, () -> facade.addBooking(event.id(), participant.id(), 0, PricingPlan.REGULAR));
        assertThrows(IllegalArgumentException.class, () -> facade.addBooking("missing", participant.id(), 1, PricingPlan.REGULAR));
        assertThrows(IllegalArgumentException.class, () -> facade.addBooking(event.id(), "missing", 1, PricingPlan.REGULAR));
        assertThrows(IllegalArgumentException.class, () -> facade.addBooking(event.id(), participant.id(), 1, null));
        assertTrue(facade.getSummary(event.id()).bookings().isEmpty());
    }
    @Test void validatesEventAndParticipantInput() {
        assertThrows(IllegalArgumentException.class, () -> facade.createEvent(" ", event.date(), "Hall", 5, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> facade.createEvent("Test", null, "Hall", 5, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> facade.createEvent("Test", event.date(), "Hall", 0, BigDecimal.ONE));
        assertThrows(IllegalArgumentException.class, () -> facade.createEvent("Test", event.date(), "Hall", 5, BigDecimal.ONE.negate()));
        assertThrows(IllegalArgumentException.class, () -> facade.addParticipant(" "));
        assertEquals(1, facade.getEvents().size()); assertEquals(1, facade.getParticipants().size());
    }
    @Test void summaryIsImmutableAndIndependentOfLaterChanges() {
        var booking = facade.addBooking(event.id(), participant.id(), 2, PricingPlan.REGULAR);
        Summary snapshot = facade.getSummary(event.id());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.bookings().clear());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.participants().clear());
        facade.changeStatus(booking.booking().id(), BookingStatus.CANCELLED);
        assertEquals(new BigDecimal("200.00"), snapshot.totalPrice());
        assertEquals(BookingStatus.CREATED, snapshot.bookings().getFirst().booking().status());
    }
    @Test void eventsHaveIndependentCapacityAndTotals() {
        Event second = facade.createEvent("Free", event.date(), "Hall", 1, BigDecimal.ZERO);
        facade.addBooking(event.id(), participant.id(), 5, PricingPlan.REGULAR);
        facade.addBooking(second.id(), participant.id(), 1, PricingPlan.REGULAR);
        assertEquals(new BigDecimal("0.00"), facade.getSummary(second.id()).totalPrice());
        assertEquals(1, facade.getSummary(second.id()).reservedSeats());
    }
    @Test void sampleDataHasExpectedTotalsAndFullWorkshop() {
        var demo = SampleData.createDemo();
        assertEquals(3, demo.getEvents().size()); assertEquals(3, demo.getParticipants().size());
        assertEquals(new BigDecimal("35750.00"), demo.getSummary(demo.getEvents().getFirst().id()).totalPrice());
        assertEquals(0, demo.getSummary(demo.getEvents().get(1).id()).availableSeats());
    }
    @Test void unsupportedFormatAndIoFailuresPreserveExistingData() throws Exception {
        Path path = directory.resolve("existing.txt"); Files.writeString(path, "keep");
        assertThrows(IllegalArgumentException.class, () -> facade.exportSummary(event.id(), "pdf", path));
        assertEquals("keep", Files.readString(path));
        assertThrows(IOException.class, () -> facade.exportSummary(event.id(), "txt", directory.resolve("missing/report.txt")));
        assertThrows(IOException.class, () -> facade.exportSummary(event.id(), "txt", directory));
        assertEquals("keep", Files.readString(path));
    }
    @Test void exportReplacesAnExistingReport() throws Exception {
        Path path = directory.resolve("existing.txt"); Files.writeString(path, "obsolete");
        facade.exportSummary(event.id(), "txt", path);
        assertTrue(Files.readString(path).contains("TOTAL (KZT): 0.00"));
        assertFalse(Files.readString(path).contains("obsolete"));
    }
    @Test void concurrentBookingsCannotOversell() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> book = () -> {
                start.await();
                try { facade.addBooking(event.id(), participant.id(), 5, PricingPlan.REGULAR); return true; }
                catch (IllegalArgumentException expected) { return false; }
            };
            var first = executor.submit(book); var second = executor.submit(book); start.countDown();
            assertNotEquals(first.get(5, TimeUnit.SECONDS), second.get(5, TimeUnit.SECONDS));
            assertEquals(5, facade.getSummary(event.id()).reservedSeats());
        }
    }
}
