package jokrey.utililities.swing.text_editor.ui.core;

import jokrey.utililities.swing.text_editor.ui.FindAndReplaceFrame;
import jokrey.utililities.swing.text_editor.ui.JPCTextEditor;
import jokrey.utililities.swing.text_editor.text_storage.*;
import jokrey.utililities.swing.text_editor.user_input.ContextFunctionalityLibrary;
import jokrey.utililities.swing.text_editor.user_input.RawUserInputHandler;
import jokrey.utililities.swing.text_editor.user_input.UserInputHandler;
import jokrey.utililities.swing.text_editor.user_input.cursor.TextDisplayCursor;
import jokrey.utilities.asap_queue.ASAP_Queue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Abstract JPCEditor.
 * Already has all the functionality, but does only provide minimal access to content and user.
 * Subclasses can extend that as they choose(the field is protected and therefore allows unlimited access)
 *
 * Functionality(most is just optional):
 *      - Wrapping(requires a special ContentEditor(that is provided in the text_storage package))
 *      - Tabs.
 *             Do it like a custom jump i+x, where x is chosen so that (i+x)%4 == 0.
 *             And where i is the index of the tab
 *      - Scrolling
 *      - keyboard shortcuts(extendable)
 *      - right click options(extendable)
 *      - nested layouts on any part of the text
 *        either
 *          global layout(fg, bg, font)
 *        or
 *          custom, anytime, anywhere, nested layouts(as portrayed in example.LayoutedTextEditor)
 *      - setting editablity of the text. (It can also be read only)
 *      - setting the selectable state. Would for example make it impossible to copy text out.
 *      - "hint"s. If the text editor is empty, you can display a custom text in a custom layout that can be an indication as to what the user should do now.
 *      - line prefixes. Will normally be the line number, but can be be changed by a user.
 *      - simple syntax highlighting (more complex syntax highlighting requires a little work by a potential user, but is possible by overriding recalculateDisplayLines)
 *
 *
 *
 * TODO: MINIMIZE REPAINT AND RECALCULATE_DISPLAY_LINES CALLS (even more).
 * TODO Make work with files that don't fit RAM.
 *      i.e. allow working directly from disk memory and not ram. Probably "just" means writing a new ContentEditor that works on a RandomAccessFile...
 * TODO: Add even more keyboard shortcuts to allow vim like workflow
 */
public abstract class Abstract_JPCTextEditor extends JPanel implements JPC_Connector, JPCTextEditor {
    protected final ContentEditor content = createContentEditor();
    protected final UserInputHandler user = createUserInputHandler(content);
    public UserInputHandler getInputHandler() {
        return user;
    }

    //Outside referenced UI Components....
//    public final JScrollPane scrollPane;
//    public final JPanel textPaintDisplay;
    public Abstract_JPCTextEditor() {
        content.jpc_connector = this;

        setLayout(new BorderLayout());
        setForeground(Color.BLACK);
        setBackground(Color.WHITE);

        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        setFocusTraversalKeysEnabled(false);


        /*
         * Initiates Listener. For one to recalculate lines if needed, but also a transfer handler to import stuff upon drag..
         */
        addContextAction(ContextFunctionalityLibrary.getBasicFunctionality(this, this, user));
        new RawUserInputHandler(this, this, user, content);//on its own initiates its jpc_connector..
        UIManager.getDefaults().put("ScrollPane.ancestorInputMap",
                new UIDefaults.LazyInputMap(new Object[] {}));  //removes arrow key scrolling
        addComponentListener(new  ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) {
                recalculateDisplayLines();
            }
            @Override public void componentShown(ComponentEvent e) {
                recalculateDisplayLines();
            }
        });
    }


    public abstract ContentEditor createContentEditor();
    protected abstract UserInputHandler createUserInputHandler(ContentEditor content);

    public LinePrefix getLinePrefix(int lineNumber, @SuppressWarnings("unused") String lineText) {
        return new LinePrefix(lineNumber+"", content.getStandardLayout().bg, content.getStandardLayout().fg);
    }
    public int getTextSpacingTop() {
        return 2;
    }
    public int getTextSpacingLeft() {
        return 26;
    }
    public int getLineCountBoxWidth() {
        return (int) (getTextSpacingLeft()*0.9);
    }

    public void clearContextActions() {
        user.clearContextActions(this);
    }
    public void addContextAction(Action... actions) {
        user.addContextAction(this, actions);
    }








    // ContentEditor Wrapping
    public void setText(String text) {
        content.setText(text);
        user.reset_step_manager();
        user.cursor.selection.clear();
    }
    public String getText() {
        return content.getText();
    }
    public String getText_with_encoded_layout() {
        return content.getText_with_encoded_layout();
    }
    public void setText_with_encoded_layout(String text) {
        content.setText_with_encoded_layout(text);
        user.reset_step_manager();
        user.cursor.selection.clear();
    }

    public final void setHint(LinePart hint) {
        content.setHint(hint);
    }
    public final void setHintText(String hint) {
        content.setHint(new LinePart(hint, null));
    }
    public final LinePart getHint() {return content.getHint();}

    //User Input Wrapping
    public void setEditable(boolean editable) {
        user.setEditable(editable);
    }
    public boolean isEditable(){
        return user.isEditable();
    }
    public void clearSelection() {
        user.cursor.clearSelection();
    }

    public void setAllowTabs(boolean tabs) {
        setFocusTraversalKeysEnabled(!tabs);
    }



