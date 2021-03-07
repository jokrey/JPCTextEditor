package jokrey.utilities.swing.text_editor;


import java.awt.*;

/**
 * Connects the various JPC sub functionality back to their parent without a direct, TOO OPEN, reference.
 * Limits functionality.
 */
public interface JPC_Connector extends FontMetricsSupplier {
    int getTextSpacingLeft();
    int getTextSpacingTop();
    int getDisplayWidth();
    void repaint();
    void recalculateSize();
    void validateCursorVisibility();

    int getFirstVisibleLine();
    int getLastVisibleLine();

    boolean enterPressed();

    void recalculateDisplayLines();
    void recalculateDisplayLine(int line);

    static JPC_Connector createEmpty() {
        return new JPC_Connector() {
            @Override public FontMetrics getFontMetrics(Font font) {
                return null;
            }
            @Override public int getTextSpacingLeft() {
                return 0;
            }
            @Override public int getTextSpacingTop() {
                return 0;
            }
            @Override public int getDisplayWidth() {
                return 0;
            }
            @Override public void repaint() { }
            @Override public void recalculateSize() { }
            @Override public void validateCursorVisibility() { }
            @Override public int getFirstVisibleLine() { return 0; }
            @Override public int getLastVisibleLine() { return 0; }
            @Override public boolean enterPressed() { return false; }
            @Override public void recalculateDisplayLines() { }
            @Override public void recalculateDisplayLine(int line) { }
        };
    }
}