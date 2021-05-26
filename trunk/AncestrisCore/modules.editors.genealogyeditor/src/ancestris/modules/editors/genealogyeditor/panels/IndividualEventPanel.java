package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.beans.ADateBean;
import ancestris.modules.editors.genealogyeditor.utilities.PropertyTag2Name;
import ancestris.modules.editors.geoplace.PlaceEditorPanel;
import ancestris.swing.UndoTextArea;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.DialogManager.ADialog;
import genj.gedcom.*;
import genj.gedcom.time.Delta;
import genj.util.swing.ChoiceWidget;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */

/*
 *
 * EVENT_DETAIL:=
 * n TYPE <EVENT_OR_FACT_CLASSIFICATION>
 * n DATE <DATE_VALUE>
 * n <<PLACE_STRUCTURE>>
 * n <<ADDRESS_STRUCTURE>>
 * n AGNC <RESPONSIBLE_AGENCY>
 * n RELI <RELIGIOUS_AFFILIATION>
 * n CAUS <CAUSE_OF_EVENT>
 * n RESN <RESTRICTION_NOTICE>
 * n <<NOTE_STRUCTURE>>
 * n <<SOURCE_CITATION>>
 * n <<MULTIMEDIA_LINK>>
 *
 * INDIVIDUAL_EVENT_DETAIL:=
 * n <<EVENT_DETAIL>>
 * n AGE <AGE_AT_EVENT>
 *
 * INDIVIDUAL_EVENT_STRUCTURE:=
 * [
 * n [ BIRT | CHR ] [Y|<NULL>]
 * +1 <<INDIVIDUAL_EVENT_DETAIL>>
 * +1 FAMC @<XREF:FAM>@
 * |
 * n DEAT [Y|<NULL>]
 * +1 <<INDIVIDUAL_EVENT_DETAIL>>
 * |
 * n [ BURI | CREM ]
 * +1 <<INDIVIDUAL_EVENT_DETAIL>>
 * |
 * n ADOP
 * +1 <<INDIVIDUAL_EVENT_DETAIL>>
 * +1 FAMC @<XREF:FAM>@
 * +2 ADOP <ADOPTED_BY_WHICH_PARENT>
 * |
 * n [ BAPM | BARM | BASM | BLES ]
 * +1 <<INDIVIDUAL_EVENT_DETAIL>>
 * |
 * n [ CHRA | CONF | FCOM | ORDN ]
 * +1 <<INDIVIDUAL_EVENT_DETAIL>>
 * |
 * n [ NATU | EMIG | IMMI ]
 * +1 <<INDIVIDUAL_EVENT_DETAIL>>
 * |
 * n [ CENS | PROB | WILL]
 * +1 <<INDIVIDUAL_EVENT_DETAIL>>
 * |
 * n [ GRAD | RETI ]
 * +1 <<INDIVIDUAL_EVENT_DETAIL>>
 * |
 * n EVEN
 * +1 <<INDIVIDUAL_EVENT_DETAIL>>
 * ]
 */
public class IndividualEventPanel extends javax.swing.JPanel {

    private final ArrayList<String> mIndividualAttributesTags = new ArrayList<String>() {
        {
            /*
             * INDIVIDUAL_ATTRIBUTE
             */
            add("CAST");
            add("DSCR");
            add("EDUC");
            add("IDNO");
            add("NATI");
            add("NCHI");
            add("NMR");
            add("OCCU");
            add("PROP");
            add("RELI");
            add("RESI");
            add("SSN");
            add("TITL");
            add("FACT");
        }
    };
    private Property mEvent = null;
    private Property mRoot;
    private PropertyPlace mPlace;
    private PropertyDate mDate;
    private PropertyAssociation mAssociation;
    private final ChangeListner changeListner = new ChangeListner();
    private final ChangeSupport changeSupport = new ChangeSupport(IndividualEventPanel.class);
    private boolean mEventModified = false;
    private boolean mEventCauseModified = false;
    private boolean mIndividualAgeModified = false;
    private boolean mEventNameModified = false;
    private boolean mEventTypeModified = false;
    private boolean mPlaceModified = false;
    private boolean mAddressModified = false;
    private boolean mResponsibleAgencyModified = false;

