package jokrey.utilities.swing.text_editor.text_storage;

import jokrey.utilities.swing.text_editor.ui.JPCTextField;

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
        e.setText("\n\n\n\n\n\n\n\n\n\n\n\n");
        assertEquals("\n\n\n\n\n\n\n\n\n\n\n\n", e.getText());
        e.setText("\nhallo");
        assertEquals("\nhallo", e.getText());
        e.setText("\nhal\nlo");
        assertEquals("\nhal\nlo", e.getText());
        e.setText("h\nal\nlo");
        assertEquals("h\nal\nlo", e.getText());
    }

    @org.junit.Test
    public void setText1() {
    }
}