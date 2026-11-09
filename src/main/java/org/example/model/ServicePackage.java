package org.example.model;

import org.example.option.BasePackage;
import org.example.option.ServiceExtra;
import org.example.option.ServiceOption;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The service package configured for one event: the chosen extras and the decorated
 * ServiceOption chain built from them (base package wrapped by one decorator per extra).
 */
public record ServicePackage(String eventId, List<ServiceExtra> extras, ServiceOption option) {
    public ServicePackage {
        eventId = Validation.required(eventId, "Event id");
        extras = List.copyOf(Objects.requireNonNull(extras, "extras"));
        Objects.requireNonNull(option, "option");
    }

    /** Wraps the base package with a decorator for every selected extra (in enum order). */
    public static ServicePackage configure(Event event, Set<ServiceExtra> selected) {
        Objects.requireNonNull(event, "event");
        Set<ServiceExtra> extras = selected == null || selected.isEmpty()
                ? EnumSet.noneOf(ServiceExtra.class) : EnumSet.copyOf(selected);
        ServiceOption option = new BasePackage();
        for (ServiceExtra extra : extras) option = extra.decorate(option, event.capacity());
        return new ServicePackage(event.id(), List.copyOf(extras), option);
    }

    public BigDecimal cost() { return option.getCost(); }
    public String description() { return option.getDescription(); }
}
