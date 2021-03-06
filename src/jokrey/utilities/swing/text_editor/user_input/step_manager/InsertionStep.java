package jokrey.utilities.swing.text_editor.user_input.step_manager;

import jokrey.utilities.swing.text_editor.text_storage.DecoratedLinePart;
import jokrey.utilities.swing.text_editor.user_input.UserCursor;

public class InsertionStep extends Step {
	public InsertionStep(DecoratedLinePart altered, int alteredAt_distanceFrom00) {
		super(altered, alteredAt_distanceFrom00);
	}

	@Override public void redo(UserCursor cursor) {
		cursor.setFromDistance(alteredAt_distanceFrom00);
		cursor.insert(altered.txt, altered.layout);
	}
	@Override public void undo(UserCursor cursor) {
		cursor.setSelection(alteredAt_distanceFrom00, alteredAt_distanceFrom00+altered.length());
		cursor.removeSelectedInterval();
	}
}