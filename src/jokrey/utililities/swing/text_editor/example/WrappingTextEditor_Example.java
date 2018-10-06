package jokrey.utililities.swing.text_editor.example;

import jokrey.utililities.swing.text_editor.JPCSimpleWrappingTextEditor;

import javax.swing.*;
import java.awt.*;

public class WrappingTextEditor_Example extends JPanel {
    public static void main(String[] args) {
        JPCSimpleWrappingTextEditor textDisplay = new JPCSimpleWrappingTextEditor();
        textDisplay.setForeground(Color.BLACK);
        textDisplay.setFont(new Font("Arial", Font.BOLD, 13));
        FrameStarter.start("JPC Editor - this edition allows writing and wrapping(not necessarily rapping)", textDisplay);
    }
}