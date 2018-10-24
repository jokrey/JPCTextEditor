package jokrey.utilities.swing.text_editor.example;

import jokrey.utilities.swing.text_editor.text_storage.lineparteffects.LPEffect_HorizontalLine;
import jokrey.utilities.swing.text_editor.text_storage.lineparteffects.LPEffect_UnderlineLine;
import jokrey.utilities.swing.text_editor.ui.JPCSimpleTextEditor;

import javax.swing.*;
import java.awt.*;

public class TextEditor_Example extends JPanel {
    public static void main(String[] args) {
        JPCSimpleTextEditor textDisplay = new JPCSimpleTextEditor();
        textDisplay.setForeground(Color.BLACK);
        textDisplay.setFont(new Font("Arial", Font.BOLD, 13));
//        textDisplay.setStandardLayout(textDisplay.getStandardLayout().copy_ChangeAddBackgroundEffect(new LPEffect_HorizontalLine(Color.black, 5)));
        textDisplay.setStandardLayout(textDisplay.getStandardLayout().copy_ChangeAddBackgroundEffect(new LPEffect_UnderlineLine()));
        textDisplay.setHintText("This is a test.");
        FrameStarter.start("JPC Editor - this edition allows writing", textDisplay);
    }
}