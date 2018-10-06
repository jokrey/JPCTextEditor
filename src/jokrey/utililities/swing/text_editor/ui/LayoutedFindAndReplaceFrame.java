package jokrey.utililities.swing.text_editor.ui;

import jokrey.utililities.swing.text_editor.ui.additional.CustomEditorConnector;
import jokrey.utililities.swing.text_editor.ui.additional.LayoutChangingPanel;
import jokrey.utililities.swing.text_editor.text_storage.*;
import jokrey.utililities.swing.text_editor.user_input.UserInputHandler;
import jokrey.utililities.swing.text_editor.user_input.cursor.TextInterval;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/**
 * Adds the functionality of finding and replacing in a text editor.
 * Now with layouting support
 */
public class LayoutedFindAndReplaceFrame {
    private ContentEditor content;
    private UserInputHandler input_handler;

    private LayoutChangingPanel find_editor_headerPanel;
    private JPCLayoutedTextEditor find_editor;
    private JPCLayoutedTextEditor replace_editor;
    private boolean ignore_layout() {
        return !find_editor_headerPanel.isVisible();
    }
	public LayoutedFindAndReplaceFrame(JComponent jc, JPCTextEditor editor, ContentEditor parent_content, UserInputHandler parent_input_handler, LinePart[] origFindWhat) {
        this.content = parent_content;
        this.input_handler = parent_input_handler;

        JFrame frame = new JFrame("Find/Replace");

        find_editor_headerPanel = new LayoutChangingPanel(new CustomEditorConnector() {
            @Override
            public Color getCurFG() {
                return find_editor.getForeground();
            }

            @Override
            public Color getCurBG() {
                return find_editor.getBackground();
            }

            @Override
            public Font getCurFont() {
                return find_editor.getFont();
            }

            @Override
            public void fg_changed(Color fg) {
                if (fg != null) find_editor.setForeground(fg);
            }

            @Override
            public void bg_changed(Color bg) {
                if (bg != null) find_editor.setCursorBackground(bg);
            }

            @Override
            public void font_changed(Font font) {
                if (font != null) find_editor.setFont(font);
            }
        });
        find_editor_headerPanel.setVisible(false);
        JPanel toggle_layout_panel_find = new JPanel(new BorderLayout());
        JButton toggle_find_layout_panel_button = new JButton("use layout");
        toggle_find_layout_panel_button.addActionListener(e -> {
            if (find_editor_headerPanel.isVisible()) {
                toggle_find_layout_panel_button.setText("use layout");
                find_editor_headerPanel.setVisible(false);
                toggle_layout_panel_find.add(find_editor_headerPanel, BorderLayout.EAST);
                toggle_layout_panel_find.add(toggle_find_layout_panel_button, BorderLayout.CENTER);
            } else {
                toggle_find_layout_panel_button.setText("ignore");
                find_editor_headerPanel.setVisible(true);

                toggle_layout_panel_find.add(find_editor_headerPanel, BorderLayout.CENTER);
                toggle_layout_panel_find.add(toggle_find_layout_panel_button, BorderLayout.WEST);
            }
        });
        toggle_layout_panel_find.add(find_editor_headerPanel, BorderLayout.EAST);
        toggle_layout_panel_find.add(toggle_find_layout_panel_button, BorderLayout.CENTER);
        find_editor = new JPCLayoutedTextEditor() {
            @Override public void start_find_replace_frame() {

            }
        };
        find_editor.addContentListener(new ContentListener() {
            @Override public void userCursorLayoutChanged() {
                if (find_editor == null) return;
                LinePartLayout layout = find_editor.getCurrentLayout();
                find_editor_headerPanel.updateDisplayValues(layout.fg, layout.bg, layout.font);
            }
        });
        find_editor.setHintText("Type what you hope to find");
        JPanel find_panel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, frame.getContentPane().getHeight() / 2);
            }
        };
        find_panel.add(toggle_layout_panel_find, BorderLayout.NORTH);
        find_panel.add(new JPC_Scroller(find_editor), BorderLayout.CENTER);


        LayoutChangingPanel replace_editor_headerPanel = new LayoutChangingPanel(new CustomEditorConnector() {
            @Override
            public Color getCurFG() {
                return replace_editor.getForeground();
            }

            @Override
            public Color getCurBG() {
                return replace_editor.getBackground();
            }

            @Override
            public Font getCurFont() {
                return replace_editor.getFont();
            }

            @Override
            public void fg_changed(Color fg) {
                if (fg != null) replace_editor.setForeground(fg);
            }

            @Override
            public void bg_changed(Color bg) {
                if (bg != null) replace_editor.setCursorBackground(bg);
            }

            @Override
            public void font_changed(Font font) {
                if (font != null) replace_editor.setFont(font);
            }
        });
        JPanel toggle_layout_panel_replace = new JPanel(new BorderLayout());


        toggle_layout_panel_replace.add(replace_editor_headerPanel, BorderLayout.CENTER);
        replace_editor = new JPCLayoutedTextEditor() {
            @Override
            public void start_find_replace_frame() {
            }
        };
        replace_editor.addContentListener(new ContentListener() {
            @Override public void userCursorLayoutChanged() {
                if (replace_editor == null) return;
                LinePartLayout layout = replace_editor.getCurrentLayout();
                replace_editor_headerPanel.updateDisplayValues(layout.fg, layout.bg, layout.font);
            }
        });
        replace_editor.setHintText("Type what will replace the found");
        replace_editor.setFont(new Font("Arial", Font.BOLD, 13));
        JPanel replace_panel = new JPanel(new BorderLayout()) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, frame.getContentPane().getHeight() / 2);
            }
        };
        replace_panel.add(toggle_layout_panel_replace, BorderLayout.NORTH);
        replace_panel.add(new JPC_Scroller(replace_editor), BorderLayout.CENTER);


        JPanel button_side_panel = new JPanel();
        button_side_panel.setLayout(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.weightx = 1;
        cons.gridx = 0;

        JButton findAB = new JButton("Find");
        findAB.addActionListener(ae -> {
            if (!input_handler._user_select_next_occurrence(ignore_layout(), find_editor.content.getAsLineParts()))
                Toolkit.getDefaultToolkit().beep();
        });
        JButton countAB = new JButton("Count");
        countAB.addActionListener(ae ->
                JOptionPane.showMessageDialog(countAB, count(find_editor.content.getAsLineParts()) + "")
        );
        JButton replaceFindAB = new JButton("Replace Sel->Find");
        replaceFindAB.addActionListener(ae -> {
            if (!input_handler._user_replace_current_and_select_next_occurrence(ignore_layout(), find_editor.content.getAsLineParts(), replace_editor.content.getAsLineParts()))
                Toolkit.getDefaultToolkit().beep();
        });
        JButton replaceAllAB = new JButton("Replace All");
        replaceAllAB.addActionListener(ae -> {
            int number_of_occurrences_replaced =
                    input_handler._user_replace_all_occurrences(ignore_layout(), find_editor.content.getAsLineParts(), replace_editor.content.getAsLineParts());
            if(number_of_occurrences_replaced==0)
                Toolkit.getDefaultToolkit().beep();
            else
                JOptionPane.showMessageDialog(countAB, "Replaced "+ number_of_occurrences_replaced + " occurrences");
        });
        JButton undoAB = new JButton("undo");
        undoAB.addActionListener(ae -> input_handler._user_undo());
        JButton redoAB = new JButton("redo");
        redoAB.addActionListener(ae -> input_handler._user_redo());

        button_side_panel.add(findAB, cons);
        button_side_panel.add(replaceFindAB, cons);
        button_side_panel.add(replaceAllAB, cons);
        button_side_panel.add(countAB, cons);
        button_side_panel.add(undoAB, cons);
        button_side_panel.add(redoAB, cons);


        JPanel centerInputPanel = new JPanel();
        centerInputPanel.setLayout(new BorderLayout());
        centerInputPanel.add(find_panel, BorderLayout.NORTH);
        centerInputPanel.add(replace_panel, BorderLayout.CENTER);

        frame.setLayout(new BorderLayout());
        frame.add(button_side_panel, BorderLayout.EAST);
        frame.add(centerInputPanel, BorderLayout.CENTER);

        frame.setForeground(parent_content.getStandardLayout().fg);
        frame.setBackground(parent_content.getStandardLayout().bg);
		frame.setAlwaysOnTop(true);
        frame.pack();
        frame.setSize(750, frame.getHeight());
        frame.setLocationRelativeTo(jc);//pretty much centering
        find_editor.requestFocus();
        find_editor.user.cursor.insert(origFindWhat);
        find_editor.setFont(editor.getFont());
        find_editor.setBackground(editor.getBackground());
        find_editor.setForeground(editor.getForeground());
        replace_editor.setFont(editor.getFont());
        replace_editor.setBackground(editor.getBackground());
        replace_editor.setForeground(editor.getForeground());
        frame.setVisible(true);
	}



    public int count(LinePart[] find) {
        if(ignore_layout()) {
            String find_as_str = LinePart.toString(find);
            if (find_as_str.isEmpty()) return 0; // to avoid / 0
            return (content.getText().length() - content.getText().replaceAll(find_as_str, "").length()) / find_as_str.length();
        } else {
            int counter = 0;
            TextInterval virtual_interval = new TextInterval(content);
            String find_as_str = LinePart.toString(find);
            int find_index = -1;
            while((find_index = content.getText().indexOf(find_as_str, find_index+1)) != -1) {
                virtual_interval.setFromDistance(find_index, find_index+find_as_str.length());
                if(Arrays.equals(virtual_interval.getIntervalSequences(), find)) {
                    counter++;
                    find_index = find_index+find_as_str.length();
                }
            }
            return counter;
        }
    }
}