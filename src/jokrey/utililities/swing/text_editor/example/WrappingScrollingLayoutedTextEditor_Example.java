package jokrey.utililities.swing.text_editor.example;

import jokrey.utililities.swing.text_editor.text_storage.ContentListener;
import jokrey.utililities.swing.text_editor.text_storage.LinePart;
import jokrey.utililities.swing.text_editor.text_storage.LinePartLayout;
import jokrey.utililities.swing.text_editor.ui.JPCLayoutedWrappingTextEditor;
import jokrey.utililities.swing.text_editor.ui.additional.CustomEditorConnector;
import jokrey.utililities.swing.text_editor.ui.additional.LayoutChangingPanel;
import jokrey.utililities.swing.text_editor.ui.additional.LoadStorePanel;
import jokrey.utililities.swing.text_editor.ui.core.JPC_Scroller;
import jokrey.utililities.swing.text_editor.user_input.ContextFunctionalityLibrary;

import javax.swing.*;
import java.awt.*;

/**
 * Example of a wrapping text editor, with extensive layout functionality.
 * This example is the one that allows the end-user the access to the most functionality.
 *     Namely the functionality of self controlled nested layouts.
 */
public class WrappingScrollingLayoutedTextEditor_Example extends JPanel {
    public static void main(String[] args) {
        FrameStarter.start("JPC Editor - this edition wraps, scrolls, and allows custom anywhere, anytime layouting options", new WrappingScrollingLayoutedTextEditor_Example());
    }

    private JPCLayoutedWrappingTextEditor textDisplay;
    public WrappingScrollingLayoutedTextEditor_Example() {
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
                if(fg!=null) textDisplay.setForeground(fg);
            }
            @Override public void bg_changed(Color bg) {
                if(bg!=null) textDisplay.setCursorBackground(bg);
            }
            @Override public void font_changed(Font font) {
                if(font!=null) textDisplay.setFont(font);
            }
        });

        textDisplay = new JPCLayoutedWrappingTextEditor();
        textDisplay.addContentListener(new ContentListener() {
            @Override public void userCursorLayoutChanged() {
                if(textDisplay==null)return;
                LinePartLayout layout = textDisplay.getCurrentLayout();
                headerPanel.updateDisplayValues(layout.fg, layout.bg, layout.font);
                wrappingToggleButton.setSelected(textDisplay.isLineWrapEnabled());
            }
        });
        textDisplay.setHint(new LinePart("Please click here and type a text.", new LinePartLayout.UnInstantiated(Color.gray, null, null)));
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

        textDisplay.setForeground(Color.BLACK);
        textDisplay.setBackground(headerPanel.getBackground());
        textDisplay.setFont(new Font("Arial", Font.BOLD, 13));
    }
}