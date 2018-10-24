package jokrey.utilities.swing.text_editor.text_storage.lineparteffects;

import java.awt.*;

public class LPEffect_UnderlineLine extends LPEffect_HorizontalLine {
    public LPEffect_UnderlineLine() {
        super(null, 80);
    }
    public LPEffect_UnderlineLine(Color color) {
        super(color, 80);
    }

    @Override public String toString() {
        return "[Effect(Underline): with"+c+"]";
    }
    @Override public boolean equals(Object o) {
        return o instanceof LPEffect_UnderlineLine && c.equals(((LPEffect_UnderlineLine)o).c);
    }
}
