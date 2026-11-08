package org.example.export;

import org.example.model.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

public final class JsonExporter implements ReportExporter {
    @Override public void export(Summary s, Path path) throws IOException {
        Event e = s.event();
        String participants = s.participants().stream().map(p -> "{\"id\":" + quote(p.id())
                + ",\"name\":" + quote(p.name()) + "}").collect(Collectors.joining(",\n    "));
        String bookings = s.bookings().stream().map(b -> "{\"id\":" + quote(b.booking().id())
                + ",\"eventId\":" + quote(b.booking().eventId())
                + ",\"participantId\":" + quote(b.participant().id())
                + ",\"customerName\":" + quote(b.booking().customerName())
                + ",\"seatCount\":" + b.booking().seatCount()
                + ",\"unitPrice\":" + b.booking().unitPrice().toPlainString()
                + ",\"pricingPlan\":" + quote(b.pricingPlan().name())
                + ",\"status\":" + quote(b.booking().status().name())
                + ",\"quotedPrice\":" + b.price().toPlainString() + "}").collect(Collectors.joining(",\n    "));
        ExportFiles.write(path, "{\n  \"event\": {\"id\":" + quote(e.id()) + ",\"name\":" + quote(e.name())
                + ",\"date\":" + quote(e.date().toString()) + ",\"venue\":" + quote(e.venue())
                + ",\"capacity\":" + e.capacity() + ",\"unitPrice\":" + e.unitPrice().toPlainString() + "},\n"
                + "  \"currency\": \"KZT\",\n  \"participants\": [" + participants + "],\n"
                + "  \"bookings\": [" + bookings + "],\n  \"reservedSeats\": " + s.reservedSeats()
                + ",\n  \"availableSeats\": " + s.availableSeats() + ",\n  \"totalPrice\": "
                + s.totalPrice().toPlainString() + "\n}\n");
    }

    static String quote(String value) {
        StringBuilder result = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
            switch (c) {
                case '"' -> result.append("\\\"");
                case '\\' -> result.append("\\\\");
                case '\n' -> result.append("\\n");
                case '\r' -> result.append("\\r");
                case '\t' -> result.append("\\t");
                default -> {
                    if (c < 0x20 || Character.isSurrogate(c)) result.append(String.format("\\u%04x", (int) c));
                    else result.append(c);
                }
            }
        }
        return result.append('"').toString();
    }
}
