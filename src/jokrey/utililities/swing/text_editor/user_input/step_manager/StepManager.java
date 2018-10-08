package jokrey.utililities.swing.text_editor.user_input.step_manager;

import jokrey.utililities.swing.text_editor.text_storage.LinePart;
import jokrey.utililities.swing.text_editor.user_input.UserCursor;

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

	private final LinkedList<Step> steps = new LinkedList<>();
	private int curPosInSteps = 0;
	private void addStep(Step step) {
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

		if(step.altered!=null && (step.altered.length()>1 || Pattern.compile("[^a-zA-Z0-9'üöäß_-]").matcher(step.altered.txt).find())) {
			//Joining steps to words:
			for(int i=steps.size()-1;i>=1;i--) {//breaks at position 1. So there always is a i-1 in steps....
				Step stepAt_i = steps.get(i);
				Step stepAt_i_minus1 = steps.get(i-1);
				if(stepAt_i.altered==null || Pattern.compile("[^a-zA-Z0-9'üöäß_-]").matcher(stepAt_i.altered.txt).find() ||
						(stepAt_i_minus1.altered != null && !stepAt_i_minus1.altered.txt.equals(" ") && Pattern.compile("[^a-zA-Z0-9'üöäß_-]").matcher(stepAt_i_minus1.altered.txt).find())) {//The Step minus 1 allows a space.
					break;//at this point any previous Steps have already been handled
				} else if(stepAt_i_minus1.altered != null) {
					if(stepAt_i.getClass().equals(stepAt_i_minus1.getClass()) &&
							stepAt_i.altered.sameLayoutAs(stepAt_i_minus1.altered) &&
							stepAt_i_minus1.alteredAt_distanceFrom00+stepAt_i_minus1.altered.length() == stepAt_i.alteredAt_distanceFrom00) {
//						if they are of the same class and occur directly after one another....
						try {
							Step newJoinedStep =
									stepAt_i.getClass().getDeclaredConstructor(LinePart.class, int.class)
									.newInstance(
										new LinePart(stepAt_i_minus1.altered.txt+stepAt_i.altered.txt, stepAt_i.altered.layout),
										stepAt_i_minus1.alteredAt_distanceFrom00
									);
							steps.set(i-1, newJoinedStep);
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

		if(steps.size()> MAX_HISTORY_SIZE)
			steps.removeFirst();
		steps.addLast(step);
		curPosInSteps = steps.size();
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




	public static Step getStepDeletion(int cursor_distance_from_00, LinePart[] removed) {
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
				return (new MultiStep(del_steps));
			}
		} else {
            return null;
		}
	}
	public static Step getStepInsert(int cursor_distance_from_00, LinePart[] toInsert) {
        ArrayList<InsertionStep> ins_steps = new ArrayList<>();
        for (LinePart aToInsert : toInsert) {
            ins_steps.add(new InsertionStep(aToInsert, cursor_distance_from_00));
        }

        if(ins_steps.size()==1) {
            return (ins_steps.get(0));
        } else {
            return (new MultiStep(ins_steps.toArray(new InsertionStep[0])));
        }
    }

	public void userPerformedOperation_remove(LinePart[] removed, UserCursor cursor) {
	    Step deletion = getStepDeletion(cursor.getDistanceFrom00(), removed);
	    if(deletion!=null)
	        addStep(deletion);
	}
	public void userPerformedOperation_insert(UserCursor cursor, LinePart[] toInsert) {
        Step insert = getStepInsert(cursor.getDistanceFrom00(), toInsert);
        if(insert!=null)
            addStep(insert);
	}
	public void userPerformedOperation_replace(LinePart[] removed, int cursor_distance_from_00, LinePart[] toInsert) {
        Step deletion = getStepDeletion(cursor_distance_from_00, removed);
        Step insert = getStepInsert(cursor_distance_from_00, toInsert);
        if(deletion!=null && insert!=null) {
            addStep(new MultiStep(deletion, insert));
        } else if(deletion!=null) {
            addStep(deletion);
        } else if(insert!=null) {
            addStep(insert);
        }
	}
	public void userPerformedOperation_custom(Step... steps) {
		addStep(new MultiStep(steps));
	}




	public static void reverseArray(Object[] arr) {
		for(int i=0; i<arr.length/2; i++) {
			Object temp = arr[i];
			arr[i] = arr[arr.length - i - 1];
			arr[arr.length - i - 1] = temp;
		}
	}
}