package jokrey.utililities.swing.text_editor.ui;

import jokrey.utililities.swing.text_editor.JPCTextEditor;
import jokrey.utililities.swing.text_editor.JPC_Connector;
import jokrey.utililities.swing.text_editor.text_storage.ContentEditor;
import jokrey.utililities.swing.text_editor.ui.core.JPC_Scroller;
import jokrey.utililities.swing.text_editor.ui.util.Util;
import jokrey.utililities.swing.text_editor.user_input.UserInputHandler;

import javax.swing.*;
import java.awt.*;

/**
 * Adds the functionality of finding and replacing simple text in a text editor.
 */
public class FindAndReplaceFrame {
    private ContentEditor content;
    private UserInputHandler input_handler;

    private JPCSimpleTextEditor find_editor;
    private JPCSimpleTextEditor replace_editor;
	public FindAndReplaceFrame(JPC_Connector jpc, JPCTextEditor editor, ContentEditor parent_content, UserInputHandler parent_input_handler, String orig_find) {
	    this.content=parent_content;
	    this.input_handler=parent_input_handler;

		JFrame frame = new JFrame("Find/Replace");

        find_editor = new JPCSimpleTextEditor() {
            @Override public void start_find_replace_frame() { } //lets not go down that rabbit hole
        };
        find_editor.setHintText("Type what you hope to find");
        find_editor.setAllowTabs(false);
        find_editor.setFont(new Font("Arial", Font.BOLD, 13));
        JPanel find_panel = new JPanel(new BorderLayout()) {
            @Override public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, frame.getContentPane().getHeight()/2);
            }
        };
        find_panel.add(new JPC_Scroller(find_editor), BorderLayout.CENTER);


        replace_editor = new JPCSimpleTextEditor() {
            @Override public void start_find_replace_frame() { } //lets not go down that rabbit hole
        };
        replace_editor.setHintText("Type what will replace the found");
        replace_editor.setAllowTabs(false);
        replace_editor.setFont(new Font("Arial", Font.BOLD, 13));
        JPanel replace_panel = new JPanel(new BorderLayout()) {
            @Override public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, frame.getContentPane().getHeight()/2);
            }
        };
        replace_panel.add(new JPC_Scroller(replace_editor), BorderLayout.CENTER);


		JPanel button_side_panel = new JPanel();
        button_side_panel.setLayout(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();
        cons.fill = GridBagConstraints.HORIZONTAL;
        cons.weightx = 1;
        cons.gridx = 0;

        JButton findAB = new JButton("Find");
        findAB.addActionListener(ae -> {
            if (!input_handler._user_select_next_occurrence(true, find_editor.getTextAsLineParts()))
                Toolkit.getDefaultToolkit().beep();
            jpc.repaint();
        });
        JButton countAB = new JButton("Count");
        countAB.addActionListener(ae ->
                JOptionPane.showMessageDialog(countAB, count(find_editor.getText()) + "")
        );
        JButton replaceFindAB = new JButton("Replace+Find");
        replaceFindAB.addActionListener(ae -> {
            if (!input_handler._user_replace_current_and_select_next_occurrence(true, find_editor.getTextAsLineParts(), replace_editor.getTextAsLineParts()))
                Toolkit.getDefaultToolkit().beep();
        });
        JButton replaceAllAB = new JButton("Replace All");
        replaceAllAB.addActionListener(ae -> {
            int number_of_occurrences_replaced =
                    input_handler._user_replace_all_occurrences(true, find_editor.getTextAsLineParts(), replace_editor.getTextAsLineParts());
            if(number_of_occurrences_replaced==0)
                Toolkit.getDefaultToolkit().beep();
            else
                JOptionPane.showMessageDialog(countAB, "Replaced "+ number_of_occurrences_replaced + " occurrences");
        });
        JButton undoAB = new JButton("undo");
        undoAB.addActionListener(ae -> {
            input_handler._user_undo();
        });
        JButton redoAB = new JButton("redo");
        redoAB.addActionListener(ae -> {
            input_handler._user_redo();
        });

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
        frame.setSize(555, frame.getHeight());
        Util.centerOnMouseScreen(frame);
        find_editor.requestFocus();
        find_editor.setText(orig_find);
        find_editor.setFont(editor.getFont());
        find_editor.setBackground(editor.getBackground());
        find_editor.setForeground(editor.getForeground());
        replace_editor.setFont(editor.getFont());
        replace_editor.setBackground(editor.getBackground());
        replace_editor.setForeground(editor.getForeground());
        frame.setVisible(true);
	}

    public int count(String toFind) {
        if(toFind.isEmpty())return 0; // to avoid / 0
        return (content.getText().length() - content.getText().replaceAll(toFind, "").length()) / toFind.length();
    }
}