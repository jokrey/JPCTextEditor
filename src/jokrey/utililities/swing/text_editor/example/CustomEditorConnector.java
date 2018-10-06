package jokrey.utililities.swing.text_editor.example;

import java.awt.*;

public interface CustomEditorConnector {
    Color getCurFG();
    Color getCurBG();
    Font getCurFont();

    void fg_changed(Color fg);
    void bg_changed(Color bg);
    void font_changed(Font font);
}
