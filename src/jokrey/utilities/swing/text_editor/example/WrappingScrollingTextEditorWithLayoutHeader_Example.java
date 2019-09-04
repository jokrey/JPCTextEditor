package jokrey.utilities.swing.text_editor.example;

import jokrey.utilities.swing.text_editor.text_storage.ContentListener;
import jokrey.utilities.swing.text_editor.ui.JPCSimpleWrappingTextEditor;
import jokrey.utilities.swing.text_editor.ui.additional.CustomEditorConnector;
import jokrey.utilities.swing.text_editor.ui.additional.LayoutChangingPanel;
import jokrey.utilities.swing.text_editor.ui.additional.LoadStorePanel;
import jokrey.utilities.swing.text_editor.ui.core.JPC_Scroller;
import jokrey.utilities.swing.text_editor.user_input.ContextFunctionalityLibrary;

import javax.swing.*;
import java.awt.*;

/**
 * Example of a wrapping text editor.
 * This example allows customizing the layout for the entire text at once.
 */
public class WrappingScrollingTextEditorWithLayoutHeader_Example extends JPanel {
    public static void main(String[] args) {
        FrameStarter.start("JPC Editor - this edition wraps, scrolls, has one for all layout", new WrappingScrollingTextEditorWithLayoutHeader_Example());
    }

    private JPCSimpleWrappingTextEditor textDisplay;
    public WrappingScrollingTextEditorWithLayoutHeader_Example() {
        setLayout(new BorderLayout());
        JCheckBox wrappingToggleButton = new JCheckBox("wrapping on", true);
        wrappingToggleButton.addActionListener(e -> textDisplay.setLineWrap(wrappingToggleButton.isSelected()));

        LayoutChangingPanel headerPanel = new LayoutChangingPanel(new CustomEditorConnector() {
            @Override public Color getCurFG() {
                return textDisplay.getForeground();
            }
            @Override public Color getCurBG() {
                return textDisplay.getBackground();
            }
            @Override public Font getCurFont() {
                return textDisplay.getFont();
            }
            @Override public void fg_changed(Color fg) {
                if(fg!=null)
                    textDisplay.setForeground(fg);
            }
            @Override public void bg_changed(Color bg) {
                if(bg!=null)
                    textDisplay.setBackground(bg);
            }
            @Override public void font_changed(Font font) {
                if(font!=null)
                    textDisplay.setFont(font);
            }
        });

        textDisplay = new JPCSimpleWrappingTextEditor() {
            @Override public void setLineWrap(boolean wrap_lines) {
                super.setLineWrap(wrap_lines);
                wrappingToggleButton.setSelected(wrap_lines);
            }
        };
        textDisplay.addContentListener(new ContentListener() {
            @Override public void standardLayoutChanged() {
                if(textDisplay==null)return;
                headerPanel.updateDisplayValues(
                        textDisplay.getForeground(),
                        textDisplay.getBackground(),
                        textDisplay.getFont());
                wrappingToggleButton.setSelected(textDisplay.isLineWrapEnabled());
            }
        });
        JPC_Scroller scroller = new JPC_Scroller(textDisplay);

        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_TOGGLE_WRAPPING(textDisplay));

        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_TOGGLE_CAPITALIZE_WORD(textDisplay.getInputHandler()));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_JUMP_TO_END_OF_LINE(textDisplay, textDisplay.getInputHandler()));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_JUMP_TO_START_OF_LINE(textDisplay, textDisplay.getInputHandler()));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_PAGE_UP(scroller, textDisplay));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_PAGE_DOWN(scroller, textDisplay));

        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_FONT_SIZE_UP(textDisplay));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_FONT_SIZE_DOWN(textDisplay));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_FONT_CYCLE_STYLE(textDisplay));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_CYCLE_FG_COLOR(textDisplay));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_CYCLE_BG_COLOR(textDisplay));
        textDisplay.addContextAction(ContextFunctionalityLibrary.getFunctionality_FONT_CYCLE(textDisplay));

        add(scroller);


        headerPanel.add(wrappingToggleButton);
        headerPanel.add(new LoadStorePanel(textDisplay,false));

        add(headerPanel, BorderLayout.NORTH);

        textDisplay.setBackground(headerPanel.getBackground());
        textDisplay.setFont(new Font("Arial", Font.BOLD, 13));
        textDisplay.setText("Lorem ipsum dolor sit amet, dolore imperdiet inciderint mei te. Cum no debet splendide, quod noster quaeque in pro. Te quo congue delicatissimi, ea sed fabellas sensibus forensibus. Nullam iuvaret cu mel, sed harum senserit ullamcorper eu. Ne his consequat sadipscing, impedit accumsan perpetua mea an, at consul invidunt vix. Eum te legere albucius dissentiet, no nemore lucilius nam, tota fugit electram an his.\n" +
                "\n" +
                "In dicant dignissim sit, an vis brute verear. Te primis malorum delectus vim, in nemore delenit instructior usu. Ea sed munere ignota. At pro malorum necessitatibus, vim alia persecuti te. Ubique tempor at eos. Quaeque perfecto menandri usu cu, vix ut utinam euismod vituperatoribus, et ius clita possim discere. Mel altera recusabo an, mei zril laudem blandit ea, molestie delectus in sea.\n" +
                "\n" +
                "Usu solum efficiendi te, ut est nemore placerat singulis. Esse aperiam in est. Semper indoctum incorrupte pri ne. An dico wisi erat cum, ignota appellantur quo ex.\n" +
                "\n" +
                "Vis ex discere dissentiunt, error detraxit forensibus at vim, usu alia nostrum nominavi no. Eu est prima facer ocurreret. Per an populo sanctus persecuti, per cu alienum expetenda signiferumque. Est no delenit accusamus reformidans, ubique praesent vulputate pro an. Cu meliore facilisis ius, id paulo quaeque atomorum ius. Conceptam reformidans cu nec, aliquam nonumes mea et.\n" +
                "\n" +
                "Ceteros moderatius reformidans ius an, brute pertinax cu eos. Zril possit appetere te cum, id vel regione tractatos. Eos ut omittantur voluptatibus, ea vix atqui veritus. Dictas intellegam in usu, dictas dicunt eleifend qui cu. Cum ea brute everti, nec ex iisque dissentiunt. Has fabulas accusata ex.");
    }
}
