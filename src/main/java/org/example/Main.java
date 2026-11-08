package org.example;

import org.example.facade.EventFacade;
import org.example.sample.SampleData;
import org.example.ui.MainWindow;
import org.example.ui.Theme;
import javax.swing.*;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Theme.install();
            EventFacade facade = Arrays.asList(args).contains("--empty") ? new EventFacade() : SampleData.createDemo();
            new MainWindow(facade).setVisible(true);
        });
    }
}
