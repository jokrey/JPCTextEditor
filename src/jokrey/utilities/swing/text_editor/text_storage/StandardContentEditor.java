package jokrey.utilities.swing.text_editor.text_storage;

import jokrey.utilities.swing.text_editor.JPC_Connector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * An implementation of the ContentEditor that implements it's abstract methods in the least invasive way possible.
 */
public class StandardContentEditor extends ContentEditor {
    private final List<Line> rawLines = new ArrayList<>();
    public StandardContentEditor(JPC_Connector con) {
        super(con);

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





    @Override public String getText_with_encoded_layout() {
        return LayoutStorageSystem.getStoredText(rawLines, getStandardLayout());
    }
    @Override public void setText_with_encoded_layout(String text) {
        rawLines.clear();
        rawLines.addAll(Arrays.asList(LayoutStorageSystem.restoreFrom(text, getStandardLayout())));
        if(rawLines.isEmpty())
            rawLines.add(new Line(getStandardLayout()));
        fireEntireTextChanged();
    }

    @Override public int getLineCount() {
        return rawLines.size();
    }
    @Override public Line getLine(int line_number) {
        return rawLines.get(line_number);
    }
    @Override public DecoratedLinePart[] getTextAsLineParts() {
        LinkedList<DecoratedLinePart> list = new LinkedList<>();
        for(int i=0;i<rawLines.size();i++) {
            for(int ii=0;ii<rawLines.get(i).partCount();ii++)
                list.add(rawLines.get(i).getPart(ii));
            if(i<rawLines.size()-1)
                list.add(new DecoratedLinePart("\n"));
        }
        return list.toArray(new DecoratedLinePart[0]);
    }

    @Override public void clearText() {
        rawLines.clear();
        rawLines.add(new Line(""));
    }
    @Override public Line removeLine(int line_number) {
        Line delLine = rawLines.remove(line_number);
        fireTextChanged(line_number, line_number, delLine.toString(),false);
        return delLine;
    }

    private int countCharsUpUntil(int lineNr) {
        int charCounter = 0;
        for(int i = 0; i< lineNr -1; i++)
            charCounter += rawLines.get(i).length();
        return charCounter;
    }

    @Override public void setLine(int i, Line line) {
        Line delLine = rawLines.set(i, line);
        fireTextChanged(i,i,delLine.toString(),false);
        fireTextChanged(i,i,line.toString(),true);
    }
    @Override public void addLine(int i, Line line) {
        rawLines.add(i, line);
        fireTextChanged(i,i,line.toString(),true);
    }
    @Override public void insert(int xInLine, int lineNr, String text, LinePartAppearance insertLayout) {
        int chars = countCharsUpUntil(lineNr);
        Line newLine = rawLines.get(lineNr).insert(xInLine, text, insertLayout);
        rawLines.set(lineNr, newLine);
        fireTextChanged(lineNr, lineNr, newLine.toString(),true);
    }

    @Override public void setText(DecoratedLinePart... text) {
        rawLines.clear();
        if(text.length == 0) {
            rawLines.add(new Line(""));
        } else {
            List<DecoratedLinePart> parts_in_current_line = new LinkedList<>();
            for (DecoratedLinePart part : text) {
                if (part.txt.contains("\n")) {
                    String[] lines_in_part = part.txt.split("\n", -1);

                    //add before \n to current line
                    parts_in_current_line.add(new DecoratedLinePart(lines_in_part[0], part.layout));
                    rawLines.add(new Line(parts_in_current_line.toArray(new DecoratedLinePart[0])));
                    parts_in_current_line.clear();

                    //add everything between '\n's as new lines
                    for (int i = 1; i < lines_in_part.length - 1; i++)
                        rawLines.add(new Line(new DecoratedLinePart(lines_in_part[i], part.layout)));

                    //add everything after last \n to parts_in_current_line
                    parts_in_current_line.add(new DecoratedLinePart(lines_in_part[lines_in_part.length - 1], part.layout));
                } else {
                    parts_in_current_line.add(part);
                }
            }
            rawLines.add(new Line(parts_in_current_line.toArray(new DecoratedLinePart[0])));
        }
        fireEntireTextChanged();
    }
}
