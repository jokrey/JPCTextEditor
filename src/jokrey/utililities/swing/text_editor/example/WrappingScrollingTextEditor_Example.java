package jokrey.utililities.swing.text_editor.example;

import jokrey.utililities.swing.text_editor.ui.JPCSimpleWrappingTextEditor;
import jokrey.utililities.swing.text_editor.ui.core.JPC_Scroller;
import jokrey.utililities.swing.text_editor.user_input.ContextFunctionalityLibrary;

import java.awt.*;

public class WrappingScrollingTextEditor_Example {
    public static void main(String[] args) {
        JPCSimpleWrappingTextEditor textDisplay = new JPCSimpleWrappingTextEditor();
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

        FrameStarter.start("JPC Editor - this edition wraps and scrolls", scroller);
    }
}