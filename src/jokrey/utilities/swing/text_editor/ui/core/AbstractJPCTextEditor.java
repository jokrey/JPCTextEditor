package jokrey.utilities.swing.text_editor.ui.core;

import jokrey.utilities.swing.text_editor.JPCTextEditor;
import jokrey.utilities.swing.text_editor.JPC_Connector;
import jokrey.utilities.swing.text_editor.text_storage.*;
import jokrey.utilities.swing.text_editor.ui.FindAndReplaceFrame;
import jokrey.utilities.swing.text_editor.user_input.ContextFunctionalityLibrary;
import jokrey.utilities.swing.text_editor.user_input.RawUserInputHandler;
import jokrey.utilities.swing.text_editor.user_input.UserInputHandler;
import jokrey.utilities.swing.text_editor.user_input.cursor.TextDisplayCursor;
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
public abstract class AbstractJPCTextEditor extends JPanel implements JPC_Connector, JPCTextEditor {
    public final ContentEditor content = createContentEditor(this);
    protected final UserInputHandler input_receiver = createUserInputHandler(content);
    public UserInputHandler getInputHandler() {
        return input_receiver;
    }

    //Outside referenced UI Components....
    public AbstractJPCTextEditor() {
        setLayout(new BorderLayout());
//        setForeground(Color.BLACK);
//        setFont();
//        setBackground(Color.WHITE);

        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));

        setFocusTraversalKeysEnabled(false);


        /*
         * Initiates Listener. For one to recalculate lines if needed, but also a transfer handler to import stuff upon drag..
         */
        RawUserInputHandler raw_input_receiver = new RawUserInputHandler(this, this, input_receiver, content);//on its own initiates its jpc_connector..
        addContextAction(ContextFunctionalityLibrary.getBasicFunctionality(this, this, input_receiver, raw_input_receiver));
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


    public abstract ContentEditor createContentEditor(JPC_Connector connector);
    protected abstract UserInputHandler createUserInputHandler(ContentEditor content);

    public LinePrefix getLinePrefix(int lineNumber, @SuppressWarnings("unused") String lineText) {
        return new LinePrefix(lineNumber+"", null, content.getStandardLayout().fg);
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
        input_receiver.clearContextActions(this);
    }
    public void addContextAction(Action... actions) {
        input_receiver.addContextAction(this, actions);
    }








    // ContentEditor Wrapping
    public void setText(String text) {
        content.setText(text);
        input_receiver.reset_step_manager();
        input_receiver.cursor.selection.clear();
        input_receiver.cursor.resetToClosestValid();
    }
    public void setText(DecoratedLinePart[] text) {
        content.setText(text);
        input_receiver.reset_step_manager();
        input_receiver.cursor.selection.clear();
        input_receiver.cursor.resetToClosestValid();
    }
    public String getText() {
        return content.getText();
    }
    @Override public DecoratedLinePart[] getTextAsLineParts() {
        return content.getTextAsLineParts();
    }
    public String getText_with_encoded_layout() {
        return content.getText_with_encoded_layout();
    }
    public void setText_with_encoded_layout(String text) {
        content.setText_with_encoded_layout(text);
        input_receiver.reset_step_manager();
        input_receiver.cursor.selection.clear();
        input_receiver.cursor.resetToClosestValid();
    }

    public final void setHint(DecoratedLinePart hint) {
        content.setHint(hint);
    }
    public final void setHintText(String hint) {
        content.setHint(new DecoratedLinePart(hint, null));
    }
    public final DecoratedLinePart getHint() {return content.getHint();}

    //User Input Wrapping
    public void setEditable(boolean editable) {
        input_receiver.setEditable(editable);
    }
    public boolean isEditable(){
        return input_receiver.isEditable();
    }
    public void clearSelection() {
        input_receiver.cursor.clearSelection();
    }

    public void setAllowTabs(boolean tabs) {
        setFocusTraversalKeysEnabled(!tabs);
    }



