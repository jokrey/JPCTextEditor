package jokrey.utililities.swing.text_editor.ui;

import jokrey.utililities.swing.text_editor.ui.core.Abstract_JPCTextEditor;
import jokrey.utililities.swing.text_editor.ui.LayoutedFindAndReplaceFrame;
import jokrey.utililities.swing.text_editor.text_storage.*;
import jokrey.utililities.swing.text_editor.user_input.UserInputHandler;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.net.MalformedURLException;

/**
 * "Removes" the standard layouting option in ContentEditor and allows access to the user cursors layouting.
 */
public class JPCLayoutedTextEditor extends Abstract_JPCTextEditor {
    public JPCLayoutedTextEditor() {
        user.cursor.setInsertLayout(content.getStandardLayout());
    }

    @Override public ContentEditor createContentEditor() {
        return new StandardContentEditor() {
            @Override public int getMaxLineCount() {
                return -1;//endless
            }
        };
    }

    @Override protected UserInputHandler createUserInputHandler(ContentEditor content) {
        return new UserInputHandler(content) {
            @Override public void _user_paste() {
                try {
                    if(Toolkit.getDefaultToolkit().getSystemClipboard().getAvailableDataFlavors().length>0) {
                        try {
                            DataFlavor something = Toolkit.getDefaultToolkit().getSystemClipboard().getAvailableDataFlavors()[0];
                            Object pasteData = Toolkit.getDefaultToolkit().getSystemClipboard().getData(something);
                            if (pasteData instanceof LinePart[]) {
                                _user_insert((LinePart[]) pasteData);
                                return;
                            }
                        } catch (MalformedURLException ex) { } //ok so hand back to super below.
                    }
                } catch(Exception ex) { ex.printStackTrace(); }
                super._user_paste();
            }
        };
    }

    @Override public void setForeground(Color fg) {
        if(content==null||fg==null)return;
        super.setForeground(fg);
        user._user_change_insert_layout(user.cursor.getValidInsertLayout().copy_ChangeFG(fg), true);
    }

    @Override public void setBackground(Color bg) {
        if(content==null||bg==null)return;
        super.setBackground(bg);
        setCursorBackground(bg);
    }
    public void setCursorBackground(Color bg) {
        user._user_change_insert_layout(user.cursor.getValidInsertLayout().copy_ChangeBG(bg), true);
    }

    @Override public void setFont(Font font) {
        if(content==null||font==null)return;
        super.setFont(font);
        user._user_change_insert_layout(user.cursor.getValidInsertLayout().copy_ChangeFont(font), true);
    }

    @Override public void start_find_replace_frame() {
        new LayoutedFindAndReplaceFrame(this, this, content, user, user.cursor.getSelection().getIntervalSequences());
    }
}