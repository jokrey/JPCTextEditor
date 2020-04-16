package jokrey.utilities.swing.text_editor.text_storage;

import jokrey.utilities.encoder.as_union.li.string.LIse;
import jokrey.utilities.encoder.tag_based.implementation.paired.length_indicator.string.LITagStringEncoder;
import jokrey.utilities.swing.text_editor.text_storage.lineparteffects.LPEffect_Fill;

import java.awt.*;
import java.util.List;

/**
 * Provides a text encoding system to encode and subsequently restore the text and it's layout.
 * Useful for layout-text file storage.
 *   Hence there is a suggestion for a FILE_ENDING
 */
public class LayoutStorageSystem {
    public static final String FILE_ENDING = "jpc";

    public static String getStoredText(List<Line> rawLines, LinePartAppearance.Instantiated fallback) {
        LIse lise_lines = new LIse();
        for(Line line:rawLines) {
            LIse lise_lps = new LIse();
            for (int part_i=0;part_i<line.partCount();part_i++) {
                LinePart linepart = line.getPart(part_i);

                LITagStringEncoder ase_linepart = new LITagStringEncoder();
                ase_linepart.addEntryT("txt", linepart.txt);
                LITagStringEncoder ase_layout = new LITagStringEncoder();
                LinePartAppearance.Instantiated valid = LinePartAppearance.valid(linepart.layout, fallback);
                ase_layout.addEntryT("fg", new int[] {valid.fg.getRed(), valid.fg.getGreen(), valid.fg.getBlue(), valid.fg.getAlpha()});
                Color bg = valid.getBG_canbenull();
                if(bg!=null)
                    ase_layout.addEntryT("bg", new int[] {bg.getRed(), bg.getGreen(), bg.getBlue(), bg.getAlpha()});
                ase_layout.addEntryT("font_name", valid.font.getName());
                ase_layout.addEntryT("font_style", valid.font.getStyle());
                ase_layout.addEntryT("font_size", valid.font.getSize());
                ase_linepart.addEntryT("layout", ase_layout.getEncodedString());

                lise_lps.encode(ase_linepart.getEncodedString());
            }

            lise_lines.encode(lise_lps.getEncodedString());
        }
        return lise_lines.getEncodedString();
    }

    public static Line[] restoreFrom(String encodedText, LinePartAppearance.Instantiated fallback) {
        LIse lise_lines = new LIse(encodedText);
        String[] lines_encoded = lise_lines.decodeAll();
        Line[] lines_decoded = new Line[lines_encoded.length];

        for(int i=0;i<lines_encoded.length;i++) {
            LIse lise_lps = new LIse(lines_encoded[i]);
            String[] lps_encoded = lise_lps.decodeAll();
            LinePart[] lps_restored = new LinePart[lps_encoded.length];

            for (int lp_i = 0; lp_i < lps_encoded.length; lp_i++) {
                LITagStringEncoder ase_linepart = new LITagStringEncoder(lps_encoded[lp_i]);
                String layout_encoded = ase_linepart.getEntry("layout");
                if (layout_encoded != null) {
                    LITagStringEncoder ase_layout = new LITagStringEncoder(layout_encoded);
                    int[] fg_a = ase_layout.getEntryT("fg", new int[0]);
                    Color fg = fg_a==null||fg_a.length == 0 ? null : new Color(fg_a[0], fg_a[1], fg_a[2], fg_a[3]);
                    int[] bg_a = ase_layout.getEntryT("bg", new int[0]);
                    Color bg = bg_a==null||bg_a.length == 0 ? null : new Color(bg_a[0], bg_a[1], bg_a[2], bg_a[3]);
                    String font_name = ase_layout.getEntry("font_name");
                    Font font = font_name == null ? null : new Font(font_name, ase_layout.getEntryT("font_style", Font.BOLD), ase_layout.getEntryT("font_size", 12));

                    //todo - correctly handle effects....
                    lps_restored[lp_i] = new LinePart(ase_linepart.getEntry("txt"), new LinePartAppearance.UnInstantiated(fg, font, new LinePartEffects(new LPEffect_Fill(bg)), null));
                } else {
                    lps_restored[lp_i] = new LinePart(ase_linepart.getEntry("txt"), fallback);
                }
            }

            lines_decoded[i] = new Line(lps_restored);
        }
        return lines_decoded;
    }
}
