package org.example.ui;

import org.example.facade.EventFacade;
import org.example.model.Event;
import org.example.validation.ValidationException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Events screen: list of events with create, edit and delete. All operations go through EventFacade. */
final class EventListPage extends JPanel {
    private final EventFacade facade;
    private final Consumer<String> onEventsChanged;
    private final Consumer<String> notice;
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Event", "Date", "Location", "Capacity", "Reserved", "Ticket price (KZT)"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField name = field("eventName");
    private final JTextField date = field("eventDate");
    private final JTextField location = field("eventLocation");
    private final JTextField capacity = field("eventCapacity");
    private final JTextField price = field("eventPrice");
    private final JButton update = new JButton("Save changes");
    private final JButton delete = new JButton("Delete event");
    private List<Event> events = List.of();
    private boolean reloading;
    /** Asks the user before deletion; replaceable so GUI tests can run without a modal dialog. */
    Predicate<Event> confirmDelete = event -> JOptionPane.showConfirmDialog(this,
            "Delete \"" + event.name() + "\" and its booking history?", "Delete event",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;

    EventListPage(EventFacade facade, Consumer<String> onEventsChanged, Consumer<String> notice) {
        super(new BorderLayout(0, 16));
        this.facade = facade;
        this.onEventsChanged = onEventsChanged;
        this.notice = notice;
        JLabel title = new JLabel("Events");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        table.setName("eventTable");
        table.setRowHeight(32); table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !reloading) onRowSelected();
        });
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(600, 180));
        add(scroll, BorderLayout.CENTER);
        add(editor(), BorderLayout.SOUTH);
        clearForm();
    }

    private JPanel editor() {
        JPanel grid = new JPanel(new GridLayout(0, 4, 12, 8));
        grid.add(new JLabel("Event name")); grid.add(name);
        grid.add(new JLabel("Date (YYYY-MM-DD)")); grid.add(date);
        grid.add(new JLabel("Location")); grid.add(location);
        grid.add(new JLabel("Capacity")); grid.add(capacity);
        grid.add(new JLabel("Ticket price (KZT)")); grid.add(price);

        JButton create = new JButton("Create event");
        JButton clear = new JButton("New / clear form");
        create.addActionListener(e -> perform(this::create));
        update.addActionListener(e -> perform(this::update));
        delete.addActionListener(e -> perform(this::delete));
        clear.addActionListener(e -> { table.clearSelection(); clearForm(); });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(create); buttons.add(update); buttons.add(delete); buttons.add(clear);

        JPanel editor = new JPanel(new BorderLayout(0, 10));
        editor.add(grid, BorderLayout.CENTER);
        editor.add(buttons, BorderLayout.SOUTH);
        return editor;
    }

    /** Reloads the table and selects the given event id (if present). */
    void reload(String selectedId) {
        reloading = true;
        try {
            events = facade.getEvents();
            model.setRowCount(0);
            int selectedRow = -1;
            for (int i = 0; i < events.size(); i++) {
                Event e = events.get(i);
                model.addRow(new Object[]{e.name(), e.date(), e.venue(), e.capacity(),
                        facade.getSummary(e.id()).reservedSeats(), e.unitPrice()});
                if (e.id().equals(selectedId)) selectedRow = i;
            }
            if (selectedRow >= 0) table.setRowSelectionInterval(selectedRow, selectedRow);
        } finally {
            reloading = false;
        }
        Event selected = selectedEvent();
        if (selected == null) clearForm(); else fillForm(selected);
    }

    private void create() {
        FormInput input = new FormInput();
        LocalDate d = input.date("Event date", date.getText());
        int c = input.wholeNumber("Capacity", capacity.getText());
        BigDecimal p = input.money("Ticket price", price.getText());
        input.throwIfInvalid();
        Event created = facade.createEvent(name.getText(), d, location.getText(), c, p);
        onEventsChanged.accept(created.id());
        notice.accept("Event created: " + created.name());
    }

    private void update() {
        Event selected = requireSelection("edit");
        FormInput input = new FormInput();
        LocalDate d = input.date("Event date", date.getText());
        int c = input.wholeNumber("Capacity", capacity.getText());
        BigDecimal p = input.money("Ticket price", price.getText());
        input.throwIfInvalid();
        Event updated = facade.updateEvent(selected.id(), name.getText(), d, location.getText(), c, p);
        onEventsChanged.accept(updated.id());
        notice.accept("Event updated: " + updated.name());
    }

    private void delete() {
        Event selected = requireSelection("delete");
        if (!confirmDelete.test(selected)) return;
        facade.deleteEvent(selected.id());
        onEventsChanged.accept(null);
        notice.accept("Event deleted: " + selected.name());
    }

    private void onRowSelected() {
        Event selected = selectedEvent();
        if (selected == null) { clearForm(); return; }
        fillForm(selected);
        onEventsChanged.accept(selected.id());
    }

    private Event selectedEvent() {
        int row = table.getSelectedRow();
        return row < 0 || row >= events.size() ? null : events.get(row);
    }

    private Event requireSelection(String action) {
        Event selected = selectedEvent();
        if (selected == null) throw new ValidationException("Select an event in the table to " + action + " it");
        return selected;
    }

    private void fillForm(Event e) {
        name.setText(e.name()); date.setText(e.date().toString()); location.setText(e.venue());
        capacity.setText(Integer.toString(e.capacity())); price.setText(e.unitPrice().toPlainString());
        update.setEnabled(true); delete.setEnabled(true);
    }

    private void clearForm() {
        name.setText(""); date.setText(LocalDate.now().plusDays(7).toString()); location.setText("");
        capacity.setText("20"); price.setText("1000.00");
        update.setEnabled(false); delete.setEnabled(false);
    }

    private void perform(Runnable action) {
        try { action.run(); }
        catch (RuntimeException ex) { ErrorDialog.show(this, ex); }
    }

    private static JTextField field(String componentName) {
        JTextField field = new JTextField();
        field.setName(componentName);
        return field;
    }
}
