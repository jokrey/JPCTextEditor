package jokrey.utililities.swing.text_editor.text_storage;

import java.awt.*;
import java.util.Objects;

/**
 * IMMUTABLE
 */
public abstract class LinePartLayout {
    public final Color fg;
    public final Color bg;
    public final Font font;
    public LinePartLayout(Color fg, Color bg, Font font) {
        this.fg = fg;
        this.bg = bg;
        this.font = font;
    }
    
    public abstract LinePartLayout copy_ChangeFG(Color fg);
    public abstract LinePartLayout copy_ChangeBG(Color bg);
    public abstract LinePartLayout copy_ChangeFont(Font font);
    public abstract LinePartLayout copy_ChangeFontSize(int font_size);
    public abstract LinePartLayout copy_ChangeFontStyle(int font_style);
    
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
        public Instantiated(Color fg, Color bg, Font font) {
            super(fg, bg, font);
            if(fg==null||bg==null||font==null)throw new NullPointerException("every field has be non null.");
        }
        public Instantiated(LinePartLayout from) {
            this(from.fg, from.bg, from.font);
        }
        @Override public LinePartLayout.Instantiated copy_ChangeFG(Color fg) {
            return new Instantiated(fg, bg, font);
        }
        @Override public LinePartLayout.Instantiated copy_ChangeBG(Color bg) {
            return new Instantiated(fg, bg, font);
        }
        @Override public LinePartLayout.Instantiated copy_ChangeFont(Font font) {
            return new Instantiated(fg, bg, font);
        }
        @Override public LinePartLayout.Instantiated copy_ChangeFontSize(int font_size) {
            if(font_size<1)font_size=1;
            return new Instantiated(fg, bg, font.deriveFont((float)font_size));
        }
        @Override public LinePartLayout.Instantiated copy_ChangeFontStyle(int font_style) {
            return new Instantiated(fg, bg, font.deriveFont(font_style));
        }
    }


    /**
     *     each field can decidedly be null. Then it will be replaced at drawing time with a standard.
     */
    public static class UnInstantiated extends LinePartLayout {
        public UnInstantiated(Color fg, Color bg, Font font) {
            super(fg, bg, font);
        }
        @Override public LinePartLayout copy_ChangeFG(Color fg) {
            return new UnInstantiated(fg, bg, font);
        }
        @Override public LinePartLayout copy_ChangeBG(Color bg) {
            return new UnInstantiated(fg, bg, font);
        }
        @Override public LinePartLayout copy_ChangeFont(Font font) {
            return new UnInstantiated(fg, bg, font);
        }
        @Override public LinePartLayout copy_ChangeFontSize(int font_size) {
            if(font_size<1)font_size=1;
            return new UnInstantiated(fg, bg, font.deriveFont((float)font_size));
        }
        @Override public LinePartLayout copy_ChangeFontStyle(int font_style) {
            return new UnInstantiated(fg, bg, font.deriveFont(font_style));
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
     * @return
     */
    private static Instantiated getFullyInstantiatedLayout(LinePartLayout layout, Instantiated fallback) {
        if(layout==null)
            return fallback;
        else {
            if (layout.fg == null && layout.bg == null && layout.font == null) {
                return fallback;//saves one instantiation.
            } else if(layout.fg != null && layout.bg != null && layout.font!=null) {
                return new Instantiated(layout);//saves one instantiation.
            } else {
                Color fg = layout.fg == null ? fallback.fg : layout.fg;
                Color bg = layout.bg == null ? fallback.bg : layout.bg;
                if(layout.font == null)
                    return new Instantiated(fg, bg, fallback.font);
                else
                    return new Instantiated(fg, bg, layout.font);
            }
        }
    }
}