package jokrey.utilities.swing.text_editor.text_storage;

import java.awt.*;
import java.io.Serializable;

/**
 * IMMUTABLE
 *    EVERY SUBCLASS HAS TO BE IMMUTABLE ALSO. IF NOT WEIRD, UNINTENDED EFFECTS MIGHT OCCUR
 *
 * Can be extended to create custom effects.
 */
public abstract class LinePartEffect implements Serializable {
    public abstract void drawEffect(Graphics2D g, Rectangle linePartArea, boolean opaque, boolean is_background);

    @Override public abstract String toString();
    @Override public abstract boolean equals(Object o);
}
