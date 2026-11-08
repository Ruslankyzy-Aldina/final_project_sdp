package org.example.ui;

import org.example.facade.EventFacade;
import org.example.model.PricingPlan;
import org.example.sample.SampleData;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;

/** Reproducible offscreen captures of the real Swing components, without a desktop session. */
public final class ScreenshotCapture {
    public static void main(String[] args) throws Exception {
        Path destination = Path.of(args.length == 0 ? "docs/screenshots" : args[0]);
        Files.createDirectories(destination);
        EventFacade facade = SampleData.createDemo();
        for (String format : new String[]{"txt", "json", "html"})
            facade.exportSummary(facade.getEvents().getFirst().id(), format, destination.resolve("sample-summary." + format));
        SwingUtilities.invokeAndWait(() -> {
            Theme.install();
            EventPanel panel = new EventPanel(facade);
            for (String page : new String[]{"Events", "Participants", "Bookings", "Summary"}) {
                panel.showPage(page); render(panel, 1180, 740, destination.resolve(page.toLowerCase() + ".png"));
            }
            try {
                facade.addBooking(facade.getEvents().get(1).id(), facade.getParticipants().getFirst().id(), 1, PricingPlan.REGULAR);
                throw new AssertionError("Expected capacity rejection");
            } catch (IllegalArgumentException expected) {
                JOptionPane error = new JOptionPane(ErrorDialog.content(ErrorDialog.message(expected)), JOptionPane.ERROR_MESSAGE);
                render(error, 650, 260, destination.resolve("capacity-error.png"));
            }
        });
    }
    private static void render(JComponent component, int width, int height, Path path) {
        component.setSize(width, height); layout(component);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics(); component.printAll(graphics); graphics.dispose();
        try { ImageIO.write(image, "png", path.toFile()); }
        catch (Exception ex) { throw new RuntimeException(ex); }
    }
    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) if (child instanceof Container nested) layout(nested);
    }
}
