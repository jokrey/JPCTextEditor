package jokrey.utilities.swing.text_editor.example;

import jokrey.utilities.swing.text_editor.ui.JPCSimpleTextEditor;

import javax.swing.*;
import java.awt.*;

public class TextEditor_Example extends JPanel {
    public static void main(String[] args) {
        JPCSimpleTextEditor textDisplay = new JPCSimpleTextEditor();
        textDisplay.setForeground(Color.BLACK);
        textDisplay.setFont(new Font("Arial", Font.BOLD, 13));
        textDisplay.setStandardLayout(textDisplay.getStandardLayout().copy_SetLines(80));
        FrameStarter.start("JPC Editor - this edition allows writing", textDisplay);
    }
}