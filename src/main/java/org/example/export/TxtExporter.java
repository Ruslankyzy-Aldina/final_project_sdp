package org.example.export;

import org.example.model.*;
import java.io.IOException;
import java.nio.file.Path;

public final class TxtExporter implements ReportExporter {
    @Override public void export(Summary summary, Path path) throws IOException {
        Event e = summary.event();
        StringBuilder text = new StringBuilder("EVENT SUMMARY\n")
                .append("Event: ").append(e.name()).append("\nID: ").append(e.id())
                .append("\nDate: ").append(e.date()).append("\nVenue: ").append(e.venue())
                .append("\nCapacity: ").append(e.capacity()).append("\nUnit price (KZT): ").append(e.unitPrice())
                .append("\nReserved seats: ").append(summary.reservedSeats())
                .append("\nAvailable seats: ").append(summary.availableSeats()).append("\n\nPARTICIPANTS\n");
        for (Participant p : summary.participants()) text.append(p.id()).append(" | ").append(p.name()).append('\n');
        text.append("\nBOOKINGS (cancelled bookings excluded from total)\n");
        for (BookingDetails b : summary.bookings()) text.append(b.booking().id()).append(" | ")
                .append(b.participant().name()).append(" | seats: ").append(b.booking().seatCount())
                .append(" | ").append(b.pricingPlan()).append(" | ").append(b.booking().status())
                .append(" | quoted price (KZT): ").append(b.price()).append('\n');
        text.append("\nTOTAL (KZT): ").append(summary.totalPrice()).append('\n');
        ExportFiles.write(path, text.toString());
    }
}
