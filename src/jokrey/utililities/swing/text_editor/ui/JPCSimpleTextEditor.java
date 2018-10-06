package jokrey.utililities.swing.text_editor.ui;

import jokrey.utililities.swing.text_editor.ui.core.Abstract_JPCTextEditor;
import jokrey.utililities.swing.text_editor.text_storage.ContentEditor;
import jokrey.utililities.swing.text_editor.text_storage.StandardContentEditor;
import jokrey.utililities.swing.text_editor.user_input.UserInputHandler;

/**
 * The most simple, NON layouted JPC Editor.
 * Also does not provide out of the box wrapping functionality.
 *   use JPCSimpleWrappingTextEditor subclass for that.
 */
public class JPCSimpleTextEditor extends Abstract_JPCTextEditor {
    public JPCSimpleTextEditor() {
        setForeground(content.getStandardLayout().fg);
        setBackground(content.getStandardLayout().bg);
        setFont(content.getStandardLayout().font);
    }

    @Override public ContentEditor createContentEditor() {
        return new StandardContentEditor();
    }
    @Override protected UserInputHandler createUserInputHandler(ContentEditor content) {
        return new UserInputHandler(content);
    }
}
