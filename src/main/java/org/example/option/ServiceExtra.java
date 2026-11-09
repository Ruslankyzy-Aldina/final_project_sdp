package org.example.option;

/**
 * Extras the user can tick in the GUI. Each constant knows which decorator wraps the current
 * option, so adding a new extra means one new decorator class and one new constant here.
 */
public enum ServiceExtra {
    CATERING("Catering (2500 KZT per guest)") {
        @Override public ServiceOption decorate(ServiceOption option, int guests) {
            return new CateringDecorator(option, guests);
        }
    },
    PHOTOGRAPHER("Photographer (30000 KZT)") {
        @Override public ServiceOption decorate(ServiceOption option, int guests) {
            return new PhotographerDecorator(option);
        }
    },
    DECOR("Decor (15000 KZT)") {
        @Override public ServiceOption decorate(ServiceOption option, int guests) {
            return new DecorDecorator(option);
        }
    };

    private final String label;

    ServiceExtra(String label) { this.label = label; }

    public String label() { return label; }

    public abstract ServiceOption decorate(ServiceOption option, int guests);
}
