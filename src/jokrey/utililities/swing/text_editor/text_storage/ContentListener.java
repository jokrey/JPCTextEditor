package jokrey.utililities.swing.text_editor.text_storage;

public interface ContentListener {
    /**
     * Called when input_receiver cursor position changes.
     */
    default void userCursorPosChanged(int old_x, int old_y) {}
    /**
     * Called when input_receiver cursor layout changes.
     */
    default void userCursorLayoutChanged() {}
    /**
     * Called when input_receiver standard layout changes.
     */
    default void standardLayoutChanged() {}
    /**
     * Called when text changes.
     */
    default void textChanged(int firstAffectedLine, int lastAffectedLine, String text, boolean insert) {}
}
