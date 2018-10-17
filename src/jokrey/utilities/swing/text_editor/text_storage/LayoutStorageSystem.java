package jokrey.utilities.swing.text_editor.text_storage;

import jokrey.utilities.encoder.string.LIse;
import jokrey.utilities.encoder.string.UniversalStringEncoder;

import java.awt.*;
import java.util.List;

/**
 * Provides a text encoding system to encode and subsequently restore the text and it's layout.
 * Useful for layout-text file storage.
 *   Hence there is a suggestion for a FILE_ENDING
 */
public class LayoutStorageSystem {
    public static final String FILE_ENDING = "jpc";

    public static String getStoredText(List<Line> rawLines, LinePartLayout.Instantiated fallback) {
        LIse lise_lines = new LIse();
        for(Line line:rawLines) {
            LIse lise_lps = new LIse();
            for (int part_i=0;part_i<line.partCount();part_i++) {
                LinePart linepart = line.getPart(part_i);

                UniversalStringEncoder ase_linepart = new UniversalStringEncoder();
                ase_linepart.addEntry("txt", linepart.txt);
                UniversalStringEncoder ase_layout = new UniversalStringEncoder();
                LinePartLayout.Instantiated valid = LinePartLayout.valid(linepart.layout, fallback);
                ase_layout.addEntry("fg",
                        valid.fg.getRed(), valid.fg.getGreen(),
                        valid.fg.getBlue(), valid.fg.getAlpha());
                ase_layout.addEntry("bg",
                        valid.bg.getRed(), valid.bg.getGreen(),
                        valid.bg.getBlue(), valid.bg.getAlpha());
                ase_layout.addEntry("font_name", valid.font.getName());
                ase_layout.addEntry("font_style", valid.font.getStyle());
                ase_layout.addEntry("font_size", valid.font.getSize());
                ase_linepart.addEntry("layout", ase_layout.getEncodedString());

                lise_lps.li_encode_single(ase_linepart.getEncodedString());
            }

            lise_lines.li_encode_single(lise_lps.getEncodedString());
        }
        return lise_lines.getEncodedString();
    }

    public static Line[] restoreFrom(String encodedText, LinePartLayout.Instantiated fallback) {
        LIse lise_lines = new LIse(encodedText);
        String[] lines_encoded = lise_lines.li_decode_all();
        Line[] lines_decoded = new Line[lines_encoded.length];

        for(int i=0;i<lines_encoded.length;i++) {
            LIse lise_lps = new LIse(lines_encoded[i]);
            String[] lps_encoded = lise_lps.li_decode_all();
            LinePart[] lps_restored = new LinePart[lps_encoded.length];

            for (int lp_i = 0; lp_i < lps_encoded.length; lp_i++) {
                UniversalStringEncoder ase_linepart = new UniversalStringEncoder(lps_encoded[lp_i]);
                String layout_encoded = ase_linepart.getEntry("layout");
                if (layout_encoded != null) {
                    UniversalStringEncoder ase_layout = new UniversalStringEncoder(layout_encoded);
                    int[] fg_a = ase_layout.getEntry_intArray("fg", new int[0]);
                    Color fg = fg_a.length == 0 ? null : new Color(fg_a[0], fg_a[1], fg_a[2], fg_a[3]);
                    int[] bg_a = ase_layout.getEntry_intArray("bg", new int[0]);
                    Color bg = bg_a.length == 0 ? null : new Color(bg_a[0], bg_a[1], bg_a[2], bg_a[3]);
                    String font_name = ase_layout.getEntry("font_name");
                    Font font = font_name == null ? null : new Font(font_name, ase_layout.getEntry_int("font_style", Font.BOLD), ase_layout.getEntry_int("font_size", 12));

                    lps_restored[lp_i] = new LinePart(ase_linepart.getEntry("txt"), new LinePartLayout.UnInstantiated(fg, bg, font));
                } else {
                    lps_restored[lp_i] = new LinePart(ase_linepart.getEntry("txt"), fallback);
                }
            }

            lines_decoded[i] = new Line(lps_restored);
        }
        return lines_decoded;
    }
}
