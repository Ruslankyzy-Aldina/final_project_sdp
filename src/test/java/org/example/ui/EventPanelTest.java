package org.example.ui;

import org.example.facade.EventFacade;
import org.example.sample.SampleData;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EventPanelTest {
    @Test void navigationAndBookingActionsUseTheFacade() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Theme.install();
            EventFacade facade = SampleData.createDemo();
            EventPanel panel = new EventPanel(facade);
            for (String page : new String[]{"Events", "Participants", "Bookings", "Summary"}) button(panel, page).doClick();
            button(panel, "Bookings").doClick();
            button(panel, "Book seats").doClick();
            assertEquals(9, facade.getSummary(facade.getEvents().getFirst().id()).reservedSeats());
            JTable bookings = descendants(panel, JTable.class).stream().filter(Component::isVisible)
                    .filter(t -> t.getColumnCount() == 5 && t.getRowCount() == 4).findFirst().orElseThrow();
            bookings.setRowSelectionInterval(3, 3);
            JComboBox<?> statuses = descendants(panel, JComboBox.class).stream()
                    .filter(c -> c.getItemCount() > 0 && c.getItemAt(0) instanceof org.example.booking.BookingStatus)
                    .findFirst().orElseThrow();
            statuses.setSelectedItem(org.example.booking.BookingStatus.CANCELLED);
            button(panel, "Apply status").doClick();
            assertEquals(8, facade.getSummary(facade.getEvents().getFirst().id()).reservedSeats());
        });
    }
    @Test void emptySessionCanNavigateAndHasNoExportUntilEventExists() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            EventPanel panel = new EventPanel(new EventFacade());
            for (String page : new String[]{"Events", "Participants", "Bookings", "Summary"}) button(panel, page).doClick();
            assertFalse(button(panel, "Export report…").isEnabled());
        });
    }
    private static JButton button(Container parent, String text) {
        return descendants(parent, JButton.class).stream().filter(b -> text.equals(b.getText())).findFirst().orElseThrow();
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
