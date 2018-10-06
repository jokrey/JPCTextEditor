package jokrey.utililities.swing.text_editor;


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
}