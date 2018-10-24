package jokrey.utilities.swing.text_editor.user_input;

import jokrey.utilities.swing.text_editor.text_storage.ContentEditor;
import jokrey.utilities.swing.text_editor.text_storage.Line;
import jokrey.utilities.swing.text_editor.text_storage.LinePart;
import jokrey.utilities.swing.text_editor.text_storage.LinePartAppearance;
import jokrey.utilities.swing.text_editor.user_input.cursor.TextInterval;
import jokrey.utilities.swing.text_editor.user_input.step_manager.Step;
import jokrey.utilities.swing.text_editor.user_input.step_manager.StepManager;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * THE logical user input handling class.
 * Anything the user does should be tunneled through this class.
 * It will make the action undoable and redo-able and provide additional features as they become available.
 */
public class UserInputHandler {
    private final StepManager step_manager = new StepManager();
    public final UserCursor cursor;

    public UserInputHandler(ContentEditor content) {
        cursor = new UserCursor(content);
    }

    private boolean editable = true;
    public void setEditable(boolean editable) {
        this.editable=editable;
    }
    public boolean isEditable() {return editable;}


    /**
     *
     * @param direction -1 for "backspace", 0 for nothing/selectionRemoval, 1 for "delete"
     * @return number of removed chars
     */
    public int _user_remove(int direction) {
        if(!isEditable()) return 0;

        LinePart[] removed;
        if(direction < 0)       removed=cursor.backspace();
        else if(direction > 0)  removed=cursor.delete();
        else                    removed=cursor.removeSelectedInterval();

        step_manager.userPerformedOperation_remove(removed, cursor);

        cursor.validateCursorVisibility();

        int removedCharCount = 0;
        for(LinePart part:removed) removedCharCount+=part.length();
        return removedCharCount;
    }
    public void _user_insert(String toInsert) {
        if(!toInsert.contains("\n") && toInsert.replaceAll("[^\\P{Cc}\t]", "").isEmpty())return;
        _user_insert(new LinePart(toInsert, cursor.getRawInsertLayout()));
    }
    public void _user_insert(LinePart... insertData) {
        if(!isEditable())return;
        if(insertData.length==0)return;
        if(!cursor.selection.isClear()) {
            LinePart[] removed = cursor.removeSelectedInterval();

            step_manager.userPerformedOperation_replace(removed, cursor.getDistanceFrom00(), insertData);

            for (LinePart lp : insertData)
                cursor.insert(lp.txt, lp.layout);
        } else {
            step_manager.userPerformedOperation_insert(cursor, insertData);

            for (LinePart lp : insertData)
                cursor.insert(lp.txt, lp.layout);
        }
        cursor.validateCursorVisibility();
    }

