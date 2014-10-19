package ancestris.modules.editors.genealogyeditor.editors;

import ancestris.modules.editors.genealogyeditor.utilities.PropertyTag2Name;
import ancestris.modules.editors.genealogyeditor.models.EventsListModel;
import ancestris.modules.editors.genealogyeditor.panels.FamiliesReferenceTreeTablePanel;
import ancestris.modules.editors.genealogyeditor.panels.NamesTablePanel;
import ancestris.util.swing.DialogManager;
import genj.gedcom.*;
import genj.view.ViewContext;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */

/*
 * n @XREF:INDI@ INDI
 * +1 RESN <RESTRICTION_NOTICE>
 * +1 <<PERSONAL_NAME_STRUCTURE>>
 * +1 SEX <SEX_VALUE>
 * +1 <<INDIVIDUAL_EVENT_STRUCTURE>>
 * +1 <<INDIVIDUAL_ATTRIBUTE_STRUCTURE>>
 * +1 <<LDS_INDIVIDUAL_ORDINANCE>>
 * +1 <<CHILD_TO_FAMILY_LINK>>
 * +1 <<SPOUSE_TO_FAMILY_LINK>>
 * +1 SUBM @<XREF:SUBM>@
 * +1 <<ASSOCIATION_STRUCTURE>>
 * +1 ALIA @<XREF:INDI>@
 * +1 ANCI @<XREF:SUBM>@
 * +1 DESI @<XREF:SUBM>@
 * +1 RFN <PERMANENT_RECORD_FILE_NUMBER>
 * +1 AFN <ANCESTRAL_FILE_NUMBER>
 * +1 REFN <USER_REFERENCE_NUMBER>
 * +2 TYPE <USER_REFERENCE_TYPE>
 * +1 RIN <AUTOMATED_RECORD_ID>
 * +1 <<CHANGE_DATE>>
 * +1 <<NOTE_STRUCTURE>>
 * +1 <<SOURCE_CITATION>>
 * +1 <<MULTIMEDIA_LINK>>
 */
public final class IndividualEditor extends EntityEditor {

