package jokrey.utililities.swing.text_editor.user_input;

import javax.swing.*;

/**
 * Allows to add all kinds of keyboard shortcut actions to the text editor.
 * Some may not work with every text editor.
 */
public abstract class Shortcut_Functionality {
	public static void addFrom(JComponent display, Action[] functionality) {
		for(int i=0;i<functionality.length;i++) {
			Object shortcut_keystroke = functionality[i].getValue("shortcut");
			String shortcut_name = functionality[i].getValue(Action.NAME).toString();
			if(shortcut_keystroke==null) continue;

			display.getInputMap().put((KeyStroke) shortcut_keystroke, shortcut_name);
			display.getActionMap().put(shortcut_name, functionality[i]);
		}
	}

	public static void clearFunctionality(JComponent display) {
		display.getInputMap().clear();
		display.getActionMap().clear();
	}
}