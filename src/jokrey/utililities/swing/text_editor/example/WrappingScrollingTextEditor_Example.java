package jokrey.utililities.swing.text_editor.example;

import jokrey.utililities.swing.text_editor.ui.JPCSimpleWrappingTextEditor;
import jokrey.utililities.swing.text_editor.ui.core.JPC_Scroller;
import jokrey.utililities.swing.text_editor.user_input.ContextFunctionalityLibrary;

import java.awt.*;

public class WrappingScrollingTextEditor_Example {
    public static void main(String[] args) {
        JPCSimpleWrappingTextEditor textDisplay = new JPCSimpleWrappingTextEditor();
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

        textDisplay.setFont(new Font("Arial", Font.BOLD, 13));

        FrameStarter.start("JPC Editor - this edition wraps and scrolls", scroller);
    }
}