package ancestris.modules.editors.standard;

import ancestris.modules.editors.standard.tools.EventUsage;
import ancestris.modules.editors.standard.tools.FamilyTreeRenderer;
import ancestris.api.editor.Editor;
import ancestris.gedcom.privacy.standard.Options;
import ancestris.modules.editors.standard.tools.AssoManager;
import ancestris.modules.editors.standard.tools.AssoWrapper;
import ancestris.modules.editors.standard.tools.EventLabel;
import ancestris.modules.editors.standard.tools.EventTableModel;
import ancestris.modules.editors.standard.tools.EventWrapper;
import ancestris.modules.editors.standard.tools.ImagePanel;
import ancestris.modules.editors.standard.tools.MediaChooser;
import ancestris.modules.editors.standard.tools.MediaWrapper;
import ancestris.modules.editors.standard.tools.NodeWrapper;
import ancestris.modules.editors.standard.tools.NoteChooser;
import ancestris.modules.editors.standard.tools.NoteWrapper;
import ancestris.modules.editors.standard.tools.RepoChooser;
import ancestris.modules.editors.standard.tools.SourceChooser;
import ancestris.modules.editors.standard.tools.SourceWrapper;
import ancestris.modules.editors.standard.tools.Utils;
import static ancestris.modules.editors.standard.tools.Utils.getImageFromFile;
import static ancestris.modules.editors.standard.tools.Utils.getResizedIcon;
import ancestris.util.TimingUtility;
import ancestris.util.swing.DialogManager;
import ancestris.util.swing.FileChooserBuilder;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyForeignXRef;
import genj.gedcom.PropertyMedia;
import genj.gedcom.PropertySex;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import genj.util.Registry;
import genj.view.ViewContext;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

/**
 *
 * @author frederic
 */
public class IndiPanel extends Editor implements DocumentListener {

    private static final Logger LOG = Logger.getLogger("ancestris.editor.indi");

    private int PHOTO_WIDTH = 160;
    private int PHOTO_HEIGHT = 186;
    private Image PHOTO_MALE = null;
    private Image PHOTO_FEMALE = null;
    private Image PHOTO_UNKNOWN = null;
    
    private Context context;
    private Gedcom gedcom;
    private Indi indi;
    private boolean reloadData = true;
    private boolean listernersOn = false;
    
    private DefaultMutableTreeNode familyTop = null;
    private Registry registry = null;

    private String SOSA_TAG = "_SOSA";                                                              // FIXME : use existing parameter
    private final static String NO_SOSA = NbBundle.getMessage(IndiPanel.class, "noSosa");
    
    private static Map<String, EventUsage> eventUsages = null;

    private ImagePanel imagePanel = null;
    
    // Media
    private List<MediaWrapper> mediaSet = null;
    private int mediaIndex = 0;
    private List<MediaWrapper> mediaRemovedSet = null;
    private boolean isBusyMedia = false;
    
    // Notes
    private List<NoteWrapper> noteSet = null;
    private int noteIndex = 0;
    private List<NoteWrapper> noteRemovedSet = null;
    private boolean isBusyNote = false;
    
    // Events
    private List<EventWrapper> eventSet = null;
    private int eventIndex = 0;
    private List<EventWrapper> eventRemovedSet = null;
    private boolean isBusyEvent = false;
    private boolean isBusyEventNote = false;
    private boolean isBusyEventSource = false;
    
    // Associations
    private DefaultComboBoxModel cbModel = new DefaultComboBoxModel();
    private List<AssoWrapper> assoSet = null;
    private List<AssoWrapper> assoRemovedSet = null;

    
    /**
     * Creates new form IndiPanel
     */
    public IndiPanel() {

        try {
            this.PHOTO_MALE = ImageIO.read(getClass().getResourceAsStream("/ancestris/modules/editors/standard/images/profile_male.png"));
            this.PHOTO_FEMALE = ImageIO.read(getClass().getResourceAsStream("/ancestris/modules/editors/standard/images/profile_female.png"));
            this.PHOTO_UNKNOWN = ImageIO.read(getClass().getResourceAsStream("/ancestris/modules/editors/standard/images/profile_unknown.png"));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        eventUsages = new HashMap<String, EventUsage>();
        EventUsage.init(eventUsages);
        
        familyTop = new DefaultMutableTreeNode(new NodeWrapper(NodeWrapper.PARENTS, null));
        
        initComponents();
        
        familyTree.setCellRenderer(new FamilyTreeRenderer());
        familyTree.addMouseListener(new FamilyTreeMouseListener());
        
        registry = Registry.get(getClass());
        eventSplitPane.setDividerLocation(registry.get("eventSplitDividerLocation", eventSplitPane.getDividerLocation()));
        
        reloadData = true; // force data load at initialisation
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGender = new javax.swing.ButtonGroup();
        title = new javax.swing.JLabel();
        mediaPanel = new javax.swing.JPanel();
        photos = new javax.swing.JLabel();
        scrollPanePhotos = new javax.swing.JScrollPane();
        textAreaPhotos = new javax.swing.JTextArea();
        scrollPhotos = new javax.swing.JScrollBar();
        addMediaButton = new javax.swing.JButton();
        delMediaButton = new javax.swing.JButton();
        namePanel = new javax.swing.JPanel();
        TopButtonsdPanel = new javax.swing.JPanel();
        fatherButton = new javax.swing.JButton();
        idLabel = new javax.swing.JLabel();
        sosaLabel = new javax.swing.JLabel();
        motherButton = new javax.swing.JButton();
        moreNamesButton = new javax.swing.JButton();
        firstnamesLabel = new javax.swing.JLabel();
        lastnameLabel = new javax.swing.JLabel();
        firstnamesText = new javax.swing.JTextField();
        lastnameText = new javax.swing.JTextField();
        maleRadioButton = new javax.swing.JRadioButton();
        femaleRadioButton = new javax.swing.JRadioButton();
        unknownRadioButton = new javax.swing.JRadioButton();
        privateCheckBox = new javax.swing.JCheckBox();
        BottomButtonsPanel = new javax.swing.JPanel();
        brothersButton = new javax.swing.JButton();
        sistersButton = new javax.swing.JButton();
        spousesButton = new javax.swing.JButton();
        childrenButton = new javax.swing.JButton();
        scrollPaneFamily = new javax.swing.JScrollPane();
        familyTree = new javax.swing.JTree(familyTop);
        NotePanel = new javax.swing.JPanel();
        scrollPaneNotes = new javax.swing.JScrollPane();
        textAreaNotes = new javax.swing.JTextArea();
        scrollNotes = new javax.swing.JScrollBar();
        addNoteButton = new javax.swing.JButton();
        delNoteButton = new javax.swing.JButton();
        separator = new javax.swing.JSeparator();
        eventSplitPane = new javax.swing.JSplitPane();
        eventLeft = new javax.swing.JPanel();
        eventBirtButton = new javax.swing.JButton();
        eventBaptButton = new javax.swing.JButton();
        eventOccuButton = new javax.swing.JButton();
        eventDeatButton = new javax.swing.JButton();
        eventBuriButton = new javax.swing.JButton();
        eventMarrButton = new javax.swing.JButton();
        eventRetiButton = new javax.swing.JButton();
        eventResiButton = new javax.swing.JButton();
        eventOthersButton = new javax.swing.JButton();
        eventRemoveButton = new javax.swing.JButton();
        eventScrollPane = new javax.swing.JScrollPane();
        eventTable = new javax.swing.JTable();
        modificationLabel = new javax.swing.JLabel();
        sourcePanel = new javax.swing.JPanel();
        imagePanel = new ImagePanel(this);
        sourceImagePanel = imagePanel;
        eventRight = new javax.swing.JPanel();
        eventTitle = new javax.swing.JLabel();
        eventDescription = new javax.swing.JTextField();
        datelabel = new javax.swing.JLabel();
        eventDate = new genj.edit.beans.DateBean();
        dayOfWeek = new javax.swing.JLabel();
        ageAtEvent = new javax.swing.JLabel();
        placeLabel = new javax.swing.JLabel();
        eventPlace = new javax.swing.JTextField();
        eventPlaceButton = new javax.swing.JButton();
        eventNotePanel = new javax.swing.JPanel();
        eventNoteScrollPane = new javax.swing.JScrollPane();
        eventNote = new javax.swing.JTextArea();
        delNoteEventButton = new javax.swing.JButton();
        addNoteEventButton = new javax.swing.JButton();
        scrollNotesEvent = new javax.swing.JScrollBar();
        eventSourcePanel = new javax.swing.JPanel();
        eventSourceTitle = new javax.swing.JTextField();
        eventSourceScrollPane = new javax.swing.JScrollPane();
        eventSourceText = new javax.swing.JTextArea();
        repoPanel = new javax.swing.JPanel();
        repoText = new javax.swing.JTextField();
        repoEditButton = new javax.swing.JButton();
        scrollSourcesEvent = new javax.swing.JScrollBar();
        addSourceEventButton = new javax.swing.JButton();
        delSourceEventButton = new javax.swing.JButton();
        assoPanel = new javax.swing.JPanel();
        assoComboBox = new javax.swing.JComboBox();
        assoEditButton = new javax.swing.JButton();
        assoEditIndi = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(32767, 500));
        setPreferredSize(new java.awt.Dimension(557, 800));

        title.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(title, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.title.text")); // NOI18N

        photos.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(photos, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.photos.text")); // NOI18N
        photos.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.photos.toolTipText")); // NOI18N
        photos.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        photos.setPreferredSize(new java.awt.Dimension(160, 186));
        photos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                photosMouseClicked(evt);
            }
        });

        textAreaPhotos.setColumns(20);
        textAreaPhotos.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        textAreaPhotos.setLineWrap(true);
        textAreaPhotos.setRows(4);
        textAreaPhotos.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.textAreaPhotos.text")); // NOI18N
        textAreaPhotos.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.textAreaPhotos.toolTipText")); // NOI18N
        textAreaPhotos.setWrapStyleWord(true);
        scrollPanePhotos.setViewportView(textAreaPhotos);

