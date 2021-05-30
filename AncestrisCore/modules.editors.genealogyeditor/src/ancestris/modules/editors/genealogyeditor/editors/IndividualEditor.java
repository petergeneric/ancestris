package ancestris.modules.editors.genealogyeditor.editors;

import ancestris.modules.editors.genealogyeditor.AriesTopComponent;
import ancestris.modules.editors.genealogyeditor.beans.ImageBean;
import ancestris.modules.editors.genealogyeditor.models.EventsListModel;
import ancestris.modules.editors.genealogyeditor.panels.FamiliesReferenceTreeTablePanel;
import ancestris.modules.editors.genealogyeditor.panels.NamesTablePanel;
import ancestris.modules.editors.genealogyeditor.utilities.PropertyTag2Name;
import ancestris.modules.gedcom.searchduplicates.IndiDuplicatesFinder;
import ancestris.util.swing.DialogManager;
import genj.gedcom.*;
import genj.util.Registry;
import genj.view.ViewContext;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
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
    private Context selectedEventContext;
    private Indi mIndividual;
    private Property mEvent = null;
    private Property mMultiMediaObject;
    private boolean updateOnGoing = false;
    private final EventsListModel mEventsListModel = new EventsListModel();
    private boolean changeListInProgress = false;
    private static final ArrayList<String> INDIVIDUAL_EVENT_TAGS = new ArrayList<String>() {
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
            add("FACT"); // gedcom 5.5.1
        }
    };
    private DefaultComboBoxModel<String> mEventsModel = new DefaultComboBoxModel<>(new String[]{});
    private NamesTablePanel namesTablePanel;
    private Registry registry = null;

    /**
     * Creates new form IndividualEditor
     */
    public IndividualEditor() {
        this(false);
    }

    public IndividualEditor(boolean isNew) {
        super(isNew);
        initComponents();
        selectedEventContext = null;
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
        sexBeanPanel.addChangeListener(changes);
        privateRecordToggleButton.addActionListener(changes);
        JTabbedPane nameEditorTabbedPane = nameEditorPanel.getNameEditorTabbedPane();
        namesTablePanel = new ancestris.modules.editors.genealogyeditor.panels.NamesTablePanel();
        nameEditorTabbedPane.addTab(org.openide.util.NbBundle.getMessage(IndividualEditor.class, "IndividualEditor.namesTablePanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/names.png")), namesTablePanel); // NOI18N

        JComboBox.KeySelectionManager manager = new JComboBox.KeySelectionManager() {
            @Override
            public int selectionForKey(char aKey, ComboBoxModel aModel) {
                System.out.println(aKey);
                return -1;
            }
        };
        eventTypeComboBox.setKeySelectionManager(manager);
        eventTypeComboBox.putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);

        registry = Registry.get(getClass());
        eventsSplitPane.setDividerLocation(registry.get("ariesindieventSplitDividerLocation", eventsSplitPane.getDividerLocation()));

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
        SOSALabel = new javax.swing.JLabel();
        SOSATextField = new javax.swing.JTextField();
        individualInformationTabbedPane = new javax.swing.JTabbedPane();
        eventsPanel = new javax.swing.JPanel();
        eventsSplitPane = new javax.swing.JSplitPane();
        eventsListPanel = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        deleteEventButton = new javax.swing.JButton();
        eventTypeComboBox = new javax.swing.JComboBox<>();
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
        notesPanel = new javax.swing.JPanel();
        noteCitationsTablePanel = new ancestris.modules.editors.genealogyeditor.panels.NoteCitationsTablePanel();
        galleryPanel = new javax.swing.JPanel();
        multimediaObjectCitationsTablePanel = new ancestris.modules.editors.genealogyeditor.panels.MultimediaObjectCitationsTablePanel();
        aliasTablePanel = new ancestris.modules.editors.genealogyeditor.panels.AliasTablePanel();
        associationsPanel = new javax.swing.JPanel();
        associationsTablePanel = new ancestris.modules.editors.genealogyeditor.panels.AssociationsTablePanel();
        changeDateLabel = new javax.swing.JLabel();
        changeDateLabeldate = new javax.swing.JLabel();

        setName(""); // NOI18N

        generalPanel.setMinimumSize(new java.awt.Dimension(100, 204));
        generalPanel.setPreferredSize(new java.awt.Dimension(500, 100));

        individualIDLabel.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.individualIDLabel.text"), new Object[] {})); // NOI18N

        individualIDTextField.setEditable(false);
        individualIDTextField.setColumns(8);
        individualIDTextField.setText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.individualIDTextField.text"), new Object[] {})); // NOI18N
        individualIDTextField.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.individualIDTextField.toolTipText"), new Object[] {})); // NOI18N

        sexBeanPanel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sexBeanPanelStateChanged(evt);
            }
        });

        imageBean.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        imageBean.setToolTipText(org.openide.util.NbBundle.getMessage(IndividualEditor.class, "IndividualEditor.imageBean.toolTipText")); // NOI18N
        imageBean.setAlignmentX(0.0F);
        imageBean.setAlignmentY(0.0F);
        imageBean.setMinimumSize(new java.awt.Dimension(135, 180));
        imageBean.setPreferredSize(new java.awt.Dimension(130, 173));
        imageBean.setRequestFocusEnabled(true);
        imageBean.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imageBeanMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout imageBeanLayout = new javax.swing.GroupLayout(imageBean);
        imageBean.setLayout(imageBeanLayout);
        imageBeanLayout.setHorizontalGroup(
            imageBeanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        imageBeanLayout.setVerticalGroup(
            imageBeanLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        privateRecordToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/lock_open.png"))); // NOI18N
        privateRecordToggleButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndividualEditor.class, "IndividualEditor.privateRecordToggleButton.toolTipText")); // NOI18N
        privateRecordToggleButton.setMaximumSize(new java.awt.Dimension(26, 26));
        privateRecordToggleButton.setMinimumSize(new java.awt.Dimension(26, 26));
        privateRecordToggleButton.setPreferredSize(new java.awt.Dimension(26, 26));
        privateRecordToggleButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/lock_open.png"))); // NOI18N
        privateRecordToggleButton.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/lock.png"))); // NOI18N
        privateRecordToggleButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/lock.png"))); // NOI18N

        nameEditorPanel.setMinimumSize(null);
        nameEditorPanel.setPreferredSize(null);

        SOSALabel.setText(org.openide.util.NbBundle.getMessage(IndividualEditor.class, "IndividualEditor.SOSALabel.text")); // NOI18N

        SOSATextField.setEditable(false);

        javax.swing.GroupLayout generalPanelLayout = new javax.swing.GroupLayout(generalPanel);
        generalPanel.setLayout(generalPanelLayout);
        generalPanelLayout.setHorizontalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addComponent(imageBean, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addComponent(sexBeanPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(SOSALabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SOSATextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(individualIDLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(individualIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(privateRecordToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(nameEditorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        generalPanelLayout.setVerticalGroup(
            generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(generalPanelLayout.createSequentialGroup()
                .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(imageBean, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(generalPanelLayout.createSequentialGroup()
                        .addGroup(generalPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(sexBeanPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(individualIDLabel)
                            .addComponent(individualIDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(privateRecordToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SOSALabel)
                            .addComponent(SOSATextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameEditorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        individualInformationTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        eventsPanel.setPreferredSize(new java.awt.Dimension(300, 150));

        eventsSplitPane.setBorder(null);
        eventsSplitPane.setDividerLocation(150);
        eventsSplitPane.setDividerSize(6);
        eventsSplitPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                eventsSplitPanePropertyChange(evt);
            }
        });

        eventsListPanel.setMinimumSize(new java.awt.Dimension(20, 57));
        eventsListPanel.setLayout(new java.awt.BorderLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

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

        eventTypeComboBox.setModel(mEventsModel);
        eventTypeComboBox.setToolTipText(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.eventTypeComboBox.toolTipText"), new Object[] {})); // NOI18N
        eventTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventTypeComboBoxActionPerformed(evt);
            }
        });
        jToolBar1.add(eventTypeComboBox);

        eventsListPanel.add(jToolBar1, java.awt.BorderLayout.NORTH);

        eventsList.setModel(mEventsListModel);
        eventsList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        eventsList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                eventsListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(eventsList);

        eventsListPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        eventsSplitPane.setLeftComponent(eventsListPanel);

        individualEventEditorPanel.setPreferredSize(new java.awt.Dimension(597, 200));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(individualEventEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(individualEventEditorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 255, Short.MAX_VALUE)
                .addContainerGap())
        );

        eventsSplitPane.setRightComponent(jPanel1);

        javax.swing.GroupLayout eventsPanelLayout = new javax.swing.GroupLayout(eventsPanel);
        eventsPanel.setLayout(eventsPanelLayout);
        eventsPanelLayout.setHorizontalGroup(
            eventsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(eventsSplitPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
        );
        eventsPanelLayout.setVerticalGroup(
            eventsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(eventsSplitPane)
                .addGap(11, 11, 11))
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

        individualInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.familiesSpousePanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/child.png")), familiesSpousePanel); // NOI18N

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

        individualInformationTabbedPane.addTab(java.text.MessageFormat.format(java.util.ResourceBundle.getBundle("ancestris/modules/editors/genealogyeditor/editors/Bundle").getString("IndividualEditor.notesPanel.TabConstraints.tabTitle"), new Object[] {}), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/note.png")), notesPanel); // NOI18N

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

        individualInformationTabbedPane.addTab(org.openide.util.NbBundle.getMessage(IndividualEditor.class, "FamilyEditor.galleryPanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/media.png")), galleryPanel); // NOI18N
        individualInformationTabbedPane.addTab(org.openide.util.NbBundle.getMessage(IndividualEditor.class, "IndividualEditor.aliasTablePanel.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Description.png")), aliasTablePanel); // NOI18N

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

        changeDateLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        changeDateLabel.setText(org.openide.util.NbBundle.getMessage(IndividualEditor.class, "IndividualEditor.changeDateLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(changeDateLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(changeDateLabeldate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(individualInformationTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
                    .addComponent(generalPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(generalPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(individualInformationTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(changeDateLabeldate, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(changeDateLabel)
                        .addContainerGap())))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void imageBeanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageBeanMouseClicked
        Gedcom gedcom = mIndividual.getGedcom();

        if ((mMultiMediaObject = mIndividual.getProperty("OBJE")) == null || (evt.getButton() == MouseEvent.BUTTON3)) {
            gedcom.doMuteUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    if (gedcom.getGrammar().getVersion().equals("5.5.1")) {
                        mMultiMediaObject = mIndividual.getGedcom().createEntity("OBJE");
                    } else {
                        mMultiMediaObject = mIndividual.addProperty("OBJE", "");
                    }
                }
            });
            final MultiMediaObjectEditor multiMediaObjectEditor = new MultiMediaObjectEditor();
            multiMediaObjectEditor.setContext(new Context(mMultiMediaObject));
            if (multiMediaObjectEditor.showPanel()) {
                if (mMultiMediaObject instanceof Media) {
                    mIndividual.addMedia((Media) mMultiMediaObject);
                    boolean correct = imageBean.setImage(((PropertyFile) mMultiMediaObject.getProperty("FILE")) != null ? ((PropertyFile) mMultiMediaObject.getProperty("FILE")).getInput().get() : null, mIndividual.getSex());
                    if (!correct) {
                        String title = NbBundle.getMessage(ImageBean.class, "ImageBean.fileType");
                        String text = NbBundle.getMessage(ImageBean.class, "ImageBean.fileType.notSupported");
                        DialogManager.create(title, text).setMessageType(DialogManager.WARNING_MESSAGE).setOptionType(DialogManager.OK_ONLY_OPTION).setDialogId("ancestris.aries.error").show();
                    }
                    repaint();
                    changes.fireChangeEvent();
                }
            } else {
                gedcom.doMuteUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        if (gedcom.getGrammar().getVersion().equals("5.5.1")) {
                            mIndividual.getGedcom().deleteEntity((Entity) mMultiMediaObject);
                        } else {
                            mIndividual.delProperty(mMultiMediaObject);
                        }
                    }
                }); // end of doUnitOfWork

            }
        } else {
            for (Property multiMediaObject : mIndividual.getProperties("OBJE")) {
                MultiMediaObjectEditor multiMediaObjectEditor = new MultiMediaObjectEditor();
                multiMediaObjectEditor.setContext(new Context(multiMediaObject));
                if (multiMediaObjectEditor.showPanel()) {
                    if (multiMediaObject instanceof Media) {
                        mIndividual.addMedia((Media) multiMediaObject);
                    }

                    // Display image
                    if (multiMediaObject instanceof PropertyMedia) {
                        multiMediaObject = ((PropertyMedia) multiMediaObject).getTargetEntity();
                    }
                    Property multimediaFile = multiMediaObject.getProperty("FILE", true);
                    boolean correct;
                    if (multimediaFile != null && multimediaFile instanceof PropertyFile) {
                        correct = imageBean.setImage(((PropertyFile) multimediaFile).getInput().orElse(null), mIndividual.getSex());
                    } else {
                        PropertyBlob propertyBlob = (PropertyBlob) multiMediaObject.getProperty("BLOB", true);
                        correct = imageBean.setImage(propertyBlob != null ? propertyBlob.getInput().get() : null, mIndividual.getSex());
                    }
                    if (!correct) {
                        String title = NbBundle.getMessage(ImageBean.class, "ImageBean.fileType");
                        String text = NbBundle.getMessage(ImageBean.class, "ImageBean.fileType.notSupported");
                        DialogManager.create(title, text).setMessageType(DialogManager.WARNING_MESSAGE).setOptionType(DialogManager.OK_ONLY_OPTION).setDialogId("ancestris.aries.error").show();
                    }
                    repaint();
                    changes.fireChangeEvent();
                }
                break;
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
                    ArrayList<Property> eventsProperties = new ArrayList<>();
                    for (Property property : mIndividual.getProperties()) {
                        if (INDIVIDUAL_EVENT_TAGS.contains(property.getTag())) {
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
                            //FIXME: use Grammar for that?
                            Logger.getLogger("ancestris").info(mEvent.getTag());
                            if (mEvent.getTag().matches(
                                    "(BIRT|CHR|DEAT|BURI|CREM|ADOP|BAPM|BARM|BASM|BLES|CHRA|CONF|FCOM|ORDN|NATU|EMIG|IMMI|CENS|PROB|WILL|GRAD|RETI)")) {
                                mEvent.setValue("y");
                            }
                        }
                    }); // end of doUnitOfWork

                    if (mEvent != null) {
                        ArrayList<Property> eventsProperties = new ArrayList<>();
                        for (Property property : mIndividual.getProperties()) {
                            if (INDIVIDUAL_EVENT_TAGS.contains(property.getTag())) {
                                eventsProperties.add(property);
                            }
                        }
                        seteventTypeComboBox(eventsProperties);
                        if (mEventsListModel.indexOf(mEvent) == -1) {
                            mEventsListModel.add(mEvent);
                        }
                        selectedEventContext = new Context(mEvent);
                        eventsList.setSelectedIndex(mEventsListModel.indexOf(mEvent));
                        changes.fireChangeEvent();
                    }
                } catch (GedcomException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_eventTypeComboBoxActionPerformed

    private void eventsListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_eventsListValueChanged
        if (!evt.getValueIsAdjusting()) {
            return;
        }
        int index = eventsList.getSelectedIndex();
        if (index != -1 && index < mEventsListModel.getSize()) {
            Property prop = mEventsListModel.getValueAt(index);
            selectedEventContext = new Context(prop);
        }
    }//GEN-LAST:event_eventsListValueChanged

    private void sexBeanPanelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sexBeanPanelStateChanged
        if (imageBean.isDefault()) {
            imageBean.setImage(null, sexBeanPanel.getSelectedSex());
        }
    }//GEN-LAST:event_sexBeanPanelStateChanged

    private void eventsSplitPanePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_eventsSplitPanePropertyChange
        registry.put("ariesindieventSplitDividerLocation", eventsSplitPane.getDividerLocation());
    }//GEN-LAST:event_eventsSplitPanePropertyChange

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel SOSALabel;
    private javax.swing.JTextField SOSATextField;
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
        if (entity != null && entity instanceof Indi && entity.getGedcom() != null) {

            mIndividual = (Indi) entity;

            setTitle(NbBundle.getMessage(IndividualEditor.class, isNew() ? "IndividualEditor.create.title" : "IndividualEditor.edit.title", mIndividual.getDisplayTitle(false)));

            String gedcomVersion = mIndividual.getGedcom().getGrammar().getVersion();
            if (!gedcomVersion.equals("5.5.1")) {
                privateRecordToggleButton.setVisible(false);
            }

            /*
             * n @XREF:INDI@ INDI
             */
            individualIDTextField.setText(mIndividual.getId());

            /*
             * SOSA number if exists
             */
            Property SOSANumber = mIndividual.getSosa(true);

            if (SOSANumber != null) {
                SOSALabel.setVisible(true);
                SOSATextField.setVisible(true);
                SOSATextField.setText(SOSANumber.getValue());
            } else {
                SOSALabel.setVisible(false);
                SOSATextField.setVisible(false);
                SOSATextField.setText("");
            }

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
            PropertySex sex = (PropertySex) mIndividual.getProperty("SEX", false);
            if (sex == null) {
                mIndividual.setSex(PropertySex.UNKNOWN);
                sex = (PropertySex) mIndividual.getProperty("SEX", true);
            } else if (!sex.isValid()) {
                sex.setValue("U");
            }
            sexBeanPanel.set(mIndividual, sex);

            /*
             * +1 <<INDIVIDUAL_EVENT_STRUCTURE>>
             * +1 <<INDIVIDUAL_ATTRIBUTE_STRUCTURE>>
             */
            List<Property> individualEvents = new ArrayList<>();
            for (Property property : mIndividual.getProperties()) {
                if (INDIVIDUAL_EVENT_TAGS.contains(property.getTag())) {
                    individualEvents.add(property);
                }
            }
            Collections.sort(individualEvents, new Comparator<Property>() {
                @Override
                public int compare(Property p1, Property p2) {
                    PropertyDate pDate1 = (PropertyDate) p1.getProperty("DATE");
                    PropertyDate pDate2 = (PropertyDate) p2.getProperty("DATE");
                    if (pDate1 == null && pDate2 == null) {
                        return 0;
                    }
                    if (pDate1 != null && pDate2 == null) {
                        return +1;
                    }
                    if (pDate1 == null && pDate2 != null) {
                        return -1;
                    }
                    return pDate1.compareTo(pDate2);
                }

            });
            changeListInProgress = true;
            mEventsListModel.clear();
            mEventsListModel.addAll(individualEvents);
            seteventTypeComboBox(individualEvents);

            // Select context event from property 
            int index = 0;
            Property prop = selectedEventContext == null ? context.getProperty() : selectedEventContext.getProperty();
            if (prop != null) {
                index = mEventsListModel.indexOf(prop);
                if (index == -1) {
                    index = 0;
                }
            }
            if (eventsList.getModel().getSize() > 0) {
                eventsList.setSelectedIndex(index);
            } else {
                individualEventEditorPanel.clear(mIndividual);
            }
            changeListInProgress = false;

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
            boolean found = false;
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

                // any kind of file
                if (true) {
                    if (multiMediaObject instanceof PropertyMedia) {
                        multiMediaObject = ((PropertyMedia) multiMediaObject).getTargetEntity();
                    }

                    Property multimediaFile = multiMediaObject.getProperty("FILE", true);
                    boolean correct;
                    if (multimediaFile != null && multimediaFile instanceof PropertyFile) {
                        correct = imageBean.setImage(((PropertyFile) multimediaFile).getInput().orElse(null), mIndividual.getSex());
                    } else {
                        PropertyBlob propertyBlob = (PropertyBlob) multiMediaObject.getProperty("BLOB", true);
                        correct = imageBean.setImage(propertyBlob != null ? propertyBlob.getInput().get() : null, mIndividual.getSex());
                    }
                    found = true;
                    if (!correct) {
                        String title = NbBundle.getMessage(ImageBean.class, "ImageBean.fileType");
                        String text = NbBundle.getMessage(ImageBean.class, "ImageBean.fileType.notSupported");
                        DialogManager.create(title, text).setMessageType(DialogManager.WARNING_MESSAGE).setOptionType(DialogManager.OK_ONLY_OPTION).setDialogId("ancestris.aries.error").show();
                    }
                    break;
                }
            }
            if (found == false) {
                imageBean.setImage(null, mIndividual.getSex());
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
        if (nameEditorPanel.hasChanged() || individualEventEditorPanel.hasChanged() || sexBeanPanel.hasChanged()) {
            final boolean nouveau = mIndividual.isNew();
            mIndividual.setOld();

            nameEditorPanel.commit();
            sexBeanPanel.commit();
            individualEventEditorPanel.commit();

            // Detect if ask for it and new or any time.
            if ((GedcomOptions.getInstance().getDetectDuplicate() && nouveau) || GedcomOptions.getInstance().getDuplicateAnyTime()) {
                
                Runnable idf = new IndiDuplicatesFinder(mIndividual);
                idf.run();
            }
        }

    }

    private void seteventTypeComboBox(List<Property> eventsList) {
        ArrayList<String> localizedEventsList = new ArrayList<>();

        for (String tag : INDIVIDUAL_EVENT_TAGS) {
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
                    && !event.getTag().equals("FACT")
                    && !event.getTag().equals("TITL")) {
                localizedEventsList.remove(PropertyTag2Name.getTagName(event.getTag()));
            }
        }

        java.util.Collections.sort(localizedEventsList);

        updateOnGoing = true;

        mEventsModel.removeAllElements();
        mEventsModel.addElement(NbBundle.getMessage(
                IndividualEditor.class, "IndividualEditor.eventTypeComboBox.firstElement.title"));

        localizedEventsList.forEach((tag) -> {
            mEventsModel.addElement(tag);
        });

        updateOnGoing = false;
    }

    @Override
    public Entity getEditedEntity() {
        return mIndividual;
    }

    private class EventsListSelectionHandler implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent lse) {
            ListSelectionModel lsm = (ListSelectionModel) lse.getSource();
            if (!lse.getValueIsAdjusting() && !lsm.isSelectionEmpty()) {
                if (!changeListInProgress && individualEventEditorPanel.hasChanged()) {
                    AriesTopComponent etc = AriesTopComponent.findEditorWindow(mIndividual.getGedcom());
                    if (etc != null) {
                        etc.commit();
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
