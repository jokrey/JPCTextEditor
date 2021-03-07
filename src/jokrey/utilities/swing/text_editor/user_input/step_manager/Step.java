package jokrey.utilities.swing.text_editor.user_input.step_manager;

import jokrey.utilities.swing.text_editor.text_storage.DecoratedLinePart;
import jokrey.utilities.swing.text_editor.user_input.UserCursor;

/**
 * Immutable.
 */
public abstract class Step implements Stepable {
	public final DecoratedLinePart altered;
    public final int alteredAt_distanceFrom00;
	public Step(DecoratedLinePart altered, int alteredAt_distanceFrom00) {
		this.altered= altered;
		this.alteredAt_distanceFrom00=alteredAt_distanceFrom00;
	}
	@Override public String toString() {
		return getClass().getSimpleName()+"[at:"+alteredAt_distanceFrom00+"-"+altered.txt+"]";
	}
}

