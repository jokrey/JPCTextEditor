package jokrey.utilities.swing.text_editor.user_input.cursor;

import jokrey.utilities.swing.text_editor.text_storage.ContentEditor;
import jokrey.utilities.swing.text_editor.text_storage.Line;
import jokrey.utilities.swing.text_editor.text_storage.DecoratedLinePart;

import java.awt.*;

/**
 * A cursor position in the provided ContentEditor.
 */
public class TextDisplayCursor {
    public static int PIXEL_WIDTH = 2;

	private int x = 0;
	    public int getX(){return x;}
	private int y = 0;
	    public int getY(){return y;}

	private final ContentEditor content;
    public TextDisplayCursor(ContentEditor content) {
    	this.content=content;
	}
    public TextDisplayCursor(ContentEditor content, int... xy) {
    	this(content);
    	setXY(xy);
	}
	public TextDisplayCursor(ContentEditor content, Point pos, int text_spacing_left, int text_spacing_top) {
		this(content);
		setPositionTo(pos, text_spacing_left, text_spacing_top);
	}

    /**
     * Returns the shape of the cursor at it's currently position
     * Works in regard to the display height at that position.
     */
    public Rectangle getShape(int text_spacing_left, int text_spacing_top) {
        int elapsedYPixels = text_spacing_top;
        for (int i = 0; i < content.getLineCount(); i++) {
            int elapsedChars_counter = 0;

            Line[] disLines = content.getDisplayLine(i);
            for (int d_i=0; d_i < disLines.length; d_i++) {
                Line disLine = disLines[d_i];
                int elapsedPixelsInLine = 0;
                int disLinePixelHeight = disLine.getPixelHeight();
                for (int part_i = 0; part_i < disLine.partCount(); part_i++) {
                    DecoratedLinePart lp = disLine.getPart(part_i);
                    int char_count_in_this_sequence = lp.length();
                    if (getY() == i && elapsedChars_counter + char_count_in_this_sequence >= getX()) {
                        int x_inSequence = getX() - elapsedChars_counter;
                        if(x_inSequence==lp.length() && d_i+1 < disLines.length) {
                            return new Rectangle(text_spacing_left, elapsedYPixels+disLinePixelHeight, PIXEL_WIDTH, disLines[d_i+1].getPixelHeight());
                        } else {
                            int pixelsInThisCharSequence = lp.getPixelWidth(0, x_inSequence);//fontM.stringWidth(x_inSequence==char_count_in_this_sequence?charsInSequence:charsInSequence.substring(0, x_inSequence));
                            return new Rectangle(elapsedPixelsInLine + pixelsInThisCharSequence + text_spacing_left, elapsedYPixels, PIXEL_WIDTH, disLinePixelHeight);
                        }
                    }
                    elapsedPixelsInLine += lp.getPixelWidth();
                    elapsedChars_counter += char_count_in_this_sequence;
                }

                elapsedYPixels += disLinePixelHeight;
            }

            if(getY()==i) { //happens on insert, if the display lines are not yet updated. or something
                Line lastDisLine = disLines[disLines.length-1];
                int height = lastDisLine.getPixelHeight();
                return new Rectangle(text_spacing_left, elapsedYPixels, PIXEL_WIDTH, height);
            }
        }
        return new Rectangle();
    }

    /**
     * Extracts the position from the cursor shape.
     */
    public Point getLocation(int text_spacing_left, int text_spacing_top) {
        return getShape(text_spacing_left, text_spacing_top).getLocation();
	}