    /**
     * Creates new form EventEditorPanel
     */
    public IndividualEventPanel() {
        initComponents();
        aDateBean.setPreferHorizontal(true);
        aDateBean.addChangeListener(changeListner);
        eventNameChoiceWidget.addChangeListener(changeListner);
        eventCauseTextArea.getDocument().addDocumentListener(changeListner);
        eventCauseTextArea.getDocument().putProperty("name", "eventCauseTextArea");
        eventDescriptorTextArea.getDocument().addDocumentListener(changeListner);
        eventDescriptorTextArea.getDocument().putProperty("name", "eventDescriptorTextArea");
        individualAgeTextField.getDocument().addDocumentListener(changeListner);
        individualAgeTextField.getDocument().putProperty("name", "individualAgeTextField");
        placeChoiceWidget.getTextEditor().getDocument().addDocumentListener(changeListner);
        placeChoiceWidget.getTextEditor().getDocument().putProperty("name", "placeChoiceWidget");
        placeChoiceWidget.setIgnoreCase(true);
        placeChoiceWidget.setHorizontalScrollBar();
        addressTextField.getDocument().addDocumentListener(changeListner);
        addressTextField.getDocument().putProperty("name", "addressTextField");
        responsibleAgencyTextField.getDocument().addDocumentListener(changeListner);
        responsibleAgencyTextField.getDocument().putProperty("name", "responsibleAgencyTextField");
        sourceCitationsTablePanel.addChangeListener(changeListner);
        noteCitationsTablePanel.addChangeListener(changeListner);
        multimediaObjectCitationsTablePanel.addChangeListener(changeListner);
        privateRecordToggleButton.addActionListener(changeListner);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        eventInformationTabbedPane = new JTabbedPane();
        EventDetailPanel = new JPanel();
        EventDetailEditorPanel = new JPanel();
        placeLabel = new JLabel();
        placeChoiceWidget = new ChoiceWidget();
        privateRecordToggleButton = new JToggleButton();
        IndividualAgeLabel = new JLabel();
        eventDescriptorLabel = new JLabel();
        dateLabel = new JLabel();
        aDateBean = new ADateBean();
        eventCauseLabel = new JLabel();
        eventNameLabel = new JLabel();
        individualAgeTextField = new JTextField();
        editPlaceButton = new JButton();
        jScrollPane1 = new JScrollPane();
        eventDescriptorTextArea = new UndoTextArea();
        jScrollPane2 = new JScrollPane();
        eventCauseTextArea = new UndoTextArea();
        eventNameChoiceWidget = new ChoiceWidget();
        responsibleAgencyLabel = new JLabel();
        responsibleAgencyTextField = new JTextField();
        associateButton = new JButton();
        addressLabel = new JLabel();
        addressTextField = new JTextField();
        sourcesPanel = new JPanel();
        sourceCitationsTablePanel = new SourceCitationsTablePanel();
        notesPanel = new JPanel();
        noteCitationsTablePanel = new NoteCitationsTablePanel();
        galleryPanel = new JPanel();
        multimediaObjectCitationsTablePanel = new MultimediaObjectCitationsTablePanel();

        EventDetailEditorPanel.setMinimumSize(new Dimension(400, 217));

        placeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        placeLabel.setText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.placeLabel.text")); // NOI18N

        placeChoiceWidget.setMaximumRowCount(19);
        placeChoiceWidget.setToolTipText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.placeChoiceWidget.toolTipText")); // NOI18N
        placeChoiceWidget.setPrototypeDisplayValue("MMMMMMMMMMMMMMMMMMMMMM");

        privateRecordToggleButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/lock_open.png"))); // NOI18N
        privateRecordToggleButton.setToolTipText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.privateRecordToggleButton.toolTipText")); // NOI18N
        privateRecordToggleButton.setMaximumSize(new Dimension(26, 26));
        privateRecordToggleButton.setMinimumSize(new Dimension(26, 26));
        privateRecordToggleButton.setPreferredSize(new Dimension(26, 26));
        privateRecordToggleButton.setRolloverIcon(new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/lock_open.png"))); // NOI18N
        privateRecordToggleButton.setRolloverSelectedIcon(new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/lock.png"))); // NOI18N
        privateRecordToggleButton.setSelectedIcon(new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/lock.png"))); // NOI18N

        IndividualAgeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        IndividualAgeLabel.setText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.IndividualAgeLabel.text")); // NOI18N

        eventDescriptorLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        eventDescriptorLabel.setText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.eventDescriptorLabel.text")); // NOI18N

        dateLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        dateLabel.setText(MessageFormat.format(ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("IndividualEventPanel.dateLabel.text"), new Object[] {})); // NOI18N

        eventCauseLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        eventCauseLabel.setText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.eventCauseLabel.text")); // NOI18N

        eventNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        eventNameLabel.setText(MessageFormat.format(ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("IndividualEventPanel.eventNameLabel.text"), new Object[] {})); // NOI18N

        individualAgeTextField.setColumns(4);
        individualAgeTextField.setToolTipText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.IndividualAgeTextField.toolTipText")); // NOI18N

        editPlaceButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/place.png"))); // NOI18N
        editPlaceButton.setToolTipText(MessageFormat.format(ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("IndividualEventPanel.editPlaceButton.toolTipText"), new Object[] {})); // NOI18N
        editPlaceButton.setFocusable(false);
        editPlaceButton.setHorizontalTextPosition(SwingConstants.CENTER);
        editPlaceButton.setMaximumSize(new Dimension(26, 26));
        editPlaceButton.setMinimumSize(new Dimension(26, 26));
        editPlaceButton.setPreferredSize(new Dimension(26, 26));
        editPlaceButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        editPlaceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editPlaceButtonActionPerformed(evt);
            }
        });

        eventDescriptorTextArea.setColumns(20);
        eventDescriptorTextArea.setLineWrap(true);
        eventDescriptorTextArea.setRows(2);
        eventDescriptorTextArea.setToolTipText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.eventDescriptorTextArea.toolTipText")); // NOI18N
        eventDescriptorTextArea.setWrapStyleWord(true);
        jScrollPane1.setViewportView(eventDescriptorTextArea);

        eventCauseTextArea.setColumns(20);
        eventCauseTextArea.setLineWrap(true);
        eventCauseTextArea.setRows(2);
        eventCauseTextArea.setToolTipText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.eventCauseTextArea.toolTipText")); // NOI18N
        eventCauseTextArea.setWrapStyleWord(true);
        jScrollPane2.setViewportView(eventCauseTextArea);

        responsibleAgencyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        responsibleAgencyLabel.setText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.responsibleAgencyLabel.text")); // NOI18N

        responsibleAgencyTextField.setToolTipText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.responsibleAgencyTextField.toolTipText")); // NOI18N

        associateButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/association.png"))); // NOI18N
        associateButton.setToolTipText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.associateButton.toolTipText")); // NOI18N
        associateButton.setMaximumSize(new Dimension(26, 26));
        associateButton.setMinimumSize(new Dimension(26, 26));
        associateButton.setPreferredSize(new Dimension(26, 26));
        associateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                associateButtonActionPerformed(evt);
            }
        });

        addressLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        addressLabel.setText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.addressLabel.text")); // NOI18N

        addressTextField.setText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.addressTextField.text")); // NOI18N

        GroupLayout EventDetailEditorPanelLayout = new GroupLayout(EventDetailEditorPanel);
        EventDetailEditorPanel.setLayout(EventDetailEditorPanelLayout);
        EventDetailEditorPanelLayout.setHorizontalGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(EventDetailEditorPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(eventDescriptorLabel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(eventCauseLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(eventNameLabel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dateLabel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(placeLabel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addressLabel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(responsibleAgencyLabel, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(IndividualAgeLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, EventDetailEditorPanelLayout.createSequentialGroup()
                        .addComponent(eventNameChoiceWidget, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(associateButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(privateRecordToggleButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(GroupLayout.Alignment.TRAILING, EventDetailEditorPanelLayout.createSequentialGroup()
                        .addComponent(placeChoiceWidget, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editPlaceButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(responsibleAgencyTextField)
                    .addComponent(addressTextField)
                    .addComponent(jScrollPane1)
                    .addComponent(jScrollPane2)
                    .addComponent(individualAgeTextField, GroupLayout.Alignment.TRAILING)
                    .addComponent(aDateBean, GroupLayout.DEFAULT_SIZE, 433, Short.MAX_VALUE))
                .addContainerGap())
        );
        EventDetailEditorPanelLayout.setVerticalGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(EventDetailEditorPanelLayout.createSequentialGroup()
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(EventDetailEditorPanelLayout.createSequentialGroup()
                        .addGap(45, 45, 45)
                        .addComponent(dateLabel))
                    .addGroup(EventDetailEditorPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(eventNameChoiceWidget, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(eventNameLabel))
                            .addComponent(associateButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(privateRecordToggleButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(aDateBean, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(editPlaceButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(placeLabel, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                    .addComponent(placeChoiceWidget, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(addressTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(addressLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(responsibleAgencyLabel)
                    .addComponent(responsibleAgencyTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(EventDetailEditorPanelLayout.createSequentialGroup()
                        .addComponent(eventCauseLabel)
                        .addGap(27, 27, 27))
                    .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(eventDescriptorLabel)
                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(individualAgeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(IndividualAgeLabel))
                .addContainerGap())
        );

        GroupLayout EventDetailPanelLayout = new GroupLayout(EventDetailPanel);
        EventDetailPanel.setLayout(EventDetailPanelLayout);
        EventDetailPanelLayout.setHorizontalGroup(EventDetailPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(EventDetailEditorPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        EventDetailPanelLayout.setVerticalGroup(EventDetailPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(EventDetailEditorPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        eventInformationTabbedPane.addTab(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.EventDetailPanel.TabConstraints.tabTitle"), new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/event.png")), EventDetailPanel); // NOI18N

        sourceCitationsTablePanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        GroupLayout sourcesPanelLayout = new GroupLayout(sourcesPanel);
        sourcesPanel.setLayout(sourcesPanelLayout);
        sourcesPanelLayout.setHorizontalGroup(sourcesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(sourceCitationsTablePanel, GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
        );
        sourcesPanelLayout.setVerticalGroup(sourcesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(sourceCitationsTablePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        eventInformationTabbedPane.addTab(MessageFormat.format(ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("IndividualEventPanel.sourcesPanel.TabConstraints.tabTitle"), new Object[] {}), new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/source.png")), sourcesPanel); // NOI18N

        noteCitationsTablePanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        GroupLayout notesPanelLayout = new GroupLayout(notesPanel);
        notesPanel.setLayout(notesPanelLayout);
        notesPanelLayout.setHorizontalGroup(notesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(noteCitationsTablePanel, GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
        );
        notesPanelLayout.setVerticalGroup(notesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(noteCitationsTablePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        eventInformationTabbedPane.addTab(MessageFormat.format(ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("IndividualEventPanel.notesPanel.TabConstraints.tabTitle"), new Object[] {}), new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/note.png")), notesPanel); // NOI18N

        multimediaObjectCitationsTablePanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        GroupLayout galleryPanelLayout = new GroupLayout(galleryPanel);
        galleryPanel.setLayout(galleryPanelLayout);
        galleryPanelLayout.setHorizontalGroup(galleryPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(multimediaObjectCitationsTablePanel, GroupLayout.DEFAULT_SIZE, 580, Short.MAX_VALUE)
        );
        galleryPanelLayout.setVerticalGroup(galleryPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(multimediaObjectCitationsTablePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        eventInformationTabbedPane.addTab(MessageFormat.format(ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("IndividualEventPanel.galleryPanel.TabConstraints.tabTitle"), new Object[] {}), new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/media.png")), galleryPanel); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(eventInformationTabbedPane)
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(eventInformationTabbedPane, GroupLayout.DEFAULT_SIZE, 454, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void associateButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_associateButtonActionPerformed
        Gedcom gedcom = mRoot.getGedcom();
        int undoNb = gedcom.getUndoNb();
        final AssociationEditorPanel associationEditorPanel = new AssociationEditorPanel();
        try {
            gedcom.doUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    mAssociation = (PropertyAssociation) mRoot.addProperty("ASSO", "@");
                }
            });
            associationEditorPanel.set((Indi) mRoot, mAssociation, mEvent);

            DialogManager.ADialog associationEditorDialog = new DialogManager.ADialog(
                    NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.create.title"),
                    associationEditorPanel);
            associationEditorDialog.setDialogId(AssociationEditorPanel.class.getName());

            if (associationEditorDialog.show() == DialogDescriptor.OK_OPTION) {
                try {
                    gedcom.doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            associationEditorPanel.commit();
                        }
                    });
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
                changeListner.stateChanged(null);
            } else {
                while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                    gedcom.undoUnitOfWork(false);
                }
            }
        } catch (GedcomException ex) {
            Exceptions.printStackTrace(ex);
        }
    }//GEN-LAST:event_associateButtonActionPerformed

    private void editPlaceButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_editPlaceButtonActionPerformed
        Gedcom gedcom = mRoot.getGedcom();
        int undoNb = gedcom.getUndoNb();
        final PlaceEditorPanel placeEditorPanel = new PlaceEditorPanel();

        JButton OKButton = new JButton(NbBundle.getMessage(getClass(), "Button_Ok"));
        JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
        Object[] options = new Object[]{OKButton, cancelButton};
        placeEditorPanel.setOKButton(OKButton);
        placeEditorPanel.set(gedcom, mPlace, false);  //mAdress not used

        ADialog eventEditorDialog = new ADialog(
                NbBundle.getMessage(getClass(), "PlaceEditorPanel.edit.title"),
                placeEditorPanel);
        eventEditorDialog.setDialogId(PlaceEditorPanel.class.getName());
        eventEditorDialog.setOptions(options);
        Object o = eventEditorDialog.show();

        placeEditorPanel.close();
        if (o == OKButton) {
            placeChoiceWidget.getTextEditor().getDocument().removeDocumentListener(changeListner);
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        placeEditorPanel.commit(mEvent, mPlace);
                    }
                });
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
            mPlace = (PropertyPlace) mEvent.getProperty(PropertyPlace.TAG, false);
          
            placeChoiceWidget.setText(mPlace != null ? mPlace.getDisplayValue() : ""); // mAddress != null ? displayAddressValue(mAddress) : "");
            changeSupport.fireChange();
            placeChoiceWidget.getTextEditor().getDocument().addDocumentListener(changeListner);
        } else {
            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                gedcom.undoUnitOfWork(false);
            }
        }
    }//GEN-LAST:event_editPlaceButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel EventDetailEditorPanel;
    private JPanel EventDetailPanel;
    private JLabel IndividualAgeLabel;
    private ADateBean aDateBean;
    private JLabel addressLabel;
    private JTextField addressTextField;
    private JButton associateButton;
    private JLabel dateLabel;
    private JButton editPlaceButton;
    private JLabel eventCauseLabel;
    private JTextArea eventCauseTextArea;
    private JLabel eventDescriptorLabel;
    private JTextArea eventDescriptorTextArea;
    private JTabbedPane eventInformationTabbedPane;
    private ChoiceWidget eventNameChoiceWidget;
    private JLabel eventNameLabel;
    private JPanel galleryPanel;
    private JTextField individualAgeTextField;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private MultimediaObjectCitationsTablePanel multimediaObjectCitationsTablePanel;
    private NoteCitationsTablePanel noteCitationsTablePanel;
    private JPanel notesPanel;
    private ChoiceWidget placeChoiceWidget;
    private JLabel placeLabel;
    private JToggleButton privateRecordToggleButton;
    private JLabel responsibleAgencyLabel;
    private JTextField responsibleAgencyTextField;
    private SourceCitationsTablePanel sourceCitationsTablePanel;
    private JPanel sourcesPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * Whether the bean has changed since first listener was attached
     *
     * @return
     */
    public boolean hasChanged() {
        return mEventModified || aDateBean.hasChanged();
    }

    /**
     * Listener
     *
     * @param l
     */
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    /**
     * Listener
     *
     * @param l
     */
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public void clear(Property root) {
        eventNameChoiceWidget.setText("");
        eventDescriptorTextArea.setText("");
        eventCauseTextArea.setText("");
        aDateBean.setContext(root, null);
        individualAgeTextField.setText("");
        placeChoiceWidget.setText("");
        addressTextField.setText("");
        responsibleAgencyTextField.setText("");
    }

    /**
     * @param root
     * @param event the event to set
     */
    public void set(Property root, Property event) {
        this.mRoot = root;
        this.mEvent = event;

        changeListner.mute();
        if (!mEvent.getGedcom().getGrammar().getVersion().equals("5.5.1")) {
            privateRecordToggleButton.setVisible(false);
        }

        if (mEvent.getTag().equals("EVEN")) {
            // Event Name
            eventNameLabel.setVisible(true);
//            eventNameLabel.setText(MessageFormat.format(ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("IndividualEventPanel.eventNameLabel.text"), new Object[]{})); // NOI18N
            eventNameLabel.setText(PropertyTag2Name.getTagName(mEvent.getTag()));
            eventNameChoiceWidget.setVisible(true);
            eventNameChoiceWidget.setEditable(true);
            eventNameChoiceWidget.setValues(mEvent.getGedcom().getReferenceSet("TYPE").getKeys(mEvent.getGedcom().getCollator()));
            Property eventType = mEvent.getProperty("TYPE", false);

            if (eventType != null) {
                eventNameChoiceWidget.setText(eventType.getValue());
            } else {
                eventNameChoiceWidget.setText("");
            }
            if (mEvent.getGedcom().getGrammar().getVersion().equals(Grammar.GRAMMAR551)) {
                eventDescriptorTextArea.setText(mEvent.getValue());
                eventDescriptorTextArea.setVisible(true);
            } else {
                eventDescriptorTextArea.setText("");
                eventDescriptorTextArea.setVisible(false);

            }

        } else if (mIndividualAttributesTags.contains(event.getTag()) && !event.getTag().equals("RESI")) {
            eventNameLabel.setVisible(true);
            eventNameLabel.setText(PropertyTag2Name.getTagName(mEvent.getTag()));
            eventNameChoiceWidget.setVisible(true);
            eventNameChoiceWidget.setValues(mEvent.getGedcom().getReferenceSet(mEvent.getTag()).getKeys(mEvent.getGedcom().getCollator()));
            eventNameChoiceWidget.setText(mEvent.getValue());
            eventNameChoiceWidget.setEditable(true);
            eventDescriptorTextArea.setVisible(true);

            Property eventType = mEvent.getProperty("TYPE");
            if (eventType != null) {
                eventDescriptorTextArea.setText(eventType.getValue());
        } else {
                eventDescriptorTextArea.setText("");
            }

        } else {
            // Event Name
            eventNameLabel.setVisible(false);
            eventNameChoiceWidget.setVisible(false);
            eventNameChoiceWidget.setText("");
            eventDescriptorTextArea.setVisible(true);

            Property eventType = mEvent.getProperty("TYPE");
            if (eventType != null) {
                eventDescriptorTextArea.setText(eventType.getValue());
            } else {
            eventDescriptorTextArea.setText("");
        }

        }

        Property eventCause = mEvent.getProperty("CAUS", false);
        if (eventCause != null) {
            eventCauseTextArea.setText(eventCause.getValue());
        } else {
            eventCauseTextArea.setText("");
        }

        /*
         * +1 RESN <RESTRICTION_NOTICE>
         */
        Property restrictionNotice = mEvent.getProperty("RESN", true);
        if (restrictionNotice != null) {
            privateRecordToggleButton.setSelected(true);
        } else {
            privateRecordToggleButton.setSelected(false);
        }

        final Property p = mEvent.getProperty("DATE", false);
        mDate = (PropertyDate) (p instanceof PropertyDate ? p : null);

        if (mDate == null) {
            aDateBean.setContext(mEvent, null);
        } else {
            aDateBean.setContext(mDate);
        }

        if (!mEvent.getTag().equals("BIRT")) {
            IndividualAgeLabel.setVisible(true);
            individualAgeTextField.setVisible(true);
            PropertyAge age = (PropertyAge) mEvent.getProperty("AGE", false);
            if (age != null) {
                individualAgeTextField.setText(age.getDisplayValue());
            } else {
                if (mDate != null && mDate.isValid()) {
                    Delta deltaAge = ((Indi) mRoot).getAge(mDate.getStart());
                    if (deltaAge != null) {
                        individualAgeTextField.setText(deltaAge.toString());
                    } else {
                        individualAgeTextField.setText("");
                    }
                } else {
                    individualAgeTextField.setText("");
                }
            }
        } else {
            IndividualAgeLabel.setVisible(false);
            individualAgeTextField.setVisible(false);
        }

        mPlace = (PropertyPlace) mEvent.getProperty(PropertyPlace.TAG, false);
     
        placeChoiceWidget.setValues(mRoot.getGedcom().getReferenceSet("PLAC").getKeys(mRoot.getGedcom().getCollator()));
        if (mPlace != null) { 
            placeChoiceWidget.setText(mPlace.getDisplayValue());
        } else {
            placeChoiceWidget.setText("");
        }

        Property[] sourcesList = mEvent.getProperties("SOUR");
        sourceCitationsTablePanel.set(mEvent, Arrays.asList(sourcesList));

        noteCitationsTablePanel.set(mEvent, Arrays.asList(mEvent.getProperties("NOTE")));

        multimediaObjectCitationsTablePanel.set(mEvent, Arrays.asList(mEvent.getProperties("OBJE")));

        Property address = mEvent.getProperty("ADDR");
        if (address != null) {
            addressTextField.setText(address.getValue());
        } else {
            addressTextField.setText("");
        }

        Property responsibleAgency = mEvent.getProperty("AGNC");
        if (responsibleAgency != null) {
            responsibleAgencyTextField.setText(responsibleAgency.getValue());
        } else {
            responsibleAgencyTextField.setText("");
        }

        mEventModified = false;
        mEventCauseModified = false;
        mIndividualAgeModified = false;
        mEventNameModified = false;
        mEventTypeModified = false;
        mPlaceModified = false;
        mAddressModified = false;
        mResponsibleAgencyModified = false;

        changeListner.unmute();
    }

    public void commit() {
        if (mRoot != null) {
            if (hasChanged()) {
                mEventModified = false;

                if (aDateBean.hasChanged()) {
                    try {
                        aDateBean.commit();
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                if (mEvent.getTag().equals("EVEN") || mEvent.getTag().equals("FACT")) {
                    if (mEventNameModified) {
                        mEventNameModified = false;
                        Property eventType = mEvent.getProperty("TYPE", false);
                        if (eventType != null) {
                            eventType.setValue(eventNameChoiceWidget.getText());
                        } else {
                            mEvent.addProperty("TYPE", eventNameChoiceWidget.getText());
                        }
                    }

                    if (mEventTypeModified) {
                        mEventTypeModified = false;
                        if (mEvent.getGedcom().getGrammar().getVersion().equals(Grammar.GRAMMAR551)) {
                            mEvent.setValue(eventDescriptorTextArea.getText());
                        }
                    }
                } else if (mIndividualAttributesTags.contains(mEvent.getTag())) {
                    if (mEventNameModified) {
                        mEventNameModified = false;
                        mEvent.setValue(eventNameChoiceWidget.getText());
                    }
                    if (mEventTypeModified) {
                        mEventTypeModified = false;
                        Property eventType = mEvent.getProperty("TYPE", false);
                        if (eventType != null) {
                            eventType.setValue(eventDescriptorTextArea.getText());
                        } else {
                            mEvent.addProperty("TYPE", eventDescriptorTextArea.getText());
                        }
                    }

                } else {
                    if (mEventTypeModified) {
                        mEventTypeModified = false;
                        Property eventType = mEvent.getProperty("TYPE", false);
                        if (eventType != null) {
                            eventType.setValue(eventDescriptorTextArea.getText());
                        } else {
                            mEvent.addProperty("TYPE", eventDescriptorTextArea.getText());
                        }
                    }

                    final Property p = mEvent.getProperty("DATE", false);
                    PropertyDate date = (PropertyDate) (p instanceof PropertyDate ? p : null);
                    if (date != null && date.isValid()) {
                        mEvent.setValue("");
                    } else {
                        //FIXME: use Grammar for that?
                        Logger.getLogger("ancestris").info(mEvent.getTag());
                        if (mEvent.getTag().matches(
                                "(BIRT|CHR|DEAT|BURI|CREM|ADOP|BAPM|BARM|BASM|BLES|CHRA|CONF|FCOM|ORDN|NATU|EMIG|IMMI|CENS|PROB|WILL|GRAD|RETI)")) {
                            mEvent.setValue("y");
                        }
                    }
                }

                if (mEventCauseModified) {
                    mEventCauseModified = false;
                    String causeText = eventCauseTextArea.getText().replaceAll("\n", " ");
                    Property eventCause = mEvent.getProperty("CAUS", false);
                    if (causeText.length() > 0) {
                        if (eventCause == null) {
                            mEvent.addProperty("CAUS", causeText);
                        } else {
                            eventCause.setValue(causeText);
                        }
                    } else if (eventCause != null) {
                        mEvent.delProperty(eventCause);
                    }
                }

                Property restrictionNotice = mEvent.getProperty("RESN", true);
                if (privateRecordToggleButton.isSelected()) {
                    if (restrictionNotice == null) {
                        mEvent.addProperty("RESN", "confidential");
                    }
                } else {
                    if (restrictionNotice != null) {
                        mEvent.delProperty(restrictionNotice);
                    }
                }

                if (mIndividualAgeModified) {
                    mIndividualAgeModified = false;
                    PropertyAge age = (PropertyAge) mEvent.getProperty("AGE", false);
                    if (age != null) {
                        age.setValue(individualAgeTextField.getText());
                    } else {
                        mEvent.addProperty("AGE", individualAgeTextField.getText());
                    }
                }

                if (mPlaceModified) {
                    mPlaceModified = false;
                    PropertyPlace place = (PropertyPlace) mEvent.getProperty("PLAC", false);
                    if (place != null) {
                        place.setValue(placeChoiceWidget.getText());
                    } else {
                        place = (PropertyPlace) mEvent.addProperty("PLAC", placeChoiceWidget.getText());
                    }
                    place.setCoordinates();
                }

                if (mAddressModified) {
                    mAddressModified = false;
                    Property address = mEvent.getProperty("ADDR", false);
                    if (address != null) {
                        address.setValue(addressTextField.getText());
                    } else {
                        mEvent.addProperty("ADDR", addressTextField.getText());
                    }
                }

                if (mResponsibleAgencyModified) {
                    mResponsibleAgencyModified = false;
                    Property responsibleAgency = mEvent.getProperty("AGNC", false);
                    if (responsibleAgency != null) {
                        responsibleAgency.setValue(responsibleAgencyTextField.getText());
                    } else {
                        mEvent.addProperty("AGNC", responsibleAgencyTextField.getText());
                    }
                }
            }
        }
    }

    public class ChangeListner implements DocumentListener, ChangeListener, ActionListener {

        private boolean mute = false;

        @Override
        public void insertUpdate(DocumentEvent de) {
            if (!mute) {
                mEventModified = true;

                Object propertyName = de.getDocument().getProperty("name");
                if (propertyName != null) {
                    if (propertyName.equals("eventNameTextField")) {
                        mEventNameModified = true;
                    }
                    if (propertyName.equals("eventCauseTextArea")) {
                        mEventCauseModified = true;
                    }
                    if (propertyName.equals("eventDescriptorTextArea")) {
                        mEventTypeModified = true;
                    }
                    if (propertyName.equals("individualAgeTextField")) {
                        mIndividualAgeModified = true;
                    }
                    if (propertyName.equals("placeChoiceWidget")) {
                        mPlaceModified = true;
                    }
                    if (propertyName.equals("addressTextField")) {
                        mAddressModified = true;
                    }
                    if (propertyName.equals("responsibleAgencyTextField")) {
                        mResponsibleAgencyModified = true;
                    }
                    changeSupport.fireChange();
                }
            }
        }

        @Override
        public void removeUpdate(DocumentEvent de) {
            if (!mute) {
                mEventModified = true;

                Object propertyName = de.getDocument().getProperty("name");
                if (propertyName != null) {
                    if (propertyName.equals("eventNameTextField")) {
                        mEventTypeModified = true;
                    }
                    if (propertyName.equals("eventCauseTextArea")) {
                        mEventCauseModified = true;
                    }
                    if (propertyName.equals("eventDescriptorTextArea")) {
                        mEventNameModified = true;
                    }
                    if (propertyName.equals("individualAgeTextField")) {
                        mIndividualAgeModified = true;
                    }
                    if (propertyName.equals("placeChoiceWidget")) {
                        mPlaceModified = true;
                    }
                    if (propertyName.equals("addressTextField")) {
                        mAddressModified = true;
                    }
                    if (propertyName.equals("responsibleAgencyTextField")) {
                        mResponsibleAgencyModified = true;
                    }
                    changeSupport.fireChange();
                }
            }
        }

        @Override
        public void changedUpdate(DocumentEvent de) {
            if (!mute) {
                mEventModified = true;

                Object propertyName = de.getDocument().getProperty("name");
                if (propertyName != null) {
                    if (propertyName.equals("eventNameTextField")) {
                        mEventTypeModified = true;
                    }
                    if (propertyName.equals("eventCauseTextArea")) {
                        mEventCauseModified = true;
                    }
                    if (propertyName.equals("eventDescriptorTextArea")) {
                        mEventNameModified = true;
                    }
                    if (propertyName.equals("individualAgeTextField")) {
                        mIndividualAgeModified = true;
                    }
                    if (propertyName.equals("placeChoiceWidget")) {
                        mPlaceModified = true;
                    }
                    if (propertyName.equals("addressTextField")) {
                        mAddressModified = true;
                    }
                    if (propertyName.equals("responsibleAgencyTextField")) {
                        mResponsibleAgencyModified = true;
                    }
                    changeSupport.fireChange();
                }
            }
        }

        @Override
        public void stateChanged(ChangeEvent ce) {
            if (!mute) {
                mEventModified = true;
                mEventNameModified = true;
                changeSupport.fireChange();
            }
        }

        public void mute() {
            mute = true;
        }

        public void unmute() {
            mute = false;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            if (!mute) {
                mEventModified = true;
                mEventNameModified = true;
                changeSupport.fireChange();
            }
        }
    }
}
