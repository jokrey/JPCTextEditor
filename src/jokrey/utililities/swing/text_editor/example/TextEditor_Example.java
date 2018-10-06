package jokrey.utililities.swing.text_editor.example;

import jokrey.utililities.swing.text_editor.JPCSimpleTextEditor;
import javax.swing.*;
import java.awt.*;

public class TextEditor_Example extends JPanel {
    public static void main(String[] args) {
        JPCSimpleTextEditor textDisplay = new JPCSimpleTextEditor();
        textDisplay.setForeground(Color.BLACK);
        textDisplay.setFont(new Font("Arial", Font.BOLD, 13));
        FrameStarter.start("JPC Editor - this edition allows writing", textDisplay);
    }
}