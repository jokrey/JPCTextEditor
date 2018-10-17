package jokrey.utilities.swing.text_editor.user_input.cursor;

import jokrey.utilities.swing.text_editor.text_storage.ContentEditor;
import jokrey.utilities.swing.text_editor.text_storage.Line;
import jokrey.utilities.swing.text_editor.text_storage.LinePart;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * A Text Interval is basically a selection. A selection between to Cursor Points.
 * This class handles the complexities that come with that.
 */
public class TextInterval {
	public final TextDisplayCursor p1;
    public final TextDisplayCursor p2;
	@Override public String toString() {
		return p1+" to "+p2;
	}

	private final ContentEditor content;
	public TextInterval(ContentEditor content) {
		this.content=content;
        p1 = new TextDisplayCursor(content);
        p2 = new TextDisplayCursor(content);
	}

    /**
     * An interval is clear if there is no text between them, io when p1==p2
     */
	public boolean isClear() {
		return p1.equals(p2);
	}


    /**
     * Equalizes the internal cursors.
     */
    public void clear() {
        p1.reset();
        p2.reset();
    }

    //cursor getter and setter wrapper
    public void setFromDistance(int distance1, int distance2) {
        p1.setFromDistance(distance1);
        p2.setFromDistance(distance2);
        validateP1SmallerP2();
    }
    public void setFromXYs(int... x1y1x2y2) {
        setFromXYs(new int[] {x1y1x2y2[0], x1y1x2y2[1]}, new int[] {x1y1x2y2[2], x1y1x2y2[3]});
    }
    public void setFromXYs(int[] newXY1, int[] newXY2) {
		p1.setXY(newXY1);
        p2.setXY(newXY2);
        validateP1SmallerP2();
	}
	private void validateP1SmallerP2() {
        if(p1.getY()>p2.getY()||(p1.getY()==p2.getY()&&p1.getX()>p2.getX())) {
            int[] temp1 = p1.getXY();
            p1.setXY(p2.getXY());
            p2.setXY(temp1);
        }
	}

    public int get1_distance00() {
        return p1.getDistanceFrom00();
    }
    public int get2_distance00() {
        return p2.getDistanceFrom00();
    }
    public int[] get1XY() {
        return p1.getXY();
    }
    public int[] get2XY() {
        return p2.getXY();
    }
    public int[] getXYs() {
        return new int[] {p1.getX(), p1.getY(), p2.getX(), p2.getY()};
    }

    //setting/getting both at the same time
    public void setFromDistance(int distance) {
    	p1.setFromDistance(distance);
    	p2.setXY(p1.getXY()); //faster
	}
	public void setXY(int... xy) {
    	p1.setXY(xy);
    	p2.setXY(xy);
	}



    //Is on Selection if is between the two cursors or one of the two cursors.
    public boolean isOnSelection(TextDisplayCursor cursor) {
		return p1.getDistanceFrom00() <= cursor.getDistanceFrom00() && cursor.getDistanceFrom00() <= p2.getDistanceFrom00();
	}
	public boolean isOnSelection(Point point, int text_spacing_left, int width, int text_spacing_top) {
		Rectangle[] rects = getSelectionBounds(text_spacing_left, width, text_spacing_top);
		for(Rectangle rect:rects)
			if(rect.contains(point))
				return true;
		return false;
	}



	//Interval text, sequences and removal.
	public String getIntervalText() {
		if(!isClear()) {
			validateP1SmallerP2();
			if(p1.getY()==p2.getY()) {
				return content.getLineText(p1.getY()).substring(p1.getX(), p2.getX());
			} else {
				StringBuilder text = new StringBuilder();
				String entireFirstLine = content.getLineText(p1.getY());
				String firstLine = entireFirstLine.substring(p1.getX());
				text.append(firstLine).append("\n");
				for(int i=p1.getY()+1;i<p2.getY();i++) text.append(content.getLineText(i)).append("\n");
				String lastLine = content.getLineText(p2.getY()).substring(0, p2.getX());
				text.append(lastLine);
				return text.toString();
			}
		}
		return "";
	}

