package jokrey.utilities.swing.text_editor.text_storage;

import java.awt.*;
import java.util.Objects;

/**
 * IMMUTABLE
 */
public abstract class LinePartLayout {
    public final Color fg;
    public final Color bg;
    public final Font font;
    public final int[] lines; //in percent
    public LinePartLayout(Color fg, Color bg, Font font, int[] lines) {
        this.fg = fg;
        this.bg = bg;
        this.font = font;
        this.lines=lines;
        for(int i=0;lines!=null && i<lines.length;i++)
            if(lines[i] < 0 || lines[i] > 100)
                lines[i] = lines[i] < 0 ? 0 : 100;
//                throw new IllegalArgumentException("supplied line ["+i+"] was not between 0 and 100 and therefore not a percentage");
    }
    public static LinePartLayout.Instantiated valid(Color fg, Color bg, Font font, int[] lines) {
        return new LinePartLayout.Instantiated(fg, bg, font, lines);
    }
    public static LinePartLayout.UnInstantiated create(Color fg, Color bg, Font font, int[] lines) {
        return new LinePartLayout.UnInstantiated(fg, bg, font, lines);
    }
    
    public abstract LinePartLayout copy_ChangeFG(Color fg);
    public abstract LinePartLayout copy_ChangeBG(Color bg);
    public abstract LinePartLayout copy_ChangeFont(Font font);
    public abstract LinePartLayout copy_ChangeFontSize(int font_size);
    public abstract LinePartLayout copy_ChangeFontStyle(int font_style);
    public LinePartLayout copy_ClearLines() {
        return copy_SetLines();
    }
    public LinePartLayout copy_SetLine(int line) {
        return copy_SetLines(line);
    }
    public LinePartLayout copy_AddLine(int line) {
        int[] nlines = new int[lines.length+1];
        System.arraycopy(lines, 0, nlines, 0, lines.length);
        nlines[lines.length]=line;
        return copy_SetLines(nlines);
    }
    public abstract LinePartLayout copy_SetLines(int... lines);
    
    @Override public String toString() {
        return "[LinePartLayout: " + fg + ", " + bg + ", " + font + "]";
    }
    @Override public boolean equals(Object o) {
        return o instanceof LinePartLayout &&
                Objects.equals(fg, ((LinePartLayout) o).fg) &&
                Objects.equals(bg, ((LinePartLayout) o).bg) &&
                Objects.equals(font, ((LinePartLayout) o).font);
    }


    public static class Instantiated extends LinePartLayout {
        public Instantiated(Color fg, Color bg, Font font, int[] lines) {
            super(fg, bg, font, lines);
            if(fg==null||bg==null||font==null||lines==null)throw new NullPointerException("every field has be non null.");
        }
        public Instantiated(LinePartLayout from) {
            this(from.fg, from.bg, from.font, from.lines);
        }
        @Override public LinePartLayout.Instantiated copy_ChangeFG(Color fg) {
            return new Instantiated(fg, bg, font, lines);
        }
        @Override public LinePartLayout.Instantiated copy_ChangeBG(Color bg) {
            return new Instantiated(fg, bg, font, lines);
        }
        @Override public LinePartLayout.Instantiated copy_ChangeFont(Font font) {
            return new Instantiated(fg, bg, font, lines);
        }
        @Override public LinePartLayout.Instantiated copy_ChangeFontSize(int font_size) {
            if(font_size<1)font_size=1;
            return new Instantiated(fg, bg, font.deriveFont((float)font_size), lines);
        }
        @Override public LinePartLayout.Instantiated copy_ChangeFontStyle(int font_style) {
            return new Instantiated(fg, bg, font.deriveFont(font_style), lines);
        }
        @Override public LinePartLayout copy_SetLines(int... lines) {
            return new Instantiated(fg, bg, font, lines);
        }
    }


    /**
     *     each field can decidedly be null. Then it will be replaced at drawing time with a standard.
     */
    public static class UnInstantiated extends LinePartLayout {
        public UnInstantiated(Color fg, Color bg, Font font, int[] lines) {
            super(fg, bg, font, lines);
        }
        @Override public LinePartLayout copy_ChangeFG(Color fg) {
            return new UnInstantiated(fg, bg, font, lines);
        }
        @Override public LinePartLayout copy_ChangeBG(Color bg) {
            return new UnInstantiated(fg, bg, font, lines);
        }
        @Override public LinePartLayout copy_ChangeFont(Font font) {
            return new UnInstantiated(fg, bg, font, lines);
        }
        @Override public LinePartLayout copy_ChangeFontSize(int font_size) {
            if(font_size<1)font_size=1;
            if(font==null)
                return this;
            return new UnInstantiated(fg, bg, font.deriveFont((float)font_size), lines);
        }
        @Override public LinePartLayout copy_ChangeFontStyle(int font_style) {
            if(font==null)
                return this;
            return new UnInstantiated(fg, bg, font.deriveFont(font_style), lines);
        }
        @Override public LinePartLayout copy_SetLines(int... lines) {
            return new UnInstantiated(fg, bg, font, lines);
        }
    }



    public static Instantiated valid(LinePartLayout orig, Instantiated fallback) {
	    if(orig==null)
	        return fallback;
	    else
	        return getFullyInstantiatedLayout(orig, fallback);
    }

    /**
     * @param fallback Each field has to be NON NULL.
     * @return a layout without null values.
     */
    private static Instantiated getFullyInstantiatedLayout(LinePartLayout layout, Instantiated fallback) {
        if(layout==null)
            return fallback;
        else {
            if (layout.fg == null && layout.bg == null && layout.font == null && layout.lines == null) {
                return fallback;//saves one instantiation.
//            } else if(layout.fg != null && layout.bg != null && layout.font!=null && layout.lines!=null) {
//                return new Instantiated(layout);
            } else {
                Color fg = layout.fg == null ? fallback.fg : layout.fg;
                Color bg = layout.bg == null ? fallback.bg : layout.bg;
                Font font = layout.font==null ? fallback.font : layout.font;
                int[] lines = layout.lines == null ? fallback.lines : layout.lines;
                return new Instantiated(fg, bg, font, lines);
            }
        }
    }
}