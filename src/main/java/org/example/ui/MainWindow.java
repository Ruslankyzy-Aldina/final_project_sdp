package org.example.ui;

import org.example.facade.EventFacade;
import javax.swing.*;

public final class MainWindow extends JFrame {
    public MainWindow(EventFacade facade) {
        super("Event Desk · Booking & Reports");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setContentPane(new EventPanel(facade));
        setSize(1180, 780);
        setMinimumSize(new java.awt.Dimension(980, 700));
        setLocationRelativeTo(null);
    }
}