	public LinePart[] getIntervalSequences() {
		if(!isClear()) {
			validateP1SmallerP2();
			if(p1.getY()==p2.getY()) {
				return content.getLine(p1.getY()).getSubSequences(p1.getX(), p2.getX());
			} else {
				LinePart[] firstLineSequences = content.getLine(p1.getY()).getSubSequences(p1.getX(), content.getLine(p1.getY()).length());
				ArrayList<LinePart> sequences = new ArrayList<>(Arrays.asList(firstLineSequences));
				for(int i=p1.getY()+1;i<p2.getY();i++) {
					LinePart lastInLastLine = sequences.get(sequences.size()-1);

					LinePart[] lastLineSequences = content.getLine(i).getSubSequences(0, content.getLine(i).length());
					LinePart firstInLine = lastLineSequences[0];
					if(lastInLastLine.sameLayoutAs(firstInLine)) {
                        sequences.set(sequences.size()-1, lastInLastLine.copy_change(lastInLastLine.txt+"\n"+firstInLine.txt));
                        sequences.addAll(Arrays.asList(lastLineSequences).subList(1, lastLineSequences.length));
					} else {
                        sequences.add(new LinePart("\n"));
                        Collections.addAll(sequences, lastLineSequences);
					}
				}

				LinePart lastInLastLine = sequences.get(sequences.size()-1);
				LinePart[] lastLineSequences = content.getLine(p2.getY()).getSubSequences(0, p2.getX());
				if(lastLineSequences.length>0) {
					LinePart firstInLine = lastLineSequences[0];
					if(lastInLastLine.sameLayoutAs(firstInLine)) {
					    sequences.set(sequences.size()-1, lastInLastLine.copy_change(lastInLastLine.txt+"\n"+firstInLine.txt));
                        sequences.addAll(Arrays.asList(lastLineSequences).subList(1, lastLineSequences.length));
					} else {
						sequences.add(new LinePart("\n"));
                        sequences.addAll(Arrays.asList(lastLineSequences));
					}
				}
				return sequences.toArray(new LinePart[sequences.size()]);
			}
		}
		return new LinePart[0];
	}
	public void removeIntervalText(TextDisplayCursor parentCursor) {
		if(!isClear()) {
			if(p1.getY()==p2.getY()) {
				content.setLine(p1.getY(), content.getLine(p1.getY()).removeInterval(p1.getX(), p2.getX()));
				parentCursor.setXY(p1.getXY());
				clear();
			} else {
				content.setLine(p1.getY(), content.getLine(p1.getY()).removeTextFrom(p1.getX()));//, new Line(UTIL.getList(new CharSequence(.getXY(p1.getY()).toString().substring(0, p1.getX())))));
                content.setLine(p2.getY(), content.getLine(p2.getY()).removeTextUpTo(p2.getX()));//, new Line(UTIL.getList(new CharSequence(.getXY(p2.getY()).toString().substring(p2.getX().getXY(p2.getY()).length())))));

				for(int i=p2.getY()-1;i>p1.getY();i--)
                    content.removeLine(i);
				parentCursor.setXY(p1.getXY());

                Line removedLine = content.removeLine(p1.getY()+1);
                content.setLine(p1.getY(), content.getLine(p1.getY()).append(removedLine));

				clear();
			}
		}
	}








	///DRAWING DRAWING

    public void draw(Graphics2D g, Color c, int text_spacing_left, int width, int text_spacing_top) {
        if(!content.isSelectionEnabled() || isClear())return;
        Composite oldC = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        g.setColor(c);
        Rectangle[] bounds = getSelectionBounds(text_spacing_left, width, text_spacing_top);
        for(Rectangle r:bounds) {
            g.fill(r);
        }
        g.setComposite(oldC);
    }
    private Rectangle[] getSelectionBounds(int text_spacing_left, int width, int text_spacing_top) {
        ArrayList<Rectangle> rects = new ArrayList<>();

        validateP1SmallerP2();

        Rectangle r1 = p1.getShape(text_spacing_left, text_spacing_top);
        Rectangle r2 = p2.getShape(text_spacing_left, text_spacing_top);

        if(r1.y == r2.y) {
            Rectangle mid = new Rectangle(r1.x, r1.y, r2.x - r1.x, r1.height);
            rects.add(mid);
        } else {
            Rectangle start = new Rectangle(r1.x, r1.y, width - r1.x, r1.height);
            Rectangle end = new Rectangle(text_spacing_left, r2.y, r2.x-(text_spacing_left-2), r2.height);
            Rectangle mid = new Rectangle(text_spacing_left, r1.y+r1.height, width-(text_spacing_left-2), r2.y - (r1.y+r1.height));
            rects.add(start);
            rects.add(end);
            rects.add(mid);
        }

        return rects.toArray(new Rectangle[0]);
    }
}