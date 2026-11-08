package org.example.ui;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;

public final class Theme {
    private Theme() { }
    public static void install() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (ReflectiveOperationException | UnsupportedLookAndFeelException ignored) { /* Keep the JDK default. */ }
        UIManager.put("defaultFont", new FontUIResource(Font.SANS_SERIF, Font.PLAIN, 14));
        UIManager.put("control", new Color(245, 248, 252));
        UIManager.put("nimbusBase", new Color(32, 72, 110));
        UIManager.put("nimbusSelectionBackground", new Color(211, 229, 247));
    }
}
