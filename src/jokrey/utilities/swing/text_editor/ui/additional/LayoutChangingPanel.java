package jokrey.utilities.swing.text_editor.ui.additional;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LayoutChangingPanel extends JPanel {
    private final CustomEditorConnector editorCon;
    public LayoutChangingPanel(CustomEditorConnector editorCon) {
        this.editorCon=editorCon;
        initiateDisplay();
    }


    private JLabel fgOptionsPanelLabel;
    private JLabel bgOptionsPanelLabel;
    private JComboBox<String> font_names_Chooser;
    private JTextField font_size_jtf;
    private JComboBox<String> font_style_Chooser;

    boolean block_listeners = false;
    public void updateDisplayValues(Color newFG, Color newBG, Font newFont) {
        if(block_listeners)return;
        block_listeners=true;
        try {
            font_names_Chooser.setSelectedItem(newFont.getFamily());
            font_size_jtf.setText(newFont.getSize() + "");
            font_style_Chooser.setSelectedIndex(newFont.getStyle() == Font.BOLD ? 1 : newFont.getStyle() == Font.ITALIC ? 2 : newFont.getStyle() == (Font.BOLD | Font.ITALIC) ? 3 : 0);
            fgOptionsPanelLabel.setBackground(newFG);
            bgOptionsPanelLabel.setBackground(newBG);
        } catch (IllegalStateException ex) {} //indicates that it was called by a listener to one of the display's. Safe to ignore.
        block_listeners=false;
    }
    
    private void initiateDisplay() {
        setOpaque(false);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        JPanel fontOptionsPanel = new JPanel() {
            @Override public Dimension getPreferredSize() {
                return new Dimension(getWidth()/3, super.getPreferredSize().height);
            }
        };
        fontOptionsPanel.setOpaque(false);
        fontOptionsPanel.setLayout(new BoxLayout(fontOptionsPanel, BoxLayout.X_AXIS));
//        JLabel fontOptionsPanelLabel = new JLabel("Font: ");
//        fontOptionsPanel.add(fontOptionsPanelLabel);
        font_size_jtf = new JTextField("", 2) {
            @Override public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        font_size_jtf.setBorder(BorderFactory.createEmptyBorder());
        font_size_jtf.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {
                if(block_listeners)return;
                try {
                    editorCon.font_changed(editorCon.getCurFont().deriveFont((float) Integer.parseInt(font_size_jtf.getText())));
                } catch(NumberFormatException ex) {} //ok so there is no int in font_size_jtf. Who cares
            }
            @Override public void removeUpdate(DocumentEvent e) {
                if(block_listeners)return;
                try {
                    editorCon.font_changed(editorCon.getCurFont().deriveFont((float) Integer.parseInt(font_size_jtf.getText())));
                } catch(NumberFormatException ex) {} //ok so there is no int in font_size_jtf. Who cares
            }
            @Override public void changedUpdate(DocumentEvent e) {
                if(block_listeners)return;
                try {
                    editorCon.font_changed(editorCon.getCurFont().deriveFont((float) Integer.parseInt(font_size_jtf.getText())));
                } catch(NumberFormatException ex) {} //ok so there is no int in font_size_jtf. Who cares
            }
        });
        fontOptionsPanel.add(font_size_jtf);
        font_style_Chooser = new JComboBox<>();
        font_style_Chooser.setModel(new DefaultComboBoxModel<>(new String[]{"Plain","Bold","Italic","Bold-Italic"}));
        font_style_Chooser.addActionListener(ae -> {
            if(block_listeners)return;
            String newStyle_str = font_style_Chooser.getSelectedItem().toString();
            int newStyle = newStyle_str.equals("Bold")?Font.BOLD:newStyle_str.equals("Italic")?Font.ITALIC:newStyle_str.equals("Bold-Italic")?Font.BOLD|Font.ITALIC:Font.PLAIN;
            editorCon.font_changed(editorCon.getCurFont().deriveFont(newStyle));
        });
        font_style_Chooser.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new BasicArrowButton(
                        BasicArrowButton.SOUTH,
                        Color.GRAY, Color.GRAY,
                        Color.DARK_GRAY, Color.GRAY);
                b.setBorder(BorderFactory.createMatteBorder(2, 2, 1, 1, Color.gray));
                return b;
            }
        });
        font_style_Chooser.setFocusable(false);
        font_style_Chooser.setForeground(Color.BLACK);