//---------------------- PAINTING PAINTING PAINTING PAINTING PAINTING PAINTING:: -------------------------
    //the following is done ENTIRELY too safe some recalculateDisplayLines calls that would not be visible anyways...
    //if that proves to be ANY issue just replace with the commented out methods.
    private ASAP_Queue runBeforePaintQueue = new ASAP_Queue();
    @Override public void recalculateDisplayLines() {
        runBeforePaintQueue.callOncePostponed(-1, content::recalculateDisplayLines);
//        content.recalculateDisplayLines();
    }
    @Override public void recalculateDisplayLine(int line) {
        runBeforePaintQueue.callOncePostponed(line, () -> content.recalculateDisplayLine(line));
//        content.recalculateDisplayLines(firstAffectedLine, lastAffectedLine);
    }

    protected boolean hasScrollPane = false;

    private Dimension lastPaintedSpace = new Dimension();
    @Override protected void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D)gg;

        runBeforePaintQueue.callAllAndIgnore();//remove if problem.... see above declaration

        drawAndRecalculateSize(g);
    }
    @Override public void recalculateSize() {
        drawAndRecalculateSize(null);
    }

    private void drawAndRecalculateSize(Graphics2D g) {
        int yDrawPos_l = getTextSpacingTop();
        int highestXDrawPos = 0;
        if(content.getLineCount() == 1 && content.getLine(0).isEmpty()) {//DRAWING THE HINT
            content.getDisplayLine(0)[0].updatePixelKnowledge(this, content.getStandardLayout());
            LinePart hint = content.getHint();
            hint.updateFontMetrics(this, content.getStandardLayout());
            int cur_line_height = hint.getPixelHeight();
            yDrawPos_l += cur_line_height;

            if(g!=null) hint.draw(this, isOpaque(), g, content.getStandardLayout(), getTextSpacingLeft(), 0, yDrawPos_l);

            highestXDrawPos = hint.getPixelWidth();
            LinePrefix linePrefix = getLinePrefix(1, hint.txt);
            if(g!=null)g.setFont(LinePartLayout.valid(hint.layout, content.getStandardLayout()).font);
            if(g!=null)linePrefix.draw(g, yDrawPos_l, g.getFontMetrics().getHeight(), user.cursor.getValidInsertLayout().bg, getLineCountBoxWidth(), true);
        } else {
            //Very important that its +-getLastFontMetrics().getHeight - Otherwise a line maybe painted to late, or just painted in half - DO NOT KNOW WHY REALLY
            int visible_pixel_start = getVisibleRect().y;
            int visible_pixel_end = getVisibleRect().y+getVisibleRect().height;
            for (int wl_i = 0; wl_i<content.getLineCount();wl_i++) {
                Line curRawLine = content.getLine(wl_i);
                curRawLine.updatePixelKnowledge(this, content.getStandardLayout());
                Line[] curDisplayLine = content.getDisplayLine(wl_i);

                int curXdrawPos;
                for(int i=0;i<curDisplayLine.length;i++) {
                    Line disLine = curDisplayLine[i];
                    int cur_line_height = disLine.getPixelHeight();
                    curXdrawPos = 0;
                    yDrawPos_l+=cur_line_height;

                    if(i==0) {
                        if(yDrawPos_l >= visible_pixel_start && yDrawPos_l-cur_line_height <= visible_pixel_end) {
                            LinePrefix linePrefix = getLinePrefix(wl_i+1, curRawLine.toString());

                            if(g!=null)g.setFont(LinePartLayout.valid(disLine.getPart(0).layout, content.getStandardLayout()).font);
                            if(g!=null)linePrefix.draw(g, yDrawPos_l, cur_line_height, user.cursor.getValidInsertLayout().bg, getLineCountBoxWidth(), user.cursor.getY()==wl_i);
                        }
                    }

                    for (int part_i=0;part_i<disLine.partCount();part_i++) {
                        LinePart lp = disLine.getPart(part_i);
                        if(yDrawPos_l >= visible_pixel_start && yDrawPos_l-cur_line_height <= visible_pixel_end) {
                            if(g!=null)lp.draw(this, isOpaque(), g, content.getStandardLayout(), getTextSpacingLeft(), curXdrawPos, yDrawPos_l);
                        }
                        curXdrawPos+=lp.getPixelWidth();
                    }

                    if(highestXDrawPos<curXdrawPos)
                        highestXDrawPos=curXdrawPos;
                }
            }
        }
        if(g!=null)g.setColor(getForeground());
        if(g!=null && getLineCountBoxWidth()>0)g.drawLine(getLineCountBoxWidth(), 0, getLineCountBoxWidth(), getHeight());
        if(g!=null)user.cursor.draw(g, getTextSpacingLeft(), getWidth(), getTextSpacingTop(), true, hasFocus()&&user.isEditable());

        handlePaintedSpaceRecalculation(new Dimension (getTextSpacingLeft()+highestXDrawPos+2, yDrawPos_l));
    }

    protected boolean handlePaintedSpaceRecalculation(Dimension newResult) {
        if(!lastPaintedSpace.equals(newResult)) {
            lastPaintedSpace = newResult;
            if (hasScrollPane) {
                revalidate();
            } else {
                repaint();
            }
            return true;
        }
        return false;
    }
    @Override public Dimension getPreferredSize() {
        return lastPaintedSpace;
    }
    @Override public Dimension getMinimumSize() {
        return lastPaintedSpace;
    }


    public void validateCursorVisibility() {
        Rectangle r = user.cursor.getShape(getTextSpacingLeft(), getTextSpacingTop());
        if(r.x == getTextSpacingLeft()) r.x=0;
        if (!getVisibleRect().contains(r)) {
            scrollRectToVisible(r);
        }
    }

    public int getDisplayWidth() {
        return getVisibleRect().width;
    }

    // ------------------------  HELPER METHODS:: ------------------------------
    public String getTextFromVisibleLines() {
        int[] visibleLines = new int[(getLastVisibleLine()-getFirstVisibleLine())+1];
        for(int i=0;i<visibleLines.length;i++)
            visibleLines[i]=getFirstVisibleLine()+i;
        return content.getTextFromLines(visibleLines);
    }
    public int getFirstVisibleLine() {
        return getLineToPoint(new Point(0, -getY()))+1;//don't know why +1, occurred while testing..
    }
    public int getMiddleVisibleLine() {
        return getFirstVisibleLine() + (getLastVisibleLine()-getFirstVisibleLine())/2;
    }
    public int getLastVisibleLine() {
        int lastL = getLineToPoint(new Point(0, -getY()+getVisibleRect().height));
        return lastL==0||lastL>content.getLineCount()?content.getLineCount():lastL;
    }
    public int getLineToPoint(Point p) {
        try {
            TextDisplayCursor cp = new TextDisplayCursor(content);
            cp.setPositionTo(p, getTextSpacingLeft(), getTextSpacingTop());
            return cp.getY();
        } catch(NeverDrawnException ex) {
            return -1;
        }
    }
    public void start_find_replace_frame() {
        new FindAndReplaceFrame(this, this, this, content, user, user.cursor.getSelection().getIntervalText());
    }
    public boolean enterPressed() {return true;}

    @Override public void addContentListener(ContentListener contentListener) {
        content.addContentListener(contentListener);
    }

    @Override public void setCursorPosition(int x, int y) {
        user.cursor.setXY(x, y);
    }
    //Layout relays.

    @Override public Color getForeground() {
        if(content==null) return super.getForeground();
        return content.getStandardLayout().fg;//not useless, if setForeground never called super.getFG maybe null.
    }
    @Override public Color getBackground() {
        if(content==null) return super.getBackground();
        return content.getStandardLayout().bg;//not useless.
    }
    @Override public Font getFont() {
        if(content==null) return super.getFont();
        return content.getStandardLayout().font;//not useless.
    }
    /**
     * Relays the provided fg to the standard layout, and display
     * @param fg if null the value will be ignored
     */
    @Override public void setForeground(Color fg) {
        if(content==null)return;
        super.setForeground(fg);
        content.setStandardLayout(content.getStandardLayout().copy_ChangeFG(fg));
    }
    /**
     * Relays the provided bg to the standard layout, and display(so the background will change color
     * @param bg if null the value will be ignored.
     */
    @Override public void setBackground(Color bg) {
        if(content==null)return;
        super.setBackground(bg);
        content.setStandardLayout(content.getStandardLayout().copy_ChangeBG(bg));
    }
    @Override public void setFont(Font font) {
        if(content==null||font==null)return;
        super.setFont(font);
        content.setStandardLayout(content.getStandardLayout().copy_ChangeFont(font));
    }

//    private void setCursorInsertLayout(LinePartLayout layout) {
//
//    }

    public LinePartLayout getCurrentLayout() {
        return user.cursor.getValidInsertLayout();
    }
}