    private Context context;
    private Indi mIndividual;
    private Property mEvent = null;
    private Property mMultiMediaObject;
    private boolean updateOnGoing = false;
    private final EventsListModel mEventsListModel = new EventsListModel();
    private static final ArrayList<String> mIndividualEventsTags = new ArrayList<String>() {
        {
            /*
             * INDIVIDUAL_EVENT
             */
            add("BIRT");
            add("CHR");
            add("DEAT");
            add("BURI");
            add("CREM");
            add("ADOP");
            add("BAPM");
            add("BARM");
            add("BASM");
            add("BLES");
            add("CHRA");
            add("CONF");
            add("FCOM");
            add("ORDN");
            add("NATU");
            add("EMIG");
            add("IMMI");
            add("CENS");
            add("PROB");
            add("WILL");
            add("GRAD");
            add("RETI");
            add("EVEN");
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
    private DefaultComboBoxModel<String> mEventsModel = new DefaultComboBoxModel<String>(new String[]{});
    private NamesTablePanel namesTablePanel;

    /**
     * Creates new form IndividualEditor
     */
    public IndividualEditor() {
        this(false);
    }

    public IndividualEditor(boolean isNew) {
        super(isNew);
        initComponents();
        eventsList.getSelectionModel().addListSelectionListener(new EventsListSelectionHandler());
        individualEventEditorPanel.setVisible(false);
        nameEditorPanel.addChangeListener(changes);
        individualEventEditorPanel.addChangeListener(changes);
        familiesChildTreeTablePanel.addChangeListener(changes);
        familiesSpouseTreeTablePanel.addChangeListener(changes);
        sourceCitationsTablePanel.addChangeListener(changes);
        aliasTablePanel.addChangeListener(changes);
        noteCitationsTablePanel.addChangeListener(changes);
        associationsTablePanel.addChangeListener(changes);
        multimediaObjectCitationsTablePanel.addChangeListener(changes);
        JTabbedPane nameEditorTabbedPane = nameEditorPanel.getNameEditorTabbedPane();
        namesTablePanel = new ancestris.modules.editors.genealogyeditor.panels.NamesTablePanel();
        nameEditorTabbedPane.addTab(org.openide.util.NbBundle.getMessage(IndividualEditor.class, "IndividualEditor.namesTablePanel.TabConstraints.tabTitle"), namesTablePanel); // NOI18N

        JComboBox.KeySelectionManager manager = new JComboBox.KeySelectionManager() {
            @Override
            public int selectionForKey(char aKey, ComboBoxModel aModel) {
                System.out.println(aKey);
                return -1;
            }
        };
        eventTypeComboBox.setKeySelectionManager(manager);
        eventTypeComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        generalPanel = new javax.swing.JPanel();
        individualIDLabel = new javax.swing.JLabel();
        individualIDTextField = new javax.swing.JTextField();
        sexBeanPanel = new ancestris.modules.editors.genealogyeditor.beans.SexBean();
        imageBean = new ancestris.modules.editors.genealogyeditor.beans.ImageBean();
        privateRecordToggleButton = new javax.swing.JToggleButton();
        nameEditorPanel = new ancestris.modules.editors.genealogyeditor.panels.NameEditorPanel();
        individualInformationTabbedPane = new javax.swing.JTabbedPane();
        eventsPanel = new javax.swing.JPanel();
        eventsSplitPane = new javax.swing.JSplitPane();
        eventsListPanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(12, 12), new java.awt.Dimension(12, 12), new java.awt.Dimension(12, 12));
        eventTypeComboBox = new javax.swing.JComboBox<String>();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(12, 12), new java.awt.Dimension(12, 12), new java.awt.Dimension(12, 12));
        deleteEventButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        eventsList = new javax.swing.JList();
        jPanel1 = new javax.swing.JPanel();
        individualEventEditorPanel = new ancestris.modules.editors.genealogyeditor.panels.IndividualEventPanel();
        familiesChildPanel = new javax.swing.JPanel();
        familiesChildTreeTablePanel = new ancestris.modules.editors.genealogyeditor.panels.FamiliesReferenceTreeTablePanel(FamiliesReferenceTreeTablePanel.EDIT_FAMC);
        familiesSpousePanel = new javax.swing.JPanel();
        familiesSpouseTreeTablePanel = new ancestris.modules.editors.genealogyeditor.panels.FamiliesReferenceTreeTablePanel(FamiliesReferenceTreeTablePanel.EDIT_FAMS);
        sourcesPanel = new javax.swing.JPanel();
        sourceCitationsTablePanel = new ancestris.modules.editors.genealogyeditor.panels.SourceCitationsTablePanel();
        aliasTablePanel = new ancestris.modules.editors.genealogyeditor.panels.AliasTablePanel();
        notesPanel = new javax.swing.JPanel();
        noteCitationsTablePanel = new ancestris.modules.editors.genealogyeditor.panels.NoteCitationsTablePanel();
        associationsPanel = new javax.swing.JPanel();
        associationsTablePanel = new ancestris.modules.editors.genealogyeditor.panels.AssociationsTablePanel();
        galleryPanel = new javax.swing.JPanel();
        multimediaObjectCitationsTablePanel = new ancestris.modules.editors.genealogyeditor.panels.MultimediaObjectCitationsTablePanel();
        changeDateLabel = new javax.swing.JLabel();
        changeDateLabeldate = new javax.swing.JLabel();

        setMinimumSize(new java.awt.Dimension(1042, 462));
        setName(""); // NOI18N

        generalPanel.setMinimumSize(new java.awt.Dimension(1018, 200));

        individualIDLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.individualIDLabel.text"), new Object[] {})); // NOI18N

        individualIDTextField.setEditable(false);
        individualIDTextField.setColumns(8);
        individualIDTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.individualIDTextField.text"), new Object[] {})); // NOI18N
        individualIDTextField.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.individualIDTextField.toolTipText"), new Object[] {})); // NOI18N

        imageBean.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imageBeanMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout imageBeanLayout = new javax.swing.GroupLayout(imageBean);
        imageBean.setLayout(imageBeanLayout);
        imageBeanLayout.setHorizontalGroup(
            imageBeanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 150, Short.MAX_VALUE)
        );
        imageBeanLayout.setVerticalGroup(
            imageBeanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 200, Short.MAX_VALUE)
        );

        privateRecordToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/lock_open.png"))); // NOI18N
        privateRecordToggleButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/lock_open.png"))); // NOI18N
        privateRecordToggleButton.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/lock.png"))); // NOI18N
        privateRecordToggleButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/lock.png"))); // NOI18N

        javax.swing.GroupLayout generalPanelLayout = new javax.swing.GroupLayout(generalPanel);
        generalPanel.setLayout(generalPanelLayout);
        generalPanelLayout.setHorizontalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addComponent(imageBean, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addComponent(sexBeanPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(individualIDLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(individualIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(privateRecordToggleButton))
                    .addComponent(nameEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addComponent(imageBean, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(sexBeanPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                        .addComponent(privateRecordToggleButton)
                        .addComponent(individualIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(individualIDLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        individualInformationTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        individualInformationTabbedPane.setMinimumSize(new java.awt.Dimension(1018, 219));

        eventsSplitPane.setBorder(null);
        eventsSplitPane.setDividerSize(1);

        eventsListPanel.setLayout(new java.awt.BorderLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.add(filler2);

        eventTypeComboBox.setModel(mEventsModel);
        eventTypeComboBox.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.eventTypeComboBox.toolTipText"), new Object[] {})); // NOI18N
        eventTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventTypeComboBoxActionPerformed(evt);
            }
        });
        jToolBar1.add(eventTypeComboBox);
        jToolBar1.add(filler3);

        deleteEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"))); // NOI18N
        deleteEventButton.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.deleteEventButton.toolTipText"), new Object[] {})); // NOI18N
        deleteEventButton.setFocusable(false);
        deleteEventButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deleteEventButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deleteEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteEventButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(deleteEventButton);

        eventsListPanel.add(jToolBar1, java.awt.BorderLayout.NORTH);

        eventsList.setModel(mEventsListModel);
        eventsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(eventsList);

        eventsListPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        eventsSplitPane.setLeftComponent(eventsListPanel);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(individualEventEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(individualEventEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        eventsSplitPane.setRightComponent(jPanel1);

        javax.swing.GroupLayout eventsPanelLayout = new javax.swing.GroupLayout(eventsPanel);
        eventsPanel.setLayout(eventsPanelLayout);
        eventsPanelLayout.setHorizontalGroup(
            eventsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(eventsSplitPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        eventsPanelLayout.setVerticalGroup(
            eventsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, eventsPanelLayout.createSequentialGroup()
                .addComponent(eventsSplitPane)
                .addGap(0, 0, 0))
        );

        individualInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.eventsPanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/event.png")), eventsPanel); // NOI18N

        javax.swing.GroupLayout familiesChildPanelLayout = new javax.swing.GroupLayout(familiesChildPanel);
        familiesChildPanel.setLayout(familiesChildPanelLayout);
        familiesChildPanelLayout.setHorizontalGroup(
            familiesChildPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(familiesChildTreeTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        familiesChildPanelLayout.setVerticalGroup(
            familiesChildPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, familiesChildPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(familiesChildTreeTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        individualInformationTabbedPane.addTab(org.openide.util.NbBundle.getMessage(IndividualEditor.class, "IndividualEditor.familiesChildPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/family.png")), familiesChildPanel); // NOI18N

        javax.swing.GroupLayout familiesSpousePanelLayout = new javax.swing.GroupLayout(familiesSpousePanel);
        familiesSpousePanel.setLayout(familiesSpousePanelLayout);
        familiesSpousePanelLayout.setHorizontalGroup(
            familiesSpousePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(familiesSpouseTreeTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        familiesSpousePanelLayout.setVerticalGroup(
            familiesSpousePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(familiesSpouseTreeTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        individualInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.familiesSpousePanel.TabConstraints.tabTitle"), new Object[] {}), familiesSpousePanel); // NOI18N

        javax.swing.GroupLayout sourcesPanelLayout = new javax.swing.GroupLayout(sourcesPanel);
        sourcesPanel.setLayout(sourcesPanelLayout);
        sourcesPanelLayout.setHorizontalGroup(
            sourcesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sourceCitationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        sourcesPanelLayout.setVerticalGroup(
            sourcesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sourceCitationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        individualInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.sourcesPanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/source.png")), sourcesPanel); // NOI18N
        individualInformationTabbedPane.addTab(org.openide.util.NbBundle.getMessage(IndividualEditor.class, "IndividualEditor.aliasTablePanel.TabConstraints.tabTitle"), aliasTablePanel); // NOI18N

        javax.swing.GroupLayout notesPanelLayout = new javax.swing.GroupLayout(notesPanel);
        notesPanel.setLayout(notesPanelLayout);
        notesPanelLayout.setHorizontalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(noteCitationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        notesPanelLayout.setVerticalGroup(
            notesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(noteCitationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        individualInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.notesPanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Note.png")), notesPanel); // NOI18N

        javax.swing.GroupLayout associationsPanelLayout = new javax.swing.GroupLayout(associationsPanel);
        associationsPanel.setLayout(associationsPanelLayout);
        associationsPanelLayout.setHorizontalGroup(
            associationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(associationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        associationsPanelLayout.setVerticalGroup(
            associationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(associationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        individualInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.associationsPanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/association.png")), associationsPanel); // NOI18N

        javax.swing.GroupLayout galleryPanelLayout = new javax.swing.GroupLayout(galleryPanel);
        galleryPanel.setLayout(galleryPanelLayout);
        galleryPanelLayout.setHorizontalGroup(
            galleryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(multimediaObjectCitationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        galleryPanelLayout.setVerticalGroup(
            galleryPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(multimediaObjectCitationsTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        individualInformationTabbedPane.addTab(org.openide.util.NbBundle.getMessage(IndividualEditor.class, "FamilyEditor.galleryPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Media.png")), galleryPanel); // NOI18N

        changeDateLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        changeDateLabel.setText(org.openide.util.NbBundle.getMessage(IndividualEditor.class, "IndividualEditor.changeDateLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(changeDateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changeDateLabeldate, javax.swing.GroupLayout.PREFERRED_SIZE, 173, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(generalPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(individualInformationTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(12, 12, 12))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generalPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(individualInformationTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(changeDateLabel)
                    .addComponent(changeDateLabeldate)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void imageBeanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageBeanMouseClicked
        Gedcom gedcom = mIndividual.getGedcom();

        if ((mMultiMediaObject = mIndividual.getProperty("OBJE")) == null) {
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        if (gedcom.getGrammar().getVersion().equals("5.5.1")) {
                            mMultiMediaObject = mIndividual.getGedcom().createEntity("OBJE");
                        } else {
                            mMultiMediaObject = mIndividual.addProperty("OBJE", "");
                        }
                    }
                }); // end of doUnitOfWork

                final MultiMediaObjectEditor multiMediaObjectEditor = new MultiMediaObjectEditor();
                multiMediaObjectEditor.setContext(new Context(mMultiMediaObject));
                if (multiMediaObjectEditor.showPanel()) {
                    if (mMultiMediaObject instanceof Media) {
                        mIndividual.addMedia((Media) mMultiMediaObject);
                    }
                    imageBean.setImage(mMultiMediaObject);
                    repaint();
                    changes.fireChangeEvent();
                }
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            for (Property multiMediaObject : mIndividual.getProperties("OBJE")) {
                String objetFormat = null;
                if (mIndividual.getGedcom().getGrammar().getVersion().equals("5.5.1")) {
                    if (multiMediaObject instanceof PropertyMedia) {
                        Property propertyFormat = ((Media) ((PropertyMedia) multiMediaObject).getTargetEntity()).getPropertyByPath(".:FILE:FORM");
                        if (propertyFormat != null) {
                            objetFormat = propertyFormat.getValue();
                        }
                    } else {
                        Property propertyFormat = multiMediaObject.getPropertyByPath(".:FILE:FORM");
                        if (propertyFormat != null) {
                            objetFormat = propertyFormat.getValue();
                        }
                    }
                } else {
                    if (multiMediaObject instanceof PropertyMedia) {
                        Property propertyFormat = ((Media) ((PropertyMedia) multiMediaObject).getTargetEntity()).getProperty("FORM");
                        if (propertyFormat != null) {
                            objetFormat = propertyFormat.getValue();
                        }
                    } else {
                        Property propertyFormat = multiMediaObject.getProperty("FORM");
                        if (propertyFormat != null) {
                            objetFormat = propertyFormat.getValue();
                        }
                    }
                }

                // bmp | gif | jpeg
                if (objetFormat != null && (objetFormat.equals("bmp") || objetFormat.equals("gif") || objetFormat.equals("jpeg") || objetFormat.equals("jpg") || objetFormat.equals("png"))) {

                    MultiMediaObjectEditor multiMediaObjectEditor = new MultiMediaObjectEditor();
                    multiMediaObjectEditor.setContext(new Context(mMultiMediaObject));
                    if (multiMediaObjectEditor.showPanel()) {
                        if (mMultiMediaObject instanceof Media) {
                            mIndividual.addMedia((Media) mMultiMediaObject);
                        }
                        imageBean.setImage(mMultiMediaObject);
                        repaint();
                        changes.fireChangeEvent();
                    }
                    break;
                }
            }
        }
    }//GEN-LAST:event_imageBeanMouseClicked

    private void deleteEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteEventButtonActionPerformed
        final int index = eventsList.getSelectedIndex();
        Gedcom gedcom = mIndividual.getGedcom();

        if (index != -1) {
            Property event = mEventsListModel.getValueAt(index);

            DialogManager createYesNo = DialogManager.createYesNo(
                    NbBundle.getMessage(
                            IndividualEditor.class, "IndividualEditor.eventsList.deleteEventConfirmation.title",
                            PropertyTag2Name.getTagName(event.getTag())),
                    NbBundle.getMessage(
                            IndividualEditor.class, "IndividualEditor.eventsList.deleteEventConfirmation.text",
                            PropertyTag2Name.getTagName(event.getTag()),
                            mIndividual));
            if (createYesNo.show() == DialogManager.YES_OPTION) {
                try {
                    gedcom.doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            mIndividual.delProperty(mEventsListModel.remove(index));
                        }
                    }); // end of doUnitOfWork
                    ArrayList<Property> eventsProperties = new ArrayList<Property>();
                    for (Property property : mIndividual.getProperties()) {
                        if (mIndividualEventsTags.contains(property.getTag())) {
                            eventsProperties.add(property);
                        }
                    }
                    seteventTypeComboBox(eventsProperties);
                    eventsList.setSelectedIndex(0);
                    changes.fireChangeEvent();
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_deleteEventButtonActionPerformed

    private void eventTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventTypeComboBoxActionPerformed
        if (!updateOnGoing) {
            Gedcom gedcom = mIndividual.getGedcom();
            mEvent = null;
            if (eventTypeComboBox.getSelectedIndex() > 0) {
                final String eventType = eventTypeComboBox.getSelectedItem().toString();
                try {
                    gedcom.doUnitOfWork(new UnitOfWork() {

                        @Override
                        public void perform(Gedcom gedcom) throws GedcomException {
                            mEvent = mIndividual.addProperty(PropertyTag2Name.getPropertyTag(eventType), "");
                        }
                    }); // end of doUnitOfWork

                    if (mEvent != null) {
                        ArrayList<Property> eventsProperties = new ArrayList<Property>();
                        for (Property property : mIndividual.getProperties()) {
                            if (mIndividualEventsTags.contains(property.getTag())) {
                                eventsProperties.add(property);
                            }
                        }
                        seteventTypeComboBox(eventsProperties);
                        if (mEventsListModel.indexOf(mEvent) == -1) {
                            mEventsListModel.add(mEvent);
                        }
                        eventsList.setSelectedIndex(mEventsListModel.indexOf(mEvent));
                        changes.fireChangeEvent();
                    }
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_eventTypeComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.editors.genealogyeditor.panels.AliasTablePanel aliasTablePanel;
    private javax.swing.JPanel associationsPanel;
    private ancestris.modules.editors.genealogyeditor.panels.AssociationsTablePanel associationsTablePanel;
    private javax.swing.JLabel changeDateLabel;
    private javax.swing.JLabel changeDateLabeldate;
    private javax.swing.JButton deleteEventButton;
    private javax.swing.JComboBox<String> eventTypeComboBox;
    private javax.swing.JList eventsList;
    private javax.swing.JPanel eventsListPanel;
    private javax.swing.JPanel eventsPanel;
    private javax.swing.JSplitPane eventsSplitPane;
    private javax.swing.JPanel familiesChildPanel;
    private ancestris.modules.editors.genealogyeditor.panels.FamiliesReferenceTreeTablePanel familiesChildTreeTablePanel;
    private javax.swing.JPanel familiesSpousePanel;
    private ancestris.modules.editors.genealogyeditor.panels.FamiliesReferenceTreeTablePanel familiesSpouseTreeTablePanel;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.JPanel galleryPanel;
    private javax.swing.JPanel generalPanel;
    private ancestris.modules.editors.genealogyeditor.beans.ImageBean imageBean;
    private ancestris.modules.editors.genealogyeditor.panels.IndividualEventPanel individualEventEditorPanel;
    private javax.swing.JLabel individualIDLabel;
    private javax.swing.JTextField individualIDTextField;
    private javax.swing.JTabbedPane individualInformationTabbedPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private ancestris.modules.editors.genealogyeditor.panels.MultimediaObjectCitationsTablePanel multimediaObjectCitationsTablePanel;
    private ancestris.modules.editors.genealogyeditor.panels.NameEditorPanel nameEditorPanel;
    private ancestris.modules.editors.genealogyeditor.panels.NoteCitationsTablePanel noteCitationsTablePanel;
    private javax.swing.JPanel notesPanel;
    private javax.swing.JToggleButton privateRecordToggleButton;
    private ancestris.modules.editors.genealogyeditor.beans.SexBean sexBeanPanel;
    private ancestris.modules.editors.genealogyeditor.panels.SourceCitationsTablePanel sourceCitationsTablePanel;
    private javax.swing.JPanel sourcesPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public ViewContext getContext() {
        return new ViewContext(context);
    }

    @Override
    public Component getEditorComponent() {
        return this;
    }

    @Override
    protected String getTitleImpl() {
        if (context == null || context.getEntity() == null) {
            return "";
        }
        return (new ViewContext(context.getEntity())).getText();
    }

    @Override
    protected void setContextImpl(Context context) {
        this.context = context;

        Entity entity = context.getEntity();
        if (entity != null && entity instanceof Indi) {

            mIndividual = (Indi) entity;

            setTitle(NbBundle.getMessage(IndividualEditor.class, isNew() ? "IndividualEditor.create.title" : "IndividualEditor.edit.title", mIndividual));

            String gedcomVersion = mIndividual.getGedcom().getGrammar().getVersion();
            if (!gedcomVersion.equals("5.5.1")) {
                privateRecordToggleButton.setVisible(false);
            }

            /*
             * n @XREF:INDI@ INDI
             */
            individualIDTextField.setText(mIndividual.getId());

            /*
             * +1 RESN <RESTRICTION_NOTICE>
             * not used
             */
            Property restrictionNotice = mIndividual.getProperty("RESN", true);
            if (restrictionNotice != null) {
                privateRecordToggleButton.setSelected(true);
            } else {
                privateRecordToggleButton.setSelected(false);
            }

            /*
             * +1 <<PERSONAL_NAME_STRUCTURE>>
             */
            List<PropertyName> namesList = mIndividual.getProperties(PropertyName.class);
            if (namesList.size() > 0) {
                PropertyName name = namesList.remove(0);
                if (name != null) {
                    nameEditorPanel.set(mIndividual, name);
                }
            } else {
                nameEditorPanel.set(mIndividual, null);
            }
            namesTablePanel.set(mIndividual, namesList);

            /*
             * +1 SEX <SEX_VALUE>
             */
            PropertySex sex = (PropertySex) mIndividual.getProperty("SEX", true);
            if (sex == null) {
                mIndividual.setSex(PropertySex.UNKNOWN);
                sex = (PropertySex) mIndividual.getProperty("SEX", true);
            }
            sexBeanPanel.set(mIndividual, sex);

            /*
             * +1 <<INDIVIDUAL_EVENT_STRUCTURE>>
             * +1 <<INDIVIDUAL_ATTRIBUTE_STRUCTURE>>
             */
            ArrayList<Property> individualEvents = new ArrayList<Property>();
            for (Property property : mIndividual.getProperties()) {
                if (mIndividualEventsTags.contains(property.getTag())) {
                    individualEvents.add(property);
                }
            }

            // Set default Events
/*        if (individualEvents.isEmpty() == true) {
             try {
             mIndividual.getGedcom().doUnitOfWork(new UnitOfWork() {

             @Override
             public void perform(Gedcom gedcom) throws GedcomException {
             mEvent = mIndividual.addProperty("BIRT", "");
             }
             }); // end of doUnitOfWork
             individualEvents.add(mEvent);
             } catch (GedcomException ex) {
             Exceptions.printStackTrace(ex);
             }
             }
             */
            mEventsListModel.clear();
            mEventsListModel.addAll(individualEvents);
            seteventTypeComboBox(individualEvents);

            eventsList.setSelectedIndex(0);

            /*
             * +1 <<LDS_INDIVIDUAL_ORDINANCE>>
             * Not Used
             */

            /*
             * +1 <<CHILD_TO_FAMILY_LINK>>
             */
            familiesChildTreeTablePanel.setFamiliesList(mIndividual, mIndividual.getProperties(PropertyFamilyChild.class));

            /*
             * +1 <<SPOUSE_TO_FAMILY_LINK>>
             */
            familiesSpouseTreeTablePanel.setFamiliesList(mIndividual, mIndividual.getProperties(PropertyFamilySpouse.class));

            /*
             * +1 SUBM @<XREF:SUBM>@
             * Not used
             */

            /*
             * +1 <<ASSOCIATION_STRUCTURE>>
             */
            associationsTablePanel.setAssociationsList(mIndividual, mIndividual.getProperties(PropertyAssociation.class));

            /*
             * +1 ALIA @<XREF:INDI>@
             * Not used
             */
            aliasTablePanel.set(mIndividual, mIndividual.getProperties(PropertyAlias.class));

            /*
             * +1 ANCI @<XREF:SUBM>@
             * Not used
             *
             * +1 DESI @<XREF:SUBM>@
             * Not used
             *
             * +1 RFN <PERMANENT_RECORD_FILE_NUMBER>
             * Not used
             *
             * +1 AFN <ANCESTRAL_FILE_NUMBER>
             * Not used
             *
             * +1 REFN <USER_REFERENCE_NUMBER>
             * Not used
             *
             * +2 TYPE <USER_REFERENCE_TYPE>
             * Not used
             *
             * +1 RIN <AUTOMATED_RECORD_ID>
             * Not used
             */
            /*
             * +1 <<CHANGE_DATE>>
             */
            Property changeDate = mIndividual.getProperty("CHAN");
            if (changeDate != null) {
                changeDateLabeldate.setText(((PropertyChange) changeDate).getDisplayValue());
            }

            /*
             * +1 <<NOTE_STRUCTURE>>
             */
            noteCitationsTablePanel.set(mIndividual, Arrays.asList(mIndividual.getProperties("NOTE")));

            /*
             * +1 <<SOURCE_CITATION>>
             */
            sourceCitationsTablePanel.set(mIndividual, Arrays.asList(mIndividual.getProperties("SOUR")));

            /*
             * +1 <<MULTIMEDIA_LINK>>
             */
            Property[] multiMediaObjects = mIndividual.getProperties("OBJE");
            Property selectedMultiMediaObject = null;
            for (Property multiMediaObject : multiMediaObjects) {
                String objetFormat = null;
                if (gedcomVersion.equals("5.5.1")) {
                    if (multiMediaObject instanceof PropertyMedia) {
                        Property propertyFormat = ((Media) ((PropertyMedia) multiMediaObject).getTargetEntity()).getPropertyByPath(".:FILE:FORM");
                        if (propertyFormat != null) {
                            objetFormat = propertyFormat.getValue();
                        }
                    } else {
                        Property propertyFormat = multiMediaObject.getPropertyByPath(".:FILE:FORM");
                        if (propertyFormat != null) {
                            objetFormat = propertyFormat.getValue();
                        }
                    }
                } else {
                    if (multiMediaObject instanceof PropertyMedia) {
                        Property propertyFormat = ((Media) ((PropertyMedia) multiMediaObject).getTargetEntity()).getProperty("FORM");
                        if (propertyFormat != null) {
                            objetFormat = propertyFormat.getValue();
                        }
                    } else {
                        Property propertyFormat = multiMediaObject.getProperty("FORM");
                        if (propertyFormat != null) {
                            objetFormat = propertyFormat.getValue();
                        }
                    }
                }

                // bmp | gif | jpeg
                if (objetFormat != null && (objetFormat.equals("bmp") || objetFormat.equals("gif") || objetFormat.equals("jpeg") || objetFormat.equals("jpg") || objetFormat.equals("png"))) {
                    selectedMultiMediaObject = multiMediaObject;
                    break;
                }
            }

            if (selectedMultiMediaObject != null) {
                imageBean.setImage(selectedMultiMediaObject);
            } else {
                imageBean.setImage(null);
            }

            multimediaObjectCitationsTablePanel.set(mIndividual, Arrays.asList(multiMediaObjects));
        }
    }

    @Override
    public void commit() {
        Property restrictionNotice = mIndividual.getProperty("RESN", true);
        if (privateRecordToggleButton.isSelected()) {
            if (restrictionNotice == null) {
                mIndividual.addProperty("RESN", "confidential");
            }
        } else {
            if (restrictionNotice != null) {
                mIndividual.delProperty(restrictionNotice);
            }
        }
        nameEditorPanel.commit();
        sexBeanPanel.commit();
        individualEventEditorPanel.commit();
    }

    private void seteventTypeComboBox(List<Property> eventsList) {
        ArrayList<String> localizedEventsList = new ArrayList<String>();

        for (String tag : mIndividualEventsTags) {
            localizedEventsList.add(PropertyTag2Name.getTagName(tag));
        }

        for (Property event : eventsList) {
            /*
             * Filter by events already present and unique
             */
            if (!event.getTag().equals("CENS")
                    && !event.getTag().equals("EMIG")
                    && !event.getTag().equals("EVEN")
                    && !event.getTag().equals("GRAD")
                    && !event.getTag().equals("IMMI")
                    && !event.getTag().equals("NATI")
                    && !event.getTag().equals("NATU")
                    && !event.getTag().equals("OCCU")
                    && !event.getTag().equals("PROB")
                    && !event.getTag().equals("PROP")
                    && !event.getTag().equals("RELI")
                    && !event.getTag().equals("RESI")
                    && !event.getTag().equals("TITL")) {
                localizedEventsList.remove(PropertyTag2Name.getTagName(event.getTag()));
            }
        }

        java.util.Collections.sort(localizedEventsList);

        updateOnGoing = true;

        mEventsModel.removeAllElements();
        mEventsModel.addElement(NbBundle.getMessage(
                IndividualEditor.class, "IndividualEditor.eventTypeComboBox.firstElement.title"));

        for (String tag : localizedEventsList) {
            mEventsModel.addElement(tag);
        }

        updateOnGoing = false;
    }

    private class EventsListSelectionHandler implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent lse) {
            ListSelectionModel lsm = (ListSelectionModel) lse.getSource();
            if (!lse.getValueIsAdjusting() && !lsm.isSelectionEmpty()) {
                if (individualEventEditorPanel.hasChanged()) {
                    try {
                        mIndividual.getGedcom().doUnitOfWork(new UnitOfWork() {

                            @Override
                            public void perform(Gedcom gedcom) throws GedcomException {
                                individualEventEditorPanel.commit();
                            }
                        });
                    } catch (GedcomException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                if (lsm.getMinSelectionIndex() < mEventsListModel.getSize()) {
                    individualEventEditorPanel.set(mIndividual, mEventsListModel.getValueAt(lsm.getMinSelectionIndex()));
                    individualEventEditorPanel.setVisible(true);
                }
            }
        }
    }
}
