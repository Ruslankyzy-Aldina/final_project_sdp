package org.example.ui;

import org.example.facade.EventFacade;
import org.example.model.Event;
import org.example.model.ServicePackage;
import org.example.option.BasePackage;
import org.example.option.ServiceExtra;
import org.example.validation.ValidationException;

import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/** Package screen: tick extras for the selected event; each extra wraps the package in a decorator. */
final class PackagePage extends JPanel {
    private final EventFacade facade;
    private final Consumer<String> notice;
    private final Map<ServiceExtra, JCheckBox> boxes = new EnumMap<>(ServiceExtra.class);
    private final JLabel eventLabel = new JLabel(" ");
    private final JTextArea description = new JTextArea(3, 40);
    private final JLabel cost = new JLabel(" ");
    private final JLabel saved = new JLabel(" ");
    private final JButton save = new JButton("Save package");
    private Event event;
    private boolean loading;

    PackagePage(EventFacade facade, Consumer<String> notice) {
        super(new BorderLayout(0, 20));
        this.facade = facade;
        this.notice = notice;
        JLabel title = new JLabel("Service package");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JPanel options = new JPanel();
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        eventLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        options.add(eventLabel); options.add(Box.createVerticalStrut(12));
        options.add(new JLabel("Included: " + BasePackage.DEFAULT_NAME + ", "
                + BasePackage.DEFAULT_PRICE.toPlainString() + " KZT"));
        options.add(Box.createVerticalStrut(12));
        options.add(new JLabel("Optional extras (catering is charged per seat of event capacity):"));
        for (ServiceExtra extra : ServiceExtra.values()) {
            JCheckBox box = new JCheckBox(extra.label());
            box.setName("extra-" + extra.name());
            box.addActionListener(e -> { if (!loading) perform(this::preview); });
            boxes.put(extra, box);
            options.add(box);
        }
        add(options, BorderLayout.CENTER);

        description.setName("packageDescription");
        description.setLineWrap(true); description.setWrapStyleWord(true); description.setEditable(false);
        description.setOpaque(false);
        cost.setName("packageCost");
        cost.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        save.addActionListener(e -> perform(this::save));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actions.add(save); actions.add(saved);
        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.add(description, BorderLayout.NORTH);
        bottom.add(cost, BorderLayout.CENTER);
        bottom.add(actions, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);
        setEvent(null);
    }

    /** Shows the saved package of the selected event, or disables the screen if none is selected. */
    void setEvent(Event selected) {
        event = selected;
        loading = true;
        try {
            boolean enabled = selected != null;
            boxes.values().forEach(b -> { b.setEnabled(enabled); b.setSelected(false); });
            save.setEnabled(enabled);
            if (!enabled) {
                eventLabel.setText("Create or select an event first");
                description.setText(""); cost.setText(" "); saved.setText(" ");
                return;
            }
            ServicePackage current = facade.getPackage(selected.id());
            current.extras().forEach(extra -> boxes.get(extra).setSelected(true));
            eventLabel.setText("Event: " + selected.name() + " · " + selected.capacity() + " guests");
            show(current);
            saved.setText("Saved package");
        } finally {
            loading = false;
        }
    }

    private void preview() {
        show(facade.previewPackage(requireEvent().id(), selectedExtras()));
        saved.setText("Unsaved changes");
    }

    private void save() {
        ServicePackage result = facade.configurePackage(requireEvent().id(), selectedExtras());
        show(result);
        saved.setText("Saved package");
        notice.accept("Package saved for " + event.name() + ": " + result.cost().toPlainString() + " KZT");
    }

    private void show(ServicePackage servicePackage) {
        description.setText(servicePackage.description());
        cost.setText("Package total: " + servicePackage.cost().toPlainString() + " KZT");
    }

    private Set<ServiceExtra> selectedExtras() {
        Set<ServiceExtra> result = EnumSet.noneOf(ServiceExtra.class);
        boxes.forEach((extra, box) -> { if (box.isSelected()) result.add(extra); });
        return result;
    }

    private Event requireEvent() {
        if (event == null) throw new ValidationException("Create or select an event first");
        return event;
    }

    private void perform(Runnable action) {
        try { action.run(); }
        catch (RuntimeException ex) { ErrorDialog.show(this, ex); }
    }
}
