package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.beans.ADateBean;
import ancestris.modules.editors.genealogyeditor.utilities.PropertyTag2Name;
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
//            add("FACT"); not defined in gedcom xml definition files
        }
    };
    private Property mEvent = null;
    private Property mRoot;
    private Property mAddress;
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
        responsibleAgencyTextField.getDocument().addDocumentListener(changeListner);
        responsibleAgencyTextField.getDocument().putProperty("name", "responsibleAgencyTextField");
        sourceCitationsTablePanel.addChangeListener(changeListner);
        noteCitationsTablePanel.addChangeListener(changeListner);
        multimediaObjectCitationsTablePanel.addChangeListener(changeListner);
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
        placeTextField = new JTextField();
        privateRecordToggleButton = new JToggleButton();
        IndividualAgeLabel = new JLabel();
        eventDescriptorLabel = new JLabel();
        dateLabel = new JLabel();
        aDateBean = new ADateBean();
        eventCauseLabel = new JLabel();
        eventNameLabel = new JLabel();
        individualAgeTextField = new JTextField();
        linkToPlaceButton = new JButton();
        editPlaceButton = new JButton();
        addPlaceButton = new JButton();
        jScrollPane1 = new JScrollPane();
        eventDescriptorTextArea = new JTextArea();
        jScrollPane2 = new JScrollPane();
        eventCauseTextArea = new JTextArea();
        eventNameChoiceWidget = new ChoiceWidget();
        responsibleAgencyLabel = new JLabel();
        responsibleAgencyTextField = new JTextField();
        associateButton = new JButton();
        sourcesPanel = new JPanel();
        sourceCitationsTablePanel = new SourceCitationsTablePanel();
        notesPanel = new JPanel();
        noteCitationsTablePanel = new NoteCitationsTablePanel();
        galleryPanel = new JPanel();
        multimediaObjectCitationsTablePanel = new MultimediaObjectCitationsTablePanel();

        EventDetailEditorPanel.setMinimumSize(new Dimension(634, 217));

        placeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        placeLabel.setText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.placeLabel.text")); // NOI18N

        placeTextField.setEditable(false);
        placeTextField.setText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.placeTextField.text")); // NOI18N

        privateRecordToggleButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/lock_open.png"))); // NOI18N
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

        linkToPlaceButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/link_add.png"))); // NOI18N
        linkToPlaceButton.setToolTipText(MessageFormat.format(ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("IndividualEventPanel.linkToPlaceButton.toolTipText"), new Object[] {})); // NOI18N
        linkToPlaceButton.setFocusable(false);
        linkToPlaceButton.setHorizontalTextPosition(SwingConstants.CENTER);
        linkToPlaceButton.setMaximumSize(new Dimension(26, 26));
        linkToPlaceButton.setMinimumSize(new Dimension(26, 26));
        linkToPlaceButton.setPreferredSize(new Dimension(26, 26));
        linkToPlaceButton.setVerticalTextPosition(SwingConstants.BOTTOM);
        linkToPlaceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                linkToPlaceButtonActionPerformed(evt);
            }
        });

        editPlaceButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit.png"))); // NOI18N
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

        addPlaceButton.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_add.png"))); // NOI18N
        addPlaceButton.setToolTipText(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.addPlaceButton.toolTipText")); // NOI18N
        addPlaceButton.setMaximumSize(new Dimension(26, 26));
        addPlaceButton.setMinimumSize(new Dimension(26, 26));
        addPlaceButton.setPreferredSize(new Dimension(26, 26));
        addPlaceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addPlaceButtonActionPerformed(evt);
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

        GroupLayout EventDetailEditorPanelLayout = new GroupLayout(EventDetailEditorPanel);
        EventDetailEditorPanel.setLayout(EventDetailEditorPanelLayout);
        EventDetailEditorPanelLayout.setHorizontalGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(EventDetailEditorPanelLayout.createSequentialGroup()
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(responsibleAgencyLabel)
                    .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(eventCauseLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(placeLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(dateLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(eventNameLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(eventDescriptorLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(IndividualAgeLabel, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, EventDetailEditorPanelLayout.createSequentialGroup()
                        .addComponent(placeTextField)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addPlaceButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editPlaceButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(linkToPlaceButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(GroupLayout.Alignment.TRAILING, EventDetailEditorPanelLayout.createSequentialGroup()
                        .addComponent(eventNameChoiceWidget, GroupLayout.PREFERRED_SIZE, 261, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(associateButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(privateRecordToggleButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addGroup(EventDetailEditorPanelLayout.createSequentialGroup()
                        .addComponent(individualAgeTextField, GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                        .addGap(251, 251, 251))
                    .addComponent(responsibleAgencyTextField)
                    .addComponent(aDateBean, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        EventDetailEditorPanelLayout.setVerticalGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(EventDetailEditorPanelLayout.createSequentialGroup()
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(eventNameLabel)
                        .addComponent(eventNameChoiceWidget, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(associateButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(privateRecordToggleButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(EventDetailEditorPanelLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(dateLabel))
                    .addGroup(EventDetailEditorPanelLayout.createSequentialGroup()
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(aDateBean, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(linkToPlaceButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(editPlaceButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(placeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(placeLabel, GroupLayout.PREFERRED_SIZE, 15, GroupLayout.PREFERRED_SIZE)
                    .addComponent(addPlaceButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(responsibleAgencyLabel)
                    .addComponent(responsibleAgencyTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(eventCauseLabel)
                    .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(eventDescriptorLabel)
                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EventDetailEditorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(IndividualAgeLabel)
                    .addComponent(individualAgeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        );

        GroupLayout EventDetailPanelLayout = new GroupLayout(EventDetailPanel);
        EventDetailPanel.setLayout(EventDetailPanelLayout);
        EventDetailPanelLayout.setHorizontalGroup(EventDetailPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(EventDetailEditorPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        EventDetailPanelLayout.setVerticalGroup(EventDetailPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(EventDetailEditorPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        eventInformationTabbedPane.addTab(NbBundle.getMessage(IndividualEventPanel.class, "IndividualEventPanel.EventDetailPanel.TabConstraints.tabTitle"), new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Event.png")), EventDetailPanel); // NOI18N

        sourceCitationsTablePanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        GroupLayout sourcesPanelLayout = new GroupLayout(sourcesPanel);
        sourcesPanel.setLayout(sourcesPanelLayout);
        sourcesPanelLayout.setHorizontalGroup(sourcesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(sourceCitationsTablePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        sourcesPanelLayout.setVerticalGroup(sourcesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(sourceCitationsTablePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        eventInformationTabbedPane.addTab(MessageFormat.format(ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("IndividualEventPanel.sourcesPanel.TabConstraints.tabTitle"), new Object[] {}), new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/source.png")), sourcesPanel); // NOI18N

        noteCitationsTablePanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        GroupLayout notesPanelLayout = new GroupLayout(notesPanel);
        notesPanel.setLayout(notesPanelLayout);
        notesPanelLayout.setHorizontalGroup(notesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(noteCitationsTablePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        notesPanelLayout.setVerticalGroup(notesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(noteCitationsTablePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        eventInformationTabbedPane.addTab(MessageFormat.format(ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("IndividualEventPanel.notesPanel.TabConstraints.tabTitle"), new Object[] {}), new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Note.png")), notesPanel); // NOI18N

        multimediaObjectCitationsTablePanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        GroupLayout galleryPanelLayout = new GroupLayout(galleryPanel);
        galleryPanel.setLayout(galleryPanelLayout);
        galleryPanelLayout.setHorizontalGroup(galleryPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(multimediaObjectCitationsTablePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        galleryPanelLayout.setVerticalGroup(galleryPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(multimediaObjectCitationsTablePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        eventInformationTabbedPane.addTab(MessageFormat.format(ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/panels/Bundle").getString("IndividualEventPanel.galleryPanel.TabConstraints.tabTitle"), new Object[] {}), new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Media.png")), galleryPanel); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(eventInformationTabbedPane)
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(eventInformationTabbedPane)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void linkToPlaceButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_linkToPlaceButtonActionPerformed
        PlacesTablePanel placesTablePanel = new PlacesTablePanel(mRoot.getGedcom());
        DialogManager.ADialog placesTableDialog = new DialogManager.ADialog(
                NbBundle.getMessage(PlacesTablePanel.class, "PlacesTableDialog.title.link"),
                placesTablePanel);
        placesTableDialog.setDialogId(PlacesTablePanel.class.getName());

        if (placesTableDialog.show() == DialogDescriptor.OK_OPTION) {
            final PropertyPlace selectedPlace = placesTablePanel.getSelectedPlace();
            if (selectedPlace != null) {
                try {
                    mRoot.getGedcom().doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            if (mPlace == null) {
                                mPlace = (PropertyPlace) mEvent.addProperty("PLAC", selectedPlace.format("all"));
                            } else {
                                mPlace.setValue(selectedPlace.format("all"));
                            }

                            Property map;
                            Property selectedPlaceMap;
                            if (mPlace.getGedcom().getGrammar().getVersion().equals("5.5.1") == true) {
                                selectedPlaceMap = selectedPlace.getProperty("MAP");
                                if (selectedPlaceMap != null) {
                                    map = mPlace.getProperty("MAP");
                                    if (map == null) {
                                        map = mPlace.addProperty("MAP", "");
                                        map.addProperty("LATI", selectedPlaceMap.getProperty("LATI").getValue());
                                        map.addProperty("LONG", selectedPlaceMap.getProperty("LONG").getValue());
                                    } else {
                                        Property latitude = map.getProperty("LATI");
                                        if (latitude == null) {
                                            map.addProperty("LATI", selectedPlaceMap.getProperty("LATI").getValue());
                                        } else {
                                            latitude.setValue(selectedPlaceMap.getProperty("LATI").getValue());
                                        }
                                        Property longitude = map.getProperty("LONG");
                                        if (longitude == null) {
                                            map.addProperty("LONG", selectedPlaceMap.getProperty("LONG").getValue());
                                        } else {
                                            longitude.setValue(selectedPlaceMap.getProperty("LONG").getValue());
                                        }
                                    }
                                } else {
                                    map = mPlace.getProperty("MAP");
                                    if (map != null) {
                                        mPlace.delProperty(map);
                                    }
                                }
                            } else {
                                selectedPlaceMap = selectedPlace.getProperty("_MAP");
                                if (selectedPlaceMap != null) {
                                    map = mPlace.getProperty("_MAP");
                                    if (map == null) {
                                        map = mPlace.addProperty("_MAP", "");
                                    }
                                    Property latitude = map.getProperty("_LATI");
                                    if (latitude == null) {
                                        map.addProperty("_LATI", selectedPlaceMap.getProperty("_LATI").getValue());
                                    } else {
                                        latitude.setValue(selectedPlaceMap.getProperty("_LATI").getValue());
                                    }
                                    Property longitude = map.getProperty("_LONG");
                                    if (longitude == null) {
                                        map.addProperty("_LONG", selectedPlaceMap.getProperty("_LONG").getValue());
                                    } else {
                                        longitude.setValue(selectedPlaceMap.getProperty("_LONG").getValue());
                                    }
                                } else {
                                    map = mPlace.getProperty("_MAP");
                                    if (map != null) {
                                        mPlace.delProperty(map);
                                    }
                                }
                            }
                        }
                    }); // end of doUnitOfWork

                    placeTextField.setText(mPlace.getDisplayValue());

                    addPlaceButton.setVisible(false);
                    editPlaceButton.setVisible(true);
                    changeSupport.fireChange();
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_linkToPlaceButtonActionPerformed

    private void editPlaceButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_editPlaceButtonActionPerformed
        Gedcom gedcom = mRoot.getGedcom();
        int undoNb = gedcom.getUndoNb();
        final PlaceEditorPanel placeEditorPanel = new PlaceEditorPanel();
        placeEditorPanel.set(mEvent, mPlace, mAddress);

        ADialog eventEditorDialog = new ADialog(
                NbBundle.getMessage(
                        PlaceEditorPanel.class, "PlaceEditorPanel.edit.title"),
                placeEditorPanel);
        eventEditorDialog.setDialogId(PlaceEditorPanel.class.getName());

        if (eventEditorDialog.show() == DialogDescriptor.OK_OPTION) {
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        placeEditorPanel.commit();
                    }
                });
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
            mPlace = (PropertyPlace) mEvent.getProperty(PropertyPlace.TAG, false);
            mAddress = mEvent.getProperty("ADDR", false);
            placeTextField.setText(mPlace != null ? mPlace.getDisplayValue() : mAddress.getDisplayValue());
            changeSupport.fireChange();
        } else {
            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                gedcom.undoUnitOfWork(false);
            }
        }
    }//GEN-LAST:event_editPlaceButtonActionPerformed

    private void addPlaceButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_addPlaceButtonActionPerformed
        Gedcom gedcom = mRoot.getGedcom();
        final PlaceEditorPanel placeEditorPanel = new PlaceEditorPanel();
        int undoNb = gedcom.getUndoNb();

        placeEditorPanel.set(mEvent, mPlace, mAddress);

        ADialog eventEditorDialog = new ADialog(
                NbBundle.getMessage(
                        PlaceEditorPanel.class, "PlaceEditorPanel.edit.title"),
                placeEditorPanel);
        eventEditorDialog.setDialogId(PlaceEditorPanel.class.getName());

        if (eventEditorDialog.show() == DialogDescriptor.OK_OPTION) {
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        placeEditorPanel.commit();
                    }
                });
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
            mPlace = (PropertyPlace) mEvent.getProperty(PropertyPlace.TAG, false);
            mAddress = mEvent.getProperty("ADDR", false);
            placeTextField.setText(mPlace != null ? mPlace.getDisplayValue() : mAddress.getDisplayValue());
            addPlaceButton.setVisible(false);
            editPlaceButton.setVisible(true);
            changeSupport.fireChange();
        } else {
            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                gedcom.undoUnitOfWork(false);
            }
        }
    }//GEN-LAST:event_addPlaceButtonActionPerformed

    private void associateButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_associateButtonActionPerformed
        Gedcom gedcom = mRoot.getGedcom();
        final Entity entity = mRoot.getEntity();
        int undoNb = gedcom.getUndoNb();
        AssociationEditorPanel associationEditorPanel = new AssociationEditorPanel();
        associationEditorPanel.set((Indi) mRoot, mAssociation);

        DialogManager.ADialog associationEditorDialog = new DialogManager.ADialog(
                NbBundle.getMessage(AssociationEditorPanel.class, "AssociationEditorPanel.create.title"),
                associationEditorPanel);
        associationEditorDialog.setDialogId(AssociationEditorPanel.class.getName());

        if (associationEditorDialog.show() == DialogDescriptor.OK_OPTION) {
            associationEditorPanel.commit();
            changeListner.stateChanged(null);
        } else {
            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                gedcom.undoUnitOfWork(false);
            }
        }
    }//GEN-LAST:event_associateButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JPanel EventDetailEditorPanel;
    private JPanel EventDetailPanel;
    private JLabel IndividualAgeLabel;
    private ADateBean aDateBean;
    private JButton addPlaceButton;
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
    private JButton linkToPlaceButton;
    private MultimediaObjectCitationsTablePanel multimediaObjectCitationsTablePanel;
    private NoteCitationsTablePanel noteCitationsTablePanel;
    private JPanel notesPanel;
    private JLabel placeLabel;
    private JTextField placeTextField;
    private JToggleButton privateRecordToggleButton;
    private JLabel responsibleAgencyLabel;
    private JTextField responsibleAgencyTextField;
    private SourceCitationsTablePanel sourceCitationsTablePanel;
    private JPanel sourcesPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * Whether the bean has changed since first listener was attached
     */
    public boolean hasChanged() {
        return mEventModified;
    }

    /**
     * Listener
     */
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    /**
     * Listener
     */
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    /**
     * @param event the event to set
     */
    public void set(Property root, Property event) {
        this.mRoot = root;
        this.mEvent = event;

        changeListner.mute();
        if (!mEvent.getGedcom().getGrammar().getVersion().equals("5.5.1")) {
            privateRecordToggleButton.setVisible(false);
        }

        if (mEvent.getTag().equals("EVEN") || mEvent.getTag().equals("FACT")) {
            // Event Name
            eventNameLabel.setVisible(true);
            eventNameChoiceWidget.setVisible(true);
            eventNameChoiceWidget.setEditable(true);
            eventNameChoiceWidget.setValues(mEvent.getGedcom().getReferenceSet("TYPE").getKeys(mEvent.getGedcom().getCollator()));
            Property eventType = mEvent.getProperty("TYPE", false);
            if (eventType != null) {
                eventNameChoiceWidget.setText(eventType.getValue());
            } else {
                eventNameChoiceWidget.setText("");
            }

            eventCauseTextArea.setText(mEvent.getValue());
        } else if (mIndividualAttributesTags.contains(event.getTag()) && !event.getTag().equals("RESI")) {
            eventNameLabel.setVisible(true);
            eventNameChoiceWidget.setVisible(true);
            eventNameChoiceWidget.setValues(mEvent.getGedcom().getReferenceSet(mEvent.getTag()).getKeys(mEvent.getGedcom().getCollator()));
            eventNameLabel.setText(PropertyTag2Name.getTagName(mEvent.getTag()));
            eventNameChoiceWidget.setText(mEvent.getValue());
            eventNameChoiceWidget.setEditable(true);

            Property eventType = mEvent.getProperty("TYPE");
            if (eventType != null) {
                eventDescriptorTextArea.setText(eventType.getValue());
            } else {
                eventDescriptorTextArea.setText("");
            }

            Property eventCause = mEvent.getProperty("CAUS", false);
            if (eventCause != null) {
                eventCauseTextArea.setText(eventCause.getValue());
            } else {
                eventCauseTextArea.setText("");
            }
        } else {
            // Event Name
            eventNameLabel.setVisible(false);
            eventNameChoiceWidget.setVisible(false);
            Property eventType = mEvent.getProperty("TYPE");
            if (eventType != null) {
                eventDescriptorTextArea.setText(eventType.getValue());
            } else {
                eventDescriptorTextArea.setText("");
            }

            Property eventCause = mEvent.getProperty("CAUS", false);
            if (eventCause != null) {
                eventCauseTextArea.setText(eventCause.getValue());
            } else {
                eventCauseTextArea.setText("");
            }
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

        mDate = (PropertyDate) mEvent.getProperty("DATE", false);
//        if (mDate == null) {
//            try {
//                mEvent.getGedcom().doUnitOfWork(new UnitOfWork() {
//
//                    @Override
//                    public void perform(Gedcom gedcom) throws GedcomException {
//                        mDate = (PropertyDate) mEvent.addProperty("DATE", "");
//                    }
//                }); // end of doUnitOfWork
//            } catch (GedcomException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
        if (mDate == null) {
            aDateBean.setContext(mEvent, null);
        } else {
            aDateBean.setContext(mDate);
        }

        if (!mEvent.getTag().equals("BIRT") && mDate != null) {
            IndividualAgeLabel.setVisible(true);
            individualAgeTextField.setVisible(true);
            PropertyAge age = (PropertyAge) mEvent.getProperty("AGE", false);
            if (age != null) {
                individualAgeTextField.setText(age.getDisplayValue());
                if (mDate.isValid() && ((Indi) mRoot).getBirthDate().isValid()) {
                    individualAgeTextField.setEditable(false);
                    individualAgeTextField.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveBackground"));
                } else {
                    individualAgeTextField.setEditable(true);
                    individualAgeTextField.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.background"));
                }
            } else {
                if (mDate != null && mDate.isValid()) {
                    Delta deltaAge = ((Indi) mRoot).getAge(mDate.getStart());
                    if (deltaAge != null) {
                        individualAgeTextField.setText(deltaAge.toString());
                        individualAgeTextField.setEditable(false);
                        individualAgeTextField.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.inactiveBackground"));
                    } else {
                        individualAgeTextField.setText("");
                        individualAgeTextField.setEditable(true);
                        individualAgeTextField.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.background"));
                    }
                } else {
                    individualAgeTextField.setText("");
                    individualAgeTextField.setEditable(true);
                    individualAgeTextField.setBackground(javax.swing.UIManager.getDefaults().getColor("TextField.background"));
                }
            }
        } else {
            IndividualAgeLabel.setVisible(false);
            individualAgeTextField.setVisible(false);
        }

        mPlace = (PropertyPlace) mEvent.getProperty(PropertyPlace.TAG, false);
        mAddress = mEvent.getProperty("ADDR", false);
        if (mPlace != null || mAddress != null) {
            placeTextField.setText(mPlace != null ? mPlace.getDisplayValue() : mAddress.getDisplayValue());
            addPlaceButton.setVisible(false);
            editPlaceButton.setVisible(true);
        } else {
            placeTextField.setText("");
            addPlaceButton.setVisible(true);
            editPlaceButton.setVisible(false);
        }

        Property[] sourcesList = mEvent.getProperties("SOUR");
        sourceCitationsTablePanel.set(mEvent, Arrays.asList(sourcesList));

        noteCitationsTablePanel.set(mEvent, Arrays.asList(mEvent.getProperties("NOTE")));

        multimediaObjectCitationsTablePanel.set(mEvent, Arrays.asList(mEvent.getProperties("OBJE")));

        Property responsibleAgency = mEvent.getProperty("AGNC");
        if (responsibleAgency != null) {
            responsibleAgencyTextField.setText(responsibleAgency.getValue());
        } else {
            responsibleAgencyTextField.setText("");
        }

        changeListner.unmute();
    }

    public void commit() {
        if (mRoot != null) {
            if (mEventModified == true || aDateBean.hasChanged()) {
                mEventModified = false;
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
                    if (mEventCauseModified) {
                        mEventCauseModified = false;
                        mEvent.setValue(eventCauseTextArea.getText());
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
                    if (mEventCauseModified) {
                        mEventCauseModified = false;
                        String causeText = eventCauseTextArea.getText();
                        Property eventCause = mEvent.getProperty("CAUS", false);
                        if (causeText.length() > 0) {
                            if (eventCause == null) {
                                mEvent.addProperty("CAUS", causeText);
                            } else {
                                eventCause.setValue(causeText);
                            }
                        } else if (eventCause != null) {
                            mRoot.delProperty(eventCause);
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
                    if (mEventCauseModified) {
                        mEventCauseModified = false;
                        String causeText = eventCauseTextArea.getText();
                        Property eventCause = mEvent.getProperty("CAUS", false);
                        if (causeText.length() > 0) {
                            if (eventCause == null) {
                                mEvent.addProperty("CAUS", causeText);
                            } else {
                                eventCause.setValue(causeText);
                            }
                        } else if (eventCause != null) {
                            mRoot.delProperty(eventCause);
                        }
                    }
                }

                if (aDateBean.hasChanged()) {
                    try {
                        aDateBean.commit();
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
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
                        age.setValue(individualAgeTextField.getText() + " y");
                    } else {
                        mEvent.addProperty("AGE", individualAgeTextField.getText() + " y");
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
//        gedcomPlacePanel.commit();
//        addressPanel.commit();
        }
    }

    public class ChangeListner implements DocumentListener, ChangeListener {

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
    }
}
