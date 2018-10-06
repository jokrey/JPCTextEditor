package jokrey.utililities.swing.text_editor.example;

import javax.swing.*;
import java.awt.*;

public class FrameStarter {
    public static void start(String name, JComponent p) {
        JFrame frame = new JFrame(name);

        frame.add(p, BorderLayout.CENTER);

        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);//centered on main monitor
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