        scrollPhotos.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        scrollPhotos.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                scrollPhotosMouseWheelMoved(evt);
            }
        });
        scrollPhotos.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollPhotosAdjustmentValueChanged(evt);
            }
        });

        addMediaButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addMediaButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addMediaButton.text")); // NOI18N
        addMediaButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addMediaButton.toolTipText")); // NOI18N
        addMediaButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addMediaButton.setIconTextGap(0);
        addMediaButton.setPreferredSize(new java.awt.Dimension(16, 16));
        addMediaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMediaButtonActionPerformed(evt);
            }
        });

        delMediaButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(delMediaButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delMediaButton.text")); // NOI18N
        delMediaButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delMediaButton.toolTipText")); // NOI18N
        delMediaButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        delMediaButton.setIconTextGap(0);
        delMediaButton.setPreferredSize(new java.awt.Dimension(16, 16));
        delMediaButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delMediaButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mediaPanelLayout = new javax.swing.GroupLayout(mediaPanel);
        mediaPanel.setLayout(mediaPanelLayout);
        mediaPanelLayout.setHorizontalGroup(
            mediaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mediaPanelLayout.createSequentialGroup()
                .addComponent(scrollPhotos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addMediaButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(delMediaButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(photos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(scrollPanePhotos, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );
        mediaPanelLayout.setVerticalGroup(
            mediaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mediaPanelLayout.createSequentialGroup()
                .addComponent(photos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPanePhotos, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addGap(2, 2, 2)
                .addGroup(mediaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(delMediaButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addMediaButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scrollPhotos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        fatherButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/father.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(fatherButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.fatherButton.text")); // NOI18N
        fatherButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.fatherButton.toolTipText")); // NOI18N
        fatherButton.setPreferredSize(new java.awt.Dimension(60, 27));
        fatherButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fatherButtonActionPerformed(evt);
            }
        });

        idLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(idLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.idLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(sosaLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.sosaLabel.text")); // NOI18N

        motherButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/mother.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(motherButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.motherButton.text")); // NOI18N
        motherButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.motherButton.toolTipText")); // NOI18N
        motherButton.setPreferredSize(new java.awt.Dimension(60, 27));
        motherButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                motherButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout TopButtonsdPanelLayout = new javax.swing.GroupLayout(TopButtonsdPanel);
        TopButtonsdPanel.setLayout(TopButtonsdPanelLayout);
        TopButtonsdPanelLayout.setHorizontalGroup(
            TopButtonsdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopButtonsdPanelLayout.createSequentialGroup()
                .addComponent(fatherButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(idLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(sosaLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(motherButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        TopButtonsdPanelLayout.setVerticalGroup(
            TopButtonsdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopButtonsdPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(TopButtonsdPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(fatherButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(idLabel)
                    .addComponent(sosaLabel)
                    .addComponent(motherButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        moreNamesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/name.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(moreNamesButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.moreNamesButton.text")); // NOI18N
        moreNamesButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.moreNamesButton.toolTipText")); // NOI18N
        moreNamesButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        moreNamesButton.setIconTextGap(0);
        moreNamesButton.setPreferredSize(new java.awt.Dimension(16, 16));
        moreNamesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moreNamesButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(firstnamesLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.firstnamesLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lastnameLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.lastnameLabel.text")); // NOI18N

        firstnamesText.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.firstnamesText.text")); // NOI18N
        firstnamesText.setPreferredSize(new java.awt.Dimension(120, 27));
        firstnamesText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firstnamesTextActionPerformed(evt);
            }
        });

        lastnameText.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.lastnameText.text")); // NOI18N
        lastnameText.setPreferredSize(new java.awt.Dimension(120, 27));

        buttonGender.add(maleRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(maleRadioButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.maleRadioButton.text")); // NOI18N
        maleRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maleRadioButtonActionPerformed(evt);
            }
        });

        buttonGender.add(femaleRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(femaleRadioButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.femaleRadioButton.text")); // NOI18N
        femaleRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                femaleRadioButtonActionPerformed(evt);
            }
        });

        buttonGender.add(unknownRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(unknownRadioButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.unknownRadioButton.text")); // NOI18N
        unknownRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unknownRadioButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(privateCheckBox, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.privateCheckBox.text")); // NOI18N
        privateCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.privateCheckBox.toolTipText")); // NOI18N
        privateCheckBox.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/private.png"))); // NOI18N
        privateCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                privateCheckBoxActionPerformed(evt);
            }
        });

        brothersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/brother.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(brothersButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.brothersButton.text")); // NOI18N
        brothersButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.brothersButton.toolTipText")); // NOI18N
        brothersButton.setMaximumSize(new java.awt.Dimension(45, 27));
        brothersButton.setMinimumSize(new java.awt.Dimension(45, 27));
        brothersButton.setPreferredSize(new java.awt.Dimension(60, 27));
        brothersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                brothersButtonActionPerformed(evt);
            }
        });

        sistersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/sister.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(sistersButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.sistersButton.text")); // NOI18N
        sistersButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.sistersButton.toolTipText")); // NOI18N
        sistersButton.setMaximumSize(new java.awt.Dimension(45, 27));
        sistersButton.setMinimumSize(new java.awt.Dimension(45, 27));
        sistersButton.setPreferredSize(new java.awt.Dimension(60, 27));
        sistersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sistersButtonActionPerformed(evt);
            }
        });

        spousesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/spouse.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(spousesButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.spousesButton.text")); // NOI18N
        spousesButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.spousesButton.toolTipText")); // NOI18N
        spousesButton.setMaximumSize(new java.awt.Dimension(45, 27));
        spousesButton.setMinimumSize(new java.awt.Dimension(45, 27));
        spousesButton.setPreferredSize(new java.awt.Dimension(60, 27));
        spousesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spousesButtonActionPerformed(evt);
            }
        });

        childrenButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/children.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(childrenButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.childrenButton.text")); // NOI18N
        childrenButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.childrenButton.toolTipText")); // NOI18N
        childrenButton.setMaximumSize(new java.awt.Dimension(45, 27));
        childrenButton.setMinimumSize(new java.awt.Dimension(45, 27));
        childrenButton.setPreferredSize(new java.awt.Dimension(60, 27));
        childrenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                childrenButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout BottomButtonsPanelLayout = new javax.swing.GroupLayout(BottomButtonsPanel);
        BottomButtonsPanel.setLayout(BottomButtonsPanelLayout);
        BottomButtonsPanelLayout.setHorizontalGroup(
            BottomButtonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BottomButtonsPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(brothersButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sistersButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spousesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(childrenButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        BottomButtonsPanelLayout.setVerticalGroup(
            BottomButtonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BottomButtonsPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(BottomButtonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(brothersButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(spousesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sistersButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(childrenButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout namePanelLayout = new javax.swing.GroupLayout(namePanel);
        namePanel.setLayout(namePanelLayout);
        namePanelLayout.setHorizontalGroup(
            namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(TopButtonsdPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(BottomButtonsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(namePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(namePanelLayout.createSequentialGroup()
                        .addComponent(maleRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(femaleRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(unknownRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(privateCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(namePanelLayout.createSequentialGroup()
                        .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(firstnamesText, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(firstnamesLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(namePanelLayout.createSequentialGroup()
                                .addComponent(lastnameLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(namePanelLayout.createSequentialGroup()
                                .addComponent(lastnameText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(2, 2, 2)
                                .addComponent(moreNamesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2))))))
        );
        namePanelLayout.setVerticalGroup(
            namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(namePanelLayout.createSequentialGroup()
                .addComponent(TopButtonsdPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(firstnamesLabel)
                    .addComponent(lastnameLabel))
                .addGap(3, 3, 3)
                .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(firstnamesText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lastnameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(moreNamesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(privateCheckBox)
                    .addComponent(maleRadioButton)
                    .addComponent(femaleRadioButton)
                    .addComponent(unknownRadioButton))
                .addGap(3, 3, 3)
                .addComponent(BottomButtonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        scrollPaneFamily.setPreferredSize(new java.awt.Dimension(106, 113));
        scrollPaneFamily.setRequestFocusEnabled(false);
        scrollPaneFamily.setViewportView(familyTree);

        NotePanel.setPreferredSize(new java.awt.Dimension(256, 30));

        textAreaNotes.setColumns(20);
        textAreaNotes.setLineWrap(true);
        textAreaNotes.setRows(3);
        textAreaNotes.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.textAreaNotes.text")); // NOI18N
        textAreaNotes.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.textAreaNotes.toolTipText")); // NOI18N
        textAreaNotes.setWrapStyleWord(true);
        textAreaNotes.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                textAreaNotesMouseWheelMoved(evt);
            }
        });
        scrollPaneNotes.setViewportView(textAreaNotes);

        scrollNotes.setBlockIncrement(1);
        scrollNotes.setVisibleAmount(5);
        scrollNotes.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                scrollNotesMouseWheelMoved(evt);
            }
        });
        scrollNotes.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollNotesAdjustmentValueChanged(evt);
            }
        });

        addNoteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addNoteButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addNoteButton.text")); // NOI18N
        addNoteButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addNoteButton.toolTipText")); // NOI18N
        addNoteButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addNoteButton.setIconTextGap(0);
        addNoteButton.setPreferredSize(new java.awt.Dimension(16, 16));
        addNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNoteButtonActionPerformed(evt);
            }
        });

        delNoteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(delNoteButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delNoteButton.text")); // NOI18N
        delNoteButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delNoteButton.toolTipText")); // NOI18N
        delNoteButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        delNoteButton.setIconTextGap(0);
        delNoteButton.setPreferredSize(new java.awt.Dimension(16, 16));
        delNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delNoteButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout NotePanelLayout = new javax.swing.GroupLayout(NotePanel);
        NotePanel.setLayout(NotePanelLayout);
        NotePanelLayout.setHorizontalGroup(
            NotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NotePanelLayout.createSequentialGroup()
                .addComponent(scrollPaneNotes)
                .addGap(2, 2, 2)
                .addGroup(NotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, NotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(delNoteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(addNoteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrollNotes, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        NotePanelLayout.setVerticalGroup(
            NotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NotePanelLayout.createSequentialGroup()
                .addComponent(scrollNotes, javax.swing.GroupLayout.DEFAULT_SIZE, 12, Short.MAX_VALUE)
                .addGap(2, 2, 2)
                .addComponent(addNoteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(delNoteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(scrollPaneNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        eventSplitPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                eventSplitPanePropertyChange(evt);
            }
        });

        eventLeft.setPreferredSize(new java.awt.Dimension(200, 306));

        eventBirtButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/birth.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventBirtButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventBirtButton.text")); // NOI18N
        eventBirtButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventBirtButton.toolTipText")); // NOI18N
        eventBirtButton.setPreferredSize(new java.awt.Dimension(30, 26));

        eventBaptButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/baptism.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventBaptButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventBaptButton.text")); // NOI18N
        eventBaptButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventBaptButton.toolTipText")); // NOI18N
        eventBaptButton.setPreferredSize(new java.awt.Dimension(30, 26));

        eventOccuButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/marr.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventOccuButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventOccuButton.text")); // NOI18N
        eventOccuButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventOccuButton.toolTipText")); // NOI18N
        eventOccuButton.setPreferredSize(new java.awt.Dimension(30, 26));

        eventDeatButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/death.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventDeatButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventDeatButton.text")); // NOI18N
        eventDeatButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventDeatButton.toolTipText")); // NOI18N
        eventDeatButton.setPreferredSize(new java.awt.Dimension(30, 26));

        eventBuriButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/burial.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventBuriButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventBuriButton.text")); // NOI18N
        eventBuriButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventBuriButton.toolTipText")); // NOI18N
        eventBuriButton.setPreferredSize(new java.awt.Dimension(30, 26));
        eventBuriButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventBuriButtonActionPerformed(evt);
            }
        });

        eventMarrButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/occu.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventMarrButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventMarrButton.text")); // NOI18N
        eventMarrButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventMarrButton.toolTipText")); // NOI18N
        eventMarrButton.setActionCommand(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventMarrButton.actionCommand")); // NOI18N
        eventMarrButton.setPreferredSize(new java.awt.Dimension(30, 26));

        eventRetiButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/retirement.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventRetiButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventRetiButton.text")); // NOI18N
        eventRetiButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventRetiButton.toolTipText")); // NOI18N
        eventRetiButton.setActionCommand(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventRetiButton.actionCommand")); // NOI18N
        eventRetiButton.setPreferredSize(new java.awt.Dimension(30, 26));

        eventResiButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/residency.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventResiButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventResiButton.text")); // NOI18N
        eventResiButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventResiButton.toolTipText")); // NOI18N
        eventResiButton.setActionCommand(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventResiButton.actionCommand")); // NOI18N
        eventResiButton.setPreferredSize(new java.awt.Dimension(30, 26));

        eventOthersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/event.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventOthersButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventOthersButton.text")); // NOI18N
        eventOthersButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventOthersButton.toolTipText")); // NOI18N
        eventOthersButton.setActionCommand(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventOthersButton.actionCommand")); // NOI18N
        eventOthersButton.setPreferredSize(new java.awt.Dimension(30, 26));

        eventRemoveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventRemoveButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventRemoveButton.text")); // NOI18N
        eventRemoveButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventRemoveButton.toolTipText")); // NOI18N
        eventRemoveButton.setActionCommand(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventRemoveButton.actionCommand")); // NOI18N
        eventRemoveButton.setPreferredSize(new java.awt.Dimension(30, 26));

        eventTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        eventScrollPane.setViewportView(eventTable);

        modificationLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        modificationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        org.openide.awt.Mnemonics.setLocalizedText(modificationLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.modificationLabel.text")); // NOI18N

        sourcePanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        sourcePanel.setPreferredSize(new java.awt.Dimension(197, 140));

        javax.swing.GroupLayout sourceImagePanelLayout = new javax.swing.GroupLayout(sourceImagePanel);
        sourceImagePanel.setLayout(sourceImagePanelLayout);
        sourceImagePanelLayout.setHorizontalGroup(
            sourceImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        sourceImagePanelLayout.setVerticalGroup(
            sourceImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 195, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout sourcePanelLayout = new javax.swing.GroupLayout(sourcePanel);
        sourcePanel.setLayout(sourcePanelLayout);
        sourcePanelLayout.setHorizontalGroup(
            sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sourceImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        sourcePanelLayout.setVerticalGroup(
            sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sourceImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout eventLeftLayout = new javax.swing.GroupLayout(eventLeft);
        eventLeft.setLayout(eventLeftLayout);
        eventLeftLayout.setHorizontalGroup(
            eventLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventLeftLayout.createSequentialGroup()
                .addGroup(eventLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(eventLeftLayout.createSequentialGroup()
                        .addComponent(eventBirtButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventBaptButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventOccuButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventDeatButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventBuriButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(eventLeftLayout.createSequentialGroup()
                        .addComponent(eventMarrButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventRetiButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventResiButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventOthersButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventRemoveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 26, Short.MAX_VALUE))
            .addGroup(eventLeftLayout.createSequentialGroup()
                .addGroup(eventLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(eventLeftLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(modificationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(eventScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(sourcePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE))
                .addGap(2, 2, 2))
        );
        eventLeftLayout.setVerticalGroup(
            eventLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventLeftLayout.createSequentialGroup()
                .addGroup(eventLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(eventBirtButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventBaptButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventOccuButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventDeatButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventBuriButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(eventLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(eventMarrButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventRetiButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventResiButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventOthersButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventRemoveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addComponent(eventScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourcePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modificationLabel))
        );

        eventSplitPane.setLeftComponent(eventLeft);

        eventRight.setPreferredSize(new java.awt.Dimension(300, 106));

        eventTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        eventTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(eventTitle, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventTitle.text")); // NOI18N

        eventDescription.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventDescription.text")); // NOI18N
        eventDescription.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventDescription.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(datelabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.datelabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dayOfWeek, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.dayOfWeek.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ageAtEvent, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.ageAtEvent.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(placeLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.placeLabel.text")); // NOI18N

        eventPlace.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventPlace.text")); // NOI18N

        eventPlaceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/place.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventPlaceButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventPlaceButton.text")); // NOI18N
        eventPlaceButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventPlaceButton.toolTipText")); // NOI18N
        eventPlaceButton.setPreferredSize(new java.awt.Dimension(28, 24));

        eventNotePanel.setPreferredSize(new java.awt.Dimension(256, 60));

        eventNote.setColumns(20);
        eventNote.setLineWrap(true);
        eventNote.setRows(3);
        eventNote.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventNote.text")); // NOI18N
        eventNote.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.textAreaNotes.toolTipText")); // NOI18N
        eventNote.setWrapStyleWord(true);
        eventNote.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                eventNoteMouseWheelMoved(evt);
            }
        });
        eventNoteScrollPane.setViewportView(eventNote);

        delNoteEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(delNoteEventButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delNoteEventButton.text")); // NOI18N
        delNoteEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delNoteEventButton.toolTipText")); // NOI18N
        delNoteEventButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        delNoteEventButton.setIconTextGap(0);
        delNoteEventButton.setPreferredSize(new java.awt.Dimension(16, 16));
        delNoteEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delNoteEventButtonActionPerformed(evt);
            }
        });

        addNoteEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addNoteEventButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addNoteEventButton.text")); // NOI18N
        addNoteEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addNoteEventButton.toolTipText")); // NOI18N
        addNoteEventButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addNoteEventButton.setIconTextGap(0);
        addNoteEventButton.setPreferredSize(new java.awt.Dimension(16, 16));
        addNoteEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNoteEventButtonActionPerformed(evt);
            }
        });

        scrollNotesEvent.setBlockIncrement(1);
        scrollNotesEvent.setVisibleAmount(5);
        scrollNotesEvent.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                scrollNotesEventMouseWheelMoved(evt);
            }
        });
        scrollNotesEvent.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollNotesEventAdjustmentValueChanged(evt);
            }
        });

        javax.swing.GroupLayout eventNotePanelLayout = new javax.swing.GroupLayout(eventNotePanel);
        eventNotePanel.setLayout(eventNotePanelLayout);
        eventNotePanelLayout.setHorizontalGroup(
            eventNotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventNotePanelLayout.createSequentialGroup()
                .addComponent(eventNoteScrollPane)
                .addGap(2, 2, 2)
                .addGroup(eventNotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(scrollNotesEvent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(delNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        eventNotePanelLayout.setVerticalGroup(
            eventNotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventNotePanelLayout.createSequentialGroup()
                .addComponent(scrollNotesEvent, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE)
                .addGap(2, 2, 2)
                .addComponent(addNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(delNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(eventNoteScrollPane)
        );

        eventSourceTitle.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventSourceTitle.text")); // NOI18N
        eventSourceTitle.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventSourceTitle.toolTipText")); // NOI18N

        eventSourceText.setColumns(20);
        eventSourceText.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        eventSourceText.setLineWrap(true);
        eventSourceText.setRows(3);
        eventSourceText.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventSourceText.text")); // NOI18N
        eventSourceText.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventSourceText.toolTipText")); // NOI18N
        eventSourceText.setWrapStyleWord(true);
        eventSourceText.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                eventSourceTextMouseWheelMoved(evt);
            }
        });
        eventSourceScrollPane.setViewportView(eventSourceText);

        repoText.setEditable(false);
        repoText.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.repoText.text")); // NOI18N

        repoEditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/repository.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(repoEditButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.repoEditButton.text")); // NOI18N
        repoEditButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.repoEditButton.toolTipText")); // NOI18N
        repoEditButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        repoEditButton.setIconTextGap(0);
        repoEditButton.setPreferredSize(new java.awt.Dimension(28, 24));
        repoEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                repoEditButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout repoPanelLayout = new javax.swing.GroupLayout(repoPanel);
        repoPanel.setLayout(repoPanelLayout);
        repoPanelLayout.setHorizontalGroup(
            repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(repoPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(repoText)
                .addGap(2, 2, 2)
                .addComponent(repoEditButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        repoPanelLayout.setVerticalGroup(
            repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, repoPanelLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(repoText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(repoEditButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        scrollSourcesEvent.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                scrollSourcesEventMouseWheelMoved(evt);
            }
        });
        scrollSourcesEvent.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollSourcesEventAdjustmentValueChanged(evt);
            }
        });

        addSourceEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addSourceEventButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addSourceEventButton.text")); // NOI18N
        addSourceEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addSourceEventButton.toolTipText")); // NOI18N
        addSourceEventButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addSourceEventButton.setIconTextGap(0);
        addSourceEventButton.setPreferredSize(new java.awt.Dimension(16, 16));
        addSourceEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSourceEventButtonActionPerformed(evt);
            }
        });

        delSourceEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(delSourceEventButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delSourceEventButton.text")); // NOI18N
        delSourceEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delSourceEventButton.toolTipText")); // NOI18N
        delSourceEventButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        delSourceEventButton.setIconTextGap(0);
        delSourceEventButton.setPreferredSize(new java.awt.Dimension(16, 16));
        delSourceEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delSourceEventButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout eventSourcePanelLayout = new javax.swing.GroupLayout(eventSourcePanel);
        eventSourcePanel.setLayout(eventSourcePanelLayout);
        eventSourcePanelLayout.setHorizontalGroup(
            eventSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventSourcePanelLayout.createSequentialGroup()
                .addGroup(eventSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(eventSourceTitle)
                    .addComponent(eventSourceScrollPane)
                    .addComponent(repoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(2, 2, 2)
                .addGroup(eventSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(scrollSourcesEvent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addSourceEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(delSourceEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        eventSourcePanelLayout.setVerticalGroup(
            eventSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventSourcePanelLayout.createSequentialGroup()
                .addComponent(scrollSourcesEvent, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                .addGap(2, 2, 2)
                .addComponent(addSourceEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(delSourceEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(eventSourcePanelLayout.createSequentialGroup()
                .addComponent(eventSourceTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(eventSourceScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(repoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        assoComboBox.setMaximumRowCount(20);
        assoComboBox.setModel(cbModel);
        assoComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.assoComboBox.toolTipText")); // NOI18N

        assoEditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/association.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(assoEditButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.assoEditButton.text")); // NOI18N
        assoEditButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.assoEditButton.toolTipText")); // NOI18N
        assoEditButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        assoEditButton.setIconTextGap(0);
        assoEditButton.setPreferredSize(new java.awt.Dimension(28, 24));
        assoEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assoEditButtonActionPerformed(evt);
            }
        });

        assoEditIndi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/editindi.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(assoEditIndi, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.assoEditIndi.text")); // NOI18N
        assoEditIndi.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.assoEditIndi.toolTipText")); // NOI18N
        assoEditIndi.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        assoEditIndi.setIconTextGap(0);
        assoEditIndi.setPreferredSize(new java.awt.Dimension(16, 16));
        assoEditIndi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assoEditIndiActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout assoPanelLayout = new javax.swing.GroupLayout(assoPanel);
        assoPanel.setLayout(assoPanelLayout);
        assoPanelLayout.setHorizontalGroup(
            assoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(assoPanelLayout.createSequentialGroup()
                .addComponent(assoComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(2, 2, 2)
                .addComponent(assoEditButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(assoEditIndi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        assoPanelLayout.setVerticalGroup(
            assoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(assoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(assoComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(assoEditButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(assoEditIndi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout eventRightLayout = new javax.swing.GroupLayout(eventRight);
        eventRight.setLayout(eventRightLayout);
        eventRightLayout.setHorizontalGroup(
            eventRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventRightLayout.createSequentialGroup()
                .addGap(2, 2, 2)
                .addGroup(eventRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(eventRightLayout.createSequentialGroup()
                        .addComponent(datelabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(eventRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(eventRightLayout.createSequentialGroup()
                                .addComponent(dayOfWeek)
                                .addGap(18, 18, 18)
                                .addComponent(ageAtEvent)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(eventRightLayout.createSequentialGroup()
                                .addComponent(eventDate, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                                .addGap(2, 2, 2))))
                    .addGroup(eventRightLayout.createSequentialGroup()
                        .addComponent(eventTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventDescription))
                    .addGroup(eventRightLayout.createSequentialGroup()
                        .addComponent(placeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventPlace)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventPlaceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(eventNotePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 335, Short.MAX_VALUE)
                    .addComponent(eventSourcePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addComponent(assoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        eventRightLayout.setVerticalGroup(
            eventRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventRightLayout.createSequentialGroup()
                .addGroup(eventRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eventDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventTitle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(eventRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(eventDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(datelabel, javax.swing.GroupLayout.Alignment.LEADING))
                .addGroup(eventRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dayOfWeek)
                    .addComponent(ageAtEvent))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(eventRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(placeLabel)
                    .addComponent(eventPlace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventPlaceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eventNotePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eventSourcePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(assoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        eventSplitPane.setRightComponent(eventRight);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(title, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(eventSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE)
                        .addGap(2, 2, 2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(mediaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(namePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(scrollPaneFamily, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(separator, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(NotePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 545, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(title)
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(namePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scrollPaneFamily, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(mediaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(NotePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(eventSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void maleRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maleRadioButtonActionPerformed
        changes.setChanged(true);
        if (mediaSet == null || mediaSet.isEmpty()) {
            setPhoto(null, PropertySex.MALE);
        }
    }//GEN-LAST:event_maleRadioButtonActionPerformed

    private void femaleRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_femaleRadioButtonActionPerformed
        changes.setChanged(true);
        if (mediaSet == null || mediaSet.isEmpty()) {
            setPhoto(null, PropertySex.FEMALE);
        }
    }//GEN-LAST:event_femaleRadioButtonActionPerformed

    private void unknownRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unknownRadioButtonActionPerformed
        changes.setChanged(true);
        if (mediaSet == null || mediaSet.isEmpty()) {
            setPhoto(null, PropertySex.UNKNOWN);
        }
    }//GEN-LAST:event_unknownRadioButtonActionPerformed

    private void privateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateCheckBoxActionPerformed
        changes.setChanged(true);
    }//GEN-LAST:event_privateCheckBoxActionPerformed

    private void scrollPhotosAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_scrollPhotosAdjustmentValueChanged
        if (isBusyMedia) {
            return;
        }
        int i = scrollPhotos.getValue();
        if (mediaSet != null && !mediaSet.isEmpty() && i >= 0 && i < mediaSet.size() && i != mediaIndex) {
            mediaIndex = scrollPhotos.getValue();
            displayPhoto();
        }
    }//GEN-LAST:event_scrollPhotosAdjustmentValueChanged

    private void photosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_photosMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1) {
            if (chooseMedia(mediaIndex)) {
                displayPhoto();
                textAreaPhotos.requestFocus();
            }
        } else if (evt.getButton() == MouseEvent.BUTTON3) {
            if (mediaSet != null && !mediaSet.isEmpty() && (mediaIndex >= 0) && (mediaIndex < mediaSet.size())) {
                try {
                    if (mediaSet.get(mediaIndex) != null && mediaSet.get(mediaIndex).getFile() != null) {
                        Desktop.getDesktop().open(mediaSet.get(mediaIndex).getFile());
                    }
                } catch (IOException ex) {
                    //Exceptions.printStackTrace(ex);
                }
            }
        }
    }//GEN-LAST:event_photosMouseClicked

    private void addMediaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMediaButtonActionPerformed
        if (chooseMedia(mediaSet.size())) {
            displayPhoto();
            textAreaPhotos.requestFocus();
        }
    }//GEN-LAST:event_addMediaButtonActionPerformed

    private void delMediaButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delMediaButtonActionPerformed
        if (mediaSet != null && !mediaSet.isEmpty() && (mediaIndex >= 0) && (mediaIndex < mediaSet.size())) {
            MediaWrapper media = mediaSet.get(mediaIndex);
            mediaRemovedSet.add(media);
            mediaSet.remove(mediaIndex);
            mediaIndex--;
            if (mediaIndex < 0) {
                mediaIndex = 0;
            }
            changes.setChanged(true);
        }
        displayPhoto();        
    }//GEN-LAST:event_delMediaButtonActionPerformed

    private void brothersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_brothersButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_brothersButtonActionPerformed

    private void sistersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sistersButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sistersButtonActionPerformed

    private void spousesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spousesButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_spousesButtonActionPerformed

    private void childrenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_childrenButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_childrenButtonActionPerformed

    private void fatherButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fatherButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_fatherButtonActionPerformed

    private void motherButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_motherButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_motherButtonActionPerformed

    private void moreNamesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moreNamesButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_moreNamesButtonActionPerformed

    private void firstnamesTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firstnamesTextActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_firstnamesTextActionPerformed

    private void eventBuriButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventBuriButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_eventBuriButtonActionPerformed

    private void scrollNotesAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_scrollNotesAdjustmentValueChanged
        if (isBusyNote) {
            return;
        }
        int i = scrollNotes.getValue();
        if (noteSet != null && !noteSet.isEmpty() && i >= 0 && i < noteSet.size() && i != noteIndex) {
            noteIndex = scrollNotes.getValue();
            displayNote();
        }
    }//GEN-LAST:event_scrollNotesAdjustmentValueChanged

    private void addNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNoteButtonActionPerformed
        if (chooseNote(noteSet.size())) {
            displayNote();
            textAreaNotes.requestFocus();
        }
    }//GEN-LAST:event_addNoteButtonActionPerformed

    private void delNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delNoteButtonActionPerformed
        if (noteSet != null && !noteSet.isEmpty() && (noteIndex >= 0) && (noteIndex < noteSet.size())) {
            NoteWrapper note = noteSet.get(noteIndex);
            noteRemovedSet.add(note);
            noteSet.remove(noteIndex);
            noteIndex--;
            if (noteIndex < 0) {
                noteIndex = 0;
            }
            changes.setChanged(true);
        }
        displayNote();        
    }//GEN-LAST:event_delNoteButtonActionPerformed

    private void scrollNotesEventAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_scrollNotesEventAdjustmentValueChanged
        if (isBusyEventNote) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        int i = scrollNotesEvent.getValue();
        if (event.eventNoteSet != null && !event.eventNoteSet.isEmpty() && i >= 0 && i < event.eventNoteSet.size() && i != event.eventNoteIndex) {
            event.eventNoteIndex = scrollNotesEvent.getValue();
            displayEventNote(event);
        }
    }//GEN-LAST:event_scrollNotesEventAdjustmentValueChanged

    private void textAreaNotesMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_textAreaNotesMouseWheelMoved
        int notches = evt.getWheelRotation();
        if (evt.isControlDown()) {
            scrollNotes(notches);
        } else {
            JScrollBar vbar = scrollPaneNotes.getVerticalScrollBar();
            int currentPosition = vbar.getValue();
            vbar.setValue(currentPosition + notches * vbar.getBlockIncrement() * 3);
        }
    }//GEN-LAST:event_textAreaNotesMouseWheelMoved

    private void scrollNotesMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollNotesMouseWheelMoved
        int notches = evt.getWheelRotation();
        scrollNotes(notches);
    }//GEN-LAST:event_scrollNotesMouseWheelMoved

    private void scrollPhotosMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollPhotosMouseWheelMoved
        int notches = evt.getWheelRotation();
        if (isBusyMedia) {
            return;
        }
        if (mediaSet != null && !mediaSet.isEmpty()) {
            int i = mediaIndex + notches;
            if (i >= mediaSet.size()) {
                i = mediaSet.size() - 1;
            }
            if (i < 0) {
                i = 0;
            }
            mediaIndex = i;
            displayPhoto();
        }
        
        
    }//GEN-LAST:event_scrollPhotosMouseWheelMoved

    private void scrollNotesEventMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollNotesEventMouseWheelMoved
        int notches = evt.getWheelRotation();
        scrollEventNotes(notches);
    }//GEN-LAST:event_scrollNotesEventMouseWheelMoved

    private void eventNoteMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_eventNoteMouseWheelMoved
        int notches = evt.getWheelRotation();
        if (evt.isControlDown()) {
            scrollEventNotes(notches);
        } else {
            JScrollBar vbar = eventNoteScrollPane.getVerticalScrollBar();
            int currentPosition = vbar.getValue();
            vbar.setValue(currentPosition + notches * vbar.getBlockIncrement() * 3);
        }
    }//GEN-LAST:event_eventNoteMouseWheelMoved

    private void addNoteEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNoteEventButtonActionPerformed
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (chooseEventNote(event, event.eventNoteSet.size())) {
            displayEventNote(event);
            eventNote.requestFocus();
        }
    }//GEN-LAST:event_addNoteEventButtonActionPerformed

    private void delNoteEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delNoteEventButtonActionPerformed
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (event.eventNoteSet != null && !event.eventNoteSet.isEmpty() && (event.eventNoteIndex >= 0) && (event.eventNoteIndex < event.eventNoteSet.size())) {
            NoteWrapper note = event.eventNoteSet.get(event.eventNoteIndex);
            event.eventNoteRemovedSet.add(note);
            event.eventNoteSet.remove(event.eventNoteIndex);
            event.eventNoteIndex--;
            if (event.eventNoteIndex < 0) {
                event.eventNoteIndex = 0;
            }
            changes.setChanged(true);
        }
        displayEventNote(event);
    }//GEN-LAST:event_delNoteEventButtonActionPerformed

    private void eventSplitPanePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_eventSplitPanePropertyChange
        registry.put("eventSplitDividerLocation", eventSplitPane.getDividerLocation());
        imagePanel.redraw();
    }//GEN-LAST:event_eventSplitPanePropertyChange

    private void addSourceEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSourceEventButtonActionPerformed
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (chooseSource(event, event.eventSourceSet.size())) {
            displayEventSource(event);
            eventSourceTitle.requestFocus();
        }
    }//GEN-LAST:event_addSourceEventButtonActionPerformed

    private void scrollSourcesEventAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_scrollSourcesEventAdjustmentValueChanged
        if (isBusyEventSource) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        int i = scrollSourcesEvent.getValue();
        if (event.eventSourceSet != null && !event.eventSourceSet.isEmpty() && i >= 0 && i < event.eventSourceSet.size() && i != event.eventSourceIndex) {
            event.eventSourceIndex = scrollSourcesEvent.getValue();
            displayEventSource(event);
        }
    }//GEN-LAST:event_scrollSourcesEventAdjustmentValueChanged

    private void scrollSourcesEventMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollSourcesEventMouseWheelMoved
        int notches = evt.getWheelRotation();
        scrollEventSources(notches);
    }//GEN-LAST:event_scrollSourcesEventMouseWheelMoved

    private void delSourceEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delSourceEventButtonActionPerformed
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (event.eventSourceSet != null && !event.eventSourceSet.isEmpty() && (event.eventSourceIndex >= 0) && (event.eventSourceIndex < event.eventSourceSet.size())) {
            SourceWrapper source = event.eventSourceSet.get(event.eventSourceIndex);
            event.eventSourceRemovedSet.add(source);
            event.eventSourceSet.remove(event.eventSourceIndex);
            event.eventSourceIndex--;
            if (event.eventSourceIndex < 0) {
                event.eventSourceIndex = 0;
            }
            changes.setChanged(true);
        }
        displayEventSource(event);
    }//GEN-LAST:event_delSourceEventButtonActionPerformed

    private void repoEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_repoEditButtonActionPerformed
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (chooseRepository(event)) {
            displayEventSource(event);
        }
        eventSourceTitle.requestFocus();

    }//GEN-LAST:event_repoEditButtonActionPerformed

    private void assoEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assoEditButtonActionPerformed
        if (manageAssociations()) {
            displayAssociationsComboBox();
            selectAssociation();
        }
        eventSourceTitle.requestFocus();

    }//GEN-LAST:event_assoEditButtonActionPerformed

    private void eventSourceTextMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_eventSourceTextMouseWheelMoved
        int notches = evt.getWheelRotation();
        if (evt.isControlDown()) {
            scrollEventSources(notches);
        } else {
            JScrollBar vbar = eventSourceScrollPane.getVerticalScrollBar();
            int currentPosition = vbar.getValue();
            vbar.setValue(currentPosition + notches * vbar.getBlockIncrement() * 3);
        }
    }//GEN-LAST:event_eventSourceTextMouseWheelMoved

    private void assoEditIndiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assoEditIndiActionPerformed
        AssoWrapper asso = (AssoWrapper) assoComboBox.getSelectedItem();
        if (asso.assoIndi != null) {
            SelectionDispatcher.fireSelection(new Context(asso.assoIndi));
        }
    }//GEN-LAST:event_assoEditIndiActionPerformed

    
    private void scrollNotes(int notches) {
        if (isBusyNote) {
            return;
        }
        if (noteSet != null && !noteSet.isEmpty()) {
            int i = noteIndex + notches;
            if (i >= noteSet.size()) {
                i = noteSet.size() - 1;
            }
            if (i < 0) {
                i = 0;
            }
            noteIndex = i;
            displayNote();
        }
    }
    
    private void scrollEventNotes(int notches) {
        if (isBusyEventNote) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (event.eventNoteSet != null && !event.eventNoteSet.isEmpty()) {
            int i = event.eventNoteIndex + notches;
            if (i >= event.eventNoteSet.size()) {
                i = event.eventNoteSet.size() - 1;
            }
            if (i < 0) {
                i = 0;
            }
            event.eventNoteIndex = i;
            displayEventNote(event);
        }
    }

    private void scrollEventSources(int notches) {
        if (isBusyEventSource) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (event.eventSourceSet != null && !event.eventSourceSet.isEmpty()) {
            int i = event.eventSourceIndex + notches;
            if (i >= event.eventSourceSet.size()) {
                i = event.eventSourceSet.size() - 1;
            }
            if (i < 0) {
                i = 0;
            }
            event.eventSourceIndex = i;
            displayEventSource(event);
        }
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel BottomButtonsPanel;
    private javax.swing.JPanel NotePanel;
    private javax.swing.JPanel TopButtonsdPanel;
    private javax.swing.JButton addMediaButton;
    private javax.swing.JButton addNoteButton;
    private javax.swing.JButton addNoteEventButton;
    private javax.swing.JButton addSourceEventButton;
    private javax.swing.JLabel ageAtEvent;
    private javax.swing.JComboBox assoComboBox;
    private javax.swing.JButton assoEditButton;
    private javax.swing.JButton assoEditIndi;
    private javax.swing.JPanel assoPanel;
    private javax.swing.JButton brothersButton;
    private javax.swing.ButtonGroup buttonGender;
    private javax.swing.JButton childrenButton;
    private javax.swing.JLabel datelabel;
    private javax.swing.JLabel dayOfWeek;
    private javax.swing.JButton delMediaButton;
    private javax.swing.JButton delNoteButton;
    private javax.swing.JButton delNoteEventButton;
    private javax.swing.JButton delSourceEventButton;
    private javax.swing.JButton eventBaptButton;
    private javax.swing.JButton eventBirtButton;
    private javax.swing.JButton eventBuriButton;
    private genj.edit.beans.DateBean eventDate;
    private javax.swing.JButton eventDeatButton;
    private javax.swing.JTextField eventDescription;
    private javax.swing.JPanel eventLeft;
    private javax.swing.JButton eventMarrButton;
    private javax.swing.JTextArea eventNote;
    private javax.swing.JPanel eventNotePanel;
    private javax.swing.JScrollPane eventNoteScrollPane;
    private javax.swing.JButton eventOccuButton;
    private javax.swing.JButton eventOthersButton;
    private javax.swing.JTextField eventPlace;
    private javax.swing.JButton eventPlaceButton;
    private javax.swing.JButton eventRemoveButton;
    private javax.swing.JButton eventResiButton;
    private javax.swing.JButton eventRetiButton;
    private javax.swing.JPanel eventRight;
    private javax.swing.JScrollPane eventScrollPane;
    private javax.swing.JPanel eventSourcePanel;
    private javax.swing.JScrollPane eventSourceScrollPane;
    private javax.swing.JTextArea eventSourceText;
    private javax.swing.JTextField eventSourceTitle;
    private javax.swing.JSplitPane eventSplitPane;
    private javax.swing.JTable eventTable;
    private javax.swing.JLabel eventTitle;
    private javax.swing.JTree familyTree;
    private javax.swing.JButton fatherButton;
    private javax.swing.JRadioButton femaleRadioButton;
    private javax.swing.JLabel firstnamesLabel;
    private javax.swing.JTextField firstnamesText;
    private javax.swing.JLabel idLabel;
    private javax.swing.JLabel lastnameLabel;
    private javax.swing.JTextField lastnameText;
    private javax.swing.JRadioButton maleRadioButton;
    private javax.swing.JPanel mediaPanel;
    private javax.swing.JLabel modificationLabel;
    private javax.swing.JButton moreNamesButton;
    private javax.swing.JButton motherButton;
    private javax.swing.JPanel namePanel;
    private javax.swing.JLabel photos;
    private javax.swing.JLabel placeLabel;
    private javax.swing.JCheckBox privateCheckBox;
    private javax.swing.JButton repoEditButton;
    private javax.swing.JPanel repoPanel;
    private javax.swing.JTextField repoText;
    private javax.swing.JScrollBar scrollNotes;
    private javax.swing.JScrollBar scrollNotesEvent;
    private javax.swing.JScrollPane scrollPaneFamily;
    private javax.swing.JScrollPane scrollPaneNotes;
    private javax.swing.JScrollPane scrollPanePhotos;
    private javax.swing.JScrollBar scrollPhotos;
    private javax.swing.JScrollBar scrollSourcesEvent;
    private javax.swing.JSeparator separator;
    private javax.swing.JButton sistersButton;
    private javax.swing.JLabel sosaLabel;
    private javax.swing.JPanel sourceImagePanel;
    private javax.swing.JPanel sourcePanel;
    private javax.swing.JButton spousesButton;
    private javax.swing.JTextArea textAreaNotes;
    private javax.swing.JTextArea textAreaPhotos;
    private javax.swing.JLabel title;
    private javax.swing.JRadioButton unknownRadioButton;
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
    public void setGedcomHasChanged(boolean flag) {
        reloadData = flag;
    }
    
    @Override
    protected void setContextImpl(Context context) {
        LOG.finer(TimingUtility.geInstance().getTime() + ": setContextImpl().start");

        // force data reload if entity selected is different
        if (this.context != null && context != null && this.context != context && this.context.getEntity() != context.getEntity()) {
            reloadData = true;
        }
        
        this.context = context;
        Entity entity = context.getEntity();
        if (entity != null && (entity instanceof Indi)) {
            this.indi = (Indi) entity;
            this.gedcom = indi.getGedcom();

            if (reloadData) {  // do not reload data when not necessary, for performance reasons when selecting properties in Gedcom editor for instance
                loadData();
                reloadData = false;
            }

            if (!listernersOn) {
                addListeners();
                listernersOn = true;
            }
            
            selectPropertyContext(context);
        }

        LOG.finer(TimingUtility.geInstance().getTime() + ": setContextImpl().finish");
    }

    /**
     * Select event corresponding to property
     * - if context is of different entity
     * - if context is an event, select if
     * TODO:
     * - if context if a wedding type event, select it
     * - if context is an Asso type data, select it ?
     * - is context is a note, select it
     * - if context is a source, select it
     * 
     * @param context 
     */
    private void selectPropertyContext(Context context) {
        Property propertyToDisplay = context.getProperty();
        if (propertyToDisplay != null && eventSet != null) {
            Property ent = propertyToDisplay.getEntity();
            while (propertyToDisplay != ent) {
                for (EventWrapper event : eventSet) {
                    if (event.eventProperty.equals(propertyToDisplay)) {
                        int index = eventSet.indexOf(event);
                        if (index != -1) {
                            int row = eventTable.convertRowIndexToView(index);
                            eventTable.setRowSelectionInterval(row, row);
                            break;
                        }
                    }
                } // end for
                propertyToDisplay = propertyToDisplay.getParent();
            }
        }
    }
    
    
    @Override
    public void commit() throws GedcomException {
        saveData();
    }


    
    
    
    
    
    
    

    
    
    
    
    
    
    
    
    
    
    
    private void loadData() {
        String str = "";
        int i = 0;
        boolean privateTagFound = false;
        
        // Title
        title.setText("<html> <font color=\"red\"><b>/!\\ TRAVAUX EN COURS /!\\</b></font> " + indi.getFirstName() + " " + indi.getLastName() + " </html> ");

        // IDs
        idLabel.setText(NbBundle.getMessage(IndiPanel.class, "IndiPanel.idLabel.text") + " " + indi.getId());
        str = indi.getPropertyDisplayValue(SOSA_TAG); // TODO : prévoir sosa daboville en référence utilisateur
        if (str == null || str.isEmpty()) {
            str = NO_SOSA;
        }
        sosaLabel.setText(NbBundle.getMessage(IndiPanel.class, "IndiPanel.sosaLabel.text") + " " + str);

        // Names
        firstnamesText.setText(indi.getFirstName());
        lastnameText.setText(indi.getLastName());
        // TODO: other names in popup to be done later

        // Sex
        i = indi.getSex();
        if (i == PropertySex.MALE) {
            maleRadioButton.setSelected(true);
        } else if (i == PropertySex.FEMALE) {
            femaleRadioButton.setSelected(true);
        } else {
            unknownRadioButton.setSelected(true);
        }
        
        // Privacy
        privateTagFound = (indi.getProperty(Options.getInstance().getPrivateTag()) != null);
        privateCheckBox.setSelected(privateTagFound);
        
        // Medias
        if (mediaSet != null) {
            mediaSet.clear();
            mediaSet = null;
        }
        if (mediaRemovedSet != null) {
            mediaRemovedSet.clear();
            mediaRemovedSet = null;
        }
        mediaSet = getMedia(indi);
        mediaRemovedSet = new ArrayList<MediaWrapper>();
        scrollPhotos.setMinimum(0);
        scrollPhotos.setBlockIncrement(1);
        scrollPhotos.setUnitIncrement(1);
        mediaIndex = 0;
        displayPhoto();

        // Family tree (parents, siblings, mariages and corresponding childrens)
        createFamilyNodes(indi);
        familyTree.repaint();
        
        // Notes of individual
        if (noteSet != null) {
            noteSet.clear();
            noteSet = null;
        }
        if (noteRemovedSet != null) {
            noteRemovedSet.clear();
            noteRemovedSet = null;
        }
        noteSet = getNotes(indi);
        noteRemovedSet = new ArrayList<NoteWrapper>();
        scrollNotes.setMinimum(0);
        scrollNotes.setBlockIncrement(1);
        scrollNotes.setUnitIncrement(1);
        noteIndex = 0;
        displayNote();
        
        // Events
        if (eventSet != null) {
            eventSet.clear();
            eventSet = null;
        }
        if (eventRemovedSet != null) {
            eventRemovedSet.clear();
            eventRemovedSet = null;
        }
        eventSet = getEvents(indi);
        eventRemovedSet = new ArrayList<EventWrapper>();
        eventIndex = 0;
        displayEventTable();
        displayEvent();
        
        // Associations
        if (assoSet != null) {
            assoSet.clear();
            assoSet = null;
        }
        assoSet = getAssociations(indi);
        assoRemovedSet = new ArrayList<AssoWrapper>();
        displayAssociationsComboBox();
        selectAssociation();
        
        // Modification timestamp
        modificationLabel.setText(NbBundle.getMessage(IndiPanel.class, "IndiPanel.modificationLabel.text") + " : " + (indi.getLastChange() != null ? indi.getLastChange().getDisplayValue() : ""));
        //
        //.......................................

    }

    private void addListeners() {
        firstnamesText.getDocument().addDocumentListener(this);
        lastnameText.getDocument().addDocumentListener(this);
        textAreaPhotos.getDocument().addDocumentListener(new PhotoTitleListener());
        textAreaNotes.getDocument().addDocumentListener(new NoteTextListener());
        
        eventDescription.getDocument().addDocumentListener(new EventDescriptionListener());
        eventDate.addChangeListener(new EventDateListener());
        eventPlace.getDocument().addDocumentListener(new EventDescriptionListener());
        
        eventNote.getDocument().addDocumentListener(new EventNoteTextListener());
        EventSourceTextListener estl = new EventSourceTextListener();
        eventSourceTitle.getDocument().addDocumentListener(estl);
        eventSourceText.getDocument().addDocumentListener(estl);
        
    }

    
    
    private void saveData() {
        
        // Save Indi and Sex
        indi.setName(firstnamesText.getText(), lastnameText.getText());
        //
        indi.setSex(getSex());
        //
        
        // Save privay
        boolean privateTagFound = (indi.getProperty(Options.getInstance().getPrivateTag()) != null);
        if (privateCheckBox.isSelected()) {
            if (!privateTagFound) {
                indi.addProperty(Options.getInstance().getPrivateTag(), "");
            }
        } else {
            if (privateTagFound) {
                indi.delProperty(indi.getProperty(Options.getInstance().getPrivateTag()));
            }
        }
        
        // Save the rest
        //
        saveMedia();
        //
        saveNotes();
        //
        saveEvents();
        //
        saveAssociations();
        //.......................................
    }

    
    
    
    //
    //
    //

    
    /**
     * Media
     * @param indi
     * @return 
     */
    
    private List<MediaWrapper> getMedia(Indi indi) {
        List<MediaWrapper> ret = new ArrayList<MediaWrapper>();
        
        // Look for media directly attached to indi (media always have a file, so look for all files underneath OBJE but not underneath SOUR)
        for (PropertyFile prop : indi.getProperties(PropertyFile.class)) {
            if (!Utils.parentTagsContains(prop, "SOUR")) {
                Property propObje = getParentTag(prop, "OBJE");
                ret.add(new MediaWrapper(propObje));
            }
        }
        
        // Look for media as links to entities
        for (PropertyMedia propMedia : indi.getProperties(PropertyMedia.class)) {
            ret.add(new MediaWrapper(propMedia));
        }
        
        return ret;
    }

    
    private void saveMedia() {
        //mediaSet
        for (MediaWrapper media : mediaSet) {
            media.update(indi);
        }
        for (MediaWrapper media : mediaRemovedSet) {
            media.remove(indi);
        }
    }

    
    

    
    private Property getParentTag(Property prop, String tag) {
        if (prop == null) {
            return null;
        }
        Property parent = prop.getParent();
        if (parent == null) {
            return null;
        }
        if (parent.getTag().equals(tag)) {
            return parent;
        }
        return getParentTag(parent, tag);
    }

    
    
    
    private void displayPhoto() {
        isBusyMedia = true;
        if (mediaSet != null && !mediaSet.isEmpty() && (mediaIndex >= 0) && (mediaIndex < mediaSet.size())) {        
            setPhoto(mediaSet.get(mediaIndex), indi.getSex());
        } else {
            setPhoto(null, getSex());
        }
        isBusyMedia = false;
    }
    
    private void setPhoto(MediaWrapper media, int sex) {
        
        File file = null;
        String localTitle = "";
        ImageIcon defaultIcon = new ImageIcon(getSexImage(sex));
        ImageIcon imageIcon = null;
        
        if (media != null) {
            file = media.getFile();
            localTitle = media.getTitle();
        }
        
        // Photo
        if (file != null && file.exists()) {
            imageIcon = new ImageIcon(getImageFromFile(file, getClass()));
        } else {
            imageIcon = defaultIcon;
        }

        photos.setIcon(getResizedIcon(imageIcon, PHOTO_WIDTH, PHOTO_HEIGHT));   // preferred size of photo label but getPreferredSize() does not return those values...
        photos.setText("");
        
        // Title
        textAreaPhotos.setText(localTitle);
        textAreaPhotos.setCaretPosition(0);
        
            
        // Update scroll
        scrollPhotos.setValues(mediaIndex, 1, 0, mediaSet.size());
        scrollPhotos.setToolTipText(getScrollPhotosLabel());
    }

    private String getScrollPhotosLabel() {
        return String.valueOf(mediaSet.size() > 0 ? mediaIndex + 1 : mediaIndex) + "/" + String.valueOf(mediaSet.size());
    }

    private int getSex() {
        return maleRadioButton.isSelected() ? PropertySex.MALE : femaleRadioButton.isSelected() ? PropertySex.FEMALE : PropertySex.UNKNOWN;
    }

    private Image getSexImage(int sex) {
        return (sex == PropertySex.MALE ? PHOTO_MALE : (sex == PropertySex.FEMALE ? PHOTO_FEMALE : PHOTO_UNKNOWN));
    }

    private boolean chooseMedia(int index) {
        boolean b = false;
        boolean exists = (mediaSet != null) && (!mediaSet.isEmpty()) && (index >= 0) && (index < mediaSet.size());
        
        JButton mediaButton = new JButton(NbBundle.getMessage(getClass(), "Button_ChooseMedia"));
        JButton fileButton = new JButton(NbBundle.getMessage(getClass(), "Button_LookForFile"));
        JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
        Object[] options = new Object[] { mediaButton, fileButton, cancelButton };
        MediaChooser mediaChooser = new MediaChooser(gedcom, exists ? mediaSet.get(index).getFile() : null,
                exists ? getImageFromFile(mediaSet.get(index).getFile(), getClass()) : getSexImage(getSex()),
                exists ? mediaSet.get(index).getTitle() : "",
                exists ? mediaSet.get(index) : null, 
                mediaButton, cancelButton
        );
        int size = mediaChooser.getNbMedia();
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_ChooseMediaTitle", size), mediaChooser).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
        if (o == mediaButton) {
            File file = mediaChooser.getSelectedFile();
            String mediaTitle = mediaChooser.getSelectedTitle();
            if (mediaChooser.isSelectedEntityMedia()) {
                Media entity = (Media) mediaChooser.getSelectedEntity();
                if (exists) {
                    mediaSet.get(index).setTargetEntity(entity);
                    mediaSet.get(index).setTitle(mediaTitle);
                    mediaIndex = index;
                } else {
                    MediaWrapper media = new MediaWrapper(entity);
                    media.setTitle(mediaTitle);
                    mediaSet.add(media);
                    mediaIndex = mediaSet.size() - 1;
                }
                changes.setChanged(true);
                b = true;
            } else {
                if (exists) {
                    mediaSet.get(index).setFile(file);
                    mediaSet.get(index).setTitle(mediaTitle);
                    mediaIndex = index;
                } else {
                    MediaWrapper media = new MediaWrapper(file, mediaTitle);
                    mediaSet.add(media);
                    mediaIndex = mediaSet.size() - 1;
                }
                changes.setChanged(true);
                b = true;
            }
        } else if (o == fileButton) {
            return chooseFileImage(index);
        }
        
        return b;
    }
    
    
    
    private boolean chooseFileImage(int index) {
        boolean b = false;
        boolean exists = (mediaSet != null) && (!mediaSet.isEmpty()) && (index >= 0) && (index < mediaSet.size());
        
        File file = new FileChooserBuilder(IndiPanel.class.getCanonicalName()+"Images")
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(getClass(), "FileChooserTitle"))
                .setApproveText(NbBundle.getMessage(getClass(), "FileChooserOKButton"))
                .setDefaultExtension(FileChooserBuilder.getImageFilter().getExtensions()[0])
                .addFileFilter(FileChooserBuilder.getImageFilter())
                .addFileFilter(FileChooserBuilder.getSoundFilter())
                .addFileFilter(FileChooserBuilder.getVideoFilter())
                .setAcceptAllFileFilterUsed(false)
                .setFileHiding(true)
                .showOpenDialog();
        if (file != null) {
            if (exists) {
                mediaSet.get(index).setFile(file);
                mediaIndex = index;
            } else {
                MediaWrapper media = new MediaWrapper(file);
                mediaSet.add(media);
                mediaIndex = mediaSet.size() - 1;
            }
            changes.setChanged(true);
            b = true;
        } else {
            textAreaPhotos.requestFocus();
        }
        return b;
    }

    private void createFamilyNodes(Indi indi) {
        familyTop.removeAllChildren();
        familyTop.setUserObject(new NodeWrapper(NodeWrapper.PARENTS, indi.getFamilyWhereBiologicalChild()));
        DefaultMutableTreeNode meNode = new DefaultMutableTreeNode(new NodeWrapper(NodeWrapper.MEUNKNOWN, indi));
        Indi[] siblings = indi.getSiblings(true);
        if (siblings.length > 0) {
            for (Indi sibling : siblings) {
                if (!sibling.equals(indi)) {
                    familyTop.add(new DefaultMutableTreeNode(new NodeWrapper(NodeWrapper.SIBLING, sibling)));
                } else {
                    familyTop.add(meNode);
                }
            }
        } else {
            familyTop.add(meNode);
        }
        Fam[] fams = indi.getFamiliesWhereSpouse();
        for (Fam fam : fams) {
            DefaultMutableTreeNode famNode = new DefaultMutableTreeNode(new NodeWrapper(NodeWrapper.SPOUSE, fam, indi));
            meNode.add(famNode);
            Indi[] children = fam.getChildren(true);
            for (Indi child : children) {
                famNode.add(new DefaultMutableTreeNode(new NodeWrapper(NodeWrapper.CHILD, child)));
            }
        }

        ((DefaultTreeModel) familyTree.getModel()).reload();
        
        for (int i = 0; i < familyTree.getRowCount(); i++) {
            familyTree.expandRow(i);
        }
    }


    //
    //
    //

    
    /**
     * Notes
     * 
     * We get notes *only as linked entities* to individuals
     * 
     * @param indi
     * @return 
     */
    
    private List<NoteWrapper> getNotes(Indi indi) {
        List<NoteWrapper> ret = new ArrayList<NoteWrapper>();
                
        // Look for only general notes directly attached to indi
        Property[] noteProps = indi.getProperties("NOTE");
        for (Property prop : noteProps) {
            if (prop != null && !prop.getDisplayValue().trim().isEmpty()) {
                ret.add(new NoteWrapper(prop));
            }
        }
        return ret;
    }

    
    private void saveNotes() {
        //noteSet
        for (NoteWrapper note : noteSet) {
            note.update(indi);
        }
        for (NoteWrapper note : noteRemovedSet) {
            note.remove(indi);
        }
    }

    
    private void displayNote() {
        isBusyNote = true;
        if (noteSet != null && !noteSet.isEmpty() && (noteIndex >= 0) && (noteIndex < noteSet.size())) {        
            setNote(noteSet.get(noteIndex));
        } else {
            setNote(null);
        }
        isBusyNote = false;
    }
    
    private void setNote(NoteWrapper note) {
        
        String localText = "";
        
        if (note != null) {
            localText = note.getText();
        }
        
        // Text
        textAreaNotes.setText(localText);
        textAreaNotes.setCaretPosition(0);
        
        // Update scroll
        scrollNotes.setValues(noteIndex, 1, 0, noteSet.size());
        scrollNotes.setToolTipText(getScrollNotesLabel());
    }


    private String getScrollNotesLabel() {
        return String.valueOf(noteSet.size() > 0 ? noteIndex + 1 : noteIndex) + "/" + String.valueOf(noteSet.size());
    }

    
    
    private boolean chooseNote(int index) {
        boolean b = false;
        boolean exists = (noteSet != null) && (!noteSet.isEmpty()) && (index >= 0) && (index < noteSet.size());
        
        JButton noteButton = new JButton(NbBundle.getMessage(getClass(), "Button_ChooseNote"));
        JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
        Object[] options = new Object[] { noteButton, cancelButton };
        NoteChooser noteChooser = new NoteChooser(gedcom, exists ? noteSet.get(index) : null, noteButton, cancelButton);
        int size = noteChooser.getNbNotes();
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_ChooseNoteTitle", size), noteChooser).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
        if (o == noteButton) {
            String noteText = noteChooser.getSelectedText();
            if (noteChooser.isSelectedEntityNote()) {
                Note entity = (Note) noteChooser.getSelectedEntity();
                if (exists) {
                    noteSet.get(index).setTargetEntity(entity);
                    noteSet.get(index).setText(noteText);
                    noteIndex = index;
                } else {
                    NoteWrapper note = new NoteWrapper(entity);
                    note.setText(noteText);
                    noteSet.add(note);
                    noteIndex = noteSet.size() - 1;
                }
                changes.setChanged(true);
                b = true;
            } else {
                if (exists) {
                    noteSet.get(index).setText(noteText);
                    noteIndex = index;
                } else {
                    NoteWrapper note = new NoteWrapper(noteText);
                    noteSet.add(note);
                    noteIndex = noteSet.size() - 1;
                }
                changes.setChanged(true);
                b = true;
            }
        }
        
        return b;
    }


    
    //
    //
    //

    
    
    /**
     * Events
     * 
     * We get events of Indi and related Fams
     * 
     * @param indi
     * @return 
     */
    
    private List<EventWrapper> getEvents(Indi indi) {
        List<EventWrapper> ret = new ArrayList<EventWrapper>();
                
        // Look for all individual events
        // - INDIVIDUAL_EVENT_STRUCTURE (birth, etc.)
        // - INDIVIDUAL_ATTRIBUTE_STRUCTURE (occu, resi, etc.)
        //
        String[] INDI_TAGS = EventUsage.getTags(eventUsages, "INDI");
        for (String tag : INDI_TAGS) {
            Property[] eventProps = indi.getProperties(tag);
            for (Property prop : eventProps) {
                if (prop != null) {
                    ret.add(new EventWrapper(prop, indi));
                }
            }
        }
        
        // Look for all family events in which indi is a spouse
        // - FAMILY_EVENT_STRUCTURE (marr, etc.)
        //
        String[] FAM_TAGS = EventUsage.getTags(eventUsages, "FAM");
        Fam[] fams = indi.getFamiliesWhereSpouse();
        for (Fam fam : fams) {
            for (String tag : FAM_TAGS) {
                Property[] eventProps = fam.getProperties(tag);
                for (Property prop : eventProps) {
                    if (prop != null) {
                        ret.add(new EventWrapper(prop, indi));
                    }
                }
            }
        }

        return ret;
    }

    
    private void saveEvents() {
        //eventSet
        for (EventWrapper event : eventSet) {
            event.update(indi);
        }
        for (EventWrapper event : eventRemovedSet) {
            event.remove(indi);
        }
    }


    private void displayEventTable() {
        EventTableModel etm = new EventTableModel(eventSet);
        eventTable.setModel(etm);    
        eventTable.setAutoCreateRowSorter(true);
        TableRowSorter sorter = new TableRowSorter<EventTableModel>(etm);
        sorter.setComparator(0, new Comparator<EventLabel>() {
            public int compare(EventLabel l1, EventLabel l2) {
                Integer i1 = eventUsages.get(l1.getTag()).getOrder();
                Integer i2 = eventUsages.get(l2.getTag()).getOrder();
                return i1.compareTo(i2);
            }
        });
        sorter.setComparator(2, new Comparator<String>() {
            public int compare(String s1, String s2) {
                try {
                    if (s1.equals("-")) {
                        s1 = "0";
                    }
                    if (s2.equals("-")) {
                        s2 = "0";
                    }
                    Double d1 = new DecimalFormat("#.###").parse(s1).doubleValue();
                    Double d2 = new DecimalFormat("#.###").parse(s2).doubleValue();
                    return d1.compareTo(d2);
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                    return 0;
                }
            }
        });
        eventTable.setRowSorter(sorter);
        List<SortKey> sortKeys = new ArrayList<SortKey>();
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();
        
        int maxWidth = 0;
        for (EventWrapper event : eventSet) {
            maxWidth = Math.max(maxWidth, getFontMetrics(getFont()).stringWidth(event.eventLabel.getShortLabel()));
        }
        eventTable.getColumnModel().getColumn(0).setPreferredWidth(maxWidth + 15); // add icon size
        eventTable.getColumnModel().getColumn(1).setPreferredWidth(getFontMetrics(getFont()).stringWidth(" 9999 "));
        eventTable.getColumnModel().getColumn(2).setPreferredWidth(getFontMetrics(getFont()).stringWidth(" 99.9 "));

        eventTable.setShowHorizontalLines(false);
        eventTable.setShowVerticalLines(false);

        eventTable.getColumnModel().getColumn(0).setCellRenderer(new IconTextCellRenderer());

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        eventTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        eventTable.getColumnModel().getColumn(2).setCellRenderer(new DoubleCellRenderer());

        DefaultTableCellRenderer centerHRenderer = new DefaultTableCellRenderer();
        centerHRenderer.setToolTipText(NbBundle.getMessage(EventTableModel.class, "TIP_Sort"));
        centerHRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        eventTable.getColumnModel().getColumn(0).setHeaderRenderer(centerHRenderer);
        eventTable.getColumnModel().getColumn(1).setHeaderRenderer(centerHRenderer);
        eventTable.getColumnModel().getColumn(2).setHeaderRenderer(centerHRenderer);
        
        eventTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        eventTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!isBusyEvent && eventTable.getSelectedRow() != -1) {
                    eventIndex = eventTable.convertRowIndexToModel(eventTable.getSelectedRow());
                    displayEvent();
                    selectAssociation();
                }
            }
        });
        if (eventTable.getRowCount() > 0) {
            eventTable.setRowSelectionInterval(0, 0);
        }
    }

    private EventWrapper getCurrentEvent() {
        EventWrapper event = null;
        if (eventSet != null && !eventSet.isEmpty() && (eventIndex >= 0) && (eventIndex < eventSet.size())) {
            event = eventSet.get(eventIndex);
        }
        return event;
    }
    
    private void displayEvent() {
        isBusyEvent = true;
        eventDate.removeChangeListener(changes);

        EventWrapper event = getCurrentEvent();
        if (event != null) {        
            
            // Title
            eventTitle.setText(event.title);
            eventDescription.setText(event.description);
            eventDescription.setCaretPosition(0);
            
            // Date
            if (event.date != null) {
                eventDate.setPropertyImpl(event.date);
            } else {
                eventDate.setPropertyImpl(null);
            }
            ageAtEvent.setText(event.age);
            dayOfWeek.setText(event.dayOfWeek);
            
            // Place
            if (event.place != null) {
                eventPlace.setText(event.place.getDisplayValue());    
            } else {
                eventPlace.setText("");
            }
            eventPlace.setCaretPosition(0);

            // Notes
            scrollNotesEvent.setMinimum(0);
            scrollNotesEvent.setBlockIncrement(1);
            scrollNotesEvent.setUnitIncrement(1);
            displayEventNote(event);
        
            
            // Sources - Media & Text & Repo
            scrollSourcesEvent.setMinimum(0);
            scrollSourcesEvent.setBlockIncrement(1);
            scrollSourcesEvent.setUnitIncrement(1);
            displayEventSource(event);
            
        }
        isBusyEvent = false;
        eventDate.addChangeListener(changes);
    }

    
    

    
    private void displayEventNote(EventWrapper event) {
        isBusyEventNote = true;
        if (event.eventNoteSet != null && !event.eventNoteSet.isEmpty() && (event.eventNoteIndex >= 0) && (event.eventNoteIndex < event.eventNoteSet.size())) {        
            setEventNote(event, event.eventNoteSet.get(event.eventNoteIndex));
        } else {
            setEventNote(event, null);
        }
        isBusyEventNote = false;
    }
    
    private void setEventNote(EventWrapper event, NoteWrapper note) {
        
        String localText = "";
        
        if (note != null) {
            localText = note.getText();
        }
        
        // Text
        eventNote.setText(localText);
        eventNote.setCaretPosition(0);
        
        // Update scroll
        scrollNotesEvent.setValues(event.eventNoteIndex, 1, 0, event.eventNoteSet.size());
        scrollNotesEvent.setToolTipText(getScrollEventNotesLabel(event));
    }


    private String getScrollEventNotesLabel(EventWrapper event) {
        return String.valueOf(event.eventNoteSet.size() > 0 ? event.eventNoteIndex + 1 : event.eventNoteIndex) + "/" + String.valueOf(event.eventNoteSet.size());
    }

    
    
    private boolean chooseEventNote(EventWrapper event, int index) {
        boolean b = false;
        boolean exists = (event.eventNoteSet != null) && (!event.eventNoteSet.isEmpty()) && (index >= 0) && (index < event.eventNoteSet.size());
        
        JButton noteButton = new JButton(NbBundle.getMessage(getClass(), "Button_ChooseNote"));
        JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
        Object[] options = new Object[] { noteButton, cancelButton };
        NoteChooser noteChooser = new NoteChooser(gedcom, exists ? event.eventNoteSet.get(index) : null, noteButton, cancelButton);
        int size = noteChooser.getNbNotes();
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_ChooseNoteTitle", size), noteChooser).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
        if (o == noteButton) {
            String noteText = noteChooser.getSelectedText();
            if (noteChooser.isSelectedEntityNote()) {
                Note entity = (Note) noteChooser.getSelectedEntity();
                if (exists) {
                    event.eventNoteSet.get(index).setTargetEntity(entity);
                    event.eventNoteSet.get(index).setText(noteText);
                    event.eventNoteIndex = index;
                } else {
                    NoteWrapper note = new NoteWrapper(entity);
                    note.setText(noteText);
                    event.eventNoteSet.add(note);
                    event.eventNoteIndex = event.eventNoteSet.size() - 1;
                }
                changes.setChanged(true);
                b = true;
            } else {
                if (exists) {
                    event.eventNoteSet.get(index).setText(noteText);
                    event.eventNoteIndex = index;
                } else {
                    NoteWrapper note = new NoteWrapper(noteText);
                    event.eventNoteSet.add(note);
                    event.eventNoteIndex = event.eventNoteSet.size() - 1;
                }
                changes.setChanged(true);
                b = true;
            }
        }
        
        return b;
    }

    
    
    
    
    
    
    private void displayEventSource(EventWrapper event) {
        isBusyEventSource = true;
        if (event.eventSourceSet != null && !event.eventSourceSet.isEmpty() && (event.eventSourceIndex >= 0) && (event.eventSourceIndex < event.eventSourceSet.size())) {        
            setEventSource(event, event.eventSourceSet.get(event.eventSourceIndex));
        } else {
            setEventSource(event, null);
        }
        isBusyEventSource = false;
    }

    private void setEventSource(EventWrapper event, SourceWrapper source) {
        
        // Title
        eventSourceTitle.setText(source == null ? "" : source.getTitle());
        eventSourceTitle.setCaretPosition(0);
        
        // Text
        eventSourceText.setText(source == null ? "" : source.getText());
        eventSourceText.setCaretPosition(0);
        
        // Media
        imagePanel.setMedia(source != null ? source.getFile() : null);
        
        // Repositoryname
        repoText.setText(source != null ? source.getRepoName() : "");
        repoText.setCaretPosition(0);
        
        // Update scroll
        scrollSourcesEvent.setValues(event.eventSourceIndex, 1, 0, event.eventSourceSet.size());
        scrollSourcesEvent.setToolTipText(getScrollEventSourcesLabel(event));
    }

    private String getScrollEventSourcesLabel(EventWrapper event) {
        return String.valueOf(event.eventSourceSet.size() > 0 ? event.eventSourceIndex + 1 : event.eventSourceIndex) + "/" + String.valueOf(event.eventSourceSet.size());
    }

    public void chooseSource() {
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (chooseSource(event, event.eventSourceIndex)) {
            displayEventSource(event);
            eventSourceTitle.requestFocus();
        }
    }
    
    public boolean chooseSource(EventWrapper event, int index) {
        boolean b = false;
        boolean exists = (event.eventSourceSet != null) && (!event.eventSourceSet.isEmpty()) && (index >= 0) && (index < event.eventSourceSet.size());
        
        JButton sourceButton = new JButton(NbBundle.getMessage(getClass(), "Button_ChooseSource"));
        JButton fileButton = new JButton(NbBundle.getMessage(getClass(), "Button_LookForFile"));
        JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
        Object[] options = new Object[] { sourceButton, fileButton, cancelButton };
        SourceChooser sourceChooser = new SourceChooser(gedcom, imagePanel.getFile(), imagePanel.getImage(), eventSourceTitle.getText(), event.getEventSource(),sourceButton, cancelButton);
        int size = sourceChooser.getNbSource();
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_ChooseSourceTitle", size), sourceChooser).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
        if (o == sourceButton) {
            if (sourceChooser.isSelectedEntitySource()) {
                Source entity = (Source) sourceChooser.getSelectedEntity();
                if (exists) {
                    event.eventSourceSet.get(index).setTargetEntity(entity);
                    event.eventSourceIndex = index;
                } else {
                    SourceWrapper source = new SourceWrapper(entity);
                    event.eventSourceSet.add(source);
                    event.eventSourceIndex = event.eventSourceSet.size() - 1;
                }   
                changes.setChanged(true);
                b = true;
            }
        } else if (o == fileButton) {
            return chooseSourceFile(event, index);
        }
        
        return b;
    }
    

    
    private boolean chooseSourceFile(EventWrapper event, int index) {
        boolean b = false;
        boolean exists = (event.eventSourceSet != null) && (!event.eventSourceSet.isEmpty()) && (index >= 0) && (index < event.eventSourceSet.size());

        File file = new FileChooserBuilder(IndiPanel.class.getCanonicalName()+"Sources")
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(getClass(), "FileChooserTitle"))
                .setApproveText(NbBundle.getMessage(getClass(), "FileChooserOKButton"))
                .setDefaultExtension(FileChooserBuilder.getImageFilter().getExtensions()[0])
                .addFileFilter(FileChooserBuilder.getImageFilter())
                .addFileFilter(FileChooserBuilder.getSoundFilter())
                .addFileFilter(FileChooserBuilder.getVideoFilter())
                .setAcceptAllFileFilterUsed(false)
                .setFileHiding(true)
                .setDefaultPreviewer()
                .showOpenDialog();
        if (file != null) {
            if (exists) {
                event.eventSourceSet.get(index).setFile(file);
                event.eventSourceIndex = index;
            } else {
                SourceWrapper source = new SourceWrapper(file);
                event.eventSourceSet.add(source);
                event.eventSourceIndex = event.eventSourceSet.size() - 1;
            }
            changes.setChanged(true);
            b = true;
        }
        return b;
    }

    
    public boolean chooseRepository(EventWrapper event) {
        boolean b = false;
        JButton repoButton = new JButton(NbBundle.getMessage(getClass(), "Button_ChooseRepo"));
        JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
        Object[] options = new Object[] { repoButton, cancelButton };
        SourceWrapper source = event.eventSourceSet.isEmpty() ?  null : event.eventSourceSet.get(event.eventSourceIndex);
        RepoChooser repoChooser = new RepoChooser(gedcom, source, repoButton, cancelButton);
        int size = repoChooser.getNbRepos();
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_ChooseRepoTitle", size), repoChooser).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
        if (o == repoButton) {
            if (repoChooser.isSelectedEntityRepo()) {
                boolean exists = (event.eventSourceSet != null) && (!event.eventSourceSet.isEmpty()) && (event.eventSourceIndex >= 0) && (event.eventSourceIndex < event.eventSourceSet.size());
                if (exists) {
                    Repository entity = (Repository) repoChooser.getSelectedEntity();
                    event.eventSourceSet.get(event.eventSourceIndex).setRepo(entity);
                } else {
                    Repository entity = (Repository) repoChooser.getSelectedEntity();
                    source = new SourceWrapper(entity);  
                    event.eventSourceSet.add(source);
                    event.eventSourceIndex = event.eventSourceSet.size()-1;
                }
                changes.setChanged(true);
                b = true;
            }
        }
        return b;
    }

    
    
    private List<AssoWrapper> getAssociations(Indi indi) {
        List<AssoWrapper> ret = new ArrayList<AssoWrapper>();
        
        // Get ASSO tags from entities where Indi is referenced
        List<PropertyForeignXRef> assoList = indi.getProperties(PropertyForeignXRef.class);
        for (PropertyForeignXRef assoProp : assoList) {
            Property eventProp = assoProp.getParent();
            EventWrapper event = getEventWherePropIs(eventProp);
            if (event != null) {
                AssoWrapper asso = new AssoWrapper(assoProp, event);
                ret.add(asso);
            }
        }
        
        // Get ASSO tags from entities where Fam is referenced (although is not Gedcom compliant)
        Fam[] fams = indi.getFamiliesWhereSpouse();
        for (Fam fam : fams) {
            assoList = fam.getProperties(PropertyForeignXRef.class);
            for (PropertyForeignXRef assoProp : assoList) {
                Property eventProp = assoProp.getParent();
                EventWrapper event = getEventWherePropIs(eventProp);
                if (event != null) {
                    AssoWrapper asso = new AssoWrapper(assoProp, event);
                    ret.add(asso);
                }
            }
        }
        return ret;
    }
    
    private EventWrapper getEventWherePropIs(Property eventProp) {
        for (EventWrapper event : eventSet) {
            if (event.eventProperty == eventProp) {
                return event;
            }
        }
        if (eventProp instanceof Entity) {
            return new EventWrapper((Entity) eventProp);
        }
        return null;
    }
    
    
    private void displayAssociationsComboBox() {
        cbModel.removeAllElements();
        for (AssoWrapper asso : assoSet) {
            cbModel.addElement(asso);
        }
        if (cbModel.getSize() == 0) {
            cbModel.addElement(new AssoWrapper(NbBundle.getMessage(getClass(), "No_Association_Text")));
        }
        assoComboBox.setModel(cbModel);
        assoEditButton.setEnabled(!(eventSet.isEmpty() && assoSet.isEmpty()));
    }

    
    private void selectAssociation() {
        if (eventSet == null || eventSet.isEmpty() || assoSet == null) {
            return;
        }
        EventWrapper event = eventSet.get(eventIndex);
        if (assoComboBox.getModel().getSize() > 0) {
            for (AssoWrapper asso : assoSet) {
                if (asso.targetEvent.eventProperty == event.eventProperty) {
                    assoComboBox.setSelectedItem(asso);
                }
            }
        }
    }

    
    private boolean manageAssociations() {
        boolean b = false;
        JButton okButton = new JButton(NbBundle.getMessage(getClass(), "Button_Ok"));
        JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
        Object[] options = new Object[] { okButton, cancelButton };
        AssoManager assoManager = new AssoManager(indi, eventSet, assoSet, (AssoWrapper) assoComboBox.getSelectedItem(), okButton, cancelButton);
        String indiStr = assoManager.getIndi();
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_AssoManagerTitle", indiStr), assoManager).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
        if (o == okButton && assoManager.hasChanged()) {
            assoSet = assoManager.clone(assoManager.getSet());
            changes.setChanged(true);
            b = true;
        }
        return b;
    }


    private void saveAssociations() {
        //assoSet
        for (AssoWrapper asso : assoSet) {
            asso.update(indi);
        }
        for (AssoWrapper asso : assoRemovedSet) {
            asso.remove(indi);
        }
    }


    
    
    

    /**
     * Updaters (user has made a change to in a field or control, data is stored in data structure)
     */
    private void updatePhotoTitle(int index) {
        if (isBusyMedia) {
            return;
        }
        String photoTitle = textAreaPhotos.getText();
        if ((mediaSet != null) && (!mediaSet.isEmpty()) && (index >= 0) && (index < mediaSet.size())) {
            mediaSet.get(index).setTitle(photoTitle);
            mediaIndex = index;
        } else {
            MediaWrapper media = new MediaWrapper(photoTitle);
            mediaSet.add(media);
            mediaIndex = mediaSet.size()-1;
        }
        changes.setChanged(true);
    }

    private void updateNoteText(int index) {
        if (isBusyNote) {
            return;
        }
        String noteText = textAreaNotes.getText();
        if ((noteSet != null) && (!noteSet.isEmpty()) && (index >= 0) && (index < noteSet.size())) {
            noteSet.get(index).setText(noteText);
            noteIndex = index;
        } else {
            NoteWrapper note = new NoteWrapper(noteText);
            noteSet.add(note);
            noteIndex = noteSet.size()-1;
        }
        changes.setChanged(true);
    }

    private void updateEventDescription(DocumentEvent e) {
        if (isBusyEvent) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event != null) {
            event.setDescription(eventDescription.getText());
            event.setPlace(eventPlace.getText());
            changes.setChanged(true);
        }
    }

    private void updateEventDate(ChangeEvent e) {
        if (isBusyEvent) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event != null) {
            event.setDate(eventDate);
            //changes.setChanged(true); // already done in the bean
        }
    }

    private void updateEventNoteText() {
        if (isBusyEvent || isBusyEventNote) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        String noteText = eventNote.getText();
        if ((event.eventNoteSet != null) && (!event.eventNoteSet.isEmpty()) && (event.eventNoteIndex >= 0) && (event.eventNoteIndex < event.eventNoteSet.size())) {
            event.eventNoteSet.get(event.eventNoteIndex).setText(noteText);
        } else {
            NoteWrapper note = new NoteWrapper(noteText);
            event.eventNoteSet.add(note);
            event.eventNoteIndex = event.eventNoteSet.size()-1;
        }
        changes.setChanged(true);
    }

    private void updateEventSourceText() {
        if (isBusyEvent || isBusyEventSource) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        String sourceTitle = eventSourceTitle.getText();
        String sourceText = eventSourceText.getText();
        if ((event.eventSourceSet != null) && (!event.eventSourceSet.isEmpty()) && (event.eventSourceIndex >= 0) && (event.eventSourceIndex < event.eventSourceSet.size())) {
            event.eventSourceSet.get(event.eventSourceIndex).setTitle(sourceTitle);
            event.eventSourceSet.get(event.eventSourceIndex).setText(sourceText);
        } else {
            SourceWrapper source = new SourceWrapper(sourceTitle);
            source.setText(sourceText);
            event.eventSourceSet.add(source);
            event.eventSourceIndex = event.eventSourceSet.size()-1;
        }
        changes.setChanged(true);
    }

    
    
    
    
    
    
    
    
    
    
    /**
     * Document listener methods for name, etc information
     */
    public void insertUpdate(DocumentEvent e) {
        if (!isBusyEvent) {
            changes.setChanged(true);
        }
    }

    public void removeUpdate(DocumentEvent e) {
        if (!isBusyEvent) {
            changes.setChanged(true);
        }
    }

    public void changedUpdate(DocumentEvent e) {
        if (!isBusyEvent) {
            changes.setChanged(true);
        }
    }



    
    
    
    
    
    /**
     * ******************  CLASSES  *********************
     */
    
    
    
    /**
     * Document listener methods for Main pictures
     */
    private class PhotoTitleListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            updatePhotoTitle(mediaIndex);
        }

        public void removeUpdate(DocumentEvent e) {
            updatePhotoTitle(mediaIndex);
        }

        public void changedUpdate(DocumentEvent e) {
            updatePhotoTitle(mediaIndex);
        }
    }

    
    /**
     * Document listener methods for Main notes
     */
    private class NoteTextListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            updateNoteText(noteIndex);
        }

        public void removeUpdate(DocumentEvent e) {
            updateNoteText(noteIndex);
        }

        public void changedUpdate(DocumentEvent e) {
            updateNoteText(noteIndex);
        }
    }

    /**
     * Document listener methods for Event description, place
     */
    private class EventDescriptionListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            updateEventDescription(e);
        }

        public void removeUpdate(DocumentEvent e) {
            updateEventDescription(e);
        }

        public void changedUpdate(DocumentEvent e) {
            updateEventDescription(e);
        }
    }

    /**
     * Document listener methods for Event date
     */
    private class EventDateListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            updateEventDate(e);
        }

    }

    /**
     * Document listener methods for Event notes
     */
    private class EventNoteTextListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            updateEventNoteText();
        }

        public void removeUpdate(DocumentEvent e) {
            updateEventNoteText();
        }

        public void changedUpdate(DocumentEvent e) {
            updateEventNoteText();
        }
    }

    /**
     * Document listener methods for Event source
     */
    private class EventSourceTextListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            updateEventSourceText();
        }

        public void removeUpdate(DocumentEvent e) {
            updateEventSourceText();
        }

        public void changedUpdate(DocumentEvent e) {
            updateEventSourceText();
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    private class FamilyTreeMouseListener implements MouseListener {

        public void mousePressed(MouseEvent e) {
            int selRow = familyTree.getRowForLocation(e.getX(), e.getY());
            if (selRow != -1 && e.getClickCount() == 2) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) familyTree.getLastSelectedPathComponent();
                NodeWrapper node = (NodeWrapper) treeNode.getUserObject();
                SelectionDispatcher.fireSelection(new Context(node.getEntity()));
            }
        }

        public void mouseClicked(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
        
    }

    private class IconTextCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                setIcon(((EventLabel) value).getIcon());
                setText(((EventLabel) value).getShortLabel());
            }
        return this;
        }
    }
    
    
    private class DoubleCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                String text = (String) value;
                try {
                    Double d = new DecimalFormat("#").parse(text).doubleValue();
                    DecimalFormat df = new DecimalFormat("#.#");
                    setText(df.format(d));
                } catch (ParseException ex) {
                    //Exceptions.printStackTrace(ex);
                    setText(text);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
            }
            return this;
        }
    }
    
    
}