//        font_style_Chooser.setBackground(prntF.getBackground());
        font_style_Chooser.setRenderer(new BasicComboBoxRenderer() {
            @Override public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
                setText(value.toString());
                setForeground(Color.LIGHT_GRAY);
                setBackground(isSelected?Color.DARK_GRAY.darker():Color.DARK_GRAY);
                return this;
            }
        });
        fontOptionsPanel.add(font_style_Chooser);
        font_names_Chooser = new JComboBox<>();
        font_names_Chooser.setModel(new DefaultComboBoxModel<>(getAvailableFontFamilies()));
        font_names_Chooser.addActionListener(ae -> {
            if(block_listeners)return;
            Font cur_font = editorCon.getCurFont();
            int pre_style = cur_font.getStyle();
            int pre_size = cur_font.getSize();
            editorCon.font_changed(new Font(font_names_Chooser.getSelectedItem().toString(), pre_style, pre_size));
        });
        font_names_Chooser.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new BasicArrowButton(
                        BasicArrowButton.SOUTH,
                        Color.GRAY, Color.GRAY,
                        Color.DARK_GRAY, Color.GRAY);
                b.setBorder(BorderFactory.createMatteBorder(2, 2, 1, 1, Color.gray));
                return b;
            }
        });
        font_names_Chooser.setFocusable(false);
        font_names_Chooser.setForeground(Color.BLACK);
//        font_names_Chooser.setBackground(prntF.getBackground());
        font_names_Chooser.setRenderer(new BasicComboBoxRenderer() {
            @Override public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
                setText(value.toString());
                setForeground(Color.LIGHT_GRAY);
                setBackground(isSelected?Color.DARK_GRAY.darker():Color.DARK_GRAY);
                return this;
            }
        });
        fontOptionsPanel.add(font_names_Chooser);
        add(fontOptionsPanel);

        fgOptionsPanelLabel = new JLabel(" :Foreground: ");
        fgOptionsPanelLabel.setOpaque(true);
        fgOptionsPanelLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if(block_listeners)return;

                Color cur_fg = editorCon.getCurFG();
                if(SwingUtilities.isRightMouseButton(e)) {
                    Color fg = JColorChooser.showDialog(LayoutChangingPanel.this,  "Choose Foreground Color", cur_fg);
                    fgOptionsPanelLabel.setBackground(fg);
                    editorCon.fg_changed(fg);//               } else if(SwingUtilities.isRightMouseButton(e)) {
//                    textDisplay.setCurrentInsertLayout(textDisplay.user.cursor.getInsertLayout().copy_ChangeFG(COLOR_UTIL.getComplimentColor(cur_fg)));
                } else if(SwingUtilities.isMiddleMouseButton(e))
                    editorCon.fg_changed(null);
            }
        });
        add(fgOptionsPanelLabel);
        bgOptionsPanelLabel = new JLabel(" :Background: ");
        bgOptionsPanelLabel.setOpaque(true);
        bgOptionsPanelLabel.addMouseListener(new MouseAdapter() {
            @Override	public void mouseClicked(MouseEvent e) {
                if(block_listeners)return;
                Color cur_bg = editorCon.getCurBG();
                if(SwingUtilities.isRightMouseButton(e)) {
                    Color bg = JColorChooser.showDialog(LayoutChangingPanel.this,  "Choose Background Color", cur_bg);
                    bgOptionsPanelLabel.setBackground(bg);
                    editorCon.bg_changed(bg);
//               } else if(SwingUtilities.isRightMouseButton(e)) {
//                    textDisplay.setCurrentInsertLayout(textDisplay.user.cursor.getInsertLayout().copy_ChangeFG(COLOR_UTIL.getComplimentColor(cur_fg)));
                } else if(SwingUtilities.isMiddleMouseButton(e))
                    editorCon.bg_changed(null);
            }
        });
        add(bgOptionsPanelLabel);
    }



    public static String[] getAvailableFontFamilies() {
        GraphicsEnvironment e = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = e.getAllFonts(); // Get the fonts
        Set<String> font_names = new HashSet<>();
        for (Font font : fonts) font_names.add(font.getFamily());
        String[] font_names_as_array = font_names.toArray(new String[font_names.size()]);
        Arrays.sort(font_names_as_array);
        return font_names_as_array;
    }
}
