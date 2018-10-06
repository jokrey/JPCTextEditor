package jokrey.utililities.swing.text_editor.text_storage;

import jokrey.utilities.timediffmarker.CallCountMarker;

import java.util.ArrayList;

/**
 * Adds a wrapping functionality to the StandardContentEditor.
 */
public class WrappingContentEditor extends StandardContentEditor {
    private ArrayList<Line[]> displayLines = new ArrayList<>();

    @Override public Line[] getDisplayLine(int line_number) {
        if (isLineWrapEnabled() && line_number < displayLines.size()) {
            return displayLines.get(line_number);
        } else {
            return new Line[]{getLine(line_number)};
        }
    }

    @Override public void recalculateDisplayLines() throws NeverDrawnException {
        displayLines.clear();
        recalculateDisplayLine(-1);//will run into else branch and recalculate all
    }

    @Override public void recalculateDisplayLine(int line) throws NeverDrawnException {
        CallCountMarker.mark_call_print("recalculateDisplayLines");
        if (isLineWrapEnabled()) {
            if(displayLines.size()==getLineCount() && line>=0 && line<getLineCount()) {
                    Line disp_line = getLine(line);
                    int[] lineWraps = getAutomaticLineWrapsFor(disp_line, jpc_connector.getTextSpacingLeft(), jpc_connector.getDisplayWidth()/*-jpc_connector.getTextSpacingLeft()*/);
                    displayLines.set(line, disp_line.splitAt(lineWraps));
            } else {
                displayLines = new ArrayList<>(getLineCount());
                for (int i = 0; i < getLineCount(); i++) {
                    Line disp_line = getLine(i);
                    int[] lineWraps = getAutomaticLineWrapsFor(disp_line, jpc_connector.getTextSpacingLeft(), jpc_connector.getDisplayWidth()/*-jpc_connector.getTextSpacingLeft()*/);
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



    /**
     * @throws NeverDrawnException if the specified lines size cannot be calculated.
     * Should not happen if recalculateDisplayLines was called using the asap queue.
     */
    private int[] getAutomaticLineWrapsFor(Line rawLine, int text_spacing_left, int width) throws NeverDrawnException {
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



    private int getNextLineWrap_butgobacktolastspace(Line remaining, int remaining_space) throws NeverDrawnException {
        int elapsedPixels = 0;
        int elapsedChars = 0;
        LinePart[] parts = remaining.getCopyOfInternalParts();
        for (LinePart part : parts) {
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


////    old optimized
//    private int getNextLineWrap_butgobacktolastspace(Line remaining, int remaining_space) throws NeverDrawnException {
//        CallCountMarker.mark_call_print("old");
//        int elapsedPixels = 0;
//        int elapsedChars = 0;
//        LinePart[] parts = remaining.getCopyOfInternalParts();
//        for(int i=0;i<parts.length;i++) {
//            int part_pixel_width = parts[i].getPixelWidth();
//            if(elapsedPixels+part_pixel_width > remaining_space) {  //if this is greater, then we have found the part in which the wrapping occurs
//                //now we know that the wrap point is somewhere in part[i].
//
//                //Optimization through jumping.
//                //Idea: Too minimize the slow calls to parts[i].getPixelWidth(0, pi), they are minimized by "pre-searching".
//                //After the significantly smaller area has been found we search like we would usually.
//                //to do (should performance problems in a real case arise) nested search area's until a size of say 10 is reached.
//                int part_length = parts[i].length();
//                int jump_step_size = Math.max(Math.min(part_length/10, 40), 6);//10, 40, 5 are experimental values obtained through deduction and testing.
//                for(int pi=part_length-1;pi>=0;pi-=jump_step_size) {
//                    if(pi<jump_step_size || elapsedPixels + parts[i].getPixelWidth(0, pi)  <= remaining_space) {
//                        pi=Math.min(part_length-1, pi+jump_step_size); //restore position before last jump
//
//                        for(;pi>=0;pi--) {
//                            if(elapsedPixels + parts[i].getPixelWidth(0, pi)  <= remaining_space) {
//                                if(pi==0) return 1; //because we still want to wrap, but the last char will be split in half..
//                                int last_space_index = parts[i].txt.lastIndexOf(" ", pi-1);
//                                if(last_space_index != -1)
//                                    return elapsedChars + last_space_index + 1;  //+1 to split after the space forcing it to remain in the prior line
//                                return elapsedChars + pi;
//                            }
//                        }
//
//                    }
//                }
//                return elapsedPixels;//technically this should not happen, but if it does we have already asserted that elapsedPixels are definitely fitting.
//            } else {
//                elapsedPixels+=part_pixel_width;
//                elapsedChars+=parts[i].length();
//            }
//        }
//        return -1;
//    }



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