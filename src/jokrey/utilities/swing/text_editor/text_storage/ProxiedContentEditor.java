package jokrey.utilities.swing.text_editor.text_storage;

/**
 * @author jokrey
 */
public abstract class ProxiedContentEditor extends ContentEditor {
    public final ContentEditor backingContent;
    public ProxiedContentEditor(ContentEditor backingContent) {
        super(backingContent.jpc_connector);
        this.backingContent = backingContent;
        backingContent.addContentListener(new ContentListener() {
            @Override public void userCursorPosChanged(int old_x, int old_y) { fireUserCursorPosChanged(old_x, old_y); }
            @Override public void userCursorLayoutChanged() { fireUserCursorLayoutChanged(); }
            @Override public void standardLayoutChanged() { fireStandardLayoutChanged(); }
            @Override public void textChanged(int firstAffectedLine, int lastAffectedLine, String text, boolean insert)
                { fireTextChanged(firstAffectedLine, lastAffectedLine, text, insert); }
        });
    }

    @Override public int getMaxLineCount() {
        return backingContent.getMaxLineCount();
    }
    @Override public boolean isSelectionEnabled() {
        return backingContent.isSelectionEnabled();
    }
    @Override public boolean isDragAndDropEnabled() {
        return backingContent.isDragAndDropEnabled();
    }
    @Override public boolean allowInsertion(String str) {
        return backingContent.allowInsertion(str);
    }
    @Override public void clearText() {
        backingContent.clearText();
    }
    @Override public DecoratedLinePart[] getTextAsLineParts() {
        return backingContent.getTextAsLineParts();
    }
    @Override public String getText_with_encoded_layout() {
        return backingContent.getText_with_encoded_layout();
    }
    @Override public void setText_with_encoded_layout(String text) {
        backingContent.setText_with_encoded_layout(text);
    }
    @Override public int getLineCount() {
        return backingContent.getLineCount();
    }
    @Override public Line getLine(int line_number) {
        return backingContent.getLine(line_number);
    }
    @Override public Line removeLine(int line_number) {
        return backingContent.removeLine(line_number);
    }
    @Override public void setLine(int i, Line line) {
        backingContent.setLine(i, line);
    }
    @Override public void addLine(int i, Line line) {
        backingContent.addLine(i, line);
    }
}
