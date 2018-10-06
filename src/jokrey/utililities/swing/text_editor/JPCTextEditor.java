package jokrey.utililities.swing.text_editor;

import jokrey.utililities.swing.text_editor.text_storage.ContentListener;
import jokrey.utililities.swing.text_editor.text_storage.LinePart;

import javax.swing.*;
import java.awt.*;

public interface JPCTextEditor {
    void clearContextActions();
    void addContextAction(Action... actions);
    void start_find_replace_frame();

    void setFont(Font font);
    void setForeground(Color fg);
    void setBackground(Color bg);
    Font getFont();
    Color getForeground();
    Color getBackground();


    void setText(String text);
    String getText();
    String getText_with_encoded_layout();
    void setText_with_encoded_layout(String text);
    String getTextFromVisibleLines();

    void setHint(LinePart hint);
    void setHintText(String hint);
    LinePart getHint();

    void setEditable(boolean editable);
    boolean isEditable();
    void clearSelection();

    void setAllowTabs(boolean tabs);

    void setCursorPosition(int x, int y);
    void addContentListener(ContentListener contentListener);
}