//---------------------- PAINTING PAINTING PAINTING PAINTING PAINTING PAINTING:: -------------------------
    //the following is done ENTIRELY too safe some recalculateDisplayLines calls that would not be visible anyways...
    //if that proves to be ANY issue just replace with the commented out methods.
    private final ASAP_Queue runBeforePaintQueue = new ASAP_Queue();
    @Override public void recalculateDisplayLines() {
        runBeforePaintQueue.callOncePostponed(-1, content::recalculateDisplayLines);
        repaint();
//        content.recalculateDisplayLines();
    }
    @Override public void recalculateDisplayLine(int line) {
        runBeforePaintQueue.callOncePostponed(line, () -> content.recalculateDisplayLine(line));
        repaint();
//        content.recalculateDisplayLines(firstAffectedLine, lastAffectedLine);
    }

    protected boolean hasScrollPane = false;

    private Dimension lastPaintedSpace = new Dimension();
    @Override protected void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D)gg;

        runBeforePaintQueue.callAndRemoveAll_orIgnore();

        drawAndRecalculateSize(g);
    }
    @Override public void recalculateSize() {
        drawAndRecalculateSize(null);
    }

    private void drawAndRecalculateSize(Graphics2D g) {
        Shape orig = g!=null? g.getClip() : null;

        int yDrawPos_l = getTextSpacingTop();
        int highestXDrawPos = 0;
        if(content.getLineCount() == 1 && content.getLine(0).isEmpty()) {//DRAWING THE HINT
            content.getDisplayLine(0)[0].updatePixelKnowledge(this, content.getStandardLayout());
            DecoratedLinePart hint = content.getHint();
            hint.updateFontMetrics(this, content.getStandardLayout());
            int cur_line_height = hint.getPixelHeight();
            yDrawPos_l += cur_line_height;

            if(g!=null) {
                Rectangle area = new Rectangle(getTextSpacingLeft(), yDrawPos_l-cur_line_height, hint.getPixelWidth(), cur_line_height);
                g.setClip(area);
                hint.draw(this, isOpaque(), g, content.getStandardLayout(), area);
                g.setClip(orig);
            }

            highestXDrawPos = hint.getPixelWidth();
            LinePrefix linePrefix = getLinePrefix(1, hint.txt);
            if(g!=null) {
                g.setFont(LinePartAppearance.valid(hint.layout, content.getStandardLayout()).font);
                Rectangle area = new Rectangle(0, yDrawPos_l-cur_line_height, getLineCountBoxWidth(), cur_line_height);
                g.setClip(area);
                linePrefix.draw(g, yDrawPos_l, g.getFontMetrics().getHeight(), input_receiver.cursor.getValidInsertLayout().fg, getLineCountBoxWidth(), true);
            }
        } else {
            //Very important that its +-getLastFontMetrics().getHeight - Otherwise a line maybe painted to late, or just painted in half - DO NOT KNOW WHY REALLY
            int visible_pixel_start = getVisibleRect().y;
            int visible_pixel_end = getVisibleRect().y+getVisibleRect().height;

            LinePartAppearance.Instantiated linePrefixLayout = null;
            for (int wl_i = 0; wl_i<content.getLineCount();wl_i++) {
                Line curRawLine = content.getLine(wl_i);
                curRawLine.updatePixelKnowledge(this, content.getStandardLayout());
                Line[] curDisplayLine = content.getDisplayLine(wl_i);

                int curXdrawPos;
                for(int i=0;i<curDisplayLine.length;i++) {
                    Line disLine = curDisplayLine[i];
                    int cur_line_height = disLine.getPixelHeight();
                    curXdrawPos = getTextSpacingLeft();
                    yDrawPos_l+=cur_line_height;

                    if(i==0) {
                        if(linePrefixLayout == null)
                            linePrefixLayout = LinePartAppearance.valid(disLine.getPart(0).layout, content.getStandardLayout());
                        if(yDrawPos_l >= visible_pixel_start && yDrawPos_l-cur_line_height <= visible_pixel_end) {
                            LinePrefix linePrefix = getLinePrefix(wl_i+1, curRawLine.toString());

                            if(g!=null) {
                                Rectangle area = new Rectangle(0, yDrawPos_l-cur_line_height, getLineCountBoxWidth(), cur_line_height);
                                g.setClip(area);
                                g.setFont(linePrefixLayout.font);
                                linePrefix.draw(g, yDrawPos_l, cur_line_height, linePrefixLayout.fg, getLineCountBoxWidth(), input_receiver.cursor.getY()==wl_i);
                            }
                        }
                    }

                    for (int part_i=0;part_i<disLine.partCount();part_i++) {
                        DecoratedLinePart lp = disLine.getPart(part_i);
                        int lpw = lp.getPixelWidth();
                        if(yDrawPos_l >= visible_pixel_start && yDrawPos_l-cur_line_height <= visible_pixel_end) {
                            if(g!=null) {
                                Rectangle area = new Rectangle(curXdrawPos, yDrawPos_l-cur_line_height, lpw, cur_line_height);
                                g.setClip(area);
                                lp.draw(this, isOpaque(), g, content.getStandardLayout(), area);
                            }
                        }
                        curXdrawPos+=lpw;
                    }

                    if(highestXDrawPos<curXdrawPos)
                        highestXDrawPos=curXdrawPos;
                }
            }
        }
        if(g!=null) {
            g.setClip(orig);
            g.setColor(getForeground());
            if (getLineCountBoxWidth() > 0)
                g.drawLine(getLineCountBoxWidth(), 0, getLineCountBoxWidth(), getHeight());
            input_receiver.cursor.draw(g, getTextSpacingLeft(), getWidth(), getTextSpacingTop(), true, hasFocus() && input_receiver.isEditable());
        }

        handlePaintedSpaceRecalculation(new Dimension (getTextSpacingLeft()+highestXDrawPos + TextDisplayCursor.PIXEL_WIDTH, yDrawPos_l));
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
        Rectangle r = input_receiver.cursor.getShape(getTextSpacingLeft(), getTextSpacingTop());
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
        return content.getTextInLines(visibleLines);
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
        TextDisplayCursor cp = new TextDisplayCursor(content);
        cp.setPositionTo(p, getTextSpacingLeft(), getTextSpacingTop());
        return cp.getY();
    }
    public Point getPointToLine(int line) {
        TextDisplayCursor virtualCursor = new TextDisplayCursor(content, 0, line);
        Rectangle r = virtualCursor.getShape(getTextSpacingLeft(), getTextSpacingTop());
        return r.getLocation();
    }
    public void start_find_replace_frame() {
        new FindAndReplaceFrame(this, this, content, input_receiver, input_receiver.cursor.getSelection().getIntervalText());
    }
    public boolean enterPressed() {return true;}

    @Override public void addContentListener(ContentListener contentListener) {
        content.addContentListener(contentListener);
    }

    @Override public void setCursorPosition(int x, int y) {
        input_receiver.cursor.setXY(x, y);
    }
    //Layout relays.

    @Override public Color getForeground() {
        if(content==null) return super.getForeground();
        return content.getStandardLayout().fg;//not useless, if setForeground never called super.getFG maybe null.
    }
    @Override public Color getBackground() {
        return super.getBackground();
//        if(content==null) return super.getBackground();
//        return content.getStandardLayout().bg;//not useless.
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
    }
    @Override public void setFont(Font font) {
        if(content==null||font==null)return;
        super.setFont(font);
        content.setStandardLayout(content.getStandardLayout().copy_ChangeFont(font));
    }

    public LinePartAppearance getCurrentInsertLayout() {
        return input_receiver.cursor.getValidInsertLayout();
    }
    public void setStandardLayout(LinePartAppearance.Instantiated standard) {
        content.setStandardLayout(standard);
    }
    public void setStandardLayout(LinePartAppearance standard) {
        content.setStandardLayout(LinePartAppearance.valid(standard, content.getStandardLayout()));
    }
    public LinePartAppearance getStandardLayout() {
        return content.getStandardLayout();
    }
}
