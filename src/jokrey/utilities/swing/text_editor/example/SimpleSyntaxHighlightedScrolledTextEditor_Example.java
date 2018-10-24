package jokrey.utilities.swing.text_editor.example;

import jokrey.utilities.swing.text_editor.JPC_Connector;
import jokrey.utilities.swing.text_editor.text_storage.ContentEditor;
import jokrey.utilities.swing.text_editor.text_storage.LinePartAppearance;
import jokrey.utilities.swing.text_editor.text_storage.SingleLineHighlighter;
import jokrey.utilities.swing.text_editor.ui.JPCSimpleTextEditor;
import jokrey.utilities.swing.text_editor.ui.core.JPC_Scroller;
import jokrey.utilities.swing.text_editor.user_input.ContextFunctionalityLibrary;

import java.awt.*;

public class SimpleSyntaxHighlightedScrolledTextEditor_Example {
    public static void main(String[] args) {
        JPCSimpleTextEditor textDisplay = new JPCSimpleTextEditor() {
            @Override public ContentEditor createContentEditor(JPC_Connector c) {
                //for java.
                //what doesn't work::
                //    1 this for example:: if(s.contains("/*")                            // /* itself will be grey
                //    2 this for example:: if(s.contains("/*") && bs.contains("*/")) {}   ////everything between /* and */ will be greyed out
                //    ((3 multi line comments(not really supposed to work either).
                //    ((4 braces highlighting ((that actually doesn't even work with this idea, since recalculateDisplayLines isn't called for that
                SingleLineHighlighter lighter = new SingleLineHighlighter(c);
                lighter.addWordBeforeMatchRule(new LinePartAppearance.UnInstantiated(Color.MAGENTA.darker().darker(), null, null, null), "(");
                lighter.addWordMatchRules(new LinePartAppearance.UnInstantiated(Color.blue, null, null, null),
                        "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double",
                        "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface",
                        "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized",
                        "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "false", "null", "true");
                lighter.addBetweenMatchesRule(new LinePartAppearance.UnInstantiated(Color.ORANGE.darker(), null, null, null), "\"", "\"", "\\");
                lighter.addBetweenMatchesRule(new LinePartAppearance.UnInstantiated(Color.ORANGE.darker(), null, null, null), "\'", "\'", "\\");
                lighter.addEverythingAfterMatchExceptInBetweenMatchRule(new LinePartAppearance.UnInstantiated(new Color(160,160,160), null, null, null), "//", "\"");
                lighter.addBetweenMatchesRule(new LinePartAppearance.UnInstantiated(new Color(160,160,160), null, null, null), "/*", "*/", null);
                return lighter;
            }
        };
        JPC_Scroller scroller = new JPC_Scroller(textDisplay);

        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_FONT_SIZE_UP(textDisplay));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_FONT_SIZE_DOWN(textDisplay));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_FONT_CYCLE_STYLE(textDisplay));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_CYCLE_FG_COLOR(textDisplay));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_CYCLE_BG_COLOR(textDisplay));

        textDisplay.setFont(new Font("Arial", Font.BOLD, 13));

        FrameStarter.start("JPC Editor - this edition scrolls and highlights simplest java syntax", scroller);
    }
}
