package org.example.sample;

import org.example.booking.BookingStatus;
import org.example.facade.EventFacade;
import org.example.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class SampleData {
    private SampleData() { }
    /** A fresh facade prevents reseeding or overwriting an existing session. */
    public static EventFacade createDemo() {
        EventFacade facade = new EventFacade();
        Event conference = facade.createEvent("Design Patterns Conference", LocalDate.of(2026, 12, 12),
                "Astana · Main Hall", 12, new BigDecimal("5000"));
        Event workshop = facade.createEvent("Java Workshop", LocalDate.of(2026, 12, 15),
                "Lab 204", 3, new BigDecimal("2000"));
        facade.createEvent("Community Meetup", LocalDate.of(2026, 12, 20), "Open Space", 20, BigDecimal.ZERO);
        Participant aida = facade.addParticipant("Aida");
        Participant dana = facade.addParticipant("Dana");
        Participant team = facade.addParticipant("Student team");
        BookingDetails confirmed = facade.addBooking(conference.id(), aida.id(), 2, PricingPlan.REGULAR);
        facade.changeStatus(confirmed.booking().id(), BookingStatus.CONFIRMED);
        facade.addBooking(conference.id(), dana.id(), 1, PricingPlan.STUDENT);
        facade.addBooking(conference.id(), team.id(), 5, PricingPlan.GROUP);
        facade.addBooking(workshop.id(), aida.id(), 3, PricingPlan.REGULAR);
        return facade;
    }
}
