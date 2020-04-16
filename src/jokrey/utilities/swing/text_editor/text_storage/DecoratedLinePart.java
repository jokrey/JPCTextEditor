package jokrey.utilities.swing.text_editor.text_storage;

import jokrey.utilities.swing.text_editor.FontMetricsSupplier;
import jokrey.utilities.transparent_storage.SubStorage;

import java.awt.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Immutable.
 */
public class DecoratedLinePart implements Serializable {
	public final String txt;
	public final LinePartAppearance layout; //can decidedly be null. Will the be replaced with a standard layout at drawtime.
    public DecoratedLinePart(String string) {
        this(string, null);
    }
    public DecoratedLinePart(String string, LinePartAppearance layout) {
		if(string==null) txt="";
		else             txt = string;
		this.layout = layout;
	}
	private DecoratedLinePart(String string, LinePartAppearance layout, FontMetrics fm) {
        this(string, layout);
        this.fm=fm;
    }
	public DecoratedLinePart copy_change(String string) {
        return new DecoratedLinePart(string, layout, fm);
    }

	//txt quick accessor.
	public boolean isEmpty() {
		return txt.isEmpty();
	}
	public int length() {
		return txt.length();
	}
    public String substring(int start) {
        return txt.substring(start);
    }
    public String substring(int start, int end) {
        return txt.substring(start, end);
    }

	public DecoratedLinePart[] splitAt(int x) {
        if(x<0 || x>length())
            throw new ArrayIndexOutOfBoundsException("x="+x);

		DecoratedLinePart cs1 = copy_change(substring(0, x));
		DecoratedLinePart cs2 = copy_change(substring(x));
		return new DecoratedLinePart[]{cs1, cs2};
	}




	//to string, equals
    @Override public String toString() {return "[LinePart: "+txt+", "+layout+"]";}
    @Override public boolean equals(Object o) {
        return o instanceof DecoratedLinePart &&
                txt.equals(((DecoratedLinePart)o).txt) &&
                layout.equals(((DecoratedLinePart)o).layout);
    }
	public boolean sameLayoutAs(DecoratedLinePart o) {
		return Objects.equals(layout, o.layout);
	}
	public static String toString(DecoratedLinePart... parts) {
        StringBuilder sb = new StringBuilder();
        for (DecoratedLinePart part : parts) sb.append(part.txt);
        return sb.toString();
    }



    private static int TAB_PIXEL_WIDTH = 33;
    public static int getNextTabXPosition(int orig_x) {
        return ( (orig_x/TAB_PIXEL_WIDTH)+1 ) * TAB_PIXEL_WIDTH;
    }
	//DRAWING
    public void draw(FontMetricsSupplier display, boolean opaque, Graphics2D g, LinePartAppearance.Instantiated fallback, Rectangle linePartArea) {
        //tab functionality, test regarding performance in large scales.
        int indexOfTab = txt.indexOf("\t");
        int x_add = 0;
        if(indexOfTab >= 0) {
            DecoratedLinePart lp = this;
            while (indexOfTab >= 0) {
                DecoratedLinePart cs1 = copy_change(lp.substring(0, indexOfTab));
                cs1.draw(display, opaque, g, fallback, new Rectangle(linePartArea.x + x_add, linePartArea.y, linePartArea.width-x_add, linePartArea.height));  //does not contain any tabs.
                x_add += g.getFontMetrics().stringWidth(cs1.txt);
                x_add = getNextTabXPosition(x_add);
                lp = copy_change(lp.substring(indexOfTab+1));
                indexOfTab = lp.txt.indexOf("\t");
            }
            lp.draw(display, opaque, g, fallback, new Rectangle(linePartArea.x + x_add, linePartArea.y, linePartArea.width-x_add, linePartArea.height)); //does not contain any tabs
            return;
        }

        LinePartAppearance.Instantiated valid = LinePartAppearance.valid(layout, fallback);

        for(LinePartEffect bg_effect:valid.background_effects) {
            g.setColor(valid.fg);
            bg_effect.drawEffect(g, linePartArea, opaque, true);
        }

        g.setFont(valid.font);

        updateFontMetrics(display, valid);
        g.setColor(valid.fg);
        g.drawString(txt, linePartArea.x, linePartArea.y + (int)(linePartArea.height * 0.75));

        for(LinePartEffect fg_effect:valid.foreground_effects) {
            g.setColor(valid.fg);
            fg_effect.drawEffect(g, linePartArea, opaque, false);
        }
    }

	//Pixel size querying
	//set with the validated layout at runtime.
	protected FontMetrics fm;
    public int getPixelWidth() throws NeverDrawnException {
        if(txt.isEmpty()) return 0;
        if (fm == null) throw new NeverDrawnException();

        //tab functionality, test regarding performance in large scales.
        int indexOfTab = txt.indexOf("\t");
        if(indexOfTab >= 0) {
            DecoratedLinePart lp = this;
            int x_draw = 0;
            while (indexOfTab >= 0) {
                DecoratedLinePart cs1 = copy_change(lp.substring(0, indexOfTab));
                x_draw += cs1.fm.stringWidth(cs1.txt);
                x_draw = getNextTabXPosition(x_draw);
                lp = copy_change(lp.substring(indexOfTab+1));
                indexOfTab = lp.txt.indexOf("\t");
            }
            return x_draw+lp.fm.stringWidth(lp.txt);
        }

        return fm.stringWidth(txt);
    }
    public int getPixelWidth(int start, int end) throws NeverDrawnException {
        if(txt.isEmpty() || start==end) return 0;
        if (fm == null) throw new NeverDrawnException();
        return copy_change(substring(start, end)).getPixelWidth();
    }
	public int getPixelHeight() throws NeverDrawnException {
        if(fm == null) {
            if(txt.isEmpty())
                return 0;
            throw new NeverDrawnException();
        }
		return fm.getHeight();
	}
    public void updateFontMetrics(FontMetricsSupplier display, LinePartAppearance.Instantiated fallback) {
        fm=display.getFontMetrics(LinePartAppearance.valid(layout, fallback).font);
    }
}