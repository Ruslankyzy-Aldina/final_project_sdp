package org.example.storage;

import org.example.model.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Session-local storage. Services synchronize on this instance for compound operations. */
public final class InMemoryEventStore {
    private final Map<String, Event> events = new LinkedHashMap<>();
    private final Map<String, Participant> participants = new LinkedHashMap<>();
    private final Map<String, BookingDetails> bookings = new LinkedHashMap<>();

    public synchronized void addEvent(Event event) { events.put(event.id(), event); }
    public synchronized void addParticipant(Participant participant) { participants.put(participant.id(), participant); }
    public synchronized void saveBooking(BookingDetails details) { bookings.put(details.booking().id(), details); }
    public synchronized List<Event> events() { return List.copyOf(events.values()); }
    public synchronized List<Participant> participants() { return List.copyOf(participants.values()); }
    public synchronized List<BookingDetails> bookings(String eventId) {
        return bookings.values().stream().filter(b -> b.booking().eventId().equals(eventId)).toList();
    }
    public synchronized Event event(String id) { return require(events, id, "Event"); }
    public synchronized Participant participant(String id) { return require(participants, id, "Participant"); }
    public synchronized BookingDetails booking(String id) { return require(bookings, id, "Booking"); }
    private static <T> T require(Map<String, T> map, String id, String kind) {
        T result = map.get(id);
        if (result == null) throw new IllegalArgumentException(kind + " was not found");
        return result;
    }
}
