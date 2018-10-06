package jokrey.utililities.swing.text_editor.user_input;

import jokrey.utililities.swing.text_editor.text_storage.*;
import jokrey.utililities.swing.text_editor.user_input.cursor.TextDisplayCursor;
import jokrey.utililities.swing.text_editor.user_input.cursor.TextInterval;

import java.awt.*;

/**
 * THE user cursor. Should not be instantiated from the outside and only be used by UserInputHandler.
 */
public class UserCursor extends TextDisplayCursor {
	private LinePartLayout insert_layout = null; //null means some standard layout
        protected LinePartLayout getRawInsertLayout() {
            return insert_layout;
        }
        public LinePartLayout.Instantiated getValidInsertLayout() {
            if(insert_layout==null) {
                return content.getStandardLayout();
            } else
                return LinePartLayout.valid(insert_layout, content.getStandardLayout());
        }
		public void setInsertLayout(LinePartLayout newInsertLayout) {
			insert_layout = newInsertLayout;
			content.fireUserCursorLayoutChanged();
		}
	public final TextInterval selection;
		public TextInterval getSelection(){return selection;}
		public void clearSelection() {selection.clear();}
	private final ContentEditor content;
		ContentEditor getContentEditor() {
		    return content;
        }
	public UserCursor(ContentEditor content) {
        super(content);
        this.content = content;
		selection = new TextInterval(content);
	}

	@Override public void setXY(int... xy) {
		int[] old = getXY();
		super.setXY(xy);
		if(getX()!=old[0] || getY()!=old[1])
    		content.fireUserCursorPosChanged(xy[0], xy[1]);
	}

	public void draw(Graphics2D g, int text_spacing_left, int width, int text_spacing_top, boolean drawSelection, boolean drawCursor) throws NeverDrawnException {
	    if(drawSelection)
        	selection.draw(g, Color.BLUE.brighter(), text_spacing_left, width, text_spacing_top);
	    if(drawCursor) {
			Color fg = LinePartLayout.valid(insert_layout, content.getStandardLayout()).fg;
			g.setColor(fg);
			g.fill(getShape(text_spacing_left, text_spacing_top));
        }
	}


	private void newLine() {
		Line[] newLines = content.getLine(getY()).splitAt(getX());
		if(getY()<content.getLineCount())		content.setLine(getY(), newLines[0].isEmpty()?new Line(insert_layout):newLines[0]);
		else 									content.addLine(getY(), newLines[0].isEmpty()?new Line(insert_layout):newLines[0]);
        content.addLine(getY()+1,  newLines[1].isEmpty()?new Line(insert_layout):newLines[1]);
		x_plus(1);
	}
	public void insert(String str) {
		insert(str, insert_layout);
	}
    public void insert(LinePart... parts) {
        for(LinePart part:parts)
            insert(part.txt, part.layout);
    }
    public void insert(LinePart part) {
        insert(part.txt, part.layout);
    }
	public void insert(String str, LinePartLayout layoutForInsert) {
		if(str==null||str.isEmpty()||!content.allowInsertion(str))return;
		if(!selection.isClear()) {
			throw new RuntimeException("SOMEONE FORGOT A SELECTION REMOVAL - NEEDS TO BE MANUAL to avoid unintentionally loosing data..");
		}

		if(str.startsWith("\n")) {
			newLine();
			insert(str.substring(1), layoutForInsert);//replaces the \n
		} else if(str.endsWith("\n")) {
			insert(str.substring(0,str.length()-1), layoutForInsert);//replaces the \n
			newLine();
		} else {
			if(str.contains("\n")) {
				String[] newLines = str.split("\n");
				for(int i=0;i<newLines.length;i++) {
					if(i==0)
						insert(newLines[i], layoutForInsert);
					else
						insert("\n"+newLines[i], layoutForInsert);
				}
			} else {
				String cleanStr = str.replaceAll("[^\\P{Cc}\t]", "");// \\p{C}

				content.setLine(getY(), content.getLine(getY()).insert(getX(), cleanStr, layoutForInsert));
//				content.getLine(getY()).insert(getX(), cleanStr, layoutForInsert);
				x_plus(cleanStr.length());
			}
		}
	}
	public LinePart[] delete() {
		if(!selection.isClear()) {
			return removeSelectedInterval();
		} else {
			LinePart removed = null;
			Line curCursorLine = content.getLine(getY());
			if (getX()==curCursorLine.length()&&getY()+1<content.getLineCount()) {
			    content.setLine(getY(), content.getLine(getY()).append(content.removeLine(getY()+1)));
				removed=new LinePart("\n", insert_layout);//fuck coloring for an invisible line wrap
			} else {
				if(getX()<curCursorLine.length()) {
					removed=content.getLine(getY()).getSingleCharAt_AsLinePart(getX());
					content.setLine(getY(), content.getLine(getY()).removeCharAt(getX()));
				}
			}

			return removed==null?new LinePart[0]:new LinePart[]{removed};
		}
	}
	public LinePart[] backspace() {
		if(!selection.isClear()) {
			return removeSelectedInterval();
		} else {
			if(getX()==0&&getY()==0) return new LinePart[0];
			LinePart removed;
			if (getX()==0&&getY()>0) {
				Line curCursorLine = content.getLine(getY());
				content.setLine(getY()-1, content.getLine(getY()-1).append(curCursorLine));
				content.removeLine(getY());
				x_minus(curCursorLine.length()+1);
				removed=new LinePart("\n", insert_layout);
			} else {
				removed=content.getLine(getY()).getSingleCharAt_AsLinePart(getX()-1);
                content.setLine(getY(), content.getLine(getY()).removeCharAt(getX()-1));
				x_minus(1);
			}
			return removed==null?new LinePart[0]:new LinePart[]{removed};
		}
	}

	public LinePart[] removeSelectedInterval() {
		if(selection.isClear())return new LinePart[0];
		int distancep1From=selection.get1_distance00();
		LinePart[] interval_clrb = selection.getIntervalSequences();
		selection.removeIntervalText(this);
		setFromDistance(distancep1From);
		return interval_clrb;
	}
	public void setSelection(int distance1, int distance2) {
		selection.setFromDistance(distance1, distance2);
	}
}