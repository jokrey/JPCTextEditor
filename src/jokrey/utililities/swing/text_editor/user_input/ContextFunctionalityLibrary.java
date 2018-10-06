package jokrey.utililities.swing.text_editor.user_input;

import jokrey.utililities.swing.text_editor.*;
import jokrey.utililities.swing.text_editor.text_storage.Line;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;

public class ContextFunctionalityLibrary {
    public static Action[] getBasicFunctionality(JPCTextEditor editor, JPC_Connector jpc_connector, UserInputHandler input_handler) {
        return new Action[] {
                getFunctionality_PASTE(input_handler),
                getFunctionality_COPY(input_handler),
                getFunctionality_CUT(input_handler),
                getFunctionality_DELETE_SELECTION(input_handler),
                getFunctionality_FIND(editor, input_handler),
                getFunctionality_UNDO(input_handler),
                getFunctionality_REDO(input_handler),
                getFunctionality_SELECT_ALL(jpc_connector, input_handler)
        };
    }


    //GENERAL EDITOR ONLY
    public static Action getFunctionality_PASTE(UserInputHandler input_handler) {
        return new AbstractAction("paste") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
                putValue("shortcut_name", "ctrl+v");}
            @Override public void actionPerformed(ActionEvent e) {
                input_handler._user_paste();
            }
        };
    }

    public static Action getFunctionality_COPY(UserInputHandler input_handler) {
        return new AbstractAction("copy") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
                putValue("shortcut_name", "ctrl+c");}
            @Override public void actionPerformed(ActionEvent e) {
                input_handler._user_copy();
            }
        };
    }

    public static Action getFunctionality_CUT(UserInputHandler input_handler) {
        return new AbstractAction("cut") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
            putValue("shortcut_name", "ctrl+x");}
            @Override public void actionPerformed(ActionEvent e) {
                input_handler._user_cut();
            }
        };
    }

    public static Action getFunctionality_DELETE_SELECTION(UserInputHandler input_handler) {
        return new AbstractAction("delete") {
            @Override public void actionPerformed(ActionEvent e) {
                input_handler._user_remove(0);
            }
        };
    }

    public static Action getFunctionality_FIND(JPCTextEditor jpc_connector, UserInputHandler input_handler) {
        return new AbstractAction("find/replace") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
            putValue("shortcut_name", "ctrl+f");}
            @Override public void actionPerformed(ActionEvent e) {
                jpc_connector.start_find_replace_frame();
            }
        };
    }

    public static Action getFunctionality_UNDO(UserInputHandler input_handler) {
        return new AbstractAction("undo") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
            putValue("shortcut_name", "ctrl+z");}
            @Override public void actionPerformed(ActionEvent e) {
                input_handler._user_undo();
            }
        };
    }

    public static Action getFunctionality_REDO(UserInputHandler input_handler) {
        return new AbstractAction("redo") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
            putValue("shortcut_name", "ctrl+y");}
            @Override public void actionPerformed(ActionEvent e) {
                input_handler._user_redo();
            }
        };
    }

    public static Action getFunctionality_SELECT_ALL(JPC_Connector jpc_connector, UserInputHandler input_handler) {
        return new AbstractAction("select all") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
            putValue("shortcut_name", "ctrl+a");}
            @Override public void actionPerformed(ActionEvent e) {
                input_handler.cursor.selection.setFromXYs(0,0,Integer.MAX_VALUE,Integer.MAX_VALUE);
                jpc_connector.repaint();
            }
        };
    }


    //Additional Functionality
    public static Action getFunctionality_SELECT_CURRENT_LINE(JPC_Connector jpc_connector, UserInputHandler input_handler) {
        return new AbstractAction("select line") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
            putValue("shortcut_name", "ctrl+q");}
            @Override public void actionPerformed(ActionEvent e) {
                input_handler.cursor.selection.setFromXYs(0,input_handler.cursor.getY(),Integer.MAX_VALUE,input_handler.cursor.getY());
                jpc_connector.repaint();
            }
        };
    }
    public static Action getFunctionality_SELECT_CURRENT_WORD(JPC_Connector jpc_connector, UserInputHandler input_handler) {
        return new AbstractAction("select word") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
            putValue("shortcut_name", "ctrl+e");}
            @Override public void actionPerformed(ActionEvent e) {
                Line l = input_handler.cursor.getContentEditor().getLine(input_handler.cursor.getY());
                String l_content = l.toString();
                int word_begin_index = l_content.lastIndexOf(" ", input_handler.cursor.getX()-1)+1;
                int word_end_index = l_content.indexOf(" ", input_handler.cursor.getX());

                input_handler.cursor.selection.setFromXYs(word_begin_index,input_handler.cursor.getY(),word_end_index==-1?Integer.MAX_VALUE:word_end_index,input_handler.cursor.getY());
                jpc_connector.repaint();
            }
        };
    }
    public static Action getFunctionality_TOGGLE_CAPITALIZE_WORD(UserInputHandler input_handler) {
        return new AbstractAction("toggle word capitalization") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
            putValue("shortcut_name", "ctrl+t");}
            @Override public void actionPerformed(ActionEvent e) {
                input_handler._user_toggle_capitalize_current_word();
            }
        };
    }
    public static Action getFunctionality_JUMP_TO_END_OF_LINE(JPC_Connector jpc_connector, UserInputHandler input_handler) {
        return new AbstractAction("jump eol") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK));
            putValue("shortcut_name", "ctrl+p");}
            @Override public void actionPerformed(ActionEvent e) {
                input_handler.cursor.setXY(Integer.MAX_VALUE, input_handler.cursor.getY());
                jpc_connector.repaint();
            }
        };
    }
    public static Action getFunctionality_JUMP_TO_START_OF_LINE(JPC_Connector jpc_connector, UserInputHandler input_handler) {
        return new AbstractAction("jump sol") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_MASK));
            putValue("shortcut_name", "ctrl+u");}
            @Override public void actionPerformed(ActionEvent e) {
                input_handler.cursor.setXY(0, input_handler.cursor.getY());
                jpc_connector.repaint();
            }
        };
    }

    public static Action getFunctionality_PAGE_UP(JPC_Scroller scroller, JPC_Connector jpc_connector) {
        return new AbstractAction("page up") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
            putValue("shortcut_name", "ctrl+o");}
            @Override public void actionPerformed(ActionEvent e) {
                System.out.println("up first line: "+jpc_connector.getFirstVisibleLine());
                System.out.println("up last line: "+jpc_connector.getLastVisibleLine());
                scroller.setFirstVisibleLine_asap(jpc_connector.getFirstVisibleLine()-(jpc_connector.getLastVisibleLine()-jpc_connector.getFirstVisibleLine()));
                jpc_connector.repaint();
            }
        };
    }
    public static Action getFunctionality_PAGE_DOWN(JPC_Scroller scroller, JPC_Connector jpc_connector) {
        return new AbstractAction("page down") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
            putValue("shortcut_name", "ctrl+l");}
            @Override public void actionPerformed(ActionEvent e) {
                System.out.println("down last line: "+jpc_connector.getLastVisibleLine());
                scroller.setFirstVisibleLine_asap(jpc_connector.getLastVisibleLine());
                jpc_connector.repaint();
            }
        };
    }



    //WRAPPING EDITOR ONLY

//    public static Action getFunctionality_TOGGLE_WRAPPING(JPC_Connector jpc_connector, WrappingContentEditor content) {
//        return new AbstractAction("toggle wrapping") {
//            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
//                putValue("shortcut_name", "ctrl+w");}
//            @Override public void actionPerformed(ActionEvent e) {
//                content.setLineWrap(!content.isLineWrapEnabled());
//                jpc_connector.repaint();
//            }
//        };
//    }
    public static Action getFunctionality_TOGGLE_WRAPPING(JPCWrappingTextEditor editor) {
        return new AbstractAction("toggle wrapping") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
                putValue("shortcut_name", "ctrl+w");}
            @Override public void actionPerformed(ActionEvent e) {
                editor.setLineWrap(!editor.isLineWrapEnabled());
            }
        };
    }




    //Layout
    public static Action getFunctionality_FONT_SIZE_UP(JPCTextEditor jpc_connector) {
        return new AbstractAction("font size add") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_MASK));
            putValue("shortcut_name", "ctrl+'+'");}
            @Override public void actionPerformed(ActionEvent e) {
                Font current = jpc_connector.getFont();
                jpc_connector.setFont(current.deriveFont(current.getSize()+1f));
            }
        };
    }
    public static Action getFunctionality_FONT_SIZE_DOWN(JPCTextEditor jpc_connector) {
        return new AbstractAction("font size sub") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_MASK));
            putValue("shortcut_name", "ctrl+'-'");}
            @Override public void actionPerformed(ActionEvent e) {
                Font current = jpc_connector.getFont();
                if(current.getSize()>1)
                    jpc_connector.setFont(current.deriveFont(current.getSize()-1f));
            }
        };
    }

    public static Action getFunctionality_FONT_CYCLE_STYLE(JPCTextEditor jpc_connector) {
        return new AbstractAction("font style cycle") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, InputEvent.CTRL_MASK));
            putValue("shortcut_name", "ctrl+'.'");}
            @Override public void actionPerformed(ActionEvent e) {
                Font current = jpc_connector.getFont();
                if(current.isBold() && current.isItalic())
                    jpc_connector.setFont(current.deriveFont(Font.PLAIN));
                else if(current.isPlain())
                    jpc_connector.setFont(current.deriveFont(Font.BOLD));
                else if(current.isBold())
                    jpc_connector.setFont(current.deriveFont(Font.ITALIC));
                else if(current.isItalic())
                    jpc_connector.setFont(current.deriveFont(Font.BOLD | Font.ITALIC));
            }
        };
    }






    public static Action getFunctionality_CYCLE_FG_COLOR(JPCTextEditor jpc_connector) {
        return new AbstractAction("next foreground") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
             putValue("shortcut_name", "ctrl+n");}
            @Override public void actionPerformed(ActionEvent e) {
                Color orig_clr = jpc_connector.getForeground();
                Color nextClr = orig_clr;
                while (!(!Objects.equals(nextClr, orig_clr) && (
                        nextClr == null || (!nextClr.equals(jpc_connector.getForeground()) && !nextClr.equals(jpc_connector.getBackground()))))) {
                    nextClr = getNextColor(nextClr);
                }
                jpc_connector.setForeground(nextClr);
            }
        };
    }
    public static Action getFunctionality_CYCLE_BG_COLOR(JPCTextEditor jpc_connector) {
        return new AbstractAction("next background") {
            {putValue("shortcut", KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_MASK));
             putValue("shortcut_name", "ctrl+m");}
            @Override public void actionPerformed(ActionEvent e) {
                Color orig_clr = jpc_connector.getBackground();
                Color nextClr = orig_clr;
                while (!(!Objects.equals(nextClr, orig_clr) && (
                        nextClr == null || (!nextClr.equals(jpc_connector.getForeground()) && !nextClr.equals(jpc_connector.getBackground()))))) {
                    nextClr = getNextColor(nextClr);
                }
                jpc_connector.setBackground(nextClr);
            }
        };
    }








    //helper

    public static Color getNextColor(Color clr) {
        if(clr == null || clr.equals(new Color(238, 238, 238))) 										return Color.BLACK;
        else if(clr.equals(Color.BLACK)) 			return Color.BLUE;
        else if(clr.equals(Color.BLUE)) 			return Color.CYAN;
        else if(clr.equals(Color.CYAN)) 			return Color.DARK_GRAY;
        else if(clr.equals(Color.DARK_GRAY)) 	return Color.GRAY;
        else if(clr.equals(Color.GRAY)) 			return Color.GREEN;
        else if(clr.equals(Color.GREEN)) 			return Color.LIGHT_GRAY;
        else if(clr.equals(Color.LIGHT_GRAY)) 	return Color.MAGENTA;
        else if(clr.equals(Color.MAGENTA)) 		return Color.ORANGE;
        else if(clr.equals(Color.ORANGE)) 		return Color.PINK;
        else if(clr.equals(Color.PINK)) 				return Color.RED;
        else if(clr.equals(Color.RED)) 				return Color.WHITE;
        else if(clr.equals(Color.WHITE)) 			return Color.YELLOW;
        else/* if(clr.equals(Color.YELLOW))*/ 		return new Color(238, 238, 238);
        //else 													return null;
    }
}