package jokrey.utilities.swing.text_editor.text_storage;

import jokrey.utilities.swing.text_editor.text_storage.lineparteffects.LPEffect_Fill;

import java.awt.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * IMMUTABLE
 */
public abstract class LinePartAppearance implements Serializable {
    public final Color fg;
    public final Font font;
    public final LinePartEffects background_effects;
    public final LinePartEffects foreground_effects;
    public LinePartAppearance(Color fg, Font font, LinePartEffects background_effects, LinePartEffects foreground_effects) {
        this.fg = fg;
        this.font = font;
        this.background_effects = background_effects;
        this.foreground_effects = foreground_effects;
    }
    
    @Override public String toString() {
        return "[LinePartAppearance: " + fg + ", " + font + ", " + background_effects + ", " + foreground_effects + "]";
    }
    @Override public boolean equals(Object o) {
        return o instanceof LinePartAppearance &&
                Objects.equals(fg, ((LinePartAppearance) o).fg) &&
                Objects.equals(font, ((LinePartAppearance) o).font) &&
                background_effects.equals(((LinePartAppearance) o).background_effects) &&
                foreground_effects.equals(((LinePartAppearance) o).foreground_effects);
    }


    //derive methods. Meaning create from this object.
    //raw:
    public abstract LinePartAppearance copy_ChangeFG(Color fg);
    public abstract LinePartAppearance copy_ChangeFont(Font font);
    public abstract LinePartAppearance copy_ChangeBackgroundEffects(LinePartEffects background_effects);
    public abstract LinePartAppearance copy_ChangeForegroundEffects(LinePartEffects foreground_effects);

    //working with/semantic:
    public LinePartAppearance copy_ChangeFontSize(int font_size) {
        return copy_ChangeFont(font.deriveFont((float)font_size));
    }
    public LinePartAppearance copy_ChangeFontStyle(int font_style) {
        return copy_ChangeFont(font.deriveFont(font_style));
    }
    public LinePartAppearance copy_ChangeAddBackgroundEffect(LinePartEffect effect) {
        return copy_ChangeBackgroundEffects(background_effects.copy_add(effect));
    }
    public LinePartAppearance copy_ChangeAddForegroundEffect(LinePartEffect effect) {
        return copy_ChangeForegroundEffects(foreground_effects.copy_add(effect));
    }
    public LinePartAppearance copy_ChangeSetBackgroundEffect(LinePartEffect... effect) {
        return copy_ChangeBackgroundEffects(new LinePartEffects(effect));
    }
    public LinePartAppearance copy_ChangeSetForegroundEffect(LinePartEffect... effect) {
        return copy_ChangeForegroundEffects(new LinePartEffects(effect));
    }

    public LinePartAppearance copy_ChangeBG(Color bg) {
        for(int i=0;i<background_effects.length();i++)
            if(background_effects.get(i) instanceof LPEffect_Fill)
                return copy_ChangeBackgroundEffects(background_effects.copy_set(i, new LPEffect_Fill(bg)));
        return copy_ChangeBackgroundEffects(background_effects.copy_add(new LPEffect_Fill(bg)));
    }
    public Color getBG_canbenull() {
        for(LinePartEffect eff:background_effects)
            if(eff instanceof LPEffect_Fill)
                return ((LPEffect_Fill)eff).c;
        return null;
    }



