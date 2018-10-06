package jokrey.utililities.swing.text_editor.ui.util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Util {
    public static Point centerOnMouseScreen(JFrame frame) {
        frame.setLocation(getLocForACenteredInRect(getScreenBoundsAt(MouseInfo.getPointerInfo().getLocation()), frame.getSize()));
        return frame.getLocation();
    }
    public static Point getLocForACenteredInRect(Rectangle r, Dimension size) {
        return new Point(r.x + r.width /2 - size.width /2,
                r.y + r.height/2 - size.height/2);
    }
    public static Rectangle getScreenBoundsAt(Point pos) {
        GraphicsDevice gd = getGraphicsDeviceAt(pos);
        Rectangle bounds = null;

        if (gd != null)
            bounds = gd.getDefaultConfiguration().getBounds();
        return bounds;
    }
    public static GraphicsDevice getGraphicsDeviceAt(Point pos) {
        GraphicsDevice device = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice lstGDs[] = ge.getScreenDevices();
        ArrayList<GraphicsDevice> lstDevices = new ArrayList<>(lstGDs.length);

        for (GraphicsDevice gd : lstGDs) {
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            Rectangle screenBounds = gc.getBounds();
            if (screenBounds.contains(pos)) {
                lstDevices.add(gd);
            }
        }

        if (lstDevices.size() == 1) {
            device = lstDevices.get(0);
        }
        return device;
    }
}
