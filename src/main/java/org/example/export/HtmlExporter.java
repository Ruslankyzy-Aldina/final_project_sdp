package org.example.export;

import org.example.model.*;
import java.io.IOException;
import java.nio.file.Path;

public final class HtmlExporter implements ReportExporter {
    @Override public void export(Summary s, Path path) throws IOException {
        Event e = s.event();
        StringBuilder html = new StringBuilder("""
                <!doctype html>
                <html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width">
                <title>Event summary</title><style>
                body{font:16px system-ui,sans-serif;color:#172c43;max-width:1100px;margin:48px auto;padding:24px}
                h1{font-size:36px}table{border-collapse:collapse;width:100%;margin:24px 0}
                th,td{padding:12px;text-align:left;border-bottom:1px solid #dce3eb;overflow-wrap:anywhere}
                th{background:#edf3f9}.total{font-size:24px;font-weight:bold}small{color:#526578}
                </style></head><body><p>EVENT DESK / SUMMARY</p>
                """);
        html.append("<h1>").append(escape(e.name())).append("</h1><p>").append(e.date())
                .append(" · ").append(escape(e.venue())).append("</p><small>Event ID: ").append(escape(e.id()))
                .append("</small><p>Capacity: ").append(e.capacity()).append(" · Reserved: ")
                .append(s.reservedSeats()).append(" · Available: ").append(s.availableSeats())
                .append(" · Unit price: ").append(e.unitPrice()).append(" KZT</p><h2>Participants</h2><ul>");
        for (Participant p : s.participants()) html.append("<li>").append(escape(p.name()))
                .append(" <small>(").append(escape(p.id())).append(")</small></li>");
        html.append("</ul><h2>Bookings</h2><table><thead><tr><th>ID</th><th>Participant</th><th>Seats</th>"
                + "<th>Plan</th><th>Status</th><th>Quoted price (KZT)</th></tr></thead><tbody>");
        for (BookingDetails b : s.bookings()) html.append("<tr><td>").append(escape(b.booking().id()))
                .append("</td><td>").append(escape(b.participant().name())).append("</td><td>")
                .append(b.booking().seatCount()).append("</td><td>").append(b.pricingPlan())
                .append("</td><td>").append(b.booking().status()).append("</td><td>").append(b.price()).append("</td></tr>");
        html.append("</tbody></table><p class=\"total\">Total: ").append(s.totalPrice())
                .append(" KZT</p><p>Cancelled bookings remain in the history and are excluded from the total.</p></body></html>");
        ExportFiles.write(path, html.toString());
    }
    static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
