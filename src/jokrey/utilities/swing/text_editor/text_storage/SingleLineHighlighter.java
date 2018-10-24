package jokrey.utilities.swing.text_editor.text_storage;

import jokrey.utilities.swing.text_editor.JPC_Connector;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Simple, single line highlighting based on the available rulz.
 *
 */
public class SingleLineHighlighter extends StandardContentEditor {
    private final ArrayList<SyntaxRule> rules;
    public SingleLineHighlighter(JPC_Connector c, SyntaxRule... rules) {
        super(c);
        this.rules = new ArrayList<>(Arrays.asList(rules));
    }
    public void addWordMatchRule(LinePartAppearance highlight, String word) {
        rules.add(new WordMatchRule(word, highlight));
    }
    public void addWordMatchRules(LinePartAppearance highlight, String... words) {
        for (String word : words) addWordMatchRule(highlight, word);
    }
    public void addBetweenMatchesRule(LinePartAppearance highlight, String start, String end, String escape) {
        rules.add(new BetweenMatchesRule(start, end, escape, highlight));
    }
    public void addEverythingAfterMatchRule(LinePartAppearance highlight, String match) {
        rules.add(new EverythingAfterMatchRule(match, highlight));
    }
    public void addEverythingAfterMatchExceptInBetweenMatchRule(LinePartAppearance highlight, String after_match, String in_between_match) {
        rules.add(new EverythingAfterMatchExceptInBetweenMatchRule(after_match, in_between_match, highlight));
    }
    public void addWordBeforeMatchRule(LinePartAppearance highlight, String match) {
        rules.add(new WordBeforeMatchRule(match, highlight));
    }
    //override possible::
    public String getWordMatcher() {
        return "[A-Za-z0-9_]+";
    }

    private ArrayList<Line> displayLines = new ArrayList<>();

    @Override public Line[] getDisplayLine(int line_number) {
        if(line_number<displayLines.size())
            return new Line[]{displayLines.get(line_number)};
        else
            return new Line[]{getLine(line_number)};
    }

    @Override public void recalculateDisplayLines()  {
        displayLines.clear();
        recalculateDisplayLine(-1);//will run into else branch and recalculate all
    }

    @Override public void recalculateDisplayLine(int line)  {
//        if (block_recalculateDisplayLines) return;
        if(displayLines.size()==getLineCount()) {
//            for (int i = firstAffectedLine; i <= lastAffectedLine; i++) {
                Line disp_line = getLine(line);
                for(SyntaxRule r:rules)
                    disp_line=r.apply(disp_line); //entails a ton of line copying
                disp_line.updatePixelKnowledge(jpc_connector, getStandardLayout());
                displayLines.set(line, disp_line);
//            }
        } else {
            displayLines = new ArrayList<>(getLineCount());
            for (int i = 0; i < getLineCount(); i++) {
                Line disp_line = getLine(i);
                for(SyntaxRule r:rules)
                    disp_line=r.apply(disp_line); //entails a ton of line copying
                disp_line.updatePixelKnowledge(jpc_connector, getStandardLayout());
                displayLines.add(disp_line);
            }
        }
        jpc_connector.repaint(); //usually not required, but sometimes(and in not easily detectable situations) it is.
    }



    public static abstract class SyntaxRule {
        abstract Line apply(Line line);
    }
    private class WordMatchRule extends SyntaxRule {
        final String word; final LinePartAppearance highlight;
        public WordMatchRule(String word, LinePartAppearance highlight) {
            this.word = word;
            this.highlight = highlight;
        }
        @Override Line apply(Line line) {
            String s = line.toString();
            int index_of_word = -1;
            while ((index_of_word = s.indexOf(word, index_of_word+1)) != -1) {
                if(isFiniteWord(s, index_of_word, index_of_word+word.length()))
                    line = line.overrideLayoutWithin(index_of_word, index_of_word + word.length(), highlight);
            }
            return line;
        }
        private boolean isFiniteWord(String line, int s, int e) {
            return (s<=0 || !Character.toString(line.charAt(s-1)).matches(getWordMatcher())) &&
                    (e>=line.length()-1 || !Character.toString(line.charAt(e)).matches(getWordMatcher()));
        }
    }
    private class BetweenMatchesRule extends SyntaxRule {
        final String start;
        final String end;
        final String escape; //for example in java a " can be escaped with a \
        final LinePartAppearance highlight;
        public BetweenMatchesRule(String start, String end, String escape, LinePartAppearance highlight) {
            this.start = start;
            this.end = end;
            this.escape = escape;
            this.highlight = highlight;
        }
        @Override Line apply(Line line) {
            int start_index = 0;
            int end_index = 0;
            while(true) {
                start_index = line.toString().indexOf(start, end_index);
                while(start_index!=-1 && escape!=null && start_index>escape.length() && line.substring(start_index - escape.length(), start_index).equals(escape)) {//skip escapes
                    start_index = line.toString().indexOf(start, start_index+1);
                }
                end_index = line.toString().indexOf(end, Math.max(end_index, start_index+start.length()));
                while(end_index!=-1 && escape!=null && end_index>escape.length() && line.substring(end_index - escape.length(), end_index).equals(escape)) {//skip escapes
                    end_index = line.toString().indexOf(end, end_index+1);
                }
                if(start_index!=-1 && end_index!=-1) {
                    line = line.overrideLayoutWithin(start_index, end_index+=end.length(), highlight);
                } else
                    break;
            }
            if(start_index!=-1 && end_index == -1)
                line = line.overrideLayoutWithin(start_index, start_index+start.length(), highlight);

            return line;
        }
    }
    private class EverythingAfterMatchRule extends SyntaxRule {
        final String match;
        final LinePartAppearance highlight;
        public EverythingAfterMatchRule(String match, LinePartAppearance highlight) {
            this.match = match;
            this.highlight = highlight;
        }
        @Override Line apply(Line line) {
            int indexOf = line.toString().indexOf(match);
            if(indexOf!=-1)
                line = line.overrideLayoutWithin(indexOf, line.length(), highlight);
            return line;
        }
    }
    private class EverythingAfterMatchExceptInBetweenMatchRule extends SyntaxRule {
        final String after_match;
        final String in_between_match;
        final LinePartAppearance highlight;
        public EverythingAfterMatchExceptInBetweenMatchRule(String after_match, String in_between_match, LinePartAppearance highlight) {
            this.after_match = after_match;
            this.in_between_match = in_between_match;
            this.highlight = highlight;
        }
        @Override Line apply(Line line) {
            int indexOf = line.toString().indexOf(after_match);
            if(indexOf!=-1 && count_occurrences(line.toString(), in_between_match, indexOf) % 2 == 0)
                line = line.overrideLayoutWithin(indexOf, line.length(), highlight);
            return line;
        }
        private int count_occurrences(String string, String occur, int upUntil) {
            int index = string.indexOf(occur);
            int count = 0;
            while (index != -1 && index<upUntil) {
                count++;
                index = string.indexOf(occur, index+occur.length());
            }
            return count;
        }
    }
    private class WordBeforeMatchRule extends SyntaxRule {
        final String match; final LinePartAppearance highlight;
        public WordBeforeMatchRule(String match, LinePartAppearance highlight) {
            this.match = match;
            this.highlight = highlight;
        }
        @Override Line apply(Line line) {
            String s = line.toString();
            int index_of_match = -1;
            while ((index_of_match = s.indexOf(match, index_of_match+1)) != -1) {
                int index_of_word = index_of_match;
                while(index_of_word>=0 && !Character.toString(s.charAt(index_of_word)).matches(getWordMatcher()))
                    index_of_word--;
                int end_index_of_word = index_of_word;
                while(index_of_word>=0 && Character.toString(s.charAt(index_of_word)).matches(getWordMatcher()))
                    index_of_word--;
                int start_index_of_word = index_of_word;
                if(start_index_of_word>=0&&end_index_of_word>0&&start_index_of_word!=end_index_of_word)
                    line = line.overrideLayoutWithin(start_index_of_word, end_index_of_word+1, highlight);
            }
            return line;
        }
    }
}