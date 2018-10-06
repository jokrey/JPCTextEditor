package jokrey.utililities.swing.text_editor.ui;

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
//                jpc_editor.recalculateSize();//needed?  -> does not seem like it
                jpc_editor.recalculateDisplayLines();
            }
            @Override public void componentShown(ComponentEvent e) {
//                jpc_editor.recalculateSize();//needed?  -> does not seem like it
                jpc_editor.recalculateDisplayLines();
            }
        });

        jpc_editor.hasScrollPane=true;
    }


    public void scrollToLine_asap(int y) {
//        if(! jpc_editor.asap_queue.callAsap(() -> {
            TextDisplayCursor virtualCursor = new TextDisplayCursor(jpc_editor.content, 0, y);
            Rectangle r = virtualCursor.getShape(jpc_editor.getTextSpacingLeft(), jpc_editor.getTextSpacingTop());
//            if (!getVisibleRect().contains(r))
//                scrollRectToVisible(r);
            getViewport().setViewPosition(r.getLocation());
//        })) { repaint(); }
    }
    public void setFirstVisibleLine_asap(int line) {
//        if(! jpc_editor.asap_queue.callAsap(() -> {
            TextDisplayCursor virtualCursor = new TextDisplayCursor(jpc_editor.content, 0, line);
            Rectangle r = virtualCursor.getShape(jpc_editor.getTextSpacingLeft(), jpc_editor.getTextSpacingTop());
//            scrollRectToVisible(r);
            System.out.println("line: "+line);
            System.out.println("getViewport().getViewPosition bef: "+getViewport().getViewPosition());
            System.out.println("r: "+r);
            getViewport().setViewPosition(r.getLocation());
            System.out.println("getViewport().getViewPosition aft: "+getViewport().getViewPosition());
//        })) { repaint(); }
    }
    public void scrollPlusLines_asap(int plus) {
//        if(! jpc_editor.asap_queue.callAsap(() -> {
            TextDisplayCursor temp = new TextDisplayCursor(jpc_editor.content, 0, jpc_editor.user.cursor.getY() + plus);
//            scrollRectToVisible(temp.getShape(getTextSpacingLeft(), getTextSpacingTop()));
            getViewport().setViewPosition(temp.getShape(jpc_editor.getTextSpacingLeft(), jpc_editor.getTextSpacingTop()).getLocation());
//        })) { repaint(); }
    }
}
