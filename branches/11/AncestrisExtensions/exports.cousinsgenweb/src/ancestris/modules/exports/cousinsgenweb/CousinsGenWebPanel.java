/*
 * CousinsGenWebPanel.java
 *
 * Created on 25 avr. 2011, 19:26:59
 */
package ancestris.modules.exports.cousinsgenweb;

import ancestris.util.swing.FileChooserBuilder;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.io.File;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author dominique
 */
public class CousinsGenWebPanel extends javax.swing.JPanel {

    private final Context context;

    private File file = null;
    private int cityPos = 0;
    private int depLength = 0;
    private int depPos = 0;

    /**
     * Creates new form CousinsGenWebPanel
     */
    public CousinsGenWebPanel(Context context) {
        this.context = context;
        initComponents();
        cityPos = Integer.parseInt(textFieldCityPos.getText());
        depLength = Integer.parseInt(textFieldDepLength.getText());
        depPos = Integer.parseInt(textFieldDepPos.getText());
        // Listen for changes in the text
        textFieldCityPos.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            // text was changed
            public void changedUpdate(DocumentEvent e) {
                try {
                    cityPos = Integer.parseInt(textFieldCityPos.getText());
                    NbPreferences.forModule(CousinsGenWebPanel.class).put("textFieldCityPos", textFieldCityPos.getText());
                } catch (NumberFormatException ex) {
                    textFieldCityPos.setText(NbPreferences.forModule(CousinsGenWebPanel.class).get("textFieldCityPos", "2"));
                    cityPos = Integer.parseInt(textFieldCityPos.getText());
                }
            }

            // text was deleted
            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            // text was inserted
            public void insertUpdate(DocumentEvent e) {
                try {
                    cityPos = Integer.parseInt(textFieldCityPos.getText());
                    NbPreferences.forModule(CousinsGenWebPanel.class).put("textFieldCityPos", textFieldCityPos.getText());
                } catch (NumberFormatException ex) {
                    textFieldCityPos.setText(NbPreferences.forModule(CousinsGenWebPanel.class).get("textFieldCityPos", "1"));
                    cityPos = Integer.parseInt(textFieldCityPos.getText());
                }
            }
        });
        textFieldDepLength.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                try {
                    depLength = Integer.parseInt(textFieldDepLength.getText());
                    NbPreferences.forModule(CousinsGenWebPanel.class).put("textFieldDepLength", textFieldDepLength.getText());
                } catch (NumberFormatException ex) {
                    textFieldDepLength.setText(NbPreferences.forModule(CousinsGenWebPanel.class).get("textFieldDepLength", "0"));
                    depLength = Integer.parseInt(textFieldDepLength.getText());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    depLength = Integer.parseInt(textFieldDepLength.getText());
                    NbPreferences.forModule(CousinsGenWebPanel.class).put("textFieldDepLength", textFieldDepLength.getText());
                } catch (NumberFormatException ex) {
                    textFieldDepLength.setText(NbPreferences.forModule(CousinsGenWebPanel.class).get("textFieldDepLength", "0"));
                    depLength = Integer.parseInt(textFieldDepLength.getText());
                }
            }
        });
        textFieldDepPos.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                try {
                    depPos = Integer.parseInt(textFieldDepPos.getText());
                    NbPreferences.forModule(CousinsGenWebPanel.class).put("textFieldDepPos", textFieldDepPos.getText());
                } catch (NumberFormatException ex) {
                    textFieldDepPos.setText(NbPreferences.forModule(CousinsGenWebPanel.class).get("textFieldDepPos", "2"));
                    depPos = Integer.parseInt(textFieldDepPos.getText());
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    depPos = Integer.parseInt(textFieldDepPos.getText());
                    NbPreferences.forModule(CousinsGenWebPanel.class).put("textFieldDepPos", textFieldDepPos.getText());
                } catch (NumberFormatException ex) {
                    textFieldDepPos.setText(NbPreferences.forModule(CousinsGenWebPanel.class).get("textFieldDepPos", "2"));
                    depPos = Integer.parseInt(textFieldDepPos.getText());
                }
            }
        });
    }

    public int getCityPos() {
        return cityPos;
    }

    public int getDepLength() {
        return depLength;
    }

    public int getDepPos() {
        return depPos;
    }

    public File getFile() {
        return file;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LabelDepPos = new javax.swing.JLabel();
        textFieldDepPos = new javax.swing.JTextField();
        labelCityPos = new javax.swing.JLabel();
        textFieldCityPos = new javax.swing.JTextField();
        labelDepLength = new javax.swing.JLabel();
        textFieldDepLength = new javax.swing.JTextField();
        textFieldFileName = new javax.swing.JTextField();
        labelFileName = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        LabelDepPos.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        LabelDepPos.setText(org.openide.util.NbBundle.getMessage(CousinsGenWebPanel.class, "CousinsGenWebPanel.LabelDepPos.text")); // NOI18N

        textFieldDepPos.setColumns(5);
        textFieldDepPos.setText(NbPreferences.forModule(CousinsGenWebPanel.class).get("textFieldDepPos", "2"));
        textFieldDepPos.setMinimumSize(new java.awt.Dimension(84, 19));

        labelCityPos.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelCityPos.setText(org.openide.util.NbBundle.getMessage(CousinsGenWebPanel.class, "CousinsGenWebPanel.labelCityPos.text")); // NOI18N

        textFieldCityPos.setColumns(5);
        textFieldCityPos.setText(NbPreferences.forModule(CousinsGenWebPanel.class).get("textFieldCityPos", "1"));

        labelDepLength.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelDepLength.setText(org.openide.util.NbBundle.getMessage(CousinsGenWebPanel.class, "CousinsGenWebPanel.labelDepLength.text")); // NOI18N

        textFieldDepLength.setColumns(5);
        textFieldDepLength.setText(NbPreferences.forModule(CousinsGenWebPanel.class).get("textFieldDepLength", "0"));

        textFieldFileName.setColumns(32);
        textFieldFileName.setText(NbPreferences.forModule(CousinsGenWebPanel.class).get("textFieldFileName", ""));
        textFieldFileName.setToolTipText(org.openide.util.NbBundle.getMessage(CousinsGenWebPanel.class, "CousinsGenWebPanel.textFieldFileName.toolTipText")); // NOI18N

        labelFileName.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelFileName.setText(org.openide.util.NbBundle.getMessage(CousinsGenWebPanel.class, "CousinsGenWebPanel.labelFileName.text")); // NOI18N

        jButton1.setText(org.openide.util.NbBundle.getMessage(CousinsGenWebPanel.class, "CousinsGenWebPanel.jButton1.text")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(LabelDepPos)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFieldDepPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelCityPos)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFieldCityPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelDepLength)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFieldDepLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(labelFileName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFieldFileName, javax.swing.GroupLayout.DEFAULT_SIZE, 167, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {LabelDepPos, labelCityPos, labelDepLength, labelFileName});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LabelDepPos)
                    .addComponent(textFieldDepPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelCityPos)
                    .addComponent(textFieldCityPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelDepLength)
                    .addComponent(textFieldDepLength, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelFileName)
                    .addComponent(textFieldFileName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        Gedcom myGedcom = context.getGedcom();
        String gedcomName = removeExtension(myGedcom.getName());

        file = new FileChooserBuilder(CousinsGenWebPanel.class)
                .setDirectoriesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(getClass(), "FileChooserTitle", gedcomName))
                .setApproveText(NbBundle.getMessage(getClass(), "FileChooserOKButton"))
                .setFileHiding(true)
                .showOpenDialog();
        if (file != null) {
            textFieldFileName.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LabelDepPos;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel labelCityPos;
    private javax.swing.JLabel labelDepLength;
    private javax.swing.JLabel labelFileName;
    private javax.swing.JTextField textFieldCityPos;
    private javax.swing.JTextField textFieldDepLength;
    private javax.swing.JTextField textFieldDepPos;
    private javax.swing.JTextField textFieldFileName;
    // End of variables declaration//GEN-END:variables

    private String removeExtension(String filename) {
        String separator = System.getProperty("file.separator");
        
        String retour = filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = retour.lastIndexOf(separator);
        if (lastSeparatorIndex != -1) {
            retour = retour.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = retour.lastIndexOf(".");
        if (extensionIndex == -1) {
            return retour;
        }

        return retour.substring(0, extensionIndex);
    }

}
