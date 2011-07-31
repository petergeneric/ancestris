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

import java.awt.Dimension;
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
        if (NbPreferences.forModule(GenenanetExportPanel.class).get("ExportAlive", "false").equals("true")) {
            jCheckBoxExportAlive.setSelected(true);
        } else {
            jCheckBoxExportAlive.setSelected(false);
        }
        if (NbPreferences.forModule(GenenanetExportPanel.class).get("ExportDivorce", "false").equals("true")) {
            jCheckBoxExportDivorce.setSelected(true);
        } else {
            jCheckBoxExportDivorce.setSelected(false);
        }
        if (NbPreferences.forModule(GenenanetExportPanel.class).get("ExportRelations", "false").equals("true")) {
            jCheckBoxExportRelations.setSelected(true);
        } else {
            jCheckBoxExportRelations.setSelected(false);
        }
        if (NbPreferences.forModule(GenenanetExportPanel.class).get("WeddingDetails", "false").equals("true")) {
            jCheckBoxExportWeddingDetails.setSelected(true);
        } else {
            jCheckBoxExportWeddingDetails.setSelected(false);
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
        return jCheckBoxExportRestricited.isSelected();
    }

    public int getRestrictionYears() {
        return (Integer) jFormattedTextFieldDuration.getValue();
    }

    public boolean isAliveExported() {
        return jCheckBoxExportAlive.isSelected();
    }

    public boolean isDivorceExported() {
        return jCheckBoxExportDivorce.isSelected();
    }

    public boolean isRelationsExported() {
        return jCheckBoxExportRelations.isSelected();
    }

    public boolean isWeddingDetailExported() {
        return jCheckBoxExportWeddingDetails.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPaneNotes = new JTabbedPane();
        jPanelGeneral = new JPanel();
        jCheckBoxExportRestricited = new JCheckBox();
        jFormattedTextFieldDuration = new JFormattedTextField();
        jLabel1 = new JLabel();
        jCheckBoxExportNotes = new JCheckBox();
        jCheckBoxExportSources = new JCheckBox();
        jPanelIndis = new JPanel();
        jCheckBoxExportAlive = new JCheckBox();
        jCheckBoxExportEvents = new JCheckBox();
        jCheckBoxExportRelations = new JCheckBox();
        jPanelFam = new JPanel();
        jCheckBoxExportWeddingDetails = new JCheckBox();
        jCheckBoxExportDivorce = new JCheckBox();
        jPanelOther = new JPanel();
        jCheckBoxLogEnable = new JCheckBox();
        jLabelExportFileName = new JLabel();
        jTextFieldExportFileName = new JTextField();
        jButtonChooseFile = new JButton();

        jPanelGeneral.setMinimumSize(new Dimension(526, 99));

        jCheckBoxExportRestricited.setSelected(true);
        jCheckBoxExportRestricited.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jRadioButtonLess100Yes.text")); // NOI18N
        jCheckBoxExportRestricited.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxExportRestricitedActionPerformed(evt);
            }
        });

        jFormattedTextFieldDuration.setColumns(4);


        jFormattedTextFieldDuration.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("#0"))));
        jFormattedTextFieldDuration.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jFormattedTextFieldDuration.text")); // NOI18N
        jLabel1.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jLabel1.text")); // NOI18N
        jCheckBoxExportNotes.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jCheckBoxExportNotes.text")); // NOI18N
        jCheckBoxExportNotes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxExportNotesActionPerformed(evt);
            }
        });

        jCheckBoxExportSources.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jCheckBoxExportSources.text")); // NOI18N
        jCheckBoxExportSources.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxExportSourcesActionPerformed(evt);
            }
        });

        GroupLayout jPanelGeneralLayout = new GroupLayout(jPanelGeneral);
        jPanelGeneral.setLayout(jPanelGeneralLayout);

        jPanelGeneralLayout.setHorizontalGroup(
            jPanelGeneralLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanelGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelGeneralLayout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jCheckBoxExportSources)
                    .addGroup(jPanelGeneralLayout.createSequentialGroup()
                        .addComponent(jCheckBoxExportRestricited, GroupLayout.PREFERRED_SIZE, 320, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextFieldDuration, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jLabel1))
                    .addComponent(jCheckBoxExportNotes))
                .addContainerGap(110, Short.MAX_VALUE))
        );
        jPanelGeneralLayout.setVerticalGroup(
            jPanelGeneralLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanelGeneralLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelGeneralLayout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jCheckBoxExportRestricited)
                    .addComponent(jLabel1)
                    .addComponent(jFormattedTextFieldDuration, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxExportNotes)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxExportSources)
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jTabbedPaneNotes.addTab(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jPanelGeneral.TabConstraints.tabTitle"), jPanelGeneral); // NOI18N
        jPanelIndis.setMinimumSize(new Dimension(526, 99));

        jCheckBoxExportAlive.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jCheckBoxExportAlive.text")); // NOI18N
        jCheckBoxExportAlive.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxExportAliveActionPerformed(evt);
            }
        });

        jCheckBoxExportEvents.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jCheckBoxExportEvents.text")); // NOI18N
        jCheckBoxExportEvents.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxExportEventsActionPerformed(evt);
            }
        });

        jCheckBoxExportRelations.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jCheckBoxExportRelations.text")); // NOI18N
        jCheckBoxExportRelations.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxExportRelationsActionPerformed(evt);
            }
        });

        GroupLayout jPanelIndisLayout = new GroupLayout(jPanelIndis);
        jPanelIndis.setLayout(jPanelIndisLayout);

        jPanelIndisLayout.setHorizontalGroup(
            jPanelIndisLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanelIndisLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelIndisLayout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jCheckBoxExportAlive)
                    .addComponent(jCheckBoxExportEvents)
                    .addComponent(jCheckBoxExportRelations))
                .addContainerGap(12, Short.MAX_VALUE))
        );
        jPanelIndisLayout.setVerticalGroup(
            jPanelIndisLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanelIndisLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxExportAlive)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxExportEvents)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxExportRelations)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        jTabbedPaneNotes.addTab(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jPanelIndis.TabConstraints.tabTitle"), jPanelIndis); // NOI18N
        jPanelFam.setMinimumSize(new Dimension(526, 99));

        jCheckBoxExportWeddingDetails.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jCheckBoxExportWeddingDetails.text")); // NOI18N
        jCheckBoxExportWeddingDetails.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxExportWeddingDetailsActionPerformed(evt);
            }
        });

        jCheckBoxExportDivorce.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jCheckBoxExportDivorce.text")); // NOI18N
        jCheckBoxExportDivorce.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxExportDivorceActionPerformed(evt);
            }
        });

        GroupLayout jPanelFamLayout = new GroupLayout(jPanelFam);
        jPanelFam.setLayout(jPanelFamLayout);

        jPanelFamLayout.setHorizontalGroup(
            jPanelFamLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanelFamLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelFamLayout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jCheckBoxExportWeddingDetails)
                    .addComponent(jCheckBoxExportDivorce))
                .addContainerGap(271, Short.MAX_VALUE))
        );
        jPanelFamLayout.setVerticalGroup(
            jPanelFamLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanelFamLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxExportWeddingDetails)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxExportDivorce)
                .addContainerGap(41, Short.MAX_VALUE))
        );

        jTabbedPaneNotes.addTab(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jPanelFam.TabConstraints.tabTitle"), jPanelFam); // NOI18N
        jPanelOther.setMinimumSize(new Dimension(526, 99));

        jCheckBoxLogEnable.setText(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jCheckBoxLogEnable.text")); // NOI18N
        jCheckBoxLogEnable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jCheckBoxLogEnableActionPerformed(evt);
            }
        });

        GroupLayout jPanelOtherLayout = new GroupLayout(jPanelOther);
        jPanelOther.setLayout(jPanelOtherLayout);


        jPanelOtherLayout.setHorizontalGroup(
            jPanelOtherLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanelOtherLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxLogEnable)
                .addContainerGap(301, Short.MAX_VALUE))
        );
        jPanelOtherLayout.setVerticalGroup(
            jPanelOtherLayout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanelOtherLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBoxLogEnable)
                .addContainerGap(68, Short.MAX_VALUE))
        );

        jTabbedPaneNotes.addTab(NbBundle.getMessage(GenenanetExportPanel.class, "GenenanetExportPanel.jPanelOther.TabConstraints.tabTitle"), jPanelOther); // NOI18N
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
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabelExportFileName)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jTextFieldExportFileName, GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(jButtonChooseFile))
                    .addComponent(jTabbedPaneNotes, Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPaneNotes, GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabelExportFileName)
                    .addComponent(jTextFieldExportFileName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonChooseFile))
                .addContainerGap())
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
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportNotes", "false");
        }
    }//GEN-LAST:event_jCheckBoxExportNotesActionPerformed

    private void jCheckBoxExportRelationsActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jCheckBoxExportRelationsActionPerformed
        if (jCheckBoxExportRelations.isSelected() == true) {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportRelations", "true");
        } else {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportRelations", "false");
        }
    }//GEN-LAST:event_jCheckBoxExportRelationsActionPerformed

    private void jCheckBoxExportWeddingDetailsActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jCheckBoxExportWeddingDetailsActionPerformed
        if (jCheckBoxExportWeddingDetails.isSelected() == true) {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportWeddingDetails", "true");
        } else {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportWeddingDetails", "false");
        }
    }//GEN-LAST:event_jCheckBoxExportWeddingDetailsActionPerformed

    private void jCheckBoxExportAliveActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jCheckBoxExportAliveActionPerformed
        if (jCheckBoxExportAlive.isSelected() == true) {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportAlive", "true");
        } else {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportAlive", "false");
        }
    }//GEN-LAST:event_jCheckBoxExportAliveActionPerformed

    private void jCheckBoxExportDivorceActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jCheckBoxExportDivorceActionPerformed
        if (jCheckBoxExportDivorce.isSelected() == true) {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportDivorce", "true");
        } else {
            NbPreferences.forModule(GenenanetExportPanel.class).put("ExportDivorce", "false");
        }
    }//GEN-LAST:event_jCheckBoxExportDivorceActionPerformed

    private void jCheckBoxLogEnableActionPerformed(ActionEvent evt) {//GEN-FIRST:event_jCheckBoxLogEnableActionPerformed
        if (jCheckBoxExportEvents.isSelected() == true) {
            NbPreferences.forModule(GenenanetExportPanel.class).put("LogEnable", "true");
        } else {
            NbPreferences.forModule(GenenanetExportPanel.class).put("LogEnable", "false");
        }
}//GEN-LAST:event_jCheckBoxLogEnableActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton jButtonChooseFile;
    private JCheckBox jCheckBoxExportAlive;
    private JCheckBox jCheckBoxExportDivorce;
    private JCheckBox jCheckBoxExportEvents;
    private JCheckBox jCheckBoxExportNotes;
    private JCheckBox jCheckBoxExportRelations;
    private JCheckBox jCheckBoxExportRestricited;
    private JCheckBox jCheckBoxExportSources;
    private JCheckBox jCheckBoxExportWeddingDetails;
    private JCheckBox jCheckBoxLogEnable;
    private JFormattedTextField jFormattedTextFieldDuration;
    private JLabel jLabel1;
    private JLabel jLabelExportFileName;
    private JPanel jPanelFam;
    private JPanel jPanelGeneral;
    private JPanel jPanelIndis;
    private JPanel jPanelOther;
    private JTabbedPane jTabbedPaneNotes;
    private JTextField jTextFieldExportFileName;
    // End of variables declaration//GEN-END:variables
}
