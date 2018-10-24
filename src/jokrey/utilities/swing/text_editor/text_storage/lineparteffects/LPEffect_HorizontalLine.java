package jokrey.utilities.swing.text_editor.text_storage.lineparteffects;

import jokrey.utilities.swing.text_editor.text_storage.LinePartEffect;

import java.awt.*;

public class LPEffect_HorizontalLine extends LinePartEffect {
    public final Color c;
    public final int at_height; //in percent
    public LPEffect_HorizontalLine(int at_height) {
        this(null, at_height);
    }
    public LPEffect_HorizontalLine(Color color, int at_height) {
        this.c = color;
        this.at_height = at_height;
        if(at_height < 0 || at_height > 100)
            throw new IllegalArgumentException("line can only be drawn within the available space, therefore at_height must be between 0 and 100( denoted as a percentage)");
    }

    @Override public void drawEffect(Graphics2D g, Rectangle linePartArea, boolean opaque, boolean is_background) {
        if(c!=null) g.setColor(c);
        int y = (int) (linePartArea.y + linePartArea.height * (at_height/100.0));
        g.drawLine(linePartArea.x, y, linePartArea.x + linePartArea.width, y);
    }

    @Override public String toString() {
        return "[Effect(Through): with"+c+" at: "+at_height+"]";
    }
    @Override public boolean equals(Object o) {
        return o instanceof LPEffect_HorizontalLine && c.equals(((LPEffect_HorizontalLine)o).c);
    }
}
