/*
 *  Copyright (C) 2011 lemovice
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/*
 * GenenanetExportPanel.java
 *
 * Created on 23 mai 2011, 21:34:49
 */
package org.ancestris.ancestrisexports.geneanet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author dominique
 */
public class GenenanetExportPanel extends javax.swing.JPanel {

    File exportFile = null;
    private String gedcomName = "";
    String exportDirName = "";
    String exportFileName = "";

    /** Creates new form GenenanetExportPanel */
    public GenenanetExportPanel(String gedcomName) {
        this.gedcomName = gedcomName;
        exportDirName = NbPreferences.forModule(GenenanetExportPanel.class).get("Dossier Export " + gedcomName, "");
        exportFileName = NbPreferences.forModule(GenenanetExportPanel.class).get("Fichier Export " + gedcomName, gedcomName + ".gw");
        initComponents();
        exportFile = new File(jTextFieldExportFileName.getText());
        jFormattedTextFieldDuration.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            // text was changed
            public void changedUpdate(DocumentEvent e) {
                NbPreferences.forModule(GenenanetExportPanel.class).put("textFieldCityPos", jFormattedTextFieldDuration.getText());
            }

            // text was deleted
            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            // text was inserted
            public void insertUpdate(DocumentEvent e) {
                try {
                    NbPreferences.forModule(GenenanetExportPanel.class).put("textFieldCityPos", jFormattedTextFieldDuration.getText());
                } catch (NumberFormatException ex) {
                    jFormattedTextFieldDuration.setText(NbPreferences.forModule(GenenanetExportPanel.class).get("textFieldCityPos", "1"));
                }
            }
        });

        if (NbPreferences.forModule(GenenanetExportPanel.class).get("ExportRestricited", "true").equals("true")) {
            jCheckBoxExportRestricited.setSelected(true);
        } else {
            jCheckBoxExportRestricited.setSelected(false);
            jFormattedTextFieldDuration.setEnabled(false);
        }

        jFormattedTextFieldDuration.setValue(new Integer(Integer.parseInt(NbPreferences.forModule(GenenanetExportPanel.class).get("RestricitionDuration", "100"))));

        if (NbPreferences.forModule(GenenanetExportPanel.class).get("ExportEvents", "true").equals("true")) {
            jCheckBoxExportEvents.setSelected(true);
        } else {
            jCheckBoxExportEvents.setSelected(false);
        }

        if (NbPreferences.forModule(GenenanetExportPanel.class).get("ExportNotes", "true").equals("true")) {
            jCheckBoxExportNotes.setSelected(true);
        } else {
            jCheckBoxExportNotes.setSelected(false);
        }

        if (NbPreferences.forModule(GenenanetExportPanel.class).get("ExportSources", "true").equals("true")) {
            jCheckBoxExportSources.setSelected(true);
        } else {
            jCheckBoxExportSources.setSelected(false);
        }

        if (NbPreferences.forModule(GenenanetExportPanel.class).get("LogEnable", "false").equals("true")) {
            jCheckBoxLogEnable.setSelected(true);
        } else {
            jCheckBoxLogEnable.setSelected(false);
        }
    }

    public File getFile() {
        return exportFile;
    }

    public boolean isNotesExported() {
        return jCheckBoxExportNotes.isSelected();
    }

    public boolean isSourcesExported() {
        return jCheckBoxExportSources.isSelected();
    }

    public boolean isEventsExported() {
        return jCheckBoxExportEvents.isSelected();
    }

    public boolean isLogEnable() {
        return jCheckBoxLogEnable.isSelected();
    }

    public boolean isExportRestricited() {
        return jCheckBoxLogEnable.isSelected();
    }

    public int getRestrictionYears() {
        return (Integer) jFormattedTextFieldDuration.getValue();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new JPanel();
        jTabbedPaneNotes = new JTabbedPane();
        jPanel1 = new JPanel();
        jCheckBoxExportRestricited = new JCheckBox();
        jLabel1 = new JLabel();
        jFormattedTextFieldDuration = new JFormattedTextField();
        jPanel3 = new JPanel();
        jCheckBoxExportEvents = new JCheckBox();
        jCheckBoxExportSources = new JCheckBox();
        jCheckBoxExportNotes = new JCheckBox();
        jCheckBoxLogEnable = new JCheckBox();
        jLabelExportFileName = new JLabel();
        jTextFieldExportFileName = new JTextField();
        jButtonChooseFile = new JButton();

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 333, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGap(0, 110, Short.MAX_VALUE)
        );

        jCheckBoxExportRestricited.setSelected(true);
        jCheckBoxExportRestricited.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jRadioButtonLess100Yes.text")); // NOI18N
        jCheckBoxExportRestricited.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxExportRestricitedActionPerformed(evt);
            }
        });

        jLabel1.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jLabel1.text")); // NOI18N

        jFormattedTextFieldDuration.setColumns(5);

        jFormattedTextFieldDuration.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0"))));
        jFormattedTextFieldDuration.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jFormattedTextFieldDuration.text")); // NOI18N
        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);

        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxExportRestricited)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jFormattedTextFieldDuration, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jCheckBoxExportRestricited)
                    .addComponent(jFormattedTextFieldDuration, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addContainerGap(77, Short.MAX_VALUE))
        );

        jTabbedPaneNotes.addTab(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jPanel1.TabConstraints.tabTitle"), jPanel1); // NOI18N
        jCheckBoxExportEvents.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jCheckBoxExportEvents.text")); // NOI18N
        jCheckBoxExportEvents.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxExportEventsActionPerformed(evt);
            }
        });

        jCheckBoxExportSources.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jCheckBoxExportSources.text")); // NOI18N
        jCheckBoxExportSources.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxExportSourcesActionPerformed(evt);
            }
        });

        jCheckBoxExportNotes.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jCheckBoxExportNotes.text")); // NOI18N
        jCheckBoxExportNotes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxExportNotesActionPerformed(evt);
            }
        });

        jCheckBoxLogEnable.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jCheckBoxLogEnable.text")); // NOI18N
        jCheckBoxLogEnable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxLogEnableActionPerformed(evt);
            }
        });

        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);


        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jCheckBoxExportEvents)
                    .addComponent(jCheckBoxExportSources)
                    .addComponent(jCheckBoxExportNotes)
                    .addComponent(jCheckBoxLogEnable))
                .addContainerGap(142, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxExportEvents)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jCheckBoxExportSources)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jCheckBoxExportNotes)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jCheckBoxLogEnable)
                .addContainerGap())
        );

        jTabbedPaneNotes.addTab(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jPanel3.TabConstraints.tabTitle"), jPanel3); // NOI18N
        jLabelExportFileName.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jLabelExportFileName.text")); // NOI18N
        jTextFieldExportFileName.setText(exportDirName + System.getProperty("file.separator") + exportFileName);

        jButtonChooseFile.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jButtonChooseFile.text")); // NOI18N
        jButtonChooseFile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButtonChooseFileActionPerformed(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jTabbedPaneNotes, Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabelExportFileName)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jTextFieldExportFileName, GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jButtonChooseFile)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPaneNotes, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabelExportFileName)
                    .addComponent(jTextFieldExportFileName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonChooseFile))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonChooseFileActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jButtonChooseFileActionPerformed
        final FileNameExtensionFilter filter = new FileNameExtensionFilter(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.fileType"), "gw");
        JFileChooser fc = new JFileChooser() {

            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(this, NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.Overwrite.Text"), NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.Overwrite.Title"), JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            super.cancelSelection();
                            return;
                    }
                } else {
                    if (filter.accept(f) == false) {
                        setSelectedFile(new File(f.getName() + ".gw"));
                    }
                }
                super.approveSelection();
            }
        };

        if (exportDirName.length() > 0) {
            // Set the current directory
            fc.setCurrentDirectory(new File(exportDirName));
        }

        fc.setFileFilter(filter);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setSelectedFile(new File(exportFileName));

        if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            exportFile = fc.getSelectedFile();
            // Get current directory
            try {
                exportDirName = fc.getCurrentDirectory().getCanonicalPath();
            } catch (IOException ex) {
                exportDirName = "";
            }
            // save export directory
            NbPreferences.forModule(GenenanetExportPanel.class).put("Dossier Export " + gedcomName, exportDirName);
            NbPreferences.forModule(GenenanetExportPanel.class).put("Fichier Export " + gedcomName, exportFile.getName());
            jTextFieldExportFileName.setText(exportDirName + System.getProperty("file.separator") + exportFile.getName());
        }
    }//GEN-LAST:event_jButtonChooseFileActionPerformed

    private void jCheckBoxExportRestricitedActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jCheckBoxExportRestricitedActionPerformed
        if (jCheckBoxExportRestricited.isSelected() == true) {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportRestricited", "true");
            jFormattedTextFieldDuration.setEnabled(true);
        } else {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportRestricited", "false");
            jFormattedTextFieldDuration.setEnabled(false);
        }
    }//GEN-LAST:event_jCheckBoxExportRestricitedActionPerformed

    private void jCheckBoxExportEventsActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jCheckBoxExportEventsActionPerformed
        if (jCheckBoxExportEvents.isSelected() == true) {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportEvents", "true");
        } else {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportEvents", "false");
        }
    }//GEN-LAST:event_jCheckBoxExportEventsActionPerformed

    private void jCheckBoxExportSourcesActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jCheckBoxExportSourcesActionPerformed
        if (jCheckBoxExportEvents.isSelected() == true) {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportSourcess", "true");
        } else {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportSources", "false");
        }
    }//GEN-LAST:event_jCheckBoxExportSourcesActionPerformed

    private void jCheckBoxExportNotesActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jCheckBoxExportNotesActionPerformed
        if (jCheckBoxExportEvents.isSelected() == true) {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportNotes", "true");
        } else {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportNotess", "false");
        }
    }//GEN-LAST:event_jCheckBoxExportNotesActionPerformed

    private void jCheckBoxLogEnableActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jCheckBoxLogEnableActionPerformed
        if (jCheckBoxExportEvents.isSelected() == true) {
            NbPreferences.forModule(GenenanetExportPanel.class).put("LogEnable", "true");
        } else {
            NbPreferences.forModule(GenenanetExportPanel.class).put("LogEnable", "false");
        }
    }//GEN-LAST:event_jCheckBoxLogEnableActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton jButtonChooseFile;
    private JCheckBox jCheckBoxExportEvents;
    private JCheckBox jCheckBoxExportNotes;
    private JCheckBox jCheckBoxExportRestricited;
    private JCheckBox jCheckBoxExportSources;
    private JCheckBox jCheckBoxLogEnable;
    private JFormattedTextField jFormattedTextFieldDuration;
    private JLabel jLabel1;
    private JLabel jLabelExportFileName;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JTabbedPane jTabbedPaneNotes;
    private JTextField jTextFieldExportFileName;
    // End of variables declaration//GEN-END:variables
}
