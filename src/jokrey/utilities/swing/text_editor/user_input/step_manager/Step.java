package jokrey.utilities.swing.text_editor.user_input.step_manager;

import jokrey.utilities.swing.text_editor.text_storage.LinePart;
import jokrey.utilities.swing.text_editor.user_input.UserCursor;

/**
 * Immutable.
 */
public abstract class Step {
	public final LinePart altered;
    public final int alteredAt_distanceFrom00;
	public Step(LinePart altered, int alteredAt_distanceFrom00) {
		this.altered= altered;
		this.alteredAt_distanceFrom00=alteredAt_distanceFrom00;
	}
	@Override public String toString() {
		return getClass().getSimpleName()+"[at:"+alteredAt_distanceFrom00+"-"+altered.txt+"]";
	}

	public abstract void redo(UserCursor cursor);
	public abstract void undo(UserCursor cursor);
}