package jokrey.utililities.swing.text_editor.user_input;

import jokrey.utililities.swing.text_editor.ui.core.JPC_Connector;
import jokrey.utililities.swing.text_editor.text_storage.ContentEditor;
import jokrey.utililities.swing.text_editor.text_storage.LinePart;
import jokrey.utililities.swing.text_editor.text_storage.NeverDrawnException;
import jokrey.utililities.swing.text_editor.user_input.cursor.TextDisplayCursor;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.io.File;
import java.io.Reader;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Listens to user input and relays that input to the UserInputHandler.
 * Should and can largely not be touched.
 */
public class RawUserInputHandler extends MouseAdapter implements KeyListener, FocusListener/*, DragGestureListener*/ {
	//Last recognised state variables
	private final TextDisplayCursor lastStartCursorPos;
	private boolean shiftPressed = false;
	private boolean ctrlPressed = false;
	private boolean altPressed=false;
	private boolean leftMousePressed = false;
	private boolean export_dragging=false;
	private boolean dragging = false;

	private final JComponent display;
	private final JPC_Connector jpc;
    private final UserInputHandler user_input_handler;
    private final ContentEditor content;
	public RawUserInputHandler(JComponent display, JPC_Connector jpc, UserInputHandler user_input_handler, ContentEditor content) {
        this.display=display;
        this.jpc=jpc;
		this.user_input_handler=user_input_handler;
		this.content=content;
		lastStartCursorPos = new TextDisplayCursor(content);
        display.addKeyListener(this);
        display.addMouseMotionListener(this);
        display.addMouseWheelListener(this);
        display.addMouseListener(this);
        display.addFocusListener(this);
		addTransferHandler(display);
	}

	

//double click selection

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
        } else if(ctrlPressed) {
//        	ctrlPressed=false;  //why was this ever here? -> sometimes on focus lost this wasn't updated, now it is.
        	return;
        }
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
				if (!leftMousePressed && !shiftPressed && user_input_handler.cursor.selection.isClear()) {
					shiftPressed = true;
					lastStartCursorPos.setXY(user_input_handler.cursor.getXY());
					calcNewCursorPosAndNewSelection(user_input_handler.cursor.getXY());
				} else
					shiftPressed = true;
				return;
			case KeyEvent.VK_LEFT:
				user_input_handler.cursor.x_minus(1);
				calcNewCursorPosAndNewSelection(user_input_handler.cursor.getXY());
				return;
			case KeyEvent.VK_RIGHT:
				user_input_handler.cursor.x_plus(1);
				calcNewCursorPosAndNewSelection(user_input_handler.cursor.getXY());
				return;
		}

        try {
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                Point oldLoc = user_input_handler.cursor.getLocation(jpc.getTextSpacingLeft(), jpc.getTextSpacingTop());
                Point newLoc;
                for (int y_counter = 1; (newLoc = user_input_handler.cursor.getLocation(jpc.getTextSpacingLeft(), jpc.getTextSpacingTop())).equals(oldLoc); y_counter++) {
                    user_input_handler.cursor.setPositionTo(new Point(newLoc.x, newLoc.y - y_counter), jpc.getTextSpacingLeft(), jpc.getTextSpacingTop());
                    if (user_input_handler.cursor.getY() == 0 && user_input_handler.cursor.getX() == 0)
                        break;
                }
                calcNewCursorPosAndNewSelection(user_input_handler.cursor.getXY());
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                Point oldLoc = user_input_handler.cursor.getLocation(jpc.getTextSpacingLeft(), jpc.getTextSpacingTop());
                Point newLoc;
                for (int y_counter = 1; (newLoc = user_input_handler.cursor.getLocation(jpc.getTextSpacingLeft(), jpc.getTextSpacingTop())).equals(oldLoc); y_counter++) {
                    user_input_handler.cursor.setPositionTo(new Point(newLoc.x, newLoc.y + y_counter), jpc.getTextSpacingLeft(), jpc.getTextSpacingTop());
                    if (user_input_handler.cursor.getY() == content.getLineCount() - 1 && user_input_handler.cursor.getX() == content.getLineLength(user_input_handler.cursor.getY()))
                        break;
                }
                calcNewCursorPosAndNewSelection(user_input_handler.cursor.getXY());
            }
        } catch(NeverDrawnException ex) {//basically guaranteed that it was drawn at this time.
		    ex.printStackTrace();
        }
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
                    try {
                        user_input_handler.cursor.setPositionTo(mouseP, jpc.getTextSpacingLeft(), jpc.getTextSpacingTop());
                    } catch (NeverDrawnException e) {//has DEFINITLY been drawn at this time.
                        e.printStackTrace();
                        return false;
                    }
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
					data = ts.getTransferable().getTransferData(ts.getDataFlavors()[0]);
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
							LinePart[] toInsert=((LinePart[])data);
							if(toInsert.length==0) return false;
							display.requestFocus();
                            user_input_handler._user_insert(toInsert);
                            jpc.repaint();
							jpc.validateCursorVisibility();
							return true;
						} else {
							String toInsert = "";
							if(data instanceof File||data instanceof String)
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
							jpc.validateCursorVisibility();
                            jpc.repaint();
							return true;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
	    	}
	    	@Override protected Transferable createTransferable(JComponent jc) {
                if(!content.isDragAndDropEnabled())return null;
	    		final String intervalText = user_input_handler.cursor.selection.getIntervalText();
	    		final LinePart[] intervalData = user_input_handler.cursor.selection.getIntervalSequences();
	    		return new Transferable() {
					@Override public boolean isDataFlavorSupported(DataFlavor flavor) {
						return true;
					}
					@Override public DataFlavor[] getTransferDataFlavors() {
						try { 
							return new DataFlavor[] {new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
							           ";class=\"" + LinePart[].class.getName() + "\""), DataFlavor.stringFlavor};
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
							return new DataFlavor[0];
						}
					}
					@Override public Object getTransferData(DataFlavor df) {
						if(df.equals(DataFlavor.stringFlavor)) {
							return intervalText;
						} else {
							return intervalData;
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
//    	    	    //But removal is not even desirable....
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
			leftMousePressed=true;
			if(e.getClickCount()==1) {
                try {
                    user_input_handler.cursor.setPositionTo(e.getPoint(), jpc.getTextSpacingLeft(), jpc.getTextSpacingTop());
                    if(!shiftPressed)
                        lastStartCursorPos.setXY(user_input_handler.cursor.getXY());
                } catch (NeverDrawnException ex) {//when somebody presses this with a mouse, then it will be drawn at that time.
                    ex.printStackTrace();
                }
			}
		}
	}
	@Override public void mouseDragged(MouseEvent e) {
		if(!dragging) {
			try {
				if(content.isDragAndDropEnabled() &&
                        !user_input_handler.cursor.selection.getIntervalText().isEmpty() &&
                        user_input_handler.cursor.selection.isOnSelection(e.getPoint(), jpc.getTextSpacingLeft(), display.getWidth(), jpc.getTextSpacingTop())) {
					export_dragging=true;
					display.getTransferHandler().exportAsDrag(display, e,
							TransferHandler.MOVE);
				}
			} catch (NeverDrawnException ex) {//when somebody presses this with a mouse, then it will be drawn at that time.
				ex.printStackTrace();
				return;
			}
		}

		dragging=true;
		calcNewCursorPosAndNewSelection(e.getPoint());
	}
	@Override public void mouseReleased(MouseEvent e) {
		dragging=false;
		if(SwingUtilities.isLeftMouseButton(e)) {
			switch (e.getClickCount()) {
				case 1:
					calcNewCursorPosAndNewSelection(e.getPoint());
					break;
				case 2:
					try {
						lastStartCursorPos.setPositionTo(e.getPoint(), jpc.getTextSpacingLeft(), jpc.getTextSpacingTop());
					} catch (NeverDrawnException ex) {//when somebody presses this with a mouse, then it will be drawn at that time.
						ex.printStackTrace();
						return;
					}
					if (Pattern.compile("[^a-zA-Z0-9'üöäß_-]").matcher(content.getLineText(user_input_handler.cursor.getY())).find()) {
						String[] wordsInLine = content.getLineText(user_input_handler.cursor.getY()).split("[^a-zA-Z0-9'äöüß_-]");//ONLY WORKS FOR GERMAN/ENGLISH TEXT
						int ellapsedCharCount = 0;
						for (String wordInLine : wordsInLine) {
							if ((ellapsedCharCount + wordInLine.length()) >= user_input_handler.cursor.getX()) {
								lastStartCursorPos.setXY(ellapsedCharCount, user_input_handler.cursor.getY());
								user_input_handler.cursor.selection.setFromXYs(
										ellapsedCharCount, user_input_handler.cursor.getY(),
										ellapsedCharCount + wordInLine.length(), user_input_handler.cursor.getY());
								user_input_handler.cursor.setXY(user_input_handler.cursor.selection.get2XY());
								break;
							}
							ellapsedCharCount += wordInLine.length() + 1;
						}
						jpc.repaint();
					} else
						selectCurrentLine();
					break;
				case 3:
					selectCurrentLine();
					break;
			}
			leftMousePressed=false;
		} else if(SwingUtilities.isRightMouseButton(e)) {
            final JPopupMenu jpm = new JPopupMenu();

            for(Action function : user_input_handler.getContextActions()) {
            	JMenuItem item = new JMenuItem(function);
            	Object shortcut_name = function.getValue("shortcut_name");
            	item.setText(function.getValue(Action.NAME).toString()+(shortcut_name==null?"":" ("+shortcut_name+")"));
                jpm.add(item);
//                jpm.add(function);
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
        if (e.getWheelRotation() < 0) {    //wheel up
            if (shiftPressed || ctrlPressed) {
                content.increaseStandardFontSize();
                jpc.recalculateDisplayLines();
            } else
            	display.getParent().dispatchEvent(e);//hand back..
        } else {                       				//wheel down
            if (shiftPressed||ctrlPressed) {
                content.decreaseStandardFontSize();
                jpc.recalculateDisplayLines();
            } else
            	display.getParent().dispatchEvent(e);//hand back..
        }
    }



   
 // HELPER METHODS================================================================

	void selectCurrentLine() {
        lastStartCursorPos.setXY(0, user_input_handler.cursor.getY());
        user_input_handler.cursor.selection.setFromXYs(
                lastStartCursorPos.getXY(),
                new int[] {content.getLineLength(user_input_handler.cursor.getY()), user_input_handler.cursor.getY()});
		user_input_handler.cursor.setXY(user_input_handler.cursor.selection.get2XY());
		if(!content.isSelectionEnabled())
			user_input_handler.cursor.selection.clear();
        jpc.repaint();
	}
	void calcNewCursorPosAndNewSelection(Point mousePos) {
        try {
            calcNewCursorPosAndNewSelection(new TextDisplayCursor(content, mousePos, jpc.getTextSpacingLeft(), jpc.getTextSpacingTop()).getXY());
        } catch (NeverDrawnException e) {//if called with a mousePos, it is basically guaranteed that it has been drawn
            e.printStackTrace();
        }
    }
	void calcNewCursorPosAndNewSelection(int... cursor_xy) {
		if(shiftPressed||leftMousePressed) {
			user_input_handler.cursor.setXY(cursor_xy);
			if(!export_dragging) {
				user_input_handler.cursor.selection.setFromXYs(lastStartCursorPos.getXY(), user_input_handler.cursor.getXY());
			}
		} else if(!export_dragging) {
			user_input_handler.cursor.clearSelection();
		}
        if(!content.isSelectionEnabled()) {
			user_input_handler.cursor.selection.clear();
		}

		jpc.validateCursorVisibility();
        jpc.repaint();
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
    }
}