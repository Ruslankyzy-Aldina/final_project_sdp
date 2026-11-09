# Class diagram: design patterns

Covers every pattern implemented so far: Builder, Decorator, Facade, Factory Method and Strategy.
The diagram renders on GitHub (Mermaid). The same model is in `class-diagram.puml` for PlantUML/IntelliJ.

```mermaid
classDiagram
    direction LR

    %% ---------- Builder ----------
    class Event {
        -String id
        -String name
        -LocalDate date
        -String venue
        -int capacity
        -BigDecimal unitPrice
        ~Event(EventBuilder)
        +builder()$ EventBuilder
        +toBuilder() EventBuilder
        +id() String
        +name() String
        +date() LocalDate
        +venue() String
        +capacity() int
        +unitPrice() BigDecimal
    }
    class EventBuilder {
        +name(String) EventBuilder
        +date(LocalDate) EventBuilder
        +location(String) EventBuilder
        +capacity(int) EventBuilder
        +price(BigDecimal) EventBuilder
        +id(String) EventBuilder
        +clock(Clock) EventBuilder
        +build() Event
    }
    class EventService {
        +createEvent(...) Event
        +updateEvent(...) Event
        +deleteEvent(String)
        +configurePackage(String, Set~ServiceExtra~) ServicePackage
    }
    class ValidationException {
        +getErrors() List~String~
    }
    EventService ..> EventBuilder : directs
    EventBuilder ..> Event : builds
    EventBuilder ..> ValidationException : throws
    IllegalArgumentException <|-- ValidationException

    %% ---------- Decorator ----------
    class ServiceOption {
        <<interface>>
        +getCost() BigDecimal
        +getDescription() String
    }
    class BasePackage {
        -String name
        -BigDecimal price
    }
    class OptionDecorator {
        <<abstract>>
        -ServiceOption wrapped
        #extraCost()* BigDecimal
        #extraDescription()* String
    }
    class CateringDecorator {
        -int guests
    }
    class PhotographerDecorator
    class DecorDecorator
    class ServiceExtra {
        <<enumeration>>
        CATERING
        PHOTOGRAPHER
        DECOR
        +decorate(ServiceOption, int) ServiceOption
    }
    class ServicePackage {
        <<record>>
        eventId
        extras
        option
        +configure(Event, Set~ServiceExtra~)$ ServicePackage
        +cost() BigDecimal
    }
    ServiceOption <|.. BasePackage
    ServiceOption <|.. OptionDecorator
    OptionDecorator o--> ServiceOption : wraps
    OptionDecorator <|-- CateringDecorator
    OptionDecorator <|-- PhotographerDecorator
    OptionDecorator <|-- DecorDecorator
    ServiceExtra ..> OptionDecorator : creates
    ServicePackage --> ServiceOption
    EventService ..> ServicePackage

    %% ---------- Facade ----------
    class EventFacade {
        +createEvent(...) Event
        +updateEvent(...) Event
        +deleteEvent(String)
        +configurePackage(...) ServicePackage
        +addBooking(...) BookingDetails
        +getSummary(String) Summary
        +exportSummary(String, String, Path)
    }
    class BookingService
    class InMemoryEventStore
    EventFacade --> EventService
    EventFacade --> BookingService
    EventFacade --> ExporterFactory
    EventService --> InMemoryEventStore
    BookingService --> InMemoryEventStore

    %% ---------- Strategy ----------
    class PricingStrategy {
        <<interface>>
        +calculate(Booking) BigDecimal
    }
    class PriceCalculator {
        +setStrategy(PricingStrategy)
    }
    PricingStrategy <|.. RegularPricing
    PricingStrategy <|.. StudentDiscountPricing
    PricingStrategy <|.. GroupDiscountPricing
    PriceCalculator o--> PricingStrategy
    BookingService ..> PriceCalculator

    %% ---------- Factory Method ----------
    class ExporterFactory {
        <<abstract>>
        +createExporter(String)* ReportExporter
        +export(Summary, Path, String)
    }
    class ReportExporter {
        <<interface>>
        +export(Summary, Path)
    }
    ExporterFactory <|-- DefaultExporterFactory
    ReportExporter <|.. TxtExporter
    ReportExporter <|.. JsonExporter
    ReportExporter <|.. HtmlExporter
    DefaultExporterFactory ..> ReportExporter : creates

    %% ---------- GUI ----------
    class EventPanel
    class EventListPage
    class PackagePage
    EventPanel *-- EventListPage
    EventPanel *-- PackagePage
    EventListPage --> EventFacade
    PackagePage --> EventFacade
    EventPanel --> EventFacade
```

## Pattern participants

| Pattern | Role | Class |
| --- | --- | --- |
| Builder | Product | `Event` (private-package constructor, only built through the builder) |
| Builder | Builder | `EventBuilder` (`name()`, `date()`, `location()`, `capacity()`, `price()`, `build()`) |
| Builder | Director / client | `EventService.createEvent` / `updateEvent`, called from `EventListPage` through `EventFacade` |
| Decorator | Component | `ServiceOption` |
| Decorator | Concrete component | `BasePackage` |
| Decorator | Decorator | `OptionDecorator` |
| Decorator | Concrete decorators | `CateringDecorator`, `PhotographerDecorator`, `DecorDecorator` |
| Decorator | Client | `ServicePackage.configure`, driven by `PackagePage` checkboxes |
