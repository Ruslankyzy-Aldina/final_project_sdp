package org.example.ui;

import org.example.booking.BookingStatus;
import org.example.facade.EventFacade;
import org.example.model.*;
import org.example.model.Event;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.*;
import java.util.List;
import java.util.Locale;

/** View and input parsing only; all business operations go through EventFacade. */
public final class EventPanel extends JPanel {
    private final EventFacade facade;
    private final JComboBox<Event> eventChoice = new JComboBox<>();
    private final JComboBox<Participant> participantChoice = new JComboBox<>();
    private final JComboBox<PricingPlan> planChoice = new JComboBox<>(PricingPlan.values());
    private final JComboBox<BookingStatus> statusChoice = new JComboBox<>(BookingStatus.values());
    private final JComboBox<String> formatChoice = new JComboBox<>(new String[]{"TXT", "JSON", "HTML"});
    private final JSpinner seats = new JSpinner(new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1));
    private final DefaultTableModel bookingModel = tableModel("Participant", "Seats", "Plan", "Status", "Price (KZT)");
    private final DefaultTableModel participantModel = tableModel("Participant", "ID");
    private final JTable bookingTable = table(bookingModel);
    private final DefaultTableModel summaryModel = tableModel("Participant", "Seats", "Plan", "Status", "Quoted price (KZT)");
    private final JLabel eventDetails = new JLabel(" ");
    private final JLabel total = new JLabel(" ");
    private final JLabel summaryHeading = new JLabel(" ");
    private final JLabel notice = new JLabel("Ready · Data is kept for this session only");
    private final CardLayout cards = new CardLayout();
    private final JPanel pages = new JPanel(cards);
    private List<BookingDetails> currentBookings = List.of();
    private boolean refreshing;
    private boolean exporting;
    private final JButton exportButton = new JButton("Export report…");
    private final EventListPage eventListPage;
    private final PackagePage packagePage;

    public EventPanel(EventFacade facade) {
        this.facade = facade;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 248, 252));
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(23, 44, 67));
        sidebar.setBorder(new EmptyBorder(30, 22, 30, 22));
        sidebar.setPreferredSize(new Dimension(210, 600));
        JLabel brand = new JLabel("EVENT DESK");
        brand.setForeground(Color.WHITE); brand.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        sidebar.add(brand); sidebar.add(Box.createVerticalStrut(36));
        for (String page : new String[]{"Events", "Packages", "Participants", "Bookings", "Summary"}) {
            JButton nav = new JButton(page);
            nav.setMaximumSize(new Dimension(166, 42)); nav.setAlignmentX(Component.LEFT_ALIGNMENT);
            nav.addActionListener(e -> showPage(page)); sidebar.add(nav); sidebar.add(Box.createVerticalStrut(12));
        }
        sidebar.add(Box.createVerticalGlue());
        JLabel foot = new JLabel("BOOK · REVIEW · EXPORT");
        foot.setForeground(new Color(174, 195, 217)); sidebar.add(foot); add(sidebar, BorderLayout.WEST);
        JPanel workspace = new JPanel(new BorderLayout(0, 20));
        workspace.setBorder(new EmptyBorder(28, 30, 18, 30));
        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.add(heading("Manage your events"), BorderLayout.NORTH);
        JPanel selector = new JPanel(new BorderLayout(12, 0));
        selector.add(new JLabel("Selected event"), BorderLayout.WEST); selector.add(eventChoice, BorderLayout.CENTER);
        top.add(selector, BorderLayout.CENTER); top.add(eventDetails, BorderLayout.SOUTH);
        workspace.add(top, BorderLayout.NORTH);
        eventListPage = new EventListPage(facade, this::refreshChoices, notice::setText);
        packagePage = new PackagePage(facade, notice::setText);
        pages.add(eventListPage, "Events"); pages.add(packagePage, "Packages");
        pages.add(participantsPage(), "Participants");
        pages.add(bookingsPage(), "Bookings"); pages.add(summaryPage(), "Summary");
        workspace.add(pages, BorderLayout.CENTER);
        notice.setForeground(new Color(67, 91, 114)); workspace.add(notice, BorderLayout.SOUTH);
        add(workspace, BorderLayout.CENTER);
        eventChoice.addActionListener(e -> { if (!refreshing) { eventListPage.reload(selectedId()); refreshDetails(); } });
        refreshChoices(null);
    }

    public void showPage(String page) { refreshDetails(); cards.show(pages, page); }

    private JPanel participantsPage() {
        JPanel page = page("Participants"); page.add(scroll(table(participantModel)), BorderLayout.CENTER);
        JTextField name = new JTextField(20); JButton add = new JButton("Add participant");
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(new JLabel("Name")); row.add(name); row.add(add);
        add.addActionListener(e -> perform(() -> {
            Participant created = facade.addParticipant(name.getText());
            refreshChoices(selectedId()); participantChoice.setSelectedItem(created); name.setText("");
            notice.setText("Participant added: " + created.name());
        }));
        page.add(row, BorderLayout.SOUTH); return page;
    }

    private JPanel bookingsPage() {
        JPanel page = page("Bookings"); page.add(scroll(bookingTable), BorderLayout.CENTER);
        JPanel controls = new JPanel(new BorderLayout(0, 10));
        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        participantChoice.setPreferredSize(new Dimension(155, 28)); seats.setPreferredSize(new Dimension(65, 28));
        planChoice.setPreferredSize(new Dimension(120, 28));
        JButton add = new JButton("Book seats");
        addRow.add(participantChoice); addRow.add(new JLabel("Seats")); addRow.add(seats);
        addRow.add(planChoice); addRow.add(add);
        add.addActionListener(e -> perform(() -> {
            Participant p = (Participant) participantChoice.getSelectedItem();
            if (p == null) throw new IllegalArgumentException("Add a participant first");
            seats.commitEdit();
            facade.addBooking(requireEvent().id(), p.id(), ((Number) seats.getValue()).intValue(),
                    (PricingPlan) planChoice.getSelectedItem());
            refreshDetails(); notice.setText("Booking created");
        }));
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT)); JButton change = new JButton("Apply status");
        statusRow.add(new JLabel("Selected booking")); statusRow.add(statusChoice); statusRow.add(change);
        change.addActionListener(e -> perform(() -> {
            int row = bookingTable.getSelectedRow();
            if (row < 0) throw new IllegalArgumentException("Select a booking in the table first");
            BookingDetails b = currentBookings.get(bookingTable.convertRowIndexToModel(row));
            facade.changeStatus(b.booking().id(), (BookingStatus) statusChoice.getSelectedItem());
            refreshDetails(); notice.setText("Booking status updated");
        }));
        controls.add(addRow, BorderLayout.NORTH); controls.add(statusRow, BorderLayout.CENTER);
        controls.add(new JLabel("Student: 10% off · Group: 15% off for at least 5 seats"), BorderLayout.SOUTH);
        page.add(controls, BorderLayout.SOUTH); return page;
    }

    private JPanel summaryPage() {
        JPanel page = page("Event summary"); JPanel center = new JPanel(new BorderLayout(0, 15));
        summaryHeading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        center.add(summaryHeading, BorderLayout.NORTH); center.add(scroll(table(summaryModel)), BorderLayout.CENTER);
        center.add(new JLabel("Cancelled bookings stay in history; their seats and prices are excluded from totals."), BorderLayout.SOUTH);
        page.add(center, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout()); total.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        bottom.add(total, BorderLayout.WEST); JPanel export = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        export.add(formatChoice); export.add(exportButton); exportButton.addActionListener(e -> perform(this::chooseExport));
        bottom.add(export, BorderLayout.EAST); page.add(bottom, BorderLayout.SOUTH); return page;
    }

    private void chooseExport() {
        Event event = requireEvent();
        String format = ((String) formatChoice.getSelectedItem()).toLowerCase(Locale.ROOT);
        JFileChooser chooser = new JFileChooser(); chooser.setDialogTitle("Export event summary");
        chooser.setFileFilter(new FileNameExtensionFilter(format.toUpperCase(Locale.ROOT) + " report", format));
        chooser.setSelectedFile(new java.io.File("event-summary." + format));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path selected = chooser.getSelectedFile().toPath();
        if (!selected.getFileName().toString().toLowerCase(Locale.ROOT).endsWith("." + format))
            selected = selected.resolveSibling(selected.getFileName() + "." + format);
        final Path path = selected;
        if (Files.exists(path) && JOptionPane.showConfirmDialog(this, "Replace the existing report?\n" + path,
                "Replace report", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        exporting = true; exportButton.setEnabled(false); notice.setText("Saving report…");
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception { facade.exportSummary(event.id(), format, path); return null; }
            @Override protected void done() {
                exporting = false; exportButton.setEnabled(true);
                try { get(); notice.setText("Report saved: " + path.toAbsolutePath()); }
                catch (Exception ex) {
                    Throwable cause = ex.getCause(); ErrorDialog.show(EventPanel.this, cause instanceof Exception actual ? actual : ex);
                    notice.setText("Export failed; choose a writable destination and try again");
                }
            }
        }.execute();
    }

    private void refreshChoices(String eventId) {
        refreshing = true; Participant old = (Participant) participantChoice.getSelectedItem();
        eventChoice.removeAllItems(); for (Event event : facade.getEvents()) eventChoice.addItem(event);
        for (int i = 0; i < eventChoice.getItemCount(); i++)
            if (eventChoice.getItemAt(i).id().equals(eventId)) eventChoice.setSelectedIndex(i);
        participantChoice.removeAllItems(); participantModel.setRowCount(0);
        for (Participant p : facade.getParticipants()) { participantChoice.addItem(p); participantModel.addRow(new Object[]{p.name(), p.id()}); }
        if (old != null) participantChoice.setSelectedItem(old);
        refreshing = false; eventListPage.reload(selectedId()); refreshDetails();
    }

    private void refreshDetails() {
        bookingModel.setRowCount(0); summaryModel.setRowCount(0);
        Event event = (Event) eventChoice.getSelectedItem();
        packagePage.setEvent(event);
        if (event == null) {
            currentBookings = List.of(); eventDetails.setText("Create your first event to get started.");
            summaryHeading.setText("No event selected"); total.setText("Total: 0.00 KZT"); exportButton.setEnabled(false); return;
        }
        Summary summary = facade.getSummary(event.id()); currentBookings = summary.bookings();
        eventDetails.setText(event.venue() + "  |  " + summary.reservedSeats() + " / " + event.capacity()
                + " seats reserved  |  " + summary.availableSeats() + " available");
        summaryHeading.setText(event.name() + " · " + summary.participants().size() + " participants");
        total.setText("Total: " + summary.totalPrice().toPlainString() + " KZT"); exportButton.setEnabled(!exporting);
        for (BookingDetails b : currentBookings) {
            Object[] row = {b.participant().name(), b.booking().seatCount(), b.pricingPlan(), b.booking().status(), b.price()};
            bookingModel.addRow(row); summaryModel.addRow(row);
        }
    }

    private String selectedId() { Event e = (Event) eventChoice.getSelectedItem(); return e == null ? null : e.id(); }
    private Event requireEvent() {
        Event e = (Event) eventChoice.getSelectedItem();
        if (e == null) throw new IllegalArgumentException("Create an event first"); return e;
    }
    private void perform(Action action) {
        try { action.run(); }
        catch (java.text.ParseException ex) { ErrorDialog.show(this, new IllegalArgumentException("Enter a valid number of seats")); }
        catch (Exception ex) { ErrorDialog.show(this, ex); }
    }
    @FunctionalInterface private interface Action { void run() throws Exception; }
    private static JLabel heading(String text) {
        JLabel label = new JLabel(text); label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28)); return label;
    }
    private static JPanel page(String title) {
        JPanel panel = new JPanel(new BorderLayout(0, 20)); JLabel label = new JLabel(title);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22)); panel.add(label, BorderLayout.NORTH); return panel;
    }
    private static DefaultTableModel tableModel(String... names) {
        return new DefaultTableModel(names, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
    }
    private static JTable table(DefaultTableModel model) {
        JTable table = new JTable(model); table.setRowHeight(36); table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false); return table;
    }
    private static JScrollPane scroll(JTable table) {
        JScrollPane pane = new JScrollPane(table);
        pane.setColumnHeaderView(table.getTableHeader());
        return pane;
    }
}
