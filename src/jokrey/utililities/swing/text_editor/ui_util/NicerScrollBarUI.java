package jokrey.utililities.swing.text_editor.ui_util;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;

/**
 * Better looking, less overblown and blue ScrollBar.
 */
public class NicerScrollBarUI extends BasicScrollBarUI {
	@Override protected JButton createDecreaseButton(int orientation) {
		JButton button = new JButton("zero button");
	    Dimension zeroDim = new Dimension(0,0);
	    button.setPreferredSize(zeroDim);
	    button.setMinimumSize(zeroDim);
	    button.setMaximumSize(zeroDim);
	    return button;
	}

	@Override protected JButton createIncreaseButton(int orientation) {
		JButton button = new JButton("zero button");
	    Dimension zeroDim = new Dimension(0,0);
	    button.setPreferredSize(zeroDim);
	    button.setMinimumSize(zeroDim);
	    button.setMaximumSize(zeroDim);
	    return button;
	}
 
	@Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(c.getBackground());
        ((Graphics2D)g).fill(trackBounds);
    }

    @Override protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        g.setColor(c.getForeground());
        g.translate(thumbBounds.x, thumbBounds.y);
        g.fillRect(0, 0, thumbBounds.width, thumbBounds.height);
        g.translate(-thumbBounds.x, -thumbBounds.y);
    }
}