package jokrey.utilities.swing.text_editor.user_input;

import jokrey.utilities.swing.text_editor.JPC_Connector;
import jokrey.utilities.swing.text_editor.text_storage.ContentEditor;
import jokrey.utilities.swing.text_editor.text_storage.LinePart;
import jokrey.utilities.swing.text_editor.user_input.cursor.TextDisplayCursor;
import jokrey.utilities.swing.text_editor.user_input.cursor.TextInterval;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Listens to user input and relays that input to the UserInputHandler.
 * Should and can largely not be touched.
 */
public class RawUserInputHandler extends MouseAdapter implements KeyListener, FocusListener/*, DragGestureListener*/ {
	//Last recognised state variables
	private final TextInterval selectionStartPos;
	private boolean shiftPressed = false;
	private boolean ctrlPressed = false;
	private boolean altPressed=false;
	private boolean leftMousePressed = false;
	private boolean export_dragging=false;
	private boolean dragging = false;

	private enum SelectionMode {
        NONE, CHARACTER, WORD, LINE
    }
	private SelectionMode selection_mode = SelectionMode.NONE;

	private final JComponent display;
	private final JPC_Connector jpc;
    private final UserInputHandler user_input_handler;
    private final ContentEditor content;
	public RawUserInputHandler(JComponent display, JPC_Connector jpc, UserInputHandler user_input_handler, ContentEditor content) {
        this.display=display;
        this.jpc=jpc;
		this.user_input_handler=user_input_handler;
		this.content=content;
        selectionStartPos = new TextInterval(content);
        display.addKeyListener(this);
        display.addMouseMotionListener(this);
        display.addMouseWheelListener(this);
        display.addMouseListener(this);
        display.addFocusListener(this);
		addTransferHandler(display);
	}

	



//INSERTION AND KEYBOARD INPUT============================================================
	@Override public void keyTyped(KeyEvent e) {
		if(e.getKeyChar() == '\t') {//Requires a call of .setFocusTraversalKeysEnabled(false); to enable tab mouse jpc_connector detection....
			user_input_handler._user_insert("\t");
		} else if (e.getKeyChar() == '\n' && !ctrlPressed) {//enter
            boolean allowed = jpc.enterPressed();
            if(allowed)
	            user_input_handler._user_insert("\n");
        } else if (e.getKeyChar() == '\u0008') {//backspace
            user_input_handler._user_remove(-1);
        } else if (e.getKeyChar() == '\u007F') {//delete
            user_input_handler._user_remove(1);
        } else if(!ctrlPressed || altPressed) {//any other character. Also, to savely allow Keyboard shortcuts, ignore if ctrl is pressed
        	String toInsert = Character.toString(e.getKeyChar());
            user_input_handler._user_insert(toInsert);
        }
        //else if(ctrlPressed) {
//        	ctrlPressed=false;  //why was this ever here? -> sometimes on focus lost this wasn't updated, now it is.
        //	return;
        //}
	}

	@Override public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_SHIFT:
				shiftPressed = false;
				break;
			case KeyEvent.VK_CONTROL:
				ctrlPressed = false;
				break;
			case KeyEvent.VK_ALT:
				altPressed = false;
				break;
		}
	}
	@Override public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
            case KeyEvent.VK_CONTROL:
                ctrlPressed=true;
                return;
            case KeyEvent.VK_ALT:
                altPressed=true;
                return;
			case KeyEvent.VK_SHIFT:
				if (!leftMousePressed && !shiftPressed) {
				    if(user_input_handler.cursor.selection.isClear()) {
                        selectionStartPos.setXY(user_input_handler.cursor.getXY());
                        calcNewCursorPosAndNewSelection(user_input_handler.cursor.getXY());
                    }
                    selection_mode = SelectionMode.CHARACTER;
				}
                shiftPressed = true;
				return;
            case KeyEvent.VK_LEFT:
                _user_move_cursor_left();
				return;
			case KeyEvent.VK_RIGHT:
                _user_move_cursor_right();
				return;
            case KeyEvent.VK_UP:
                _user_move_cursor_up();
                return;
            case KeyEvent.VK_DOWN:
                _user_move_cursor_down();
                return;
		}
	}
    private void _user_move_cursor_left() {
        if(!user_input_handler.cursor.isSelectionClear() && !shiftPressed) {
            selection_mode = SelectionMode.NONE;
            user_input_handler.cursor.setXY(user_input_handler.cursor.selection.get1XY());
        } else {
            if(ctrlPressed)
                jumpWordLeft();
            else
                user_input_handler.cursor.x_minus(1);
        }
        calcNewCursorPosAndNewSelection(user_input_handler.cursor.getXY());
    }
    private void _user_move_cursor_right() {
        if(!user_input_handler.cursor.isSelectionClear() && !shiftPressed) {
            selection_mode = SelectionMode.NONE;
            user_input_handler.cursor.setXY(user_input_handler.cursor.selection.get2XY());
        } else {
            if(ctrlPressed)
                jumpWordRight();
            else
                user_input_handler.cursor.x_plus(1);
        }
        calcNewCursorPosAndNewSelection(user_input_handler.cursor.getXY());
    }
    private void _user_move_cursor_up() {
        if(!user_input_handler.cursor.isSelectionClear() && !shiftPressed) {
            selection_mode = SelectionMode.NONE;
            user_input_handler.cursor.setXY(user_input_handler.cursor.selection.get1XY());
        } else {
//            if(ctrlPressed) {
//                user_input_handler.cursor.y_minus(1);
//                user_input_handler.cursor.setX(0);
//            } else {
                Point oldCursorLoc_up = user_input_handler.cursor.getLocation(jpc.getTextSpacingLeft(), jpc.getTextSpacingTop());
                Point newLoc_up;
                for (int y_counter = 1; (newLoc_up = user_input_handler.cursor.getLocation(jpc.getTextSpacingLeft(), jpc.getTextSpacingTop())).equals(oldCursorLoc_up); y_counter++) {
                    user_input_handler.cursor.setPositionTo(new Point(newLoc_up.x, newLoc_up.y - y_counter), jpc.getTextSpacingLeft(), jpc.getTextSpacingTop());
                    if (user_input_handler.cursor.getY() == 0 && user_input_handler.cursor.getX() == 0)
                        break;
                }
//                if(ctrlPressed)
//                    jumpWordLeft();
//            }
        }
        calcNewCursorPosAndNewSelection(user_input_handler.cursor.getXY());
    }
    private void _user_move_cursor_down() {
	    if(!user_input_handler.cursor.isSelectionClear() && !shiftPressed) {
            user_input_handler.cursor.setXY(user_input_handler.cursor.selection.get2XY());
        } else {
//            if(ctrlPressed) {
//                user_input_handler.cursor.y_plus(1);
//                user_input_handler.cursor.setX(0);
//            } else {
                Point oldCursorLoc_down = user_input_handler.cursor.getLocation(jpc.getTextSpacingLeft(), jpc.getTextSpacingTop());
                Point newLoc_down;
                for (int y_counter = 1; (newLoc_down = user_input_handler.cursor.getLocation(jpc.getTextSpacingLeft(), jpc.getTextSpacingTop())).equals(oldCursorLoc_down); y_counter++) {
                    user_input_handler.cursor.setPositionTo(new Point(newLoc_down.x, newLoc_down.y + y_counter), jpc.getTextSpacingLeft(), jpc.getTextSpacingTop());
                    if (user_input_handler.cursor.getY() == content.getLineCount() - 1 && user_input_handler.cursor.getX() == content.getLineLength(user_input_handler.cursor.getY()))
                        break;
                }
//            if(ctrlPressed)
//                jumpWordRight();
//            }
        }
        calcNewCursorPosAndNewSelection(user_input_handler.cursor.getXY());
    }
	
	private void addTransferHandler(JComponent display) {
        display.setTransferHandler(new TransferHandler() {
			Point last_mouseP_in_import = null;//spamming filter basicly
	    	@Override public boolean canImport(JComponent jc, DataFlavor[] flavors) {
	    	    if(!content.isDragAndDropEnabled())return false;
	    		Point mouseP = MouseInfo.getPointerInfo().getLocation();
	    		SwingUtilities.convertPointFromScreen(mouseP, display);
	    		if(last_mouseP_in_import==null || !mouseP.equals(last_mouseP_in_import)) {//Spam block
		    		last_mouseP_in_import=mouseP;
                    user_input_handler.cursor.setPositionTo(mouseP, jpc.getTextSpacingLeft(), jpc.getTextSpacingTop());
                    if(!export_dragging) {
			    		user_input_handler.cursor.clearSelection();
		    		}
                    display.requestFocus();
                    display.repaint();
	    		}
	    		return true;
	    	}
	    	@Override public boolean importData(TransferSupport ts) {
                if(!content.isDragAndDropEnabled())return false;
	    		Point mouseP = MouseInfo.getPointerInfo().getLocation();
	    		SwingUtilities.convertPointFromScreen(mouseP, display);
				Object data;
				try {
				    List<DataFlavor> availableFlavors = Arrays.asList(ts.getDataFlavors());
				    DataFlavor selectedAvailableFlavor = availableFlavors.get(0);
				    if(availableFlavors.contains(customLinePartArrayDataFlavor))
				        selectedAvailableFlavor = customLinePartArrayDataFlavor;
				    else if(availableFlavors.contains(DataFlavor.stringFlavor))
				        selectedAvailableFlavor = DataFlavor.stringFlavor;

					data = ts.getTransferable().getTransferData(selectedAvailableFlavor);
					if(export_dragging && user_input_handler.cursor.selection.isOnSelection(user_input_handler.cursor)) {
						//if the data is coming from the textfield itself and is on the selection to be exported...  Nothing should happen anyways, but just to be safe
			    		user_input_handler.cursor.clearSelection();
					} else {
						//if the data is coming from outside, or from inside, but is supposed to getXY elsewhere..
						if(export_dragging) {
							int insert_cursor_distance_from_00 = user_input_handler.cursor.getDistanceFrom00();
							int selection_start_distance_from_00 = user_input_handler.cursor.selection.get1_distance00();
							int removedCharsCount = user_input_handler._user_remove(0);
                            jpc.repaint();
							jpc.validateCursorVisibility();

							if(selection_start_distance_from_00 < insert_cursor_distance_from_00) {
								 //extraction before insertion
								user_input_handler.cursor.setFromDistance(insert_cursor_distance_from_00 - removedCharsCount);
							} else {
								user_input_handler.cursor.setFromDistance(insert_cursor_distance_from_00);
							}
						}
			    		user_input_handler.cursor.clearSelection();

						if(data instanceof LinePart[]) {
						    //todo - if the editor does not use layouting(which is currently undeterminable), then insert as string..
							LinePart[] toInsert=((LinePart[])data);
							if(toInsert.length==0) return false;
							display.requestFocus();
                            user_input_handler._user_insert(toInsert);
						} else {
							String toInsert = "";
							if(data instanceof File || data instanceof String)
								toInsert=data.toString();
							else if(data instanceof List)
								toInsert=((List<?>)data).get(0).toString();
							else if(data instanceof Reader) {
								Reader reader_data = (Reader)data;
							    Scanner s = new Scanner(reader_data);
							    Scanner del_s = s.useDelimiter("\\A");
								toInsert=del_s.hasNext() ? del_s.next() : "";
								reader_data.close();
								s.close();
								del_s.close();
								StringBuilder remover = new StringBuilder(toInsert);
								while(toInsert.contains("<")&&toInsert.contains(">")) {
									remover.delete(toInsert.indexOf("<"), toInsert.indexOf(">")+1);
				    				toInsert=remover.toString();
								}
							}
							if(toInsert==null||toInsert.isEmpty()) return false;
                            display.requestFocus();
							user_input_handler._user_insert(toInsert);
						}
                        jpc.repaint();
                        jpc.validateCursorVisibility();
                        return true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
	    	}

            private DataFlavor customLinePartArrayDataFlavor = null;
            {
                try {
                    customLinePartArrayDataFlavor = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + LinePart[].class.getName() + "\"");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            private DataFlavor[] flavors = customLinePartArrayDataFlavor ==null? new DataFlavor[] {DataFlavor.stringFlavor} : new DataFlavor[] {customLinePartArrayDataFlavor, DataFlavor.stringFlavor, DataFlavor.getTextPlainUnicodeFlavor(), new DataFlavor("text/plain", "Text")};
	    	@Override protected Transferable createTransferable(JComponent jc) {
                if(!content.isDragAndDropEnabled())return null;
	    		final String intervalText = user_input_handler.cursor.selection.getIntervalText();
	    		final LinePart[] intervalData = user_input_handler.cursor.selection.getIntervalSequences();
                return new Transferable() {
                    @Override public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return Arrays.asList(flavors).contains(flavor);
                    }
                    @Override public DataFlavor[] getTransferDataFlavors() {
                        return flavors;
                    }
                    @Override public Object getTransferData(DataFlavor df) {
                        System.out.println("df = " + df);
                        if(df.isFlavorTextType() || df.equals(DataFlavor.stringFlavor))
                            return intervalText;
                        if(df.equals(DataFlavor.getTextPlainUnicodeFlavor())) {
                            return new ByteArrayInputStream(intervalText.getBytes());
                        } else if(customLinePartArrayDataFlavor!=null && df.equals(customLinePartArrayDataFlavor)) {
                            return intervalData;
                        } else {
                            return new ByteArrayInputStream(intervalText.getBytes());
                        }
                    }
                };
            }
    	    @Override public int getSourceActions(JComponent c) {
    	        return MOVE;
    	    }
    	    @Override protected void exportDone(JComponent source, Transferable data, int action) {
//    	    	if(/*!user_input_handler.cursor.selection.isOnSelection(mouseP) && */export_dragging) {
//    	    		//seems doubled to importDrag, but isn't, since this gets called if the export is outside of this field
//    	    		//Also won't remove something twice, since if the export finishes inside this textfield, importDrag gets called first and already clears or removes selection
//    	    	    //But removal is not even desirable........................................................
//        	    	user_input_handler.cursor.removeSelectedInterval();
//    	    	}
    			export_dragging=false;
    	    }
	    });
	}


// MOUSE EVENTS ===================================================================
	@Override public void mousePressed(MouseEvent e) {
		display.requestFocus();
		if(SwingUtilities.isLeftMouseButton(e)) {
            selection_mode = SelectionMode.NONE;
			leftMousePressed=true;
            user_input_handler.cursor.setPositionTo(e.getPoint(), jpc.getTextSpacingLeft(), jpc.getTextSpacingTop());
            if(!shiftPressed)
                selectionStartPos.setXY(user_input_handler.cursor.getXY());
            if(!content.isSelectionEnabled())
                selection_mode = SelectionMode.NONE;
            else if(e.getClickCount()==1) {
                selection_mode = SelectionMode.CHARACTER;
			} else if(e.getClickCount() == 2) {
			    selectCurrentWord();
            } else if(e.getClickCount() == 3) {
			    selectCurrentLine();
            }
		}
	}
	@Override public void mouseDragged(MouseEvent e) {
		if(!dragging) {
            if(selection_mode == SelectionMode.CHARACTER && content.isDragAndDropEnabled() &&
                    !user_input_handler.cursor.selection.getIntervalText().isEmpty() &&
                     user_input_handler.cursor.selection.isOnSelection(e.getPoint(), jpc.getTextSpacingLeft(), display.getWidth(), jpc.getTextSpacingTop())) {
                export_dragging=true;
                display.getTransferHandler().exportAsDrag(display, e,
                        TransferHandler.MOVE);
            }
		}

		dragging=true;
		calcNewCursorPosAndNewSelection(e.getPoint());
	}
	@Override public void mouseReleased(MouseEvent e) {
		dragging=false;
		if(SwingUtilities.isLeftMouseButton(e)) {
		    if(e.getClickCount()==1)
                calcNewCursorPosAndNewSelection(e.getPoint());
			leftMousePressed=false;
		} else if(SwingUtilities.isRightMouseButton(e)) {
            final JPopupMenu jpm = new JPopupMenu();

            for(Action function : user_input_handler.getContextActions()) {
            	JMenuItem item = new JMenuItem(function);
            	Object shortcut_name = function.getValue("shortcut_name");
            	item.setText(function.getValue(Action.NAME).toString()+(shortcut_name==null?"":" ("+shortcut_name+")"));
                jpm.add(item);
			}

            jpm.setBorder(BorderFactory.createEmptyBorder());
            jpm.setForeground(Color.WHITE);
            jpm.setBackground(Color.DARK_GRAY.brighter());
            Point mouseP = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(mouseP, display);
            jpm.show(display, mouseP.x, mouseP.y);
		}
	}

    @Override public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getWheelRotation() < 0) {                 //wheel up
            if (shiftPressed || ctrlPressed) {
                content.increaseStandardFontSize();
                jpc.recalculateDisplayLines();
            } else
            	display.getParent().dispatchEvent(e);   //hand back..
        } else {                       				    //wheel down
            if (shiftPressed||ctrlPressed) {
                content.decreaseStandardFontSize();
                jpc.recalculateDisplayLines();
            } else
            	display.getParent().dispatchEvent(e);   //hand back..
        }
    }



   
 // HELPER METHODS================================================================

	void calcNewCursorPosAndNewSelection(Point mousePos) {
	    calcNewCursorPosAndNewSelection(new TextDisplayCursor(content, mousePos, jpc.getTextSpacingLeft(), jpc.getTextSpacingTop()).getXY());
    }
	void calcNewCursorPosAndNewSelection(int... cursor_xy) {
        jumpSelectionTo(cursor_xy);
		if(shiftPressed||leftMousePressed) {
			user_input_handler.cursor.setXY(cursor_xy);
		} else {
            selection_mode=SelectionMode.NONE;
		    if(!export_dragging)
                user_input_handler.cursor.clearSelection();
        }
	}

    @Override public void focusGained(FocusEvent e) {
        jpc.repaint();
    }

    @Override public void focusLost(FocusEvent e) {
        jpc.repaint();
        shiftPressed = false;
        ctrlPressed = false;
        altPressed=false;
        leftMousePressed = false;
        export_dragging=false;
        dragging = false;
        selection_mode=SelectionMode.CHARACTER;
    }







    //word selection mode logic:::
    void selectCurrentLine() {
        if(!content.isSelectionEnabled()) return;
        user_input_handler.cursor.selection.setFromXYs(
                0, user_input_handler.cursor.getY(),
                content.getLineLength(user_input_handler.cursor.getY()), user_input_handler.cursor.getY());
        selectionStartPos.setFromXYs(user_input_handler.cursor.selection.getXYs());
        user_input_handler.cursor.setXY(user_input_handler.cursor.selection.get2XY());
        selection_mode=SelectionMode.LINE;
        jpc.repaint();
    }

    public void selectCurrentWord() {
	    if(!content.isSelectionEnabled()) return;
        selectionStartPos.setXY(user_input_handler.cursor.getXY());
        selection_mode = SelectionMode.WORD;
        jumpSelectionToCursor();
        selectionStartPos.setFromXYs(user_input_handler.cursor.selection.getXYs());
        user_input_handler.cursor.setXY(user_input_handler.cursor.selection.get2XY());
        if(selectionStartPos.isClear())
            selection_mode = SelectionMode.CHARACTER;
        jpc.repaint();
    }


    private void jumpWordRight() {
        int x = user_input_handler.cursor.getX();
        int y = user_input_handler.cursor.getY();
        String line = content.getLineText(y);
        if(x==line.length()) {
            user_input_handler.cursor.x_plus(1); //jump to next line
        } else {
            int i=x;
            while (i < line.length() && !Character.toString(line.charAt(i)).matches("[a-zA-Z0-9'äöüß_-]"))
                i++;
            if (Math.abs(i - (x)) <= 1) //if one or no characters jumped (i.e. a single space or non-word character)
                while (i < line.length() && Character.toString(line.charAt(i)).matches("[a-zA-Z0-9'äöüß_-]"))
                    i++;
            user_input_handler.cursor.setXY(i, y);
        }
    }
    private void jumpWordLeft() {
        int x = user_input_handler.cursor.getX();
        int y = user_input_handler.cursor.getY();
        String line = content.getLineText(y);
        if(x==0) {
            user_input_handler.cursor.x_minus(1); //jump to previous line
        } else {
            int i = x - 1;
            while (i >= 0 && !Character.toString(line.charAt(i)).matches("[a-zA-Z0-9'äöüß_-]"))
                i--;
            if (Math.abs(i - (x - 1)) <= 1) //if one or no characters jumped (i.e. a single space or non-word character)
                while (i >= 0 && Character.toString(line.charAt(i)).matches("[a-zA-Z0-9'äöüß_-]"))
                    i--;
            user_input_handler.cursor.setXY(i + 1, y);
        }
    }

    private void jumpSelectionToCursor() {
        jumpSelectionTo(user_input_handler.cursor.getXY());
    }
    private void jumpSelectionTo(int... newCursorXY) {
	    try {
            if (!content.isSelectionEnabled()) {
                user_input_handler.cursor.selection.clear();
                return;
            }
            if (selection_mode == SelectionMode.NONE || export_dragging) return;
            if (selection_mode == SelectionMode.CHARACTER) {
                user_input_handler.cursor.selection.setFromXYs(selectionStartPos.get1XY(), newCursorXY);
                return;
            }

            TextDisplayCursor c1 = null;
            TextDisplayCursor c2 = null;
            {
                int x = user_input_handler.cursor.getX();
                int y = user_input_handler.cursor.getY();
                if (selection_mode == SelectionMode.WORD && Pattern.compile("[^a-zA-Z0-9'üöäß_-]").matcher(content.getLineText(y)).find()) {
                    String[] wordsInLine = content.getLineText(y).split("[^a-zA-Z0-9'äöüß_-]");//ONLY WORKS FOR GERMAN/ENGLISH TEXT
                    int ellapsedCharCount = 0;
                    for (String wordInLine : wordsInLine) {
                        if ((ellapsedCharCount + wordInLine.length()) >= x) {
                            c1 = new TextDisplayCursor(content, ellapsedCharCount, y);
                            c2 = new TextDisplayCursor(content, Math.max(ellapsedCharCount + wordInLine.length(), x), y);
                            break;
                        }
                        ellapsedCharCount += wordInLine.length() + 1;
                    }
                    if (c1 == null) {
                        c1 = new TextDisplayCursor(content, ellapsedCharCount, y);
                        c2 = new TextDisplayCursor(content, content.getLineLength(y), y);
                    }
                } else {//selectionMode == LINE || no word in line
                    c1 = new TextDisplayCursor(content, 0, y);
                    c2 = new TextDisplayCursor(content, content.getLineLength(y), y);
                }
            }
            int lastSelection_distance1 = selectionStartPos.get1_distance00();
            int lastSelection_distance2 = selectionStartPos.get2_distance00();
            int c1_distance = c1.getDistanceFrom00();
            int c2_distance = c2.getDistanceFrom00();
            if (lastSelection_distance2 < c1_distance && lastSelection_distance1 < c2_distance) {
                user_input_handler.cursor.setSelection(lastSelection_distance1, c2_distance);
            } else if (lastSelection_distance1 > c1_distance && lastSelection_distance1 > c2_distance) {
                user_input_handler.cursor.setSelection(c1_distance, lastSelection_distance2);
            } else { //lastCursor_distance's between c1 and c2
                user_input_handler.cursor.setSelection(c1_distance, c2_distance);
            }
        } finally {
            jpc.validateCursorVisibility();
            jpc.repaint();
        }
    }
}