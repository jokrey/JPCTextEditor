package jokrey.utililities.swing.text_editor;

import jokrey.utililities.swing.text_editor.text_storage.ContentEditor;
import jokrey.utililities.swing.text_editor.text_storage.WrappingContentEditor;

import java.awt.*;

public class JPCSimpleWrappingTextEditor extends JPCSimpleTextEditor implements JPCWrappingTextEditor {
    public JPCSimpleWrappingTextEditor() {}

    @Override public ContentEditor createContentEditor() {
        return new WrappingContentEditor();
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