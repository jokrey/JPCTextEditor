package jokrey.utilities.swing.text_editor.user_input.step_manager;

import jokrey.utilities.swing.text_editor.user_input.UserCursor;

public class MultiStep extends Step {
	private final Step[] steps; //DO NOT CALL ALTERING OPERATIONS. Then a copy has to be obtained in constructor.

	public MultiStep(Step... array) {
		super(null, -1);
//		steps= Arrays.copyOf(array, array.length); //not required. steps is private anyways.
        steps=array;
	}

	@Override public void redo(UserCursor cursor) {
		for(Step step:steps) {
			step.redo(cursor);
		}
	}

	@Override public void undo(UserCursor cursor) {
		for(int i=steps.length-1;i>=0;i--) {
			steps[i].undo(cursor);
		}
	}
}