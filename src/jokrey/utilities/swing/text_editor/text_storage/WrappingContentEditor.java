package jokrey.utilities.swing.text_editor.text_storage;

import jokrey.utilities.swing.text_editor.user_input.cursor.TextDisplayCursor;
import jokrey.utilities.debug_analysis_helper.CallCountMarker;

import java.util.ArrayList;

/**
 * Adds a wrapping functionality to the StandardContentEditor.
 */
public class WrappingContentEditor extends ProxiedContentEditor {
    private ArrayList<Line[]> displayLines = new ArrayList<>();
    public WrappingContentEditor(ContentEditor backingContent) {
        super(backingContent);
    }

    @Override public Line[] getDisplayLine(int line_number) {
        if (isLineWrapEnabled() && line_number < displayLines.size()) {
            return displayLines.get(line_number);
        } else {
            return new Line[]{getLine(line_number)};
        }
    }

    @Override public void recalculateDisplayLines() {
        displayLines.clear();
        recalculateDisplayLine(-1);//will run into else branch and recalculate all
    }

    @Override public void recalculateDisplayLine(int line) {
//        CallCountMarker.mark_call_print("recalculateDisplayLines");
        if (isLineWrapEnabled()) {
            if(displayLines.size()==getLineCount() && line>=0 && line<getLineCount()) {
                Line disp_line = getLine(line);
                int[] lineWraps = getAutomaticLineWrapsFor(disp_line, jpc_connector.getTextSpacingLeft(), jpc_connector.getDisplayWidth()- TextDisplayCursor.PIXEL_WIDTH);
                displayLines.set(line, disp_line.splitAt(lineWraps));
            } else {
                displayLines = new ArrayList<>(getLineCount());
                for (int i = 0; i < getLineCount(); i++) {
                    Line disp_line = getLine(i);
                    int[] lineWraps = getAutomaticLineWrapsFor(disp_line, jpc_connector.getTextSpacingLeft(), jpc_connector.getDisplayWidth()- TextDisplayCursor.PIXEL_WIDTH);
                    displayLines.add(disp_line.splitAt(lineWraps));
                }
            }
        } else {
            displayLines.clear();
        }
        jpc_connector.repaint(); //usually not required, but sometimes(and in not easily detectable situations) it is.
     }

    private boolean wrap_lines = true;
    /**
     * Sets the new configurations and recalculates the displayed lines.  (using asap)
     */
    public void setLineWrap(boolean wrap_lines) {
        this.wrap_lines=wrap_lines;
        jpc_connector.recalculateDisplayLines();
    }
    public boolean isLineWrapEnabled() {return wrap_lines;}



    private int[] getAutomaticLineWrapsFor(Line rawLine, int text_spacing_left, int width) {
        rawLine.updatePixelKnowledge(jpc_connector, getStandardLayout());
        int horizontal_space = width - text_spacing_left;
        if(rawLine.getPixelWidth() <= horizontal_space) {
            return new int[0];
        } else {
            ArrayList<Integer> wraps = new ArrayList<>();
            int lastWrapPoint = 0;
            Line rest = rawLine;
            int currentLineWrap;
            //each return of getNextLineWrap means that space was full at that point.
            //after a wrap that same function is called again for the remaining line.
            while ((currentLineWrap = getNextLineWrap_butgobacktolastspace(rest, horizontal_space)) > 0) {
                lastWrapPoint += currentLineWrap;
                if(lastWrapPoint>rawLine.length()) break;
                wraps.add(lastWrapPoint);
                rest = rawLine.splitAt(lastWrapPoint)[1];
            }

            int[] wraps_conv = new int[wraps.size()];
            for(int i=0; i<wraps_conv.length; i++)
                wraps_conv[i] = wraps.get(i);
            return wraps_conv;
        }
    }



    private int getNextLineWrap_butgobacktolastspace(Line remaining, int remaining_space) {
        int elapsedPixels = 0;
        int elapsedChars = 0;
        DecoratedLinePart[] parts = remaining.getCopyOfInternalParts();
        for (DecoratedLinePart part : parts) {
            int part_pixel_width = part.getPixelWidth();
            if (elapsedPixels + part_pixel_width > remaining_space) {  //if this is greater, then we have found the part in which the wrapping occurs
                //now we know that the wrap point is somewhere in part[i].

                int pi = part.length();
                while (pi>=2 && elapsedPixels + part.getPixelWidth(0, pi) > remaining_space) {
                    int jump_step_size = pi/2;
                    pi -= jump_step_size;

                    if (pi < jump_step_size || elapsedPixels + part.getPixelWidth(0, pi) <= remaining_space || pi <= 4) { //where of course, pi < 4 is a random number
                        pi = pi + jump_step_size; //restore position before last jump

                        for (; pi >= 0; pi--) {
                            if (elapsedPixels + part.getPixelWidth(0, pi) <= remaining_space) {
                                if (pi == 0)
                                    return 1; //because we still want to wrap, but the last char will be split in half..
                                int last_space_index = part.txt.lastIndexOf(" ", pi - 1);
                                if (last_space_index != -1)
                                    return elapsedChars + last_space_index + 1;  //+1 to split after the space forcing it to remain in the prior line
                                return elapsedChars + pi;
                            }
                        }

                    }

                }
                return 1;//technically this should not happen, but if it does we have already asserted that elapsedPixels are definitely fitting.
            } else {
                elapsedPixels += part_pixel_width;
                elapsedChars += part.length();
            }
        }
        return -1;
    }




//    //the following is very clean, very easy to understand, but slow for huge lines.
    //however I will still keep it in case I forget how the algorithm works without optimizations.

//    private int getNextLineWrap_butgobacktolastspace(Line remaining, int remaining_space) throws NeverDrawnException {
//        if(remaining.getPixelWidth() <= remaining_space) //check if it fits right out of the box
//            return -1;
//
//        for(int i=remaining.length()-2;i>=1;i--) { //-2 because we check for the first one immediately up front.
//            if(remaining.getPixelWidth(0, i) <= remaining_space) {
//                int last_space_index = remaining.toString().lastIndexOf(" ", i-1);
//                if(last_space_index != -1)
//                    return last_space_index + 1;
//                return i;
//            }
//        }
//        return -1;
//    }
}