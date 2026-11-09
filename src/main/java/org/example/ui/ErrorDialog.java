package org.example.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;

public final class ErrorDialog {
    private ErrorDialog() { }
    public static void show(Component parent, Exception error) {
        JOptionPane.showMessageDialog(parent, content(message(error)), "Action could not be completed", JOptionPane.ERROR_MESSAGE);
    }
    static String message(Exception error) {
        if (error instanceof IOException)
            return "The report could not be saved. Check the destination folder and write permissions.\n" + error.getMessage();
        if (error instanceof NumberFormatException) return "Enter a valid number for capacity, seats or ticket price.";
        if (error instanceof java.time.DateTimeException) return "Enter a valid date in YYYY-MM-DD format.";
        if (error instanceof IllegalArgumentException) return error.getMessage();
        error.printStackTrace();
        return "An unexpected error occurred. Please try again. Technical details were written to the console.";
    }
    public static JPanel content(String message) {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        JLabel title = new JLabel("Please review your request");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        JTextArea detail = new JTextArea(message, 4, 42);
        detail.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        detail.setLineWrap(true); detail.setWrapStyleWord(true); detail.setEditable(false); detail.setOpaque(false);
        panel.add(title, BorderLayout.NORTH); panel.add(detail, BorderLayout.CENTER);
        return panel;
    }
}
