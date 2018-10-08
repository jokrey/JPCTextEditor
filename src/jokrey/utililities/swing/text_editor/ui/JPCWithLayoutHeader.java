package jokrey.utililities.swing.text_editor.ui;

import jokrey.utililities.swing.text_editor.text_storage.ContentListener;
import jokrey.utililities.swing.text_editor.text_storage.LinePartLayout;
import jokrey.utililities.swing.text_editor.ui.additional.CustomEditorConnector;
import jokrey.utililities.swing.text_editor.ui.additional.LayoutChangingPanel;

import javax.swing.*;
import java.awt.*;

public class JPCWithLayoutHeader extends JPCLayoutedTextEditor {
    private JPanel headerPanel;
    private LayoutChangingPanel headerPanelLayout;
    public JPCWithLayoutHeader() {
        setLayout(new BorderLayout());
        headerPanel = new JPanel();
        headerPanelLayout = new LayoutChangingPanel(new CustomEditorConnector() {
            @Override public Color getCurFG() {
                return getForeground();
            }
            @Override public Color getCurBG() {
                return getBackground();
            }
            @Override public Font getCurFont() {
                return getFont();
            }
            @Override public void fg_changed(Color fg) {
                if(fg!=null) setForeground(fg);
            }
            @Override public void bg_changed(Color bg) {
                if(bg!=null) setCursorBackground(bg);
            }
            @Override public void font_changed(Font font) {
                if(font!=null) setFont(font);
            }
        });
        headerPanel.add(headerPanelLayout);
        add(headerPanel, BorderLayout.NORTH);

        addContentListener(new ContentListener() {
            @Override public void userCursorLayoutChanged() {
                LinePartLayout layout = getCurrentLayout();
                if(headerPanelLayout==null)return;
                headerPanelLayout.updateDisplayValues(layout.fg, layout.bg, layout.font);
            }
        });
    }

    @Override public int getTextSpacingTop() {
        return super.getTextSpacingTop()+headerPanel.getHeight();
    }
}
