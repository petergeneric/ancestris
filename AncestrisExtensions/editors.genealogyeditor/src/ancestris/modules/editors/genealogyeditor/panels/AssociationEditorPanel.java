package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.util.swing.DialogManager;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.UnitOfWork;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openide.DialogDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
/*
 * 5.5
 * n ASSO @<XREF:INDI>@
 * +1 TYPE <RECORD_TYPE>
 *    [ FAM | INDI | NOTE | OBJE | REPO | SOUR | SUBM | SUBN ]
 * +1 RELA <RELATION_IS_DESCRIPTOR>
 * +1 <<NOTE_STRUCTURE>>
 * +1 <<SOURCE_CITATION>>
 *
 * 5.5.1
 * n ASSO @<XREF:INDI>@
 * +1 RELA <RELATION_IS_DESCRIPTOR>
 * +1 <<SOURCE_CITATION>>
 * +1 <<NOTE_STRUCTURE>>
 */
public class AssociationEditorPanel extends javax.swing.JPanel {

    private PropertyAssociation mAssociation;
    private Indi mIndividual;

    /**
     * Creates new form AssociationEditorPanel
     */
    public AssociationEditorPanel() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        referenceIndividualLabel = new javax.swing.JLabel();
        referenceIndividualTextField = new javax.swing.JTextField();
        linkToIndividualButton = new javax.swing.JButton();
        relationLabel = new javax.swing.JLabel();
        relationTextField = new javax.swing.JTextField();
        associationTabbedPane = new javax.swing.JTabbedPane();
        notesPanel = new javax.swing.JPanel();
        noteCitationsListPanel = new ancestris.modules.editors.genealogyeditor.panels.NoteCitationsListPanel();
        sourcesPanel = new javax.swing.JPanel();
        sourceCitationsListPanel = new ancestris.modules.editors.genealogyeditor.panels.SourceCitationsListPanel();

        org.openide.awt.Mnemonics.setLocalizedText(referenceIndividualLabel, org.openide.util.NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.referenceIndividualLabel.text")); // NOI18N

        referenceIndividualTextField.setText(org.openide.util.NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.referenceIndividualTextField.text")); // NOI18N

        linkToIndividualButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/link_add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(linkToIndividualButton, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("AssociationEditorPanel.linkToIndividualButton.text"), new Object[] {})); // NOI18N
        linkToIndividualButton.setFocusable(false);
        linkToIndividualButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        linkToIndividualButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        linkToIndividualButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkToIndividualButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(relationLabel, org.openide.util.NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.relationLabel.text")); // NOI18N

        relationTextField.setText(org.openide.util.NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.relationTextField.text")); // NOI18N

        javax.swing.GroupLayout notesPanelLayout = new javax.swing.GroupLayout(notesPanel);
        notesPanel.setLayout(notesPanelLayout);
        notesPanelLayout.setHorizontalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, notesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(noteCitationsListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        notesPanelLayout.setVerticalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, notesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(noteCitationsListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
        );

        associationTabbedPane.addTab(org.openide.util.NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.notesPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Note.png")), notesPanel); // NOI18N

        javax.swing.GroupLayout sourcesPanelLayout = new javax.swing.GroupLayout(sourcesPanel);
        sourcesPanel.setLayout(sourcesPanelLayout);
        sourcesPanelLayout.setHorizontalGroup(
            sourcesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sourcesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(sourceCitationsListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        sourcesPanelLayout.setVerticalGroup(
            sourcesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sourcesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(sourceCitationsListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE))
        );

        associationTabbedPane.addTab(org.openide.util.NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.sourcesPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/source.png")), sourcesPanel); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(associationTabbedPane)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(referenceIndividualLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(relationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(linkToIndividualButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(relationTextField)
                            .addComponent(referenceIndividualTextField))
                        .addGap(163, 163, 163)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(referenceIndividualLabel)
                        .addComponent(referenceIndividualTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(linkToIndividualButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(relationLabel)
                    .addComponent(relationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(associationTabbedPane)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void linkToIndividualButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkToIndividualButtonActionPerformed
        IndividualsListPanel individualsListPanel = new IndividualsListPanel();
        List<Indi> individualsList = new ArrayList<Indi>(mAssociation.getGedcom().getIndis());

        individualsListPanel.setIndividualsList(mAssociation, individualsList);
        individualsListPanel.setToolBarVisible(false);
        DialogManager.ADialog individualsListDialog = new DialogManager.ADialog(
                NbBundle.getMessage(IndividualsListPanel.class, "IndividualsListPanel.title.select.husband"),
                individualsListPanel);
        individualsListDialog.setDialogId(IndividualsListPanel.class.getName());

        if (individualsListDialog.show() == DialogDescriptor.OK_OPTION) {
            mIndividual = individualsListPanel.getSelectedIndividual();
            try {
                mAssociation.getGedcom().doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        mAssociation.setValue('@' + mIndividual.getId() + '@');
                        mAssociation.link();
                    }

                }); // end of doUnitOfWork
                referenceIndividualTextField.setText(mIndividual.getName());

            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }//GEN-LAST:event_linkToIndividualButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane associationTabbedPane;
    private javax.swing.JButton linkToIndividualButton;
    private ancestris.modules.editors.genealogyeditor.panels.NoteCitationsListPanel noteCitationsListPanel;
    private javax.swing.JPanel notesPanel;
    private javax.swing.JLabel referenceIndividualLabel;
    private javax.swing.JTextField referenceIndividualTextField;
    private javax.swing.JLabel relationLabel;
    private javax.swing.JTextField relationTextField;
    private ancestris.modules.editors.genealogyeditor.panels.SourceCitationsListPanel sourceCitationsListPanel;
    private javax.swing.JPanel sourcesPanel;
    // End of variables declaration//GEN-END:variables

    void set(PropertyAssociation association) {
        mAssociation = association;
        Entity targetEntity = association.getTargetEntity();
        if (targetEntity != null) {
            linkToIndividualButton.setVisible(false);
            referenceIndividualTextField.setText(targetEntity.getDisplayValue());
        }
        
        Property property = association.getProperty("RELA", false);
        if (property != null) {
            relationTextField.setText(property.getValue());
        }

        noteCitationsListPanel.setNotesList(association, Arrays.asList(association.getProperties("NOTE")));

        sourceCitationsListPanel.set(association, Arrays.asList(association.getProperties("SOUR")));
    }
}
