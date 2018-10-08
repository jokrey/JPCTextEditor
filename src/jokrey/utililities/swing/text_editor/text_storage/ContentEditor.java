package jokrey.utililities.swing.text_editor.text_storage;

import jokrey.utililities.swing.text_editor.JPC_Connector;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * The content of a jpc editor.
 * Handed around as reference a bunch, but only one per JPC should actually be on heap.
 */
public abstract class ContentEditor {
    private final List<Line> rawLines = new ArrayList<>();
    protected final JPC_Connector jpc_connector;
    public ContentEditor(JPC_Connector con) {
        jpc_connector=con;
        rawLines.add(new Line(""));
        addContentListener(new ContentListener() {
            @Override public void textChanged(int firstAffectedLine, int lastAffectedLine, String text, boolean insert) {
                while(getMaxLineCount()>0 && rawLines.size()>getMaxLineCount())
                    rawLines.remove(rawLines.size()-1);
                jpc_connector.recalculateSize();
                for (int i = firstAffectedLine; i <= lastAffectedLine; i++)
                    jpc_connector.recalculateDisplayLine(i);
                jpc_connector.repaint();
            }
        });
    }

    private LinePartLayout.Instantiated standard_layout = new LinePartLayout.Instantiated(Color.black, new Color(0,0,0,0), new Font("Arial", Font.BOLD, 13));
    public LinePartLayout.Instantiated getStandardLayout() { return standard_layout; }
    public void increaseStandardFontSize() {
        float newFontSize = standard_layout.font.getSize()+1;
        if(newFontSize>=4)
            setStandardLayout(standard_layout.copy_ChangeFont(standard_layout.font.deriveFont(newFontSize)));
    }
    public void decreaseStandardFontSize() {
        float newFontSize = standard_layout.font.getSize()-1;
        if(newFontSize>=4)
            setStandardLayout((standard_layout.copy_ChangeFont(standard_layout.font.deriveFont(newFontSize))));
    }
    public void setStandardLayout(LinePartLayout.Instantiated n) {
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




    private ArrayList<ContentListener> listeners = new ArrayList<>();
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




    public void setText(String text) {
        setText(new LinePart(text, null));
    }
    public String getText() {
        StringBuilder text = new StringBuilder();
        for(Line line:rawLines)
            text.append(line.toString()).append("\n");
        return text.deleteCharAt(text.length()-1).toString();//deleting last \n...
    }
    public void setText(LinePart... text) {
        rawLines.clear();
        List<LinePart> parts_in_current_line = new LinkedList<>();
        for(LinePart part:text) {
            if(part.txt.contains("\n")) {
                String[] lines_in_part = part.txt.split("\n", -1);
                parts_in_current_line.add(new LinePart(lines_in_part[0], part.layout));
                rawLines.add(new Line(parts_in_current_line.toArray(new LinePart[0])));
                for (int i = 1; i < lines_in_part.length - 1; i++)
                    rawLines.add(new Line(new LinePart(lines_in_part[i], part.layout)));
                parts_in_current_line.clear();
                parts_in_current_line.add(new LinePart(lines_in_part[lines_in_part.length - 1], part.layout));
            } else {
                parts_in_current_line.add(part);
            }
        }
        rawLines.add(new Line(parts_in_current_line.toArray(new LinePart[0])));
        fireEntireTextChanged();
    }
    public LinePart[] getTextAsLineParts() {
        LinkedList<LinePart> list = new LinkedList<>();
        for(int i=0;i<rawLines.size();i++) {
            for(int ii=0;ii<rawLines.get(i).partCount();ii++)
                list.add(rawLines.get(i).getPart(ii));
            if(i<rawLines.size()-1)
                list.add(new LinePart("\n"));
        }
        return list.toArray(new LinePart[0]);
    }
    public String getTextFromLines(int... lines) {
        StringBuilder text = new StringBuilder();
        for(int i=0;i<lines.length;i++)
            text.append(rawLines.get(lines[i]-1).toString()).append("\n");
        return text.toString();
    }

    public String getText_with_encoded_layout() {
        return LayoutStorageSystem.getStoredText(rawLines, getStandardLayout());
    }
    public void setText_with_encoded_layout(String text) {
        rawLines.clear();
        rawLines.addAll(Arrays.asList(LayoutStorageSystem.restoreFrom(text, getStandardLayout())));
        if(rawLines.isEmpty())
            rawLines.add(new Line(getStandardLayout()));
        fireEntireTextChanged();
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
    public int getLineCount() {
        return rawLines.size();
    }
    public Line getLine(int line_number) {
        return rawLines.get(line_number);
    }
    public String getLineText(int line_number) {
        return rawLines.get(line_number).toString();
    }
    public int getLineLength(int line_number) {
        if(line_number<0||line_number>=getLineCount())
            return 0;
        return rawLines.get(line_number).length();
    }
    public int getLinePixelWidth(int line_number) {
        return getLine(line_number).getPixelWidth();
    }
    public int getLinePixelWidth(int line_number, int start, int end) {
        Line line = getLine(line_number);
        return line.getPixelWidth(start, Math.min(end, line.length()));
    }
    public int getLinePixelHeight(int line_number) {
        return getLine(line_number).getPixelHeight();
    }

    //mutating
    public Line removeLine(int line_number) {
        Line ret =  rawLines.remove(line_number);
        fireTextChanged(line_number,line_number,ret.toString(),false);
        return ret;
    }
    public void setLineText(int i, String s, LinePartLayout layout) {
        setLine(i, new Line(s, layout));
    }
    public void setLine(int i, Line line) {
        rawLines.set(i, line);
        fireTextChanged(i,i,line.toString(),true);
    }
    public void addLine(int i, Line line) {
        rawLines.add(i, line);
        fireTextChanged(i,i,line.toString(),true);
    }

    private LinePart hint = new LinePart("");
    public final void setHint(LinePart hint) {
        if(hint!=null)
            this.hint=hint;
    }
    public final LinePart getHint() {return hint;}

    public void validateCursorVisibility() {
        jpc_connector.validateCursorVisibility();
    }
}

