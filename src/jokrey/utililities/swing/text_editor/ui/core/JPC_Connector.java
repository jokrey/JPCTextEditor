package jokrey.utililities.swing.text_editor.ui.core;


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
//    void recalculateDisplayLines_asap();
//    void recalculateDisplayLines_postponed();
//    void recalculateDisplayLines_asap(int firstAffectedLine, int lastAffectedLine);
//    void recalculateDisplayLines_postponed(int firstAffectedLine, int lastAffectedLine);

    int getFirstVisibleLine();
    int getLastVisibleLine();

    boolean enterPressed();

    void recalculateDisplayLines();
    void recalculateDisplayLine(int line);
}