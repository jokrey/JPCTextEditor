package jokrey.utilities.swing.text_editor.text_storage;

import java.awt.*;

public class LinePrefix {
	public final String prefix;
	public final Color bg;
	public final Color fg;
	public LinePrefix(String prefix, Color bg, Color fg){
		this.prefix = prefix;
		this.bg = bg;
		this.fg = fg;
	}

	public void draw(Graphics2D g, int yDrawPos, int lineH, Color cursorBG, int prefixW, boolean cursor_line) {
		if(prefixW<=0)return;
    	if(bg!=null){
    		g.setColor(cursor_line?(cursorBG==null?Color.BLUE:cursorBG):bg);
    		g.fillRect(0, (yDrawPos-lineH),prefixW, lineH);
    	}
    	if(fg!=null)
    		g.setColor(fg);
        while(g.getFontMetrics().stringWidth(prefix) > prefixW || g.getFontMetrics().getHeight() > lineH)
            g.setFont(g.getFont().deriveFont((float) (g.getFont().getSize()-1.0)));
        g.drawString(prefix, 0, (float) ((yDrawPos-lineH)+g.getFontMetrics().getHeight()*0.66));
	}
}