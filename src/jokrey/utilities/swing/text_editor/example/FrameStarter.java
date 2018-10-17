package jokrey.utilities.swing.text_editor.example;

import jokrey.utilities.swing.text_editor.ui.util.Util;

import javax.swing.*;
import java.awt.*;

public class FrameStarter {
    public static void start(String name, JComponent p) {
        JFrame frame = new JFrame(name);

        frame.add(p, BorderLayout.CENTER);

        frame.setSize(800, 600);
        Util.centerOnMouseScreen(frame);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
