package jokrey.utililities.swing.text_editor.example;

import jokrey.utililities.swing.text_editor.Abstract_JPCTextEditor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class LoadStorePanel extends JPanel {
    public LoadStorePanel(Abstract_JPCTextEditor editor) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        JButton loadB = new JButton("load");
        loadB.addActionListener(e -> {
            JFileChooser jf = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt", "text");
            jf.setFileFilter(filter);
            int result = jf.showSaveDialog(editor);
            if (result == JOptionPane.OK_OPTION) {
                File f = jf.getSelectedFile();

                try {
                    StringBuilder builder = new StringBuilder();
                    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
                    String str;
                    while ((str = in.readLine()) != null)
                        builder.append(str).append("\n");
                    editor.setText(builder.toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        add(loadB);

        JButton storeB = new JButton("store");
        storeB.addActionListener(e -> {
            JFileChooser jf = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt", "text");
            jf.setFileFilter(filter);
            int result = jf.showSaveDialog(editor);
            if (result == JOptionPane.OK_OPTION) {
                File f = jf.getSelectedFile();
                f = new File(f.getAbsolutePath() + (f.getName().endsWith(".txt") ? "" : ".txt"));

                try {
                    String encoded = editor.getText();
                    OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8);
                    writer.write(encoded);
                    writer.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        add(storeB);
    }
}
