package org.example.sample;

import org.example.booking.BookingStatus;
import org.example.facade.EventFacade;
import org.example.model.*;
import org.example.option.ServiceExtra;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumSet;

public final class SampleData {
    private SampleData() { }
    /** A fresh facade prevents reseeding or overwriting an existing session. */
    public static EventFacade createDemo() {
        EventFacade facade = new EventFacade();
        // Relative dates keep the demo valid: events in the past are rejected by EventBuilder.
        LocalDate today = LocalDate.now();
        Event conference = facade.createEvent("Design Patterns Conference", today.plusDays(30),
                "Astana · Main Hall", 12, new BigDecimal("5000"));
        Event workshop = facade.createEvent("Java Workshop", today.plusDays(33),
                "Lab 204", 3, new BigDecimal("2000"));
        facade.createEvent("Community Meetup", today.plusDays(38), "Open Space", 20, BigDecimal.ZERO);
        Participant aida = facade.addParticipant("Aida");
        Participant dana = facade.addParticipant("Dana");
        Participant team = facade.addParticipant("Student team");
        BookingDetails confirmed = facade.addBooking(conference.id(), aida.id(), 2, PricingPlan.REGULAR);
        facade.changeStatus(confirmed.booking().id(), BookingStatus.CONFIRMED);
        facade.addBooking(conference.id(), dana.id(), 1, PricingPlan.STUDENT);
        facade.addBooking(conference.id(), team.id(), 5, PricingPlan.GROUP);
        facade.addBooking(workshop.id(), aida.id(), 3, PricingPlan.REGULAR);
        facade.configurePackage(conference.id(), EnumSet.of(ServiceExtra.CATERING, ServiceExtra.PHOTOGRAPHER));
        return facade;
    }
}
