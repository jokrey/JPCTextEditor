package jokrey.utilities.swing.text_editor.ui;

import jokrey.utilities.swing.text_editor.JPC_Connector;
import jokrey.utilities.swing.text_editor.text_storage.ContentEditor;
import jokrey.utilities.swing.text_editor.text_storage.LinePrefix;
import jokrey.utilities.swing.text_editor.text_storage.StandardContentEditor;
import jokrey.utilities.swing.text_editor.ui.core.AbstractJPCTextEditor;
import jokrey.utilities.swing.text_editor.user_input.UserInputHandler;

import java.awt.*;

public class JPCTextField extends AbstractJPCTextEditor {
    public JPCTextField() {
    	setAllowTabs(false);
	}

    @Override public ContentEditor createContentEditor(JPC_Connector c) {
        return new StandardContentEditor(c) {
            @Override public int getMaxLineCount() {
                return 1;
            }
        };
    }

    public String getInput(){return content.getLineText(0);}
	public void setInput(String string) {
		if(content.allowInsertion(string)) {
			content.setLineText(0, string, input_receiver.cursor.getValidInsertLayout());
			input_receiver.cursor.setFromDistance(Integer.MAX_VALUE);
		}
	}

	@Override public boolean enterPressed() {
		return false;
	}

	public void clearInput() {
		content.setLineText(0, "", input_receiver.cursor.getValidInsertLayout());
	}

	@Override public int getTextSpacingLeft() {return 0;}
	@Override public LinePrefix getLinePrefix(int lineNumber, String lineText){return new LinePrefix("", Color.black, Color.black);}

	@Override protected UserInputHandler createUserInputHandler(ContentEditor content) {
		return new UserInputHandler(content);
	}
}
