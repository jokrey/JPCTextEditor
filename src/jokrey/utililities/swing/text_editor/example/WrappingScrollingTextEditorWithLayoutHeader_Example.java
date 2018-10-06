package jokrey.utililities.swing.text_editor.example;

import jokrey.utililities.swing.text_editor.JPCSimpleWrappingTextEditor;
import jokrey.utililities.swing.text_editor.JPC_Scroller;
import jokrey.utililities.swing.text_editor.text_storage.ContentListener;
import jokrey.utililities.swing.text_editor.user_input.ContextFunctionalityLibrary;
import jokrey.utililities.swing.text_editor.text_storage.ContentEditor;
import jokrey.utililities.swing.text_editor.text_storage.WrappingContentEditor;

import javax.swing.*;
import java.awt.*;

/**
 * Example of a wrapping text editor.
 * This example allows customizing the layout for the entire text at once.
 */
public class WrappingScrollingTextEditorWithLayoutHeader_Example extends JPanel {
    public static void main(String[] args) {
        FrameStarter.start("JPC Editor - this edition wraps, scrolls, has one for all layout", new WrappingScrollingTextEditorWithLayoutHeader_Example());
    }

    private JPCSimpleWrappingTextEditor textDisplay;
    public WrappingScrollingTextEditorWithLayoutHeader_Example() {
        setLayout(new BorderLayout());
        JCheckBox wrappingToggleButton = new JCheckBox("wrapping on", true);
        wrappingToggleButton.addActionListener(e -> textDisplay.setLineWrap(wrappingToggleButton.isSelected()));

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
                if(fg!=null)
                    textDisplay.setForeground(fg);
            }
            @Override public void bg_changed(Color bg) {
                if(bg!=null)
                    textDisplay.setBackground(bg);
            }
            @Override public void font_changed(Font font) {
                if(font!=null)
                    textDisplay.setFont(font);
            }
        });

        textDisplay = new JPCSimpleWrappingTextEditor() {
            @Override public void setLineWrap(boolean wrap_lines) {
                super.setLineWrap(wrap_lines);
                wrappingToggleButton.setSelected(wrap_lines);
            }
        };
        textDisplay.addContentListener(new ContentListener() {
            @Override public void standardLayoutChanged() {
                if(textDisplay==null)return;
                headerPanel.updateDisplayValues(
                        textDisplay.getForeground(),
                        textDisplay.getBackground(),
                        textDisplay.getFont());
                wrappingToggleButton.setSelected(textDisplay.isLineWrapEnabled());
            }
        });
        JPC_Scroller scroller = new JPC_Scroller(textDisplay);

        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_TOGGLE_WRAPPING(textDisplay));

        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_SELECT_CURRENT_LINE(textDisplay, textDisplay.getInputHandler()));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_SELECT_CURRENT_WORD(textDisplay, textDisplay.getInputHandler()));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_TOGGLE_CAPITALIZE_WORD(textDisplay.getInputHandler()));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_JUMP_TO_END_OF_LINE(textDisplay, textDisplay.getInputHandler()));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_JUMP_TO_START_OF_LINE(textDisplay, textDisplay.getInputHandler()));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_PAGE_UP(scroller, textDisplay));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_PAGE_DOWN(scroller, textDisplay));

        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_FONT_SIZE_UP(textDisplay));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_FONT_SIZE_DOWN(textDisplay));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_FONT_CYCLE_STYLE(textDisplay));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_CYCLE_FG_COLOR(textDisplay));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_CYCLE_BG_COLOR(textDisplay));

        add(scroller);


        headerPanel.add(wrappingToggleButton);
        headerPanel.add(new LoadStorePanel(textDisplay));

        add(headerPanel, BorderLayout.NORTH);

        textDisplay.setBackground(headerPanel.getBackground());
        textDisplay.setFont(new Font("Arial", Font.BOLD, 13));
    }
}
