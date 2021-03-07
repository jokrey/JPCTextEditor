package jokrey.utilities.swing.text_editor.user_input.step_manager;

import jokrey.utilities.swing.text_editor.user_input.UserCursor;
import java.lang.reflect.Array;

public class MultiStep implements Stepable {
	private final Stepable[] steps; //DO NOT CALL ALTERING OPERATIONS. Then a copy has to be obtained in constructor.

	public MultiStep(Stepable... array) {
		steps = filterNulls(array);
	}

	private<E> E[] filterNulls(E[] array) {
		E[] r = (E[]) Array.newInstance(array.getClass().getComponentType(), countNotNull(array));
		for(int i=0,c=0;i<array.length;i++) {
			E a = array[i];
			if(a != null)
				r[c++] = a;
		}
		return r;
	}

	private int countNotNull(Object[] array) {
		int notNull = 0;
		for(Object a : array) if(a != null) notNull++;
		return notNull;
	}

	@Override public void redo(UserCursor cursor) {
		for(Stepable step:steps) {
			step.redo(cursor);
		}
	}

	@Override public void undo(UserCursor cursor) {
		for(int i=steps.length-1;i>=0;i--) {
			steps[i].undo(cursor);
		}
	}
}