package org.example.ui;

import org.example.facade.EventFacade;
import org.example.option.ServiceExtra;
import org.example.sample.SampleData;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** GUI actions of the Events and Packages screens, run on the EDT without modal dialogs. */
class EventListPageTest {

    @Test
    void createEditAndDeleteEventFromTheEventsScreen() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            EventFacade facade = new EventFacade();
            EventPanel panel = new EventPanel(facade);
            EventListPage page = descendants(panel, EventListPage.class).getFirst();
            page.confirmDelete = event -> true;

            text(panel, "eventName").setText("Spring Gala");
            text(panel, "eventDate").setText(LocalDate.now().plusDays(10).toString());
            text(panel, "eventLocation").setText("Main Hall");
            text(panel, "eventCapacity").setText("15");
            text(panel, "eventPrice").setText("2500");
            button(panel, "Create event").doClick();
            assertEquals(1, facade.getEvents().size());
            assertEquals(1, table(panel, "eventTable").getRowCount());

            // The created event is selected and loaded into the form for editing.
            assertEquals("Spring Gala", text(panel, "eventName").getText());
            text(panel, "eventCapacity").setText("30");
            button(panel, "Save changes").doClick();
            assertEquals(30, facade.getEvents().getFirst().capacity());
            assertEquals(new BigDecimal("2500.00"), facade.getEvents().getFirst().unitPrice());

            button(panel, "Delete event").doClick();
            assertTrue(facade.getEvents().isEmpty());
            assertEquals(0, table(panel, "eventTable").getRowCount());
        });
    }

    @Test
    void packageScreenShowsDecoratedPriceAndSavesExtras() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            EventFacade facade = SampleData.createDemo();
            EventPanel panel = new EventPanel(facade);
            button(panel, "Packages").doClick();
            String conference = facade.getEvents().getFirst().id();

            // Demo conference: base 20000 + catering 12 * 2500 + photographer 30000
            assertTrue(label(panel, "packageCost").getText().contains("80000.00"));
            checkbox(panel, ServiceExtra.DECOR).doClick();
            assertTrue(label(panel, "packageCost").getText().contains("95000.00"));
            assertEquals(2, facade.getPackage(conference).extras().size(), "preview must not save");

            button(panel, "Save package").doClick();
            assertEquals(new BigDecimal("95000.00"), facade.getPackage(conference).cost());
        });
    }

    private static JTextField text(Container root, String name) {
        return descendants(root, JTextField.class).stream().filter(f -> name.equals(f.getName())).findFirst().orElseThrow();
    }
    private static JTable table(Container root, String name) {
        return descendants(root, JTable.class).stream().filter(t -> name.equals(t.getName())).findFirst().orElseThrow();
    }
    private static JLabel label(Container root, String name) {
        return descendants(root, JLabel.class).stream().filter(l -> name.equals(l.getName())).findFirst().orElseThrow();
    }
    private static JCheckBox checkbox(Container root, ServiceExtra extra) {
        return descendants(root, JCheckBox.class).stream()
                .filter(b -> ("extra-" + extra.name()).equals(b.getName())).findFirst().orElseThrow();
    }
    private static JButton button(Container root, String text) {
        return descendants(root, JButton.class).stream().filter(b -> text.equals(b.getText())).findFirst().orElseThrow();
    }
    private static <T> List<T> descendants(Container parent, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (Component child : parent.getComponents()) {
            if (type.isInstance(child)) result.add(type.cast(child));
            if (child instanceof Container nested) result.addAll(descendants(nested, type));
        }
        return result;
    }
}
