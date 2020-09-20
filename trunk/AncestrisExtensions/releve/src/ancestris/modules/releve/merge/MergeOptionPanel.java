/*
 * MergeOptionPanel.java
 *
 * Created on 23 févr. 2014, 11:27:52
 */

package ancestris.modules.releve.merge;

import ancestris.gedcom.GedcomDirectory;
import ancestris.modules.releve.model.PlaceFormatModel;
import ancestris.modules.releve.model.PlaceFormatModel.RecordJuridiction;
import genj.gedcom.Context;
import genj.gedcom.GedcomOptions;
import genj.gedcom.Source;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.ListModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michel
 */
public class MergeOptionPanel extends javax.swing.JPanel {

    private File currentFile; 
    private Frame parent;
    private GedcomFormatModel gedcomFormatModel;
    private int currentGedcomIndex;
    private final HashMap<RecordJuridiction,JComboBox<String>> comboxBoxMap = new HashMap<RecordJuridiction,JComboBox<String>> ();
    
    /** Creates new form MergeOptionPanel */
    public MergeOptionPanel() {
        initComponents();
        
    }

    public void initData(java.awt.Frame parent, File  currentFile) {
        this.parent = parent;
        this.currentFile = currentFile;

        // liste des formats des fichiers gedcom 
        PlaceFormatModel placeFormatModel = PlaceFormatModel.getModel();
        gedcomFormatModel = new GedcomFormatModel(placeFormatModel);
        currentGedcomIndex = 0;
        jListGedcomFile.setModel(gedcomFormatModel);
        
        
        ItemListener comboBoxItemListener = new ItemListener() {
            @Override
            @SuppressWarnings({"unchecked"})
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED) {
                    JComboBox<String> comboBox = (JComboBox<String>) event.getSource();
                    PlaceComboBoxModel comnoBoxModel = (PlaceComboBoxModel) comboBox.getModel();
                    gedcomFormatModel.getGedcomInfo(currentGedcomIndex).setOrder(comnoBoxModel.getJuridiction(), comboBox.getSelectedIndex());
                }
            }
        };
        comboxBoxMap.put(RecordJuridiction.HAMLET, jComboBoxHamlet);
        comboxBoxMap.put(RecordJuridiction.CITY_NAME, jComboBoxCityName);
        comboxBoxMap.put(RecordJuridiction.CITY_CODE, jComboBoxCityCode);
        comboxBoxMap.put(RecordJuridiction.COUNTY, jComboBoxCounty);
        comboxBoxMap.put(RecordJuridiction.STATE, jComboBoxState);
        comboxBoxMap.put(RecordJuridiction.COUNTRY, jComboBoxCountry);
        
        
        
        for ( RecordJuridiction recordJuridiction : comboxBoxMap.keySet()) {
            JComboBox<String> combobox = comboxBoxMap.get(recordJuridiction);
            combobox.setModel(new PlaceComboBoxModel(recordJuridiction ));
            combobox.addItemListener(comboBoxItemListener);
        }

        // je branche le listenr de la list des gedcom vers les combobox
        jListGedcomFile.addListSelectionListener(new GedcomListSelectionHandler()); 

        jListGedcomFile.setSelectedIndex(currentGedcomIndex); 
        
        
        // correspondance des sources
        SourceModel sourceModel = SourceModel.getModel();
        jTableSource.setModel(sourceModel);
        if ( sourceModel.getRowCount() > 0) {
            // je selectionne la première ligne
            jTableSource.getSelectionModel().setSelectionInterval(0, 0);
        }

        if( currentFile != null && sourceModel.exist(currentFile.getName())) {
            jButtonAddSource.setVisible(false);
        } else {
            jButtonAddSource.setVisible(true);
        }

    }

    public void loadPreferences() {
        SourceModel.getModel().loadPreferences();
    }
    
    public void savePreferences() {
        PlaceFormatModel.getModel().savePreferences(
                gedcomFormatModel.getGedcomInfo(currentGedcomIndex).getOrder(RecordJuridiction.HAMLET),
                gedcomFormatModel.getGedcomInfo(currentGedcomIndex).getOrder(RecordJuridiction.CITY_NAME),
                gedcomFormatModel.getGedcomInfo(currentGedcomIndex).getOrder(RecordJuridiction.CITY_CODE),
                gedcomFormatModel.getGedcomInfo(currentGedcomIndex).getOrder(RecordJuridiction.COUNTY),
                gedcomFormatModel.getGedcomInfo(currentGedcomIndex).getOrder(RecordJuridiction.STATE),
                gedcomFormatModel.getGedcomInfo(currentGedcomIndex).getOrder(RecordJuridiction.COUNTRY),
                gedcomFormatModel.getGedcomInfo(currentGedcomIndex).getNbJuridictions()
        );
        
        
        SourceModel.getModel().savePreferences();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelSimilar = new javax.swing.JPanel();
        jButtonSimilarLastNames = new javax.swing.JButton();
        jButtonSimilarFirstNames = new javax.swing.JButton();
        jPanelPlace = new javax.swing.JPanel();
        jLabelHamlet = new javax.swing.JLabel();
        jLabelCityName = new javax.swing.JLabel();
        jLabelCityCode = new javax.swing.JLabel();
        jLabelCounty = new javax.swing.JLabel();
        jLabelState = new javax.swing.JLabel();
        jLabelCountry = new javax.swing.JLabel();
        jComboBoxHamlet = new javax.swing.JComboBox<>();
        jComboBoxCityName = new javax.swing.JComboBox<>();
        jComboBoxCityCode = new javax.swing.JComboBox<>();
        jComboBoxCounty = new javax.swing.JComboBox<>();
        jComboBoxState = new javax.swing.JComboBox<>();
        jComboBoxCountry = new javax.swing.JComboBox<>();
        jLabelGedcomFile = new javax.swing.JLabel();
        jScrollPaneGedcomFile = new javax.swing.JScrollPane();
        jListGedcomFile = new javax.swing.JList<>();
        jLabelRecordJuridiction = new javax.swing.JLabel();
        jLabelGedcomJuridiction = new javax.swing.JLabel();
        jPanelSource = new javax.swing.JPanel();
        jScrollPaneSource = new javax.swing.JScrollPane();
        jTableSource = new javax.swing.JTable();
        jButtonAddSource = new javax.swing.JButton();
        jButtonModify = new javax.swing.JButton();
        jButtonRemoveSource = new javax.swing.JButton();
        jLabelFiller = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jPanel.border.title"))); // NOI18N
        setMinimumSize(new java.awt.Dimension(260, 500));
        setName(""); // NOI18N
        setPreferredSize(new java.awt.Dimension(400, 500));
        setLayout(new java.awt.GridBagLayout());

        jPanelSimilar.setMinimumSize(new java.awt.Dimension(300, 33));
        jPanelSimilar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jButtonSimilarLastNames.setText(org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jButtonSimilarLastNames.text")); // NOI18N
        jButtonSimilarLastNames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSimilarLastNamesActionPerformed(evt);
            }
        });
        jPanelSimilar.add(jButtonSimilarLastNames);

        jButtonSimilarFirstNames.setText(org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jButtonSimilarFirstNames.text")); // NOI18N
        jButtonSimilarFirstNames.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSimilarFirstNamesActionPerformed(evt);
            }
        });
        jPanelSimilar.add(jButtonSimilarFirstNames);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanelSimilar, gridBagConstraints);

        jPanelPlace.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jPanelPlace.border.title"))); // NOI18N
        jPanelPlace.setMinimumSize(new java.awt.Dimension(200, 100));
        jPanelPlace.setOpaque(false);
        jPanelPlace.setPreferredSize(new java.awt.Dimension(300, 230));
        jPanelPlace.setLayout(new java.awt.GridBagLayout());

        jLabelHamlet.setText(org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jLabelHamlet.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jLabelHamlet, gridBagConstraints);

        jLabelCityName.setText(org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jLabelCityName.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jLabelCityName, gridBagConstraints);

        jLabelCityCode.setText(org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jLabelCityCode.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jLabelCityCode, gridBagConstraints);

        jLabelCounty.setText(org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jLabelCounty.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jLabelCounty, gridBagConstraints);

        jLabelState.setText(org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jLabelState.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jLabelState, gridBagConstraints);

        jLabelCountry.setText(org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jLabelCountry.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jLabelCountry, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jComboBoxHamlet, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jComboBoxCityName, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jComboBoxCityCode, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jComboBoxCounty, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jComboBoxState, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jComboBoxCountry, gridBagConstraints);

        jLabelGedcomFile.setText(org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jLabelGedcomFile.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jLabelGedcomFile, gridBagConstraints);

        jListGedcomFile.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPaneGedcomFile.setViewportView(jListGedcomFile);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jScrollPaneGedcomFile, gridBagConstraints);

        jLabelRecordJuridiction.setText(org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jLabelRecordJuridiction.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jLabelRecordJuridiction, gridBagConstraints);

        jLabelGedcomJuridiction.setText(org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jLabelGedcomJuridiction.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 10, 2, 2);
        jPanelPlace.add(jLabelGedcomJuridiction, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanelPlace, gridBagConstraints);

        jPanelSource.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jPanelSource.border.title"))); // NOI18N
        jPanelSource.setPreferredSize(new java.awt.Dimension(300, 150));
        jPanelSource.setLayout(new java.awt.GridBagLayout());

        jTableSource.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title1", "Title2"
            }
        ));
        jTableSource.setMaximumSize(new java.awt.Dimension(2147483647, 500));
        jTableSource.setMinimumSize(new java.awt.Dimension(60, 60));
        jTableSource.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPaneSource.setViewportView(jTableSource);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        jPanelSource.add(jScrollPaneSource, gridBagConstraints);

        jButtonAddSource.setText(org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jButtonAddSource.text")); // NOI18N
        jButtonAddSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddSourceActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanelSource.add(jButtonAddSource, gridBagConstraints);

        jButtonModify.setText(org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jButtonModify.text")); // NOI18N
        jButtonModify.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonModifyActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanelSource.add(jButtonModify, gridBagConstraints);

        jButtonRemoveSource.setText(org.openide.util.NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jButtonRemoveSource.text")); // NOI18N
        jButtonRemoveSource.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveSourceActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 10, 5, 10);
        jPanelSource.add(jButtonRemoveSource, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanelSource, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jLabelFiller, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    private void jButtonAddSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddSourceActionPerformed
        
        if( currentFile != null && !currentFile.getName().isEmpty()) {

            // j'affiche la fenetre de selection d'une source
            Source newSource = RecordSourceConfigDialog.show(parent, currentFile.getName(), "");
            // j'enregistre la source
            if( newSource != null) {
                SourceModel.getModel().add(currentFile.getName(), newSource.getTitle());
                // je selectionne la dernière ligne du modele
                int index = jTableSource.convertRowIndexToView(SourceModel.getModel().getRowCount() - 1);
                jTableSource.getSelectionModel().setSelectionInterval(index, index);
            }
        } else {
            // j'affiche le message d'erreur
            String message = NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.message.FirstSaveFile");
            String title = NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.jPanelSource.border.title");
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        }
}//GEN-LAST:event_jButtonAddSourceActionPerformed

    private void jButtonRemoveSourceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveSourceActionPerformed
        if( jTableSource.getSelectedRow() != -1) {
            SourceModel.getModel().remove(jTableSource.convertRowIndexToModel(jTableSource.getSelectedRow()));
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }//GEN-LAST:event_jButtonRemoveSourceActionPerformed

    private void jButtonModifyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonModifyActionPerformed
        if (jTableSource.getSelectedRow() != -1) {
            int index = jTableSource.convertRowIndexToModel(jTableSource.getSelectedRow());
            String fileName = (String) SourceModel.getModel().getValueAt(index, 0);
            String sourceName = (String) SourceModel.getModel().getValueAt(index, 1);
            // j'affiche la fenetre de selection d'une source
            Source newSource = RecordSourceConfigDialog.show(parent, fileName, sourceName);
            // j'enregistre la source
            if (newSource != null) {
                SourceModel.getModel().modify(index, newSource.getTitle());
            }
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }//GEN-LAST:event_jButtonModifyActionPerformed

    private void jButtonSimilarFirstNamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSimilarFirstNamesActionPerformed
        SimilarNameDialog.showSimilarFirstNamePanel();
}//GEN-LAST:event_jButtonSimilarFirstNamesActionPerformed

    private void jButtonSimilarLastNamesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSimilarLastNamesActionPerformed
        SimilarNameDialog.showSimilarLastNamePanel();
}//GEN-LAST:event_jButtonSimilarLastNamesActionPerformed

    private void jButtonShowGedcomJuridictionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonShowGedcomJuridictionsActionPerformed
        
    }//GEN-LAST:event_jButtonShowGedcomJuridictionsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonAddSource;
    private javax.swing.JButton jButtonModify;
    private javax.swing.JButton jButtonRemoveSource;
    private javax.swing.JButton jButtonSimilarFirstNames;
    private javax.swing.JButton jButtonSimilarLastNames;
    private javax.swing.JComboBox<String> jComboBoxCityCode;
    private javax.swing.JComboBox<String> jComboBoxCityName;
    private javax.swing.JComboBox<String> jComboBoxCountry;
    private javax.swing.JComboBox<String> jComboBoxCounty;
    private javax.swing.JComboBox<String> jComboBoxHamlet;
    private javax.swing.JComboBox<String> jComboBoxState;
    private javax.swing.JLabel jLabelCityCode;
    private javax.swing.JLabel jLabelCityName;
    private javax.swing.JLabel jLabelCountry;
    private javax.swing.JLabel jLabelCounty;
    private javax.swing.JLabel jLabelFiller;
    private javax.swing.JLabel jLabelGedcomFile;
    private javax.swing.JLabel jLabelGedcomJuridiction;
    private javax.swing.JLabel jLabelHamlet;
    private javax.swing.JLabel jLabelRecordJuridiction;
    private javax.swing.JLabel jLabelState;
    private javax.swing.JList<String> jListGedcomFile;
    private javax.swing.JPanel jPanelPlace;
    private javax.swing.JPanel jPanelSimilar;
    private javax.swing.JPanel jPanelSource;
    private javax.swing.JScrollPane jScrollPaneGedcomFile;
    private javax.swing.JScrollPane jScrollPaneSource;
    private javax.swing.JTable jTableSource;
    // End of variables declaration//GEN-END:variables

    
    public class GedcomListSelectionHandler implements ListSelectionListener  {
        
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (jListGedcomFile.getSelectedIndex() == -1) {
                //System.out.println("valueChanged gedcomCurrentIndex="+jListGedcomFile.getSelectedIndex() );
                return;
            }
            // je memorise l'index nouveau gedcom qui sert de modele pour les combobox
            currentGedcomIndex = jListGedcomFile.getSelectedIndex();

            for (RecordJuridiction recordJuridiction : comboxBoxMap.keySet()) {
                JComboBox<String> combobox = comboxBoxMap.get(recordJuridiction);
                // je rafraichis le modele de la combobox
                combobox.setModel(new PlaceComboBoxModel(recordJuridiction));                
                // je selectionne la juridiction dans chaque combobox 
                int juridictionOrder = gedcomFormatModel.getGedcomInfo(currentGedcomIndex).getOrder(recordJuridiction);
                if (juridictionOrder >= 0 && juridictionOrder < combobox.getItemCount()) {
                    combobox.setSelectedIndex(juridictionOrder);
                } else {
                    combobox.setSelectedIndex(-1);
                }
            }

        }
    }
    
    // 
    public class PlaceComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {
        
         String selection = null;
         private final RecordJuridiction juridiction;

        PlaceComboBoxModel(RecordJuridiction juridiction) {
            this.juridiction = juridiction;
        }

        RecordJuridiction getJuridiction() {
             return juridiction;
        }        
                
        @Override
        public int getSize() {
            return gedcomFormatModel.getGedcomInfo(currentGedcomIndex).getNbJuridictions();
        }

        @Override
        public String getElementAt(int index) {
            if(index >=0  && index < gedcomFormatModel.getGedcomInfo(currentGedcomIndex).getNbJuridictions()-1) {
                return String.valueOf(index+1)+ " - " + gedcomFormatModel.getGedcomInfo(currentGedcomIndex).getPlaceFormatList()[index].trim().replaceAll("_", " ");
            } else {
                return "";
            }
        }

        @Override
        public void setSelectedItem(Object anItem) {
            if( anItem != null ) {
                selection = anItem.toString();
            } else {
                selection = "";
            }
        }

        @Override
        public String getSelectedItem() {
            return selection;
        }

    }

    static public class GedcomInfo  {
        private final String    gedcomName;
        private final String [] placeFormatList;     
        private final HashMap<RecordJuridiction, Integer> juridictionOrder = new HashMap<RecordJuridiction, Integer>(); 
        
        
        public GedcomInfo (String gedcomName, String placeFormat, PlaceFormatModel placeFormatModel ) {
            this.gedcomName = gedcomName; 
            this.placeFormatList = placeFormat.split(",",-1);
            
            juridictionOrder.put(RecordJuridiction.HAMLET, placeFormatModel.getHamletJuridiction());
            juridictionOrder.put(RecordJuridiction.CITY_NAME, placeFormatModel.getCityNameJuridiction());
            juridictionOrder.put(RecordJuridiction.CITY_CODE, placeFormatModel.getCityCodeJuridiction());
            juridictionOrder.put(RecordJuridiction.COUNTY, placeFormatModel.getCountyJuridiction());
            juridictionOrder.put(RecordJuridiction.STATE, placeFormatModel.getStateJuridiction());
            juridictionOrder.put(RecordJuridiction.COUNTRY, placeFormatModel.getCountryJuridiction());
            
        }
        
        public int getNbJuridictions() {
            return placeFormatList.length+1;
        }
        
        public String getGedcomName() {
            return gedcomName;
        }
        
        public String[] getPlaceFormatList() {
            return placeFormatList;
        }
        
        public int getOrder(RecordJuridiction juridiction) {
            return juridictionOrder.get(juridiction);
        }

        public void setOrder(RecordJuridiction juridiction, int order) {
            juridictionOrder.put(juridiction, order);
        }

        
    }
    
    static public class GedcomFormatModel extends AbstractListModel<String> implements ListModel<String> {
        
        GedcomInfo [] gedcomInfoArray;
        
        GedcomFormatModel(PlaceFormatModel placeFormatModel) {
            super();
            
            List<Context> contexts = GedcomDirectory.getDefault().getContexts();
            gedcomInfoArray = new GedcomInfo[contexts.size()+1];
            String defaultName = NbBundle.getMessage(MergeOptionPanel.class, "MergeOptionPanel.defaultFormat");
            gedcomInfoArray[0] = new GedcomInfo(defaultName, GedcomOptions.getInstance().getPlaceFormat(), placeFormatModel);
            
            for (int i = 0; i < contexts.size(); i++) {
                gedcomInfoArray[i+1] = new GedcomInfo(contexts.get(i).getGedcom().getName(), contexts.get(i).getGedcom().getPlaceFormat(), placeFormatModel);

            }
        }
          
        
        @Override
        public int getSize() {
            return gedcomInfoArray.length;
        }

        @Override
        public String getElementAt(int index) {
            if( index >=0 && index < gedcomInfoArray.length ) {
                return gedcomInfoArray[index].getGedcomName();
            } else {
                return "";
            }
            
        }
        
        public String[] getGedcomPlaceFormatList(int index) {
            if( index >=0 && index < gedcomInfoArray.length ) {
                return gedcomInfoArray[index].getPlaceFormatList();
            } else {
                return new String[0];
            }
        }
        
        public GedcomInfo getGedcomInfo(int index) {
            return gedcomInfoArray[index];            
        }
        
    }
    
    static private class SourceModelElement {
        public String fileName;
        public String sourceName;

        public SourceModelElement(String fileName, String sourceName) {
            this.fileName = fileName;
            this.sourceName = sourceName;
        }
    }

    static public class SourceModel extends AbstractTableModel {
        static final  String SOURCE_PREFERENCE = "RecordSource";
        static private  SourceModel sourceModel = null;

        final String columnName[] = {
            NbBundle.getMessage(SourceModel.class, "MergeOptionPanel.column.title.file"),
            NbBundle.getMessage(SourceModel.class, "MergeOptionPanel.column.title.source")
        };

        final Class<?> columnClass[] = {String.class, String.class};

        // données du modèle 
        private final ArrayList<SourceModelElement> sourceList = new ArrayList<SourceModelElement>();

        /** 
         * model factory
         * @return 
         */
        static public SourceModel getModel() {

            if (sourceModel == null) {
                sourceModel = new SourceModel();
                sourceModel.loadPreferences();
            }
            return sourceModel;
        }

        /**
         * charge les repertoires
         */
        private void loadPreferences() {
            sourceList.clear();
            // je recupere la liste des valeurs similaires
            String stringData = NbPreferences.forModule(SourceModel.class).get(
                    SOURCE_PREFERENCE, "");
            String[] arrayData = stringData.split(";");
            for (String arrayData1 : arrayData) {
                if (!arrayData1.isEmpty()) {
                    String[] item = arrayData1.split("=");
                    if( item.length == 2) {
                        sourceList.add(new SourceModelElement(item[0], item[1]));
                    }
                }
            }
        }

        /**
         * enregistre les paires fileName=sourceName
         */
        public void savePreferences() {
            StringBuilder values = new StringBuilder();

            for (SourceModelElement element : sourceList) {
                values.append(element.fileName)
                        .append("=")
                        .append(element.sourceName)
                        .append(";");
            }
            NbPreferences.forModule(SourceModel.class).put(
                    SOURCE_PREFERENCE, values.toString());
        }

        
        public void add(String fileName, String sourceName) {
            int index = index(fileName);
            if (index!= -1) {
                sourceList.get(index).sourceName = sourceName;
                fireTableRowsUpdated(index, index);
            } else {
                sourceList.add(new SourceModelElement(fileName, sourceName));
                index = sourceList.size()-1;
                fireTableRowsInserted(index, index);
            }            
        }

        public void remove(int index) {
            sourceList.remove(index);
            fireTableRowsDeleted(index, index);
        }

        public void modify(int index, String sourceName) {
            sourceList.get(index).sourceName = sourceName;
            fireTableRowsUpdated(index, index);
        }

        public int index(String fileName) {
            for(int i=0; i < sourceList.size(); i++) {
                if( sourceList.get(i).fileName.equalsIgnoreCase(fileName)) {
                    return i;
                }
            }
            return -1;
        }

        public boolean exist(String fileName) {
            if( index(fileName)== -1) {
                return false;
            } else {
                return true;
            }
        }

        public String getSource(String fileName) {
            int index = index(fileName);
            if( index != -1) {
                return sourceList.get(index).sourceName;
            } else {
                return "";
            }            
        }

        @Override
        public String getColumnName(int col) {
            return columnName[col];
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return columnClass[col];
        }

        @Override
        public int getRowCount() {
            return sourceList.size();
        }

        @Override
        public int getColumnCount() {
            return columnClass.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return sourceList.get(rowIndex).fileName;
                default:
                    return sourceList.get(rowIndex).sourceName;
            }

        }
    }
}
