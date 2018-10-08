package jokrey.utililities.swing.text_editor.text_storage;

import jokrey.utililities.swing.text_editor.JPC_Connector;

/**
 * An implementation of the ContentEditor that implements it's abstract methods in the least invasive way possible.
 */
public class StandardContentEditor extends ContentEditor {
    public StandardContentEditor(JPC_Connector con) {
        super(con);
    }

    @Override public int getMaxLineCount() {
        return -1;
    }
    @Override public boolean isSelectionEnabled() {
        return true;
    }
    @Override public boolean isDragAndDropEnabled() {
        return true;
    }
    @Override public boolean allowInsertion(String str) {
        return true;
    }
    @Override public Line[] getDisplayLine(int line_number) {
        return new Line[]{getLine(line_number)};
    }
    @Override public void recalculateDisplayLine(int line)  { }
}
