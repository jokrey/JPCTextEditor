package jokrey.utililities.swing.text_editor.ui;

import jokrey.utililities.swing.text_editor.ui.core.Abstract_JPCTextEditor;
import jokrey.utililities.swing.text_editor.text_storage.ContentEditor;
import jokrey.utililities.swing.text_editor.text_storage.LinePrefix;
import jokrey.utililities.swing.text_editor.text_storage.StandardContentEditor;
import jokrey.utililities.swing.text_editor.user_input.UserInputHandler;

import java.awt.Color;

public class JPCTextField extends Abstract_JPCTextEditor {
    public JPCTextField() {
    	setAllowTabs(false);
	}

    @Override public ContentEditor createContentEditor() {
        return new StandardContentEditor() {
            @Override public int getMaxLineCount() {
                return 1;
            }
        };
    }

    public String getInput(){return content.getLineText(0);}
	public void setInput(String string) {
		if(content.allowInsertion(string)) {
			content.setLineText(0, string, user.cursor.getValidInsertLayout());
			user.cursor.setFromDistance(Integer.MAX_VALUE);
		}
	}

	@Override public boolean enterPressed() {
		return false;
	}

	public void clearInput() {
		content.setLineText(0, "", user.cursor.getValidInsertLayout());
	}

	@Override public int getTextSpacingLeft() {return 0;}
	@Override public LinePrefix getLinePrefix(int lineNumber, String lineText){return new LinePrefix("", Color.black, Color.black);}

	@Override protected UserInputHandler createUserInputHandler(ContentEditor content) {
		return new UserInputHandler(content);
	}
}
