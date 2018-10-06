package jokrey.utililities.swing.text_editor.text_storage;

import jokrey.utililities.swing.text_editor.ui.core.FontMetricsSupplier;

import java.awt.*;
import java.util.Objects;

/**
 * Immutable.
 */
public class LinePart {
	public final String txt;
	public final LinePartLayout layout; //can decidedly be null. Will the be replaced with a standard layout at drawtime.
    public LinePart(String string) {
        this(string, null);
    }
    public LinePart(String string, LinePartLayout layout) {
		if(string==null)txt="";
		else            txt = string;
		this.layout = layout;
	}
	private LinePart(String string, LinePartLayout layout, FontMetrics fm) {
        this(string, layout);
        this.fm=fm;
    }
	public LinePart copy_change(String string) {
        return new LinePart(string, layout, fm);
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

	public LinePart[] splitAt(int x) {
        if(x<0 || x>length())
            throw new ArrayIndexOutOfBoundsException("x="+x);

		LinePart cs1 = copy_change(substring(0, x));
		LinePart cs2 = copy_change(substring(x));
		return new LinePart[]{cs1, cs2};
	}




	//to string, equals
    @Override public String toString() {return "[LinePart: "+txt+", "+layout+"]";}
    @Override public boolean equals(Object o) {
        return o instanceof LinePart &&
                txt.equals(((LinePart)o).txt) &&
                layout.equals(((LinePart)o).layout);
    }
	public boolean sameLayoutAs(LinePart o) {
		return Objects.equals(layout, o.layout);
	}
	public static String toString(LinePart... parts) {
        StringBuilder sb = new StringBuilder();
        for (LinePart part : parts) sb.append(part.txt);
        return sb.toString();
    }



    private static int TAB_PIXEL_WIDTH = 33;
    public static int getNextTabXPosition(int orig_x) {
        return ( (orig_x/TAB_PIXEL_WIDTH)+1 ) * TAB_PIXEL_WIDTH;
    }
	//DRAWING
    public void draw(FontMetricsSupplier display, boolean opaque, Graphics2D g, LinePartLayout.Instantiated fallback, int left_offset, int x_draw, int y_draw) {
        LinePartLayout.Instantiated valid = LinePartLayout.valid(layout, fallback);
        g.setFont(valid.font);
        //tab functionality, test regarding performance in large scales.
        int indexOfTab = txt.indexOf("\t");
        if(indexOfTab >= 0) {
            LinePart lp = this;
            while (indexOfTab >= 0) {
                LinePart cs1 = copy_change(lp.substring(0, indexOfTab));
                cs1.draw(display, opaque, g, fallback, left_offset, x_draw, y_draw);  //does not contain any tabs.
                x_draw += g.getFontMetrics().stringWidth(cs1.txt);
                x_draw = getNextTabXPosition(x_draw);
                lp = copy_change(lp.substring(indexOfTab+1));
                indexOfTab = lp.txt.indexOf("\t");
            }
            lp.draw(display, opaque, g, fallback, left_offset, x_draw, y_draw); //does not contain any tabs
            return;
        }


        updateFontMetrics(display, valid);
        int w = g.getFontMetrics().stringWidth(txt);
        int h = g.getFontMetrics().getHeight();
        if(opaque) {
            g.setColor(valid.bg);
            g.fillRect(left_offset + x_draw, y_draw - h, w, h);
        }
        g.setColor(valid.fg);
        g.drawString(txt, left_offset+x_draw, y_draw - h / 3);
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
            LinePart lp = this;
            int x_draw = 0;
            while (indexOfTab >= 0) {
                LinePart cs1 = copy_change(lp.substring(0, indexOfTab));
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
    public void updateFontMetrics(FontMetricsSupplier display, LinePartLayout.Instantiated fallback) {
        fm=display.getFontMetrics(LinePartLayout.valid(layout, fallback).font);
    }
    public void invalidatePixelCache() {
        fm=null;
    }
}