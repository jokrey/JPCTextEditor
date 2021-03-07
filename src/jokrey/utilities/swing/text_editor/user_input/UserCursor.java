package jokrey.utilities.swing.text_editor.user_input;

import jokrey.utilities.swing.text_editor.text_storage.ContentEditor;
import jokrey.utilities.swing.text_editor.text_storage.Line;
import jokrey.utilities.swing.text_editor.text_storage.DecoratedLinePart;
import jokrey.utilities.swing.text_editor.text_storage.LinePartAppearance;
import jokrey.utilities.swing.text_editor.user_input.cursor.TextDisplayCursor;
import jokrey.utilities.swing.text_editor.user_input.cursor.TextInterval;

import java.awt.*;

/**
 * THE user cursor. Should not be instantiated from the outside and only be used by UserInputHandler.
 */
public class UserCursor extends TextDisplayCursor {
	private LinePartAppearance insert_layout = null; //null means some standard layout
        protected LinePartAppearance getRawInsertLayout() {
            return insert_layout;
        }
        public LinePartAppearance.Instantiated getValidInsertLayout() {
            if(insert_layout==null) {
                return content.getStandardLayout();
            } else
                return LinePartAppearance.valid(insert_layout, content.getStandardLayout());
        }
		public void setInsertLayout(LinePartAppearance newInsertLayout) {
			insert_layout = newInsertLayout;
			content.fireUserCursorLayoutChanged();
		}
	public final TextInterval selection;
		public TextInterval getSelection(){return selection;}
		public void clearSelection() {selection.clear();}
		public boolean isSelectionClear() {
			return selection.isClear();
		}
	private final ContentEditor content;
		ContentEditor getContentEditor() {
		    return content;
        }
	public UserCursor(ContentEditor content) {
        super(content);
        this.content = content;
		selection = new TextInterval(content);
	}
    public void validateCursorVisibility() {
        content.validateCursorVisibility();
    }

	@Override public void setXY(int x, int y) {
		int[] old = getXY();
		super.setXY(x, y);
		if(getX()!=old[0] || getY()!=old[1])
    		content.fireUserCursorPosChanged(x, y);
	}

	public void draw(Graphics2D g, int text_spacing_left, int width, int text_spacing_top, boolean drawSelection, boolean drawCursor) {
	    if(drawSelection)
        	selection.draw(g, Color.BLUE.brighter(), text_spacing_left, width, text_spacing_top);
	    if(drawCursor) {
			Color fg = LinePartAppearance.valid(insert_layout, content.getStandardLayout()).fg;
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
    public void insert(DecoratedLinePart... parts) {
        for(DecoratedLinePart part:parts)
            insert(part.txt, part.layout);
    }
    public void insert(DecoratedLinePart part) {
        insert(part.txt, part.layout);
    }
	public void insert(String str, LinePartAppearance layoutForInsert) {
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

				content.insert(getX(), getY(), cleanStr, layoutForInsert);
				x_plus(cleanStr.length());
			}
		}
	}
	public DecoratedLinePart[] delete() {
		if(!selection.isClear()) {
			return removeSelectedInterval();
		} else {
			DecoratedLinePart removed = null;
			Line curCursorLine = content.getLine(getY());
			if (getX()==curCursorLine.length()&&getY()+1<content.getLineCount()) {
			    content.setLine(getY(), content.getLine(getY()).append(content.removeLine(getY()+1)));
				removed=new DecoratedLinePart("\n", insert_layout);//fuck coloring for an invisible line wrap
			} else {
				if(getX()<curCursorLine.length()) {
					removed=content.getLine(getY()).getSingleCharAt_AsLinePart(getX());
					content.setLine(getY(), content.getLine(getY()).removeCharAt(getX()));
				}
			}

			return removed==null?new DecoratedLinePart[0]:new DecoratedLinePart[]{removed};
		}
	}
	public DecoratedLinePart[] backspace() {
		if(!selection.isClear()) {
			return removeSelectedInterval();
		} else {
			if(getX()==0&&getY()==0) return new DecoratedLinePart[0];
			DecoratedLinePart removed;
			if (getX()==0&&getY()>0) {
				Line curCursorLine = content.getLine(getY());
				content.setLine(getY()-1, content.getLine(getY()-1).append(curCursorLine));
				content.removeLine(getY());
				x_minus(curCursorLine.length()+1);
				removed=new DecoratedLinePart("\n", insert_layout);
			} else {
				removed=content.getLine(getY()).getSingleCharAt_AsLinePart(getX()-1);
                content.setLine(getY(), content.getLine(getY()).removeCharAt(getX()-1));
				x_minus(1);
			}
			return removed==null?new DecoratedLinePart[0]:new DecoratedLinePart[]{removed};
		}
	}

	public DecoratedLinePart[] removeSelectedInterval() {
		if(selection.isClear())return new DecoratedLinePart[0];
		int distancep1From=selection.get1_distance00();
		DecoratedLinePart[] interval_clrb = selection.getIntervalSequences();
		selection.removeIntervalText(this);
		setFromDistance(distancep1From);
		return interval_clrb;
	}
	public void setSelection(int distance1, int distance2) {
		selection.setFromDistance(distance1, distance2);
	}
}