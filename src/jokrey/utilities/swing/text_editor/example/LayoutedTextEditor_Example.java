package jokrey.utilities.swing.text_editor.example;

import jokrey.utilities.swing.text_editor.text_storage.ContentListener;
import jokrey.utilities.swing.text_editor.text_storage.LinePartLayout;
import jokrey.utilities.swing.text_editor.ui.JPCLayoutedTextEditor;
import jokrey.utilities.swing.text_editor.ui.JPCLayoutedWrappingTextEditor;
import jokrey.utilities.swing.text_editor.ui.additional.CustomEditorConnector;
import jokrey.utilities.swing.text_editor.ui.additional.LayoutChangingPanel;
import jokrey.utilities.swing.text_editor.ui.additional.LoadStorePanel;

import javax.swing.*;
import java.awt.*;

/**
 * Example of a layouted text editor.
 * This example is the one that allows the end-user the access to the most functionality.
 *     Namely the functionality of self controlled nested layouting.
 */
public class LayoutedTextEditor_Example extends JPanel {
    public static void main(String[] args) {
        FrameStarter.start("JPC Editor - this edition simply allows anywhere, anytime custom layouting options", new LayoutedTextEditor_Example());
    }

    private JPCLayoutedTextEditor textDisplay;
    public LayoutedTextEditor_Example() {
        setLayout(new BorderLayout());

        LayoutChangingPanel headerPanel = new LayoutChangingPanel(new CustomEditorConnector() {
            @Override public Color getCurFG() {
                return textDisplay.getForeground();
            }
            @Override public Color getCurBG() {
                return textDisplay.getBackground();
            }
            @Override public Font getCurFont() {
                return textDisplay.getFont();
            }
            @Override public void fg_changed(Color fg) {
                if(fg != null)
                    textDisplay.setForeground(fg);
            }
            @Override public void bg_changed(Color bg) {
                if(bg != null)
                    textDisplay.setCursorBackground(bg);
            }
            @Override public void font_changed(Font font) {
                if(font != null)
                    textDisplay.setFont(font);
            }
        });

        textDisplay = new JPCLayoutedWrappingTextEditor();
        textDisplay.addContentListener(new ContentListener() {
            @Override public void userCursorLayoutChanged() {
                if(textDisplay==null)return;
                LinePartLayout layout = textDisplay.getCurrentLayout();
                headerPanel.updateDisplayValues(layout.fg, layout.bg, layout.font);
            }
        });

        add(textDisplay);
        headerPanel.add(new LoadStorePanel(textDisplay, true));

        add(headerPanel, BorderLayout.NORTH);

        textDisplay.setForeground(Color.BLACK);
        textDisplay.setBackground(new Color(0,0,0,0));
        textDisplay.setFont(new Font("Arial", Font.BOLD, 13));
    }
}
