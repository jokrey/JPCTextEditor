package jokrey.utilities.swing.text_editor;

import jokrey.utilities.swing.text_editor.text_storage.ContentListener;
import jokrey.utilities.swing.text_editor.text_storage.DecoratedLinePart;

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
    void setText(DecoratedLinePart[] text);
    void setText_with_encoded_layout(String text);
    String getText();
    DecoratedLinePart[] getTextAsLineParts();
    String getText_with_encoded_layout();
    String getTextFromVisibleLines();

    void setHint(DecoratedLinePart hint);
    void setHintText(String hint);
    DecoratedLinePart getHint();

    void setEditable(boolean editable);
    boolean isEditable();
    void clearSelection();

    void setAllowTabs(boolean tabs);

    void setCursorPosition(int x, int y);
    void addContentListener(ContentListener contentListener);
}
