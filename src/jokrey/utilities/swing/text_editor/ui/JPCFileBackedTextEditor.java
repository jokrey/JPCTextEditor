package jokrey.utilities.swing.text_editor.ui;

import jokrey.utilities.swing.text_editor.JPCWrappingTextEditor;
import jokrey.utilities.swing.text_editor.JPC_Connector;
import jokrey.utilities.swing.text_editor.text_storage.ContentEditor;
import jokrey.utilities.swing.text_editor.text_storage.FileBackedContentEditor;
import jokrey.utilities.swing.text_editor.text_storage.WrappingContentEditor;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class JPCFileBackedTextEditor extends JPCSimpleTextEditor implements JPCWrappingTextEditor {
    public JPCFileBackedTextEditor() {}

    @Override public ContentEditor createContentEditor(JPC_Connector c) {
        return new FileBackedContentEditor(c);
    }

    public void setFile(File f) throws FileNotFoundException {
        ((FileBackedContentEditor) content).setFile(f);
    }

    public void setLineWrap(boolean wrap_lines) {
        ((WrappingContentEditor)content).setLineWrap(wrap_lines);
    }
    public boolean isLineWrapEnabled() {return ((WrappingContentEditor)content).isLineWrapEnabled();}

    @Override protected boolean handlePaintedSpaceRecalculation(Dimension newResult) {
        if(isLineWrapEnabled())
            newResult.width-=getTextSpacingLeft();//do i get why? no, but it works like that - otherwise sometimes the horizontal scrollbar pops up (not because of scrollbar size which is roughly the same, right?)
        return super.handlePaintedSpaceRecalculation(newResult);
    }
}