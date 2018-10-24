package jokrey.utilities.swing.text_editor.text_storage.lineparteffects;

import jokrey.utilities.swing.text_editor.text_storage.LinePartEffect;

import java.awt.*;

public class LPEffect_Fill extends LinePartEffect {
    public final Color c;
    public LPEffect_Fill(Color color) {
        this.c = color;
    }

    @Override public void drawEffect(Graphics2D g, Rectangle linePartArea, boolean opaque, boolean is_background) {
        if(!opaque) return;
        g.setColor(c);
        g.fill(linePartArea);
    }

    @Override public String toString() {
        return "[Effect(FILL): with"+c+"]";
    }
    @Override public boolean equals(Object o) {
        return o instanceof LPEffect_Fill && c.equals(((LPEffect_Fill)o).c);
    }
}
