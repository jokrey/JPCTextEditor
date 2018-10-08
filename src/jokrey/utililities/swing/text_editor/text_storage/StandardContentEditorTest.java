package jokrey.utililities.swing.text_editor.text_storage;

import jokrey.utililities.swing.text_editor.ui.JPCTextField;

import static org.junit.Assert.assertEquals;

public class StandardContentEditorTest {
    @org.junit.Test
    public void setText() {
        StandardContentEditor e = new StandardContentEditor(new JPCTextField());
        assertEquals("", e.getText());
        e.setText("");
        assertEquals("", e.getText());
        e.setText("\n");
        assertEquals("\n", e.getText());
    }

    @org.junit.Test
    public void setText1() {
    }
}