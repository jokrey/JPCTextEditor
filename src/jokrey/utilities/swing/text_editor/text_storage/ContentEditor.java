package jokrey.utilities.swing.text_editor.text_storage;

import jokrey.utilities.swing.text_editor.JPC_Connector;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * The content of a jpc editor.
 * Handed around as reference a bunch, but only one per JPC should actually be on heap.
 */
public abstract class ContentEditor {
    protected final JPC_Connector jpc_connector;
    public ContentEditor(JPC_Connector con) {
        jpc_connector=con;
    }

    private LinePartAppearance.Instantiated standard_layout = new LinePartAppearance.Instantiated(Color.black, new Font("Arial", Font.BOLD, 13));
    public LinePartAppearance.Instantiated getStandardLayout() { return standard_layout; }
    public void increaseStandardFontSize() {
        int newFontSize = standard_layout.font.getSize()+1;
        if(newFontSize>=4)
            setStandardLayout(standard_layout.copy_ChangeFontSize(newFontSize));
    }
    public void decreaseStandardFontSize() {
        float newFontSize = standard_layout.font.getSize()-1;
        if(newFontSize>=4)
            setStandardLayout((standard_layout.copy_ChangeFont(standard_layout.font.deriveFont(newFontSize))));
    }
    public void setStandardLayout(LinePartAppearance.Instantiated n) {
        standard_layout=n;
//        if(!rawLines.isEmpty())
//            getLine(0).getPart(0).invalidatePixelCache(); //makes sure all operations that require pixel knowledge fail until repaint was done.
        fireStandardLayoutChanged();
        jpc_connector.repaint();
    }

    /**
     * Called if something substantial changed.
     * Used for example by the wrapping ContentEditor.
     * Recalculate all display lines
     */
    public void recalculateDisplayLines() {
//        if(block_recalculateDisplayLines)return;
        recalculateDisplayLines(0, getLineCount()-1);
    }
    /**
     * Called if something substantial changed.
     * Used for example by the wrapping ContentEditor.
     *
     * @param firstAffectedLine first line to recalculate
     * @param lastAffectedLine last line to recalculate
     */
    public void recalculateDisplayLines(int firstAffectedLine, int lastAffectedLine) {
        for (int i = firstAffectedLine; i <= lastAffectedLine; i++)
            recalculateDisplayLine(i);
    }

    public abstract void recalculateDisplayLine(int line);

    /**
     * Used to determine what lines are actually display for storage line at line_number
     */
    public abstract Line[] getDisplayLine(int line_number);
    /**
     * @return max line count, Negative implies endless lines.
     */
    public abstract int getMaxLineCount();
    /**
     * @return Whether or not selection is prohibited. For example useful if use is not supposed to copy text.
     */
    public abstract boolean isSelectionEnabled();
    /**
     * @return Whether or not selection is prohibited. For example useful if use is not supposed to copy text.
     */
    public abstract boolean isDragAndDropEnabled();
    /**
     * @return whether or not the user is allowed to insert the str
     */
    public abstract boolean allowInsertion(String str);




    private final ArrayList<ContentListener> listeners = new ArrayList<>();
    public void addContentListener(ContentListener contentListener) {
        listeners.add(contentListener);
    }
    public void removeContentListener(ContentListener contentListener) {
        listeners.remove(contentListener);
    }

    protected void fireEntireTextChanged() {
        fireTextChanged(0,getLineCount()-1, null, false);
    }
    //null allowed for text
    protected void fireTextChanged(int firstAffectedLine, int lastAffectedLine, String text, boolean insert) {
        for(ContentListener listener:listeners)
            listener.textChanged(firstAffectedLine, lastAffectedLine, text, insert);
    }
    protected void fireStandardLayoutChanged() {
        for(ContentListener listener:listeners)
            listener.standardLayoutChanged();
    }
    public void fireUserCursorPosChanged(int old_x, int old_y) {
        for(ContentListener listener:listeners)
            listener.userCursorPosChanged(old_x, old_y);
    }
    public void fireUserCursorLayoutChanged() {
        for(ContentListener listener:listeners)
            listener.userCursorLayoutChanged();
    }



    public abstract void clearText();
    public String getText() {
        StringBuilder text = new StringBuilder();
        for(DecoratedLinePart line:getTextAsLineParts())
            text.append(line.txt);
        return text.toString();
    }

    public void setText(String text) {
        setText(new DecoratedLinePart(text, null));
    }
    public abstract void setText(DecoratedLinePart... text);
    public String getTextInLines(int... lineIndices) {
        StringBuilder text = new StringBuilder();
        for (int lineIndex : lineIndices) text.append(getLine(lineIndex - 1).toString()).append("\n");
        return text.toString();
    }
    public abstract DecoratedLinePart[] getTextAsLineParts();
    public abstract String getText_with_encoded_layout();
    public abstract void setText_with_encoded_layout(String text);

    public int count(String toFind) {
        if(toFind.isEmpty())return 0; // to avoid / 0
        return (getText().length() - getText().replaceAll(toFind, "").length()) / toFind.length();
    }










    public int getDistanceFrom00(int... cursor_xy) {
        int counter = 0;
        for(int i=0;i<getLineCount();i++) {
            if(i==cursor_xy[1]) {
                return counter+cursor_xy[0];
            } else {
                counter+=getLineLength(i)+1;
            }
        }
        return counter;
    }
    //Wrapper on the raw Lines list.
    //Provides semantic helpers
    public abstract int getLineCount();
    public abstract Line getLine(int line_number);
    public int getLineLength(int line_number) {
        if(line_number<0||line_number>=getLineCount())
            return 0;
        return getLine(line_number).length();
    }
    public String getLineText(int line_number) {
        return getLine(line_number).toString();
    }

    //mutating
    public void setLineText(int i, String s, LinePartAppearance layout) {
        setLine(i, new Line(s, layout));
    }
    public abstract Line removeLine(int line_number);
    public abstract void setLine(int i, Line line);
    public abstract void addLine(int i, Line line);
    public abstract void insert(int xInLine, int lineNr, String text, LinePartAppearance insertLayout);
    public void addLine(Line line) {
        addLine(getLineCount(), line);
    }

    private DecoratedLinePart hint = new DecoratedLinePart("");
    public void setHint(DecoratedLinePart hint) {
        if(hint!=null)
            this.hint=hint;
    }
    public DecoratedLinePart getHint() {return hint;}

    public void validateCursorVisibility() {
        jpc_connector.validateCursorVisibility();
    }

}

