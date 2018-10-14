package jokrey.utililities.swing.text_editor.ui.additional;

import jokrey.utililities.swing.text_editor.JPCTextEditor;
import jokrey.utililities.swing.text_editor.text_storage.LayoutStorageSystem;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class LoadStorePanel extends JPanel {
    public LoadStorePanel(JPCTextEditor editor, boolean decodeLayout) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        String file_extension = decodeLayout? LayoutStorageSystem.FILE_ENDING:"txt";
        FileNameExtensionFilter filter;
        if(decodeLayout)
           filter = new FileNameExtensionFilter("JPC Layout Files", LayoutStorageSystem.FILE_ENDING);
        else
            filter = new FileNameExtensionFilter("Text Files", "txt", "text");

        JButton loadB = new JButton("load");
        loadB.addActionListener(e -> {
            JFileChooser jf = new JFileChooser();
            jf.setFileFilter(filter);
            int result = jf.showSaveDialog(this);
            if (result == JOptionPane.OK_OPTION) {
                File f = jf.getSelectedFile();

                try {
                    StringBuilder builder = new StringBuilder();
                    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
                    String str;
                    while ((str = in.readLine()) != null)
                        builder.append(str).append("\n");
                    if(decodeLayout)
                        editor.setText_with_encoded_layout(builder.toString());
                    else
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
            jf.setFileFilter(filter);
            int result = jf.showSaveDialog(this);
            if (result == JOptionPane.OK_OPTION) {
                File f = jf.getSelectedFile();
                f = new File(f.getAbsolutePath() + (f.getName().endsWith("."+file_extension) ? "" : "."+file_extension));

                try {
                    String encoded;
                    if(decodeLayout)
                        encoded = editor.getText_with_encoded_layout();
                    else
                        encoded = editor.getText();
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
