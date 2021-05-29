package ancestris.modules.releve.merge;

import ancestris.gedcom.GedcomDirectory;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Source;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.plaf.basic.BasicComboBoxEditor;

/**
 * Affiche une fenetre pour configurer le le lieu du relevé (cityName etc...)
 *
 * @author Michel
 */
public class RecordSourceConfigDialog extends javax.swing.JDialog {

    Source result = null;

    /**
     * affiche la fenêtre de configuration du relevé
     * @param parent
     * @param dataManager
     */
    public static Source show(Frame parent, String fileName, String sourceTitle) {
        final RecordSourceConfigDialog dialog = new RecordSourceConfigDialog(parent, fileName, sourceTitle, null);
        dialog.setVisible(true);

        return dialog.result;
    }

    public static Source show(Frame parent, String fileName, String sourceTitle, Gedcom gedcom) {
        final RecordSourceConfigDialog dialog = new RecordSourceConfigDialog(parent, fileName, sourceTitle, gedcom);
        dialog.setVisible(true);

        return dialog.result;
    }

    /**
     * Creates new form ReleveConfig
     */
    private RecordSourceConfigDialog(java.awt.Frame parent, String fileName, String sourceTitle, Gedcom gedcom) {
        super(parent, true);
        initComponents();
        setModal(true);
        setLocationRelativeTo(parent);
        
        jLabelFileName.setText(fileName);
        jLabelSourceTitle.setText(sourceTitle);

        // listener de la combo des gedcom
        jComboBoxGedcom.setEditor(new MyEditor());
        jComboBoxGedcom.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent arg0) {
                Gedcom gedcom = (Gedcom) arg0.getItem();
                Entity[] entities = gedcom.getEntities("SOUR", "SOUR:TITL"); 
                ArrayList<Source> sources = new ArrayList<Source> ();
                for(Entity entity : entities) {
                    if ( entity instanceof Source && !((Source)entity).getTitle().isEmpty()) {
                        sources.add((Source)entity);
                    }
                }
                jComboBoxSources.setModel(new DefaultComboBoxModel<Source>(sources.toArray(new Source[sources.size()])));
            }
        });

        jComboBoxSources.setEditor(new MyEditor());
        jComboBoxSources.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent arg0) {
                jLabelSourceTitle.setText( ((Source)arg0.getItem()).getTitle());
            }
        });

        // j'initialise la liste des gedcom
        Gedcom[] gedcoms;
        if (gedcom == null) {
            List<Context> contexts = GedcomDirectory.getDefault().getContexts();
            gedcoms = new Gedcom[contexts.size()];
            for (int i = 0; i < contexts.size(); i++) {
                gedcoms[i] = contexts.get(i).getGedcom();
            }
            // je choisis le premier gedcom
            if (gedcoms.length > 0) {
                gedcom = contexts.get(0).getGedcom();
            }

        } else {
            gedcoms = new Gedcom[1]; 
            gedcoms[0] = gedcom;
        } 

        if (gedcom != null) {
            jComboBoxGedcom.setModel(new DefaultComboBoxModel<Gedcom>(gedcoms));            
            Entity[] entities = gedcom.getEntities("SOUR", "SOUR:TITL");
            ArrayList<Source> sources = new ArrayList<Source>();
            for (Entity entity : entities) {
                if (entity instanceof Source && !((Source) entity).getTitle().isEmpty()) {
                    sources.add((Source) entity);
                }
            }
            jComboBoxSources.setModel(new DefaultComboBoxModel<Source>(sources.toArray(new Source[sources.size()])));
            // je sélectionne la source dans la comobobox
            if( !sourceTitle.isEmpty()) {
                for( Entity source : sources) {
                    if ( ((Source) source).getTitle().equals(sourceTitle)) {
                        jComboBoxSources.getModel().setSelectedItem(source);
                        break;
                    }
                }
            }
        } else {
            // j'affiche un message d'erreur
        }
    }

    static class MyEditor extends BasicComboBoxEditor{
        JScrollPane scroller = new JScrollPane();
        //NOTE: editor is a JTextField defined in BasicComboBoxEditor

        public MyEditor(){
            super();
            scroller.setViewportView(editor);
            scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        }

        // Return a JScrollPane containing the JTextField instead of the JTextField
        @Override
        public Component getEditorComponent() {
            return scroller;
        }

        /** Override to create your own JTextField. **/
        @Override
        protected JTextField createEditorComponent() {
            JTextField editorTemp = new JTextField();
            editorTemp.setBorder(null);
            editorTemp.setEditable(false);
            return editorTemp;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sourcePanel = new javax.swing.JPanel();
        jLabelFileLabel = new javax.swing.JLabel();
        jLabelSource = new javax.swing.JLabel();
        jLabelSourceTitle = new javax.swing.JLabel();
        jLabelFileName = new javax.swing.JLabel();
        jLabelSelectGedcom = new javax.swing.JLabel();
        jComboBoxGedcom = new javax.swing.JComboBox<Gedcom>();
        jLabelSelectSource = new javax.swing.JLabel();
        jComboBoxSources = new javax.swing.JComboBox<Source>();
        jPanelButton = new javax.swing.JPanel();
        jButtonOk = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        sourcePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RecordSourceConfigDialog.class, "RecordSourceConfigDialog.sourcePanel.border.title"))); // NOI18N
        sourcePanel.setMinimumSize(new java.awt.Dimension(200, 114));
        sourcePanel.setPreferredSize(new java.awt.Dimension(400, 200));
        sourcePanel.setLayout(new java.awt.GridBagLayout());

        jLabelFileLabel.setText(org.openide.util.NbBundle.getMessage(RecordSourceConfigDialog.class, "RecordSourceConfigDialog.jLabelFileLabel.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        sourcePanel.add(jLabelFileLabel, gridBagConstraints);

        jLabelSource.setText(org.openide.util.NbBundle.getMessage(RecordSourceConfigDialog.class, "RecordSourceConfigDialog.jLabelSource.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        sourcePanel.add(jLabelSource, gridBagConstraints);

        jLabelSourceTitle.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabelSourceTitle.setMaximumSize(new java.awt.Dimension(32767, 32767));
        jLabelSourceTitle.setMinimumSize(new java.awt.Dimension(60, 18));
        jLabelSourceTitle.setPreferredSize(new java.awt.Dimension(200, 18));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        sourcePanel.add(jLabelSourceTitle, gridBagConstraints);

        jLabelFileName.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabelFileName.setMaximumSize(new java.awt.Dimension(32767, 32767));
        jLabelFileName.setMinimumSize(new java.awt.Dimension(60, 18));
        jLabelFileName.setName(""); // NOI18N
        jLabelFileName.setPreferredSize(new java.awt.Dimension(80, 18));
        jLabelFileName.setRequestFocusEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        sourcePanel.add(jLabelFileName, gridBagConstraints);

        jLabelSelectGedcom.setText(org.openide.util.NbBundle.getMessage(RecordSourceConfigDialog.class, "RecordSourceConfigDialog.jLabelSelectGedcom.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 4, 4, 4);
        sourcePanel.add(jLabelSelectGedcom, gridBagConstraints);

        jComboBoxGedcom.setPreferredSize(new java.awt.Dimension(28, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        sourcePanel.add(jComboBoxGedcom, gridBagConstraints);

        jLabelSelectSource.setText(org.openide.util.NbBundle.getMessage(RecordSourceConfigDialog.class, "RecordSourceConfigDialog.jLabelSelectSource.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 4, 4, 4);
        sourcePanel.add(jLabelSelectSource, gridBagConstraints);

        jComboBoxSources.setPreferredSize(new java.awt.Dimension(28, 24));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        sourcePanel.add(jComboBoxSources, gridBagConstraints);

        getContentPane().add(sourcePanel, java.awt.BorderLayout.CENTER);

        jButtonOk.setText(org.openide.util.NbBundle.getMessage(RecordSourceConfigDialog.class, "RecordSourceConfigDialog.jButtonOk.text")); // NOI18N
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonOk);

        jButtonCancel.setText(org.openide.util.NbBundle.getMessage(RecordSourceConfigDialog.class, "RecordSourceConfigDialog.jButtonCancel.text")); // NOI18N
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonCancel);

        getContentPane().add(jPanelButton, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * enregistre les modifications et ferme la fenetre
     * @param evt 
     */
    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        // je memorise la surce selectionnée
        if( jComboBoxSources.getSelectedIndex() != -1) {
            result = jComboBoxSources.getModel().getElementAt(jComboBoxSources.getSelectedIndex());
        }
        dispose();
    }//GEN-LAST:event_jButtonOkActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

   
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JComboBox<Gedcom> jComboBoxGedcom;
    private javax.swing.JComboBox<Source> jComboBoxSources;
    private javax.swing.JLabel jLabelFileLabel;
    private javax.swing.JLabel jLabelFileName;
    private javax.swing.JLabel jLabelSelectGedcom;
    private javax.swing.JLabel jLabelSelectSource;
    private javax.swing.JLabel jLabelSource;
    private javax.swing.JLabel jLabelSourceTitle;
    private javax.swing.JPanel jPanelButton;
    private javax.swing.JPanel sourcePanel;
    // End of variables declaration//GEN-END:variables
}
