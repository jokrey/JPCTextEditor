package jokrey.utilities.swing.text_editor.user_input.step_manager;

import jokrey.utilities.swing.text_editor.user_input.UserCursor;

public interface Stepable {
    void redo(UserCursor cursor);
    void undo(UserCursor cursor);
}
