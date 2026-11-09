package org.example.option;

import org.example.validation.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** Automated test of the Decorator pattern: price with several extras. */
class ServiceOptionDecoratorTest {

    @Test
    void basePackageAloneCostsBasePrice() {
        ServiceOption base = new BasePackage();
        assertEquals(new BigDecimal("20000.00"), base.getCost());
        assertEquals(BasePackage.DEFAULT_NAME, base.getDescription());
    }

    @Test
    void severalDecoratorsAddTheirCostsAndDescriptions() {
        ServiceOption option = new DecorDecorator(
                new PhotographerDecorator(
                        new CateringDecorator(new BasePackage("Base", new BigDecimal("20000")), 10)));
        // 20000 + 10 * 2500 + 30000 + 15000
        assertEquals(new BigDecimal("90000.00"), option.getCost());
        assertEquals("Base + Catering for 10 guests + Photographer + Decor", option.getDescription());
    }

    @Test
    void wrappingOrderDoesNotChangeTheTotal() {
        ServiceOption a = new PhotographerDecorator(new DecorDecorator(new BasePackage()));
        ServiceOption b = new DecorDecorator(new PhotographerDecorator(new BasePackage()));
        assertEquals(new BigDecimal("65000.00"), a.getCost());
        assertEquals(a.getCost(), b.getCost());
    }

    @Test
    void invalidDecoratorInputIsRejected() {
        assertThrows(ValidationException.class, () -> new CateringDecorator(new BasePackage(), 0));
        assertThrows(ValidationException.class, () -> new BasePackage("Base", new BigDecimal("-1")));
        assertThrows(ValidationException.class, () -> new BasePackage(" ", BigDecimal.ONE));
        assertThrows(NullPointerException.class, () -> new DecorDecorator(null));
    }
}