    public void _user_copy() {
        if(!cursor.selection.getIntervalText().isEmpty()) {
            final String intervalText = cursor.selection.getIntervalText();
            final LinePart[] intervalData = cursor.selection.getIntervalSequences();

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
                @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return true;
                }
                @Override public DataFlavor[] getTransferDataFlavors() {
                    try {
                        return new DataFlavor[] {new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
                                ";class=\"" + LinePart[].class.getName() + "\""), DataFlavor.stringFlavor};
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return new DataFlavor[0];
                    }
                }
                @Override public Object getTransferData(DataFlavor df) {
                    if(df.equals(DataFlavor.stringFlavor)) {
                        return intervalText;//copy to external
                    } else {
                        return intervalData;//copy to internal
                    }
                }
            }, null);
        }
    }
    public void _user_cut() {
        _user_copy();
        _user_remove(0);
    }
    public void _user_paste() {
        if(!isEditable())return;
        try {
            if(Toolkit.getDefaultToolkit().getSystemClipboard().getAvailableDataFlavors().length>0) {
                String pasteData = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor).toString();
                _user_insert(pasteData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void _user_undo() {
        step_manager.undo(cursor);
        cursor.validateCursorVisibility();
    }
    public void _user_redo() {
        step_manager.redo(cursor);
        cursor.validateCursorVisibility();
    }
    public void reset_step_manager() {
        step_manager.reset();
    }



    //additional functionality
    public void _user_toggle_capitalize_current_word() {
        Line l = cursor.getContentEditor().getLine(cursor.getY());
        String l_content = l.toString();
        int word_begin_index = l_content.lastIndexOf(" ", cursor.getX()-1)+1;
        if(word_begin_index < l.length()) {
            LinePart word_leading_char = l.getSingleCharAt_AsLinePart(word_begin_index);
            String orig_text_content = word_leading_char.txt;
            String as_upper_case = word_leading_char.txt.toUpperCase();
            String as_lower_case = word_leading_char.txt.toLowerCase();
            String case_changed = which_is_different_to_arg0(orig_text_content, as_upper_case, as_lower_case);
            if (case_changed!=null) {
                LinePart case_changed_word_leading_char = word_leading_char.copy_change(case_changed);
                Line line_with_removed_word_leading_char = l.removeCharAt(word_begin_index);
                Line finished_line = line_with_removed_word_leading_char.insert(word_begin_index, case_changed_word_leading_char);
                cursor.getContentEditor().setLine(cursor.getY(), finished_line);

                step_manager.userPerformedOperation_replace(new LinePart[]{word_leading_char}, cursor.getContentEditor().getDistanceFrom00(word_begin_index, cursor.getY()), new LinePart[]{case_changed_word_leading_char});
            }
        }
    }

    public void _user_change_insert_layout(LinePartAppearance new_layout, boolean replace_selected_interval_with_new_layout) {
        cursor.setInsertLayout(new_layout);
        if (!cursor.getSelection().isClear() && replace_selected_interval_with_new_layout) {
            int[] previous_selection_1 = cursor.getSelection().get1XY();
            int[] previous_selection_2 = cursor.getSelection().get2XY();

            LinePart[] selected_interval = cursor.getSelection().getIntervalSequences();
            String interval_text = cursor.getSelection().getIntervalText();
            cursor.getSelection().removeIntervalText(cursor);
            LinePart replacement_part = new LinePart(interval_text, new_layout);
            step_manager.userPerformedOperation_replace(selected_interval, cursor.getDistanceFrom00(), new LinePart[]{replacement_part});
            cursor.insert(replacement_part.txt, replacement_part.layout);
            cursor.getSelection().setFromXYs(previous_selection_1, previous_selection_2);
        }
    }





    public boolean _user_select_next_occurrence(boolean ignoreLayout, LinePart[] find) {
        return _user_select_next_occurrence(ignoreLayout, find, cursor.getDistanceFrom00()) || _user_select_next_occurrence(ignoreLayout, find, 0);
    }
    public boolean _user_replace_current_and_select_next_occurrence(boolean ignoreLayout, LinePart[] find, LinePart[] replace) {
        if(!cursor.selection.isClear() &&
                //check that the correct text is selected:
                (ignoreLayout && cursor.getSelection().getIntervalText().equals(LinePart.toString(find))) ||
                Arrays.equals(cursor.getSelection().getIntervalSequences(), find)) {
            if(replace.length==0 || LinePart.toString(replace).length()==0) {
                _user_remove(0);
            } else {
                _user_insert(replace);
            }
        }
        return _user_select_next_occurrence(ignoreLayout, find);
    }
    public int _user_replace_all_occurrences(boolean ignoreLayout, LinePart[] find, LinePart[] replace) {
        LinkedList<Step> steps = new LinkedList<>();
        int counter = 0;
        boolean replace_with_nothing = LinePart.toString(replace).isEmpty();
        cursor.clearSelection();
        cursor.setFromDistance(0);
        int last_cursor_pos = 0;
        while(_user_select_next_occurrence(ignoreLayout, find) && last_cursor_pos < cursor.getDistanceFrom00()) {
            LinePart[] removed = cursor.removeSelectedInterval();

            steps.add(StepManager.getStepDeletion(cursor.getDistanceFrom00(), removed));
            if(!replace_with_nothing)
                steps.add(StepManager.getStepInsert(cursor.getDistanceFrom00(), replace));

            for (LinePart lp : replace)
                cursor.insert(lp.txt, lp.layout);

            counter++;
            last_cursor_pos = cursor.getDistanceFrom00();
        }
        cursor.clearSelection();
        cursor.setFromDistance(0);
        step_manager.userPerformedOperation_custom(steps.toArray(new Step[0]));
        return counter;
    }

    private boolean _user_select_next_occurrence(boolean ignoreLayout, LinePart[] find, int from) {
        String find_as_str = LinePart.toString(find);
        if(find_as_str.isEmpty())return false;
        if(ignoreLayout) {
            int find_index = cursor.getContentEditor().getText().indexOf(find_as_str, from);
            if(find_index==-1)
                return false;
            else {
                cursor.selection.setFromDistance(find_index, find_index + find_as_str.length());
                cursor.setXY(cursor.selection.get2XY());
                cursor.validateCursorVisibility();
                if(cursor.getDistanceFrom00()==from && from!=0)
                    cursor.setFromDistance(0);
            }
            return true;
        } else {
            TextInterval virtual_interval = new TextInterval(cursor.getContentEditor());
            int find_index = from-1;
            while((find_index = cursor.getContentEditor().getText().indexOf(find_as_str, find_index+1)) != -1) {
                virtual_interval.setFromDistance(find_index, find_index+find_as_str.length());
                if(Arrays.equals(virtual_interval.getIntervalSequences(), find)) {
                    cursor.selection.setFromDistance(find_index, find_index + find_as_str.length());
                    cursor.setXY(cursor.selection.get2XY());
                    cursor.validateCursorVisibility();
                    return true;
                }
            }
            return false;
        }
    }



    private final ArrayList<Action> context_actions = new ArrayList<>();

    public void clearContextActions(JComponent display) {
        context_actions.clear();
        Shortcut_Functionality.clearFunctionality(display);
    }
    public void addContextAction(JComponent display, Action... context_actions) {
        this.context_actions.addAll(Arrays.asList(context_actions));
        Shortcut_Functionality.addFrom(display, context_actions);
    }
    public Iterable<Action> getContextActions() {
        return context_actions;
    }



    private String which_is_different_to_arg0(String arg0, String... others) {
        for(String other:others)
            if(!other.equals(arg0))
                return other;
        return null;
    }
}