    public static class Instantiated extends LinePartAppearance {
        public Instantiated(Color fg, Font font, LinePartEffects background_effects, LinePartEffects foreground_effects) {
            super(fg, font, background_effects, foreground_effects);
            if(fg==null||font==null||background_effects==null||foreground_effects==null)
                throw new NullPointerException("every field has be non null.");
        }
        public Instantiated(LinePartAppearance from) {
            this(from.fg, from.font, from.background_effects, from.foreground_effects);
        }
        public Instantiated(Color fg, Font font) {
            this(fg, font, new LinePartEffects(), new LinePartEffects());
        }
        @Override public LinePartAppearance.Instantiated copy_ChangeFG(Color fg) {
            return new Instantiated(fg, font, background_effects, foreground_effects);
        }
        @Override public LinePartAppearance.Instantiated copy_ChangeFont(Font font) {
            return new Instantiated(fg, font, background_effects, foreground_effects);
        }
        @Override public LinePartAppearance.Instantiated copy_ChangeBackgroundEffects(LinePartEffects background_effects) {
            return new Instantiated(fg, font, background_effects, foreground_effects);
        }
        @Override public LinePartAppearance.Instantiated copy_ChangeForegroundEffects(LinePartEffects foreground_effects) {
            return new Instantiated(fg, font, background_effects, foreground_effects);
        }
        public LinePartAppearance.Instantiated copy_ChangeFontSize(int font_size) {
            return copy_ChangeFont(font.deriveFont((float)font_size));
        }
        public LinePartAppearance.Instantiated copy_ChangeFontStyle(int font_style) {
            return copy_ChangeFont(font.deriveFont(font_style));
        }
        public LinePartAppearance.Instantiated copy_ChangeBG(Color bg) {
            for(int i=0;i<background_effects.length();i++)
                if(background_effects.get(i) instanceof LPEffect_Fill)
                    return copy_ChangeBackgroundEffects(background_effects.copy_set(i, new LPEffect_Fill(bg)));
            return copy_ChangeBackgroundEffects(background_effects.copy_add(new LPEffect_Fill(bg)));
        }
    }


    /**
     *     each field can decidedly be null. Then it will be replaced at drawing time with a standard.
     */
    public static class UnInstantiated extends LinePartAppearance {
        public UnInstantiated(Color fg, Font font, LinePartEffects background_effects, LinePartEffects foreground_effects) {
            super(fg, font, background_effects, foreground_effects);
        }
        @Override public LinePartAppearance.UnInstantiated copy_ChangeFG(Color fg) {
            return new UnInstantiated(fg, font, background_effects, foreground_effects);
        }
        @Override public LinePartAppearance.UnInstantiated copy_ChangeFont(Font font) {
            return new UnInstantiated(fg, font, background_effects, foreground_effects);
        }
        @Override public LinePartAppearance.UnInstantiated copy_ChangeBackgroundEffects(LinePartEffects background_effects) {
            return new UnInstantiated(fg, font, background_effects, foreground_effects);
        }
        @Override public LinePartAppearance.UnInstantiated copy_ChangeForegroundEffects(LinePartEffects foreground_effects) {
            return new UnInstantiated(fg, font, background_effects, foreground_effects);
        }
    }



    public static Instantiated valid(LinePartAppearance orig, Instantiated fallback) {
	    if(orig==null)
	        return fallback;
	    else
	        return getFullyInstantiatedLayout(orig, fallback);
    }

    /**
     * @param fallback Each field has to be NON NULL.
     * @return a layout without null values.
     */
    private static Instantiated getFullyInstantiatedLayout(LinePartAppearance layout, Instantiated fallback) {
        if(layout==null)
            return fallback;
        else {
            if (layout.fg == null && layout.font == null && layout.background_effects == null && layout.foreground_effects == null) {
                return fallback;//saves one instantiation.
//            } else if(layout.fg != null && layout.bg != null && layout.font!=null && layout.lines!=null) {
//                return new Instantiated(layout);
            } else {
                Color fg = layout.fg == null ? fallback.fg : layout.fg;
                Font font = layout.font==null ? fallback.font : layout.font;
                LinePartEffects background_effects = layout.background_effects == null ? fallback.background_effects : layout.background_effects;
                LinePartEffects foreground_effects = layout.foreground_effects == null ? fallback.foreground_effects : layout.foreground_effects;
                return new Instantiated(fg, font, background_effects, foreground_effects);
            }
        }
    }
}