package jokrey.utilities.swing.text_editor.text_storage;

import jokrey.utilities.swing.text_editor.text_storage.lineparteffects.LPEffect_Fill;

import java.util.Arrays;
import java.util.Iterator;

public class LinePartEffects implements Iterable<LinePartEffect> {
    private final LinePartEffect[] effects;
    public LinePartEffects(LinePartEffect... effects) {
        this.effects = effects;
    }

    public LinePartEffect get(int i) {
        return effects[i];
    }
    public int length() {
        return effects.length;
    }

    @Override public boolean equals(Object o) {
        return o instanceof LinePartEffects && Arrays.equals(effects, ((LinePartEffects)o).effects);
    }
    @Override public String toString() {
        return Arrays.toString(effects);
    }

    @Override public Iterator<LinePartEffect> iterator() {
        return new Iterator<LinePartEffect>() {
            int i = 0;
            @Override public boolean hasNext() {
                return i<length();
            }
            @Override public LinePartEffect next() {
                return get(i++);
            }
        };
    }

    public LinePartEffects copy_set(int i, LinePartEffect new_eff) {
        LinePartEffect[] new_effs = effects.clone();
        new_effs[i] = new_eff;
        return new LinePartEffects(new_effs);
    }

    public LinePartEffects copy_add(LinePartEffect new_eff) {
        LinePartEffect[] new_effs = new LinePartEffect[effects.length+1];
        System.arraycopy(effects, 0, new_effs, 0, effects.length);
        new_effs[effects.length] = new_eff;
        return new LinePartEffects(new_effs);
    }
}
