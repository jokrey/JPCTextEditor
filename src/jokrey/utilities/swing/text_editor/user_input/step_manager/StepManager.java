package jokrey.utilities.swing.text_editor.user_input.step_manager;

import jokrey.utilities.swing.text_editor.text_storage.DecoratedLinePart;
import jokrey.utilities.swing.text_editor.user_input.UserCursor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.regex.Pattern;

/**
 * Provides undo and redo functionality to UserInputHandler.
 */
public class StepManager {
	public static final int MAX_HISTORY_SIZE = 99;

	public void reset() {
		steps.clear();
		curPosInSteps=0;
	}

	private final LinkedList<Stepable> steps = new LinkedList<>();
	private int curPosInSteps = 0;
	private Stepable addStep(Stepable step) {
		if(curPosInSteps<0)steps.clear();
		else if(!steps.isEmpty()) {
			for(int i=curPosInSteps;i<steps.size();i++) {
				try {
					steps.remove(i--);
				} catch(ConcurrentModificationException ex) {
					ex.printStackTrace();
				}
			}
		}

		if(step instanceof Step) {
			Step combinableStep = (Step) step;
			if((combinableStep.altered.length()>1 || Pattern.compile("[^a-zA-Z0-9'üöäß_-]").matcher(combinableStep.altered.txt).find())) {
				squashSteps();
			}
		}

		if(steps.size()> MAX_HISTORY_SIZE)
			steps.removeFirst();
		steps.addLast(step);
		curPosInSteps = steps.size();

		return step;
	}

	public void redo(UserCursor cursor) {
		if(curPosInSteps<steps.size()) {
			steps.get(curPosInSteps++).redo(cursor);
		}
	}
	public void undo(UserCursor cursor) {
		if(curPosInSteps>0) {
			steps.get(--curPosInSteps).undo(cursor);
		}
	}




	public static Stepable getStepDeletion(int cursor_distance_from_00, DecoratedLinePart[] removed) {
		if(removed.length!=0) {
			DeletionStep[] del_steps = new DeletionStep[removed.length];
			int elapsedChars = 0;
			for (int i = 0; i < removed.length; i++) {
				del_steps[i] = new DeletionStep(removed[i], cursor_distance_from_00 + elapsedChars);
				elapsedChars += removed[i].length();
			}
			reverseArray(del_steps);
			if(removed.length==1) {
				return (del_steps[0]);
			} else {
				return new MultiStep(del_steps);
			}
		} else {
            return null;
		}
	}
	public static Stepable getStepInsert(int cursor_distance_from_00, DecoratedLinePart[] toInsert) {
        ArrayList<InsertionStep> ins_steps = new ArrayList<>();
        for (DecoratedLinePart aToInsert : toInsert) {
            ins_steps.add(new InsertionStep(aToInsert, cursor_distance_from_00));
        }

        if(ins_steps.size()==1) {
            return (ins_steps.get(0));
        } else {
            return (new MultiStep(ins_steps.toArray(new InsertionStep[0])));
        }
    }

	public Stepable deletion(DecoratedLinePart[] removed, UserCursor cursor) {
		Stepable deletion = getStepDeletion(cursor.getDistanceFrom00(), removed);
	    if(deletion!=null)
	        return addStep(deletion);
		return null;
	}
	public Stepable insertion(UserCursor cursor, DecoratedLinePart[] toInsert) {
		Stepable insert = getStepInsert(cursor.getDistanceFrom00(), toInsert);
        if(insert!=null)
            return addStep(insert);
        return null;
	}
	public Stepable multiStep(Stepable... steps) {
		return addStep(new MultiStep(steps));
	}




	private void squashSteps() {
		//Joining steps to words:
		for (int i = steps.size() - 1; i >= 1; i--) {//breaks at position 1. So there always is a i-1 in steps....
			Stepable stepAt_i = steps.get(i);
			Step combinableStepAtI = null;
			if(stepAt_i instanceof Step)
				combinableStepAtI = (Step) stepAt_i;
			Stepable stepAt_i_minus1 = steps.get(i - 1);
			Step combinableStepAt_i_minus1 = null;
			if(stepAt_i_minus1 instanceof Step)
				combinableStepAt_i_minus1 = (Step) stepAt_i_minus1;
			if (combinableStepAtI == null || Pattern.compile("[^a-zA-Z0-9'üöäß_-]").matcher(combinableStepAtI.altered.txt).find() ||
					(combinableStepAt_i_minus1 != null && !combinableStepAt_i_minus1.altered.txt.equals(" ") && Pattern.compile("[^a-zA-Z0-9'üöäß_-]").matcher(combinableStepAt_i_minus1.altered.txt).find())) {//The Step minus 1 allows a space.
				break;//at this point any previous Steps have already been handled
			} else if (combinableStepAt_i_minus1 != null) {
				if (stepAt_i.getClass().equals(stepAt_i_minus1.getClass()) &&
						combinableStepAtI.altered.sameLayoutAs(combinableStepAt_i_minus1.altered) &&
						combinableStepAt_i_minus1.alteredAt_distanceFrom00 + combinableStepAt_i_minus1.altered.length() == combinableStepAtI.alteredAt_distanceFrom00) {
//						if they are of the same class and occur directly after one another....
					try {
						Step newJoinedStep =
								combinableStepAtI.getClass().getDeclaredConstructor(DecoratedLinePart.class, int.class)
										.newInstance(
												new DecoratedLinePart(combinableStepAt_i_minus1.altered.txt + combinableStepAtI.altered.txt, combinableStepAtI.altered.layout),
												combinableStepAt_i_minus1.alteredAt_distanceFrom00
										);
						steps.set(i - 1, newJoinedStep);
						steps.remove(i);
//							curPosInSteps--;//not neccassary
					} catch (InstantiationException
							| IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
						e.printStackTrace();
						break;
					}
				} else {
					break;
				}
			}
		}
	}

	public static void reverseArray(Object[] arr) {
		for(int i=0; i<arr.length/2; i++) {
			Object temp = arr[i];
			arr[i] = arr[arr.length - i - 1];
			arr[arr.length - i - 1] = temp;
		}
	}
}