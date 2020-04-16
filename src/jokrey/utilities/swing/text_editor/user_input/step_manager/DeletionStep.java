package jokrey.utilities.swing.text_editor.user_input.step_manager;

import jokrey.utilities.swing.text_editor.text_storage.DecoratedLinePart;
import jokrey.utilities.swing.text_editor.user_input.UserCursor;

public class DeletionStep extends Step {
	public DeletionStep(DecoratedLinePart altered, int alteredAt_distanceFrom0) {
		super(altered, alteredAt_distanceFrom0);
	}

	@Override public void redo(UserCursor cursor) {
		cursor.setSelection(alteredAt_distanceFrom00, alteredAt_distanceFrom00+altered.length());
		cursor.removeSelectedInterval();
	}
	@Override public void undo(UserCursor cursor) {
		cursor.setFromDistance(alteredAt_distanceFrom00);
		cursor.insert(altered.txt, altered.layout);
	}
}