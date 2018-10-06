package jokrey.utililities.swing.text_editor.ui.core;

import jokrey.utililities.swing.text_editor.ui.core.Abstract_JPCTextEditor;
import jokrey.utililities.swing.text_editor.ui.util.NicerScrollBarUI;
import jokrey.utililities.swing.text_editor.user_input.cursor.TextDisplayCursor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class JPC_Scroller extends JScrollPane {
    private Abstract_JPCTextEditor jpc_editor;
    public JPC_Scroller(Abstract_JPCTextEditor jpc_editor) {
        this.jpc_editor = jpc_editor;
        UIManager.getDefaults().put("ScrollPane.ancestorInputMap",
                new UIDefaults.LazyInputMap(new Object[] {}));  //removes arrow key scrolling

        setViewportView(jpc_editor);
        getVerticalScrollBar().setUI(new NicerScrollBarUI());
        getHorizontalScrollBar().setUI(new NicerScrollBarUI());
        getHorizontalScrollBar().setBackground(Color.darkGray);
        getHorizontalScrollBar().setForeground(Color.gray);
        getVerticalScrollBar().setBackground(Color.darkGray);
        getVerticalScrollBar().setForeground(Color.gray);
        setBackground(getBackground());
        setBorder(BorderFactory.createEmptyBorder());
        getVerticalScrollBar().setUnitIncrement(20);

        addComponentListener(new  ComponentAdapter() {
            @Override public void componentResized(ComponentEvent arg0) {
                jpc_editor.recalculateDisplayLines();
            }
            @Override public void componentShown(ComponentEvent e) {
                jpc_editor.recalculateDisplayLines();
            }
        });

        jpc_editor.hasScrollPane=true;
    }


    public void scrollToLine_asap(int y) {
        getViewport().setViewPosition(jpc_editor.getPointToLine(y));
    }
    public void setFirstVisibleLine_asap(int line) {
        getViewport().setViewPosition(jpc_editor.getPointToLine(line));
    }
    public void scrollPlusLines_asap(int plus) {
        getViewport().setViewPosition(jpc_editor.getPointToLine(jpc_editor.input_receiver.cursor.getY() + plus));
    }
}