    /**
     * Sets the cursors x and y position to the closest match to the provided Point p.
     */
	public void setPositionTo(Point p, int text_spacing_left, int text_spacing_top) {
        if(p.y < 0) {
            setXY(0, 0);
            return;
        }

        int elapsedYPixels = text_spacing_top;
        for (int i = 0; i < content.getLineCount(); i++) {
            int chars_in_real_line_counter = 0;

            Line[] disLines = content.getDisplayLine(i);
            for (int d_i=0; d_i < disLines.length; d_i++) {
                Line disLine = disLines[d_i];
                int elapsedPixelsInCurLine = text_spacing_left;
                int disLinePixelHeight = disLine.getPixelHeight();
                for (int ii = 0; ii < disLine.partCount(); ii++) {
                    DecoratedLinePart lp = disLine.getPart(ii);
                    int pixelsInThisCharSequence = lp.getPixelWidth();
                    if (elapsedYPixels >= (p.y - disLinePixelHeight)
                            && (ii == disLine.partCount() - 1 || p.x <= elapsedPixelsInCurLine + pixelsInThisCharSequence)) {
                        int chars_in_sequence_counter = 0;
                        if (p.x <= elapsedPixelsInCurLine + pixelsInThisCharSequence) {
                            for (; chars_in_sequence_counter < lp.length(); chars_in_sequence_counter++) {  //todo: Maybe use a homing in/jumping algorithm instead
                                if (p.x <= elapsedPixelsInCurLine + lp.getPixelWidth(0, chars_in_sequence_counter)
                                        + lp.getPixelWidth(chars_in_sequence_counter, Math.min(lp.length(), chars_in_sequence_counter + 1)) / 2 //EXPERIMENTAL
                                        ) {
                                    break;
                                }
                            }
                        } else {
                            if(d_i+1 < disLines.length)
                                chars_in_sequence_counter = disLine.length() - 1;
                            else
                                chars_in_sequence_counter = disLine.length();
                        }

                        int newX = chars_in_real_line_counter + chars_in_sequence_counter;
                        int newY = i;
                        if (chars_in_sequence_counter > content.getLineLength(i)) {
                            newX = chars_in_real_line_counter + content.getLineLength(i);
                        }
                        setXY(newX, newY);
                        return;
                    }
                    chars_in_real_line_counter += lp.length();
                    elapsedPixelsInCurLine += pixelsInThisCharSequence;
                }
                elapsedYPixels += disLinePixelHeight;
            }
        }
        if (elapsedYPixels <= p.y) {
            setXY(Integer.MAX_VALUE, Integer.MAX_VALUE);
        }
	}



	public void reset() {
		setXY(0, 0);
	}

    /**
     * Adds hm to the x position of this cursor. If x would be greater than the line length then the cursor will jump one line down and x will be set to 0
     */
	public void x_plus(int hm) {
        int v_x = (getX()+hm);
		if(v_x>content.getLineLength(getY())) {
		    int ny = getY() + 1;
		    setXY(0, ny);
		} else
            setXY(v_x, getY());
	}
	public void x_minus(int hm) {
		int v_x = (getX()-hm);
		if(v_x<0) {
		    int ny = getY()-1;
            setXY(content.getLineLength(ny) + (v_x+1), ny);
		} else
            setXY(v_x, getY());
	}

    public void y_minus(int hm) {
        setXY(getX(), getY()-hm);
    }
    public void y_plus(int hm) {
        setXY(getX(), getY()+hm);
    }

    public void setXY(int[] newXY) {
	    setXY(newXY[0], newXY[1]);
    }
    /**
     * sets x and y and checks if the are valid, validating them to the next best fit if they are not.
     * Array is required to have 2 cells. More won't be a problem, but will be disregarded
     */
	public void setXY(int newX, int newY) {
        if(newY < 0) {
            y=0;
            x=0;
        } else if (newY > content.getLineCount()-1) {
            y= content.getLineCount()-1;
            x= content.getLineLength(y);
        } else {
            y = newY;
            x = Math.max(0, Math.min(content.getLineLength(y), newX));
        }
	}
    public void setX(int x) {
        setXY(x, y);
    }
    public void setY(int y) {
        setXY(x, y);
    }
    public void resetToClosestValid() {
	    setXY(getX(), getY());
    }

    /**
     * @return getX and getY as a 2 part array.
     */
	public int[] getXY() {
		return new int[]{getX(),getY()};
	}

    /**
     * @return the number of chars this cursor is away from the start of the string.
     */
	public int getDistanceFrom00() {
	    return content.getDistanceFrom00(getXY());
	}
    /**
     * sets x and y from the number of chars this cursor is away from the start of the string.
     */
	public void setFromDistance(int distance) {
		int elapsedChars = 0;
		for(int i=0;i<content.getLineCount();i++) {
			int charsInLine = content.getLineLength(i)+1;
			if(elapsedChars + charsInLine > distance) {
			    setXY(distance-elapsedChars, i);
				return;
			}
			elapsedChars+=charsInLine;
		}
        setXY(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	@Override public String toString() {
		return "["+getClass().getSimpleName()+": "+getX()+", "+getY()+"]";
	}
	@Override public boolean equals(Object obj) {
//		try {
			return obj instanceof TextDisplayCursor && getX()==((TextDisplayCursor)obj).getX()&&getY()==((TextDisplayCursor)obj).getY();
//		} catch(Exception ex){return false;}
	}
}