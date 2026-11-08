package org.example.model;

public record Participant(String id, String name) {
    public Participant {
        id = Validation.required(id, "Participant id");
        name = Validation.required(name, "Participant name");
    }
    @Override public String toString() { return name; }
}
