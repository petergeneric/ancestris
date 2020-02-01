package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.utilities.PropertyTag2Name;
import ancestris.util.swing.DialogManager;
import genj.gedcom.Entity;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.PropertyRelationship;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
    private Entity mIndividual;
    private String mEventTag = "";
    private Indi mAssociatedIndividual;
    private boolean mRelationModified = false;
    private final ChangeListner changeListner = new ChangeListner();
    private static final String[] mIndividualEventsTags = {
        "",
        /*
         * INDIVIDUAL_EVENT
         */
        PropertyTag2Name.getTagName("BIRT"),
        PropertyTag2Name.getTagName("CHR"),
        PropertyTag2Name.getTagName("DEAT"),
        PropertyTag2Name.getTagName("BURI"),
        PropertyTag2Name.getTagName("CREM"),
        PropertyTag2Name.getTagName("ADOP"),
        PropertyTag2Name.getTagName("BAPM"),
        PropertyTag2Name.getTagName("BARM"),
        PropertyTag2Name.getTagName("BASM"),
        PropertyTag2Name.getTagName("BLES"),
        PropertyTag2Name.getTagName("CHRA"),
        PropertyTag2Name.getTagName("CONF"),
        PropertyTag2Name.getTagName("FCOM"),
        PropertyTag2Name.getTagName("ORDN"),
        PropertyTag2Name.getTagName("NATU"),
        PropertyTag2Name.getTagName("EMIG"),
        PropertyTag2Name.getTagName("IMMI"),
        PropertyTag2Name.getTagName("CENS"),
        PropertyTag2Name.getTagName("PROB"),
        PropertyTag2Name.getTagName("WILL"),
        PropertyTag2Name.getTagName("GRAD"),
        PropertyTag2Name.getTagName("RETI"),
        PropertyTag2Name.getTagName("EVEN"),
        /*
         * INDIVIDUAL_ATTRIBUTE
         */
        PropertyTag2Name.getTagName("CAST"),
        PropertyTag2Name.getTagName("DSCR"),
        PropertyTag2Name.getTagName("EDUC"),
        PropertyTag2Name.getTagName("IDNO"),
        PropertyTag2Name.getTagName("NATI"),
        PropertyTag2Name.getTagName("NCHI"),
        PropertyTag2Name.getTagName("NMR"),
        PropertyTag2Name.getTagName("OCCU"),
        PropertyTag2Name.getTagName("PROP"),
        PropertyTag2Name.getTagName("RELI"),
        PropertyTag2Name.getTagName("RESI"),
        PropertyTag2Name.getTagName("SSN"),
        PropertyTag2Name.getTagName("TITL")
    };
    private final DefaultComboBoxModel<String> mEventsModel = new DefaultComboBoxModel<String>(mIndividualEventsTags);

    /**
     * Creates new form AssociationEditorPanel
     */
    public AssociationEditorPanel() {
        initComponents();
        relationChoiceWidget.addChangeListener(changeListner);
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
        associationTabbedPane = new javax.swing.JTabbedPane();
        notesPanel = new javax.swing.JPanel();
        noteCitationsTablePanel = new ancestris.modules.editors.genealogyeditor.panels.NoteCitationsTablePanel();
        sourcesPanel = new javax.swing.JPanel();
        sourceCitationsTablePanel = new ancestris.modules.editors.genealogyeditor.panels.SourceCitationsTablePanel();
        relationChoiceWidget = new genj.util.swing.ChoiceWidget();
        eventTypeComboBox = new javax.swing.JComboBox<String>();
        jLabel1 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(referenceIndividualLabel, org.openide.util.NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.referenceIndividualLabel.text")); // NOI18N

        referenceIndividualTextField.setEditable(false);
        referenceIndividualTextField.setText(org.openide.util.NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.referenceIndividualTextField.text")); // NOI18N

        linkToIndividualButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/link_add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(linkToIndividualButton, java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("AssociationEditorPanel.linkToIndividualButton.text"), new Object[] {})); // NOI18N
        linkToIndividualButton.setFocusable(false);
        linkToIndividualButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        linkToIndividualButton.setMaximumSize(new java.awt.Dimension(26, 26));
        linkToIndividualButton.setMinimumSize(new java.awt.Dimension(26, 26));
        linkToIndividualButton.setPreferredSize(new java.awt.Dimension(26, 26));
        linkToIndividualButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        linkToIndividualButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkToIndividualButtonActionPerformed(evt);
            }
        });

        relationLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(relationLabel, org.openide.util.NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.relationLabel.text")); // NOI18N

        javax.swing.GroupLayout notesPanelLayout = new javax.swing.GroupLayout(notesPanel);
        notesPanel.setLayout(notesPanelLayout);
        notesPanelLayout.setHorizontalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, notesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(noteCitationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        notesPanelLayout.setVerticalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, notesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(noteCitationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE))
        );

        associationTabbedPane.addTab(org.openide.util.NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.notesPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/note.png")), notesPanel); // NOI18N

        javax.swing.GroupLayout sourcesPanelLayout = new javax.swing.GroupLayout(sourcesPanel);
        sourcesPanel.setLayout(sourcesPanelLayout);
        sourcesPanelLayout.setHorizontalGroup(
            sourcesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sourcesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(sourceCitationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        sourcesPanelLayout.setVerticalGroup(
            sourcesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sourcesPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(sourceCitationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE))
        );

        associationTabbedPane.addTab(org.openide.util.NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.sourcesPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/source.png")), sourcesPanel); // NOI18N

        eventTypeComboBox.setModel(mEventsModel);
        eventTypeComboBox.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("AssociationEditorPanel.eventTypeComboBox.toolTipText"), new Object[] {})); // NOI18N
        eventTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventTypeComboBoxActionPerformed(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(associationTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(referenceIndividualLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(relationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(linkToIndividualButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(referenceIndividualTextField))
                            .addComponent(relationChoiceWidget, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(eventTypeComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eventTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(9, 9, 9)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(referenceIndividualLabel)
                    .addComponent(linkToIndividualButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(referenceIndividualTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(relationLabel)
                    .addComponent(relationChoiceWidget, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(associationTabbedPane)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void linkToIndividualButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkToIndividualButtonActionPerformed
        IndividualsTablePanel individualsTablePanel = new IndividualsTablePanel();
        List<Indi> individualsList = new ArrayList<Indi>(mIndividual.getGedcom().getIndis());

        individualsTablePanel.set(mAssociation, individualsList);
        individualsTablePanel.setToolBarVisible(false);
        DialogManager.ADialog individualsTableDialog = new DialogManager.ADialog(
                NbBundle.getMessage(IndividualsTablePanel.class, "individualsTableDialog.title.select.husband"),
                individualsTablePanel);
        individualsTableDialog.setDialogId(IndividualsTablePanel.class.getName());

        if (individualsTableDialog.show() == DialogDescriptor.OK_OPTION) {
            mAssociatedIndividual = individualsTablePanel.getSelectedIndividual();
            referenceIndividualTextField.setText(mAssociatedIndividual.getName());
            referenceIndividualTextField.setVisible(true);
            linkToIndividualButton.setVisible(false);
            mRelationModified = true;
        }
    }//GEN-LAST:event_linkToIndividualButtonActionPerformed

    private void eventTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventTypeComboBoxActionPerformed
        final String eventName = eventTypeComboBox.getSelectedItem().toString();
        mEventTag = PropertyTag2Name.getPropertyTag(eventName);
    }//GEN-LAST:event_eventTypeComboBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane associationTabbedPane;
    private javax.swing.JComboBox<String> eventTypeComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton linkToIndividualButton;
    private ancestris.modules.editors.genealogyeditor.panels.NoteCitationsTablePanel noteCitationsTablePanel;
    private javax.swing.JPanel notesPanel;
    private javax.swing.JLabel referenceIndividualLabel;
    private javax.swing.JTextField referenceIndividualTextField;
    private genj.util.swing.ChoiceWidget relationChoiceWidget;
    private javax.swing.JLabel relationLabel;
    private ancestris.modules.editors.genealogyeditor.panels.SourceCitationsTablePanel sourceCitationsTablePanel;
    private javax.swing.JPanel sourcesPanel;
    // End of variables declaration//GEN-END:variables

    void set(Entity individual, PropertyAssociation association, Property event) {
        mIndividual = individual;
        if (event != null) {
            mEventTag = event.getTag();
            eventTypeComboBox.setEnabled(false);
        }
        eventTypeComboBox.setSelectedItem(PropertyTag2Name.getTagName(mEventTag));
        relationChoiceWidget.setValues(mIndividual.getGedcom().getReferenceSet("RELA").getKeys());
        changeListner.mute();
        mAssociation = association;
        Entity targetEntity = association.getTargetEntity();
        if (targetEntity != null) {
            linkToIndividualButton.setVisible(false);
            referenceIndividualTextField.setText(((Indi) targetEntity).getName());
            referenceIndividualTextField.setVisible(true);
        } else {
            referenceIndividualTextField.setVisible(false);
        }

        PropertyRelationship propertyRelationship = (PropertyRelationship) association.getProperty("RELA", false);
        if (propertyRelationship != null) {
            relationChoiceWidget.setText(propertyRelationship.getDisplayValue());
        }

        noteCitationsTablePanel.set(association, Arrays.asList(association.getProperties("NOTE")));

        sourceCitationsTablePanel.set(association, Arrays.asList(association.getProperties("SOUR")));
        changeListner.unmute();
    }

    PropertyAssociation commit() {
        if (mRelationModified) {
            if (mAssociatedIndividual != null) {
                mAssociation.setValue('@' + mAssociatedIndividual.getId() + '@');
                try {
                    mAssociation.link();
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            PropertyRelationship propertyRelationship = (PropertyRelationship) mAssociation.getProperty("RELA", false);
            if (propertyRelationship == null) {
                mAssociation.addProperty("RELA", relationChoiceWidget.getText() + (mEventTag.isEmpty() == false ? "@" + "INDI:" + mEventTag : ""));
            } else {
                propertyRelationship.setValue(relationChoiceWidget.getText());
            }
        }

        return mAssociation;
    }

    private class ChangeListner implements ChangeListener {

        private boolean mute = false;

        @Override
        public void stateChanged(ChangeEvent ce) {
            if (!mute) {
                mRelationModified = true;
            }
        }

        public void mute() {
            mute = true;
        }

        public void unmute() {
            mute = false;
        }
    }
}
