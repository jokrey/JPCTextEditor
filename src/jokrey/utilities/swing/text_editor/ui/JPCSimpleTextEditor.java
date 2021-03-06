package jokrey.utilities.swing.text_editor.ui;

import jokrey.utilities.swing.text_editor.JPC_Connector;
import jokrey.utilities.swing.text_editor.text_storage.ContentEditor;
import jokrey.utilities.swing.text_editor.text_storage.StandardContentEditor;
import jokrey.utilities.swing.text_editor.ui.core.AbstractJPCTextEditor;
import jokrey.utilities.swing.text_editor.user_input.UserInputHandler;

/**
 * The most simple, NON layouted JPC Editor.
 * Also does not provide out of the box wrapping functionality.
 *   use JPCSimpleWrappingTextEditor subclass for that.
 */
public class JPCSimpleTextEditor extends AbstractJPCTextEditor {
    public JPCSimpleTextEditor() {
        setForeground(content.getStandardLayout().fg);
        setFont(content.getStandardLayout().font);
    }

    @Override public ContentEditor createContentEditor(JPC_Connector c) {
        return new StandardContentEditor(c);
    }
    @Override protected UserInputHandler createUserInputHandler(ContentEditor content) {
        return new UserInputHandler(content);
    }
}
