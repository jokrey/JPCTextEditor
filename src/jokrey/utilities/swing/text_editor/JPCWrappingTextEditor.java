package jokrey.utilities.swing.text_editor;

public interface JPCWrappingTextEditor extends JPCTextEditor {
    void setLineWrap(boolean wrap_lines);
    boolean isLineWrapEnabled();
}
