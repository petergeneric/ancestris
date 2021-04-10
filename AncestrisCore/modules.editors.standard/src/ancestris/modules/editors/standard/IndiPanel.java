/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2016 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.standard;

import ancestris.api.editor.Editor;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.privacy.standard.Options;
import ancestris.modules.editors.geoplace.PlaceEditorPanel;
import ancestris.modules.editors.standard.actions.ActionCreation;
import ancestris.modules.editors.standard.tools.AssoManager;
import ancestris.modules.editors.standard.tools.AssoWrapper;
import ancestris.modules.editors.standard.tools.AutoCompletion;
import ancestris.modules.editors.standard.tools.ErrorPanel;
import ancestris.modules.editors.standard.tools.EventLabel;
import ancestris.modules.editors.standard.tools.EventTableModel;
import ancestris.modules.editors.standard.tools.EventWrapper;
import ancestris.modules.editors.standard.tools.FamilyTreeRenderer;
import ancestris.modules.editors.standard.tools.ImagePanel;
import ancestris.modules.editors.standard.tools.IndiChooser;
import ancestris.modules.editors.standard.tools.IndiCreator;
import ancestris.modules.editors.standard.tools.MediaChooser;
import ancestris.modules.editors.standard.tools.MediaWrapper;
import ancestris.modules.editors.standard.tools.NameDetailsPanel;
import ancestris.modules.editors.standard.tools.NodeWrapper;
import ancestris.modules.editors.standard.tools.NoteChooser;
import ancestris.modules.editors.standard.tools.NoteDetailsPanel;
import ancestris.modules.editors.standard.tools.NoteWrapper;
import ancestris.modules.editors.standard.tools.RepoChooser;
import ancestris.modules.editors.standard.tools.SourceChooser;
import ancestris.modules.editors.standard.tools.SourceWrapper;
import ancestris.modules.editors.standard.tools.Utils;
import static ancestris.modules.editors.standard.tools.Utils.getImageFromFile;
import ancestris.modules.gedcom.searchduplicates.IndiDuplicatesFinder;
import ancestris.util.EventUsage;
import ancestris.util.TimingUtility;
import ancestris.util.Utilities;
import ancestris.util.swing.DialogManager;
import static ancestris.util.swing.DialogManager.OK_ONLY_OPTION;
import ancestris.util.swing.FileChooserBuilder;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomOptions;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyForeignXRef;
import genj.gedcom.PropertyLatitude;
import genj.gedcom.PropertyLongitude;
import genj.gedcom.PropertyName;
import genj.gedcom.PropertySex;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import genj.gedcom.TagPath;
import genj.io.InputSource;
import genj.util.ChangeSupport;
import genj.util.Registry;
import genj.util.Validator;
import genj.view.ViewContext;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
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
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

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

    private BufferedImage PHOTO_MALE = null;
    private BufferedImage PHOTO_FEMALE = null;
    private BufferedImage PHOTO_UNKNOWN = null;
    private BufferedImage SOURCE_UNKNOWN = null;

    private Context context;
    private Gedcom gedcom;
    private Indi indi;
    private boolean reloadData = true;
    private boolean listernersOn = false;

    private DefaultMutableTreeNode familyTop = null;
    private Registry registry = null;

    private final static String NO_SOSA = NbBundle.getMessage(IndiPanel.class, "noSosa");

    private static Map<String, EventUsage> eventUsages = null;

    private ImagePanel photoPanel = null;
    private ImagePanel imagePanel = null;
    private NameDetailsPanel nameDetails = null;
    private NoteDetailsPanel textDetails = null;

    // Events
    private EventTableModel eventTableModel = null;
    private ListSelectionListener eventTableListener = null;
    private List<EventWrapper> eventSet = null;
    private List<EventWrapper> eventRemovedSet = null;
    private boolean isBusyEvent = false;
    private PlaceEditorPanel placeEditor = null;
    private boolean isBusyEventMedia = false;
    private boolean isBusyEventNote = false;
    private boolean isBusyEventSource = false;

    // Memorise all posiitons to return on same selected bits
    private String savedEventTagDateDesc = "-1";
    private Component savedFocusedControl = null;
    private int eventIndex = 0,
            savedEventMediaIndex = -1,
            savedEventNoteIndex = -1,
            savedEventSourceIndex = -1,
            savedEventSourceMediaIndex = -1;

    // Associations
    private DefaultComboBoxModel cbModel = new DefaultComboBoxModel();
    private List<AssoWrapper> assoSet = null;
    private List<AssoWrapper> assoRemovedSet = null;

    // Warnings
    private List<ViewContext> errorSet = null;

    // Filters
    private List<String> allFirstNames = null;
    private JTextField firstnamesText = null;
    private List<String> allLastNames = null;
    private JTextField lastnameText = null;
    private List<String> allDescriptions = null;
    private JTextField eventDescriptionText = null;
    private EventDescriptionListener edl = null;
    private List<String> allPlaces = null;
    private JTextField eventPlaceText = null;

    //LAst url used
    private String theUrl = "";

    /**
     * Creates new form IndiPanel
     */
    public IndiPanel() {

        // Fixed variables
        try {
            this.PHOTO_MALE = ImageIO.read(getClass().getResourceAsStream("/ancestris/modules/editors/standard/images/profile_male.png"));
            this.PHOTO_FEMALE = ImageIO.read(getClass().getResourceAsStream("/ancestris/modules/editors/standard/images/profile_female.png"));
            this.PHOTO_UNKNOWN = ImageIO.read(getClass().getResourceAsStream("/ancestris/modules/editors/standard/images/profile_unknown.png"));
            this.SOURCE_UNKNOWN = ImageIO.read(getClass().getResourceAsStream("/ancestris/modules/editors/standard/images/source_dummy.png"));
        } catch (IOException ex) {
            LOG.log(Level.INFO, "Unable to load default images.", ex);
        }

        // Data
        eventUsages = new HashMap<>();
        EventUsage.init(eventUsages);

        familyTop = new DefaultMutableTreeNode(new NodeWrapper(NodeWrapper.PARENTS, null));
        placeEditor = null;

        reloadData = true; // force data load at initialisation

        // Components
        initComponents();
        nameDetails = new NameDetailsPanel();
        textDetails = new NoteDetailsPanel();

        familyTree.setCellRenderer(new FamilyTreeRenderer());
        familyTree.addMouseListener(new FamilyTreeMouseListener());

        firstnamesCombo.setPrototypeDisplayValue("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
        lastnameCombo.setPrototypeDisplayValue("MMMMMMMMMMMMMMMMMMMMMMMMM");

        firstnamesText = (JTextField) firstnamesCombo.getEditor().getEditorComponent();
        lastnameText = (JTextField) lastnameCombo.getEditor().getEditorComponent();
        eventDescriptionText = (JTextField) eventDescriptionCombo.getEditor().getEditorComponent();
        edl = new EventDescriptionListener();
        eventPlaceText = (JTextField) eventPlaceCombo.getEditor().getEditorComponent();

        registry = Registry.get(getClass());
        splitPane.setDividerLocation(registry.get("cygnustopSplitDividerLocation", splitPane.getDividerLocation()));
        eventSplitPane.setDividerLocation(registry.get("cygnuseventSplitDividerLocation", eventSplitPane.getDividerLocation()));
        focusButton.setSelected(registry.get("focus", false));

        // Remove Enter key stroke bindings
        removeEnterKeyBindingsFromButtons();

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
        stickyButton = new javax.swing.JButton();
        focusButton = new javax.swing.JToggleButton();
        warningButton = new javax.swing.JButton();
        indiAddButton = new javax.swing.JButton();
        title = new javax.swing.JLabel();
        indiDelButton = new javax.swing.JButton();
        splitPane = new javax.swing.JSplitPane();
        topPanel = new javax.swing.JPanel();
        mediaPanel = new javax.swing.JPanel();
        photoPanel = new ImagePanel(this);
        mediaImagePanel = photoPanel;
        scrollPanePhotos = new javax.swing.JScrollPane();
        textAreaPhotos = new ancestris.swing.UndoTextArea();
        scrollMediaEvent = new javax.swing.JScrollBar();
        prefMediaEventButton = new javax.swing.JButton();
        addMediaEventButton = new javax.swing.JButton();
        delMediaEventButton = new javax.swing.JButton();
        namePanel = new javax.swing.JPanel();
        TopButtonsdPanel = new javax.swing.JPanel();
        fatherButton = new javax.swing.JButton();
        idLabel = new javax.swing.JLabel();
        sosaLabel = new javax.swing.JLabel();
        motherButton = new javax.swing.JButton();
        moreNamesButton = new javax.swing.JButton();
        firstnamesLabel = new javax.swing.JLabel();
        lastnameLabel = new javax.swing.JLabel();
        firstnamesCombo = new javax.swing.JComboBox();
        lastnameCombo = new javax.swing.JComboBox();
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
        eventSplitPane = new javax.swing.JSplitPane();
        eventLeft = new javax.swing.JPanel();
        eventBirtButton = new javax.swing.JButton();
        eventBaptButton = new javax.swing.JButton();
        eventMarrButton = new javax.swing.JButton();
        eventDeatButton = new javax.swing.JButton();
        eventBuriButton = new javax.swing.JButton();
        eventOccuButton = new javax.swing.JButton();
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
        mediaSourceText = new javax.swing.JTextField();
        scrollMediaSource = new javax.swing.JScrollBar();
        addMediaSourceButton = new javax.swing.JButton();
        delMediaSourceButton = new javax.swing.JButton();
        eventRight = new javax.swing.JPanel();
        eventTitle = new javax.swing.JLabel();
        eventDescriptionCombo = new javax.swing.JComboBox();
        datelabel = new javax.swing.JLabel();
        dayOfWeek = new javax.swing.JLabel();
        ageAtEvent = new javax.swing.JLabel();
        timelabel = new javax.swing.JLabel();
        eventTime = new javax.swing.JTextField();
        placeLabel = new javax.swing.JLabel();
        eventPlaceCombo = new javax.swing.JComboBox();
        eventPlaceButton = new javax.swing.JButton();
        eventNotePanel = new javax.swing.JPanel();
        noteLabel = new javax.swing.JLabel();
        addNoteEventButton = new javax.swing.JButton();
        replaceNoteEventButton = new javax.swing.JButton();
        delNoteEventButton = new javax.swing.JButton();
        maxNoteEventButton = new javax.swing.JButton();
        eventNoteScrollPane = new javax.swing.JScrollPane();
        eventNote = new ancestris.swing.UndoTextArea();
        scrollNotesEvent = new javax.swing.JScrollBar();
        eventSourcePanel = new javax.swing.JPanel();
        sourceLabel = new javax.swing.JLabel();
        addSourceEventButton = new javax.swing.JButton();
        replaceSourceEventButton = new javax.swing.JButton();
        delSourceEventButton = new javax.swing.JButton();
        maxSourceEventButton = new javax.swing.JButton();
        eventSourceTitle = new javax.swing.JTextField();
        eventSourceScrollPane = new javax.swing.JScrollPane();
        eventSourceText = new ancestris.swing.UndoTextArea();
        repoPanel = new javax.swing.JPanel();
        repoText = new javax.swing.JTextField();
        repoEditButton = new javax.swing.JButton();
        scrollSourcesEvent = new javax.swing.JScrollBar();
        assoPanel = new javax.swing.JPanel();
        assoComboBox = new javax.swing.JComboBox();
        assoEditButton = new javax.swing.JButton();
        assoEditIndi = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(32767, 500));
        setPreferredSize(new java.awt.Dimension(531, 550));
        setRequestFocusEnabled(false);

        stickyButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/StickOff.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(stickyButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.stickyButton.text")); // NOI18N
        stickyButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.stickyButton.toolTipText")); // NOI18N
        stickyButton.setPreferredSize(new java.awt.Dimension(30, 26));
        stickyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stickyButtonActionPerformed(evt);
            }
        });

        focusButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/Focus.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(focusButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.focusButton.text")); // NOI18N
        focusButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.focusButton.toolTipText")); // NOI18N
        focusButton.setPreferredSize(new java.awt.Dimension(30, 26));
        focusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                focusButtonActionPerformed(evt);
            }
        });

        warningButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/warning.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(warningButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.warningButton.text")); // NOI18N
        warningButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.warningButton.toolTipText")); // NOI18N
        warningButton.setPreferredSize(new java.awt.Dimension(30, 26));
        warningButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                warningButtonActionPerformed(evt);
            }
        });

        indiAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/indi-add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(indiAddButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.indiAddButton.text")); // NOI18N
        indiAddButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.indiAddButton.toolTipText")); // NOI18N
        indiAddButton.setPreferredSize(new java.awt.Dimension(30, 26));
        indiAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indiAddButtonActionPerformed(evt);
            }
        });

        title.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(title, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.title.text")); // NOI18N

        indiDelButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/indi-delete.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(indiDelButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.indiDelButton.text")); // NOI18N
        indiDelButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.indiDelButton.toolTipText")); // NOI18N
        indiDelButton.setPreferredSize(new java.awt.Dimension(30, 26));
        indiDelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indiDelButtonActionPerformed(evt);
            }
        });

        splitPane.setDividerLocation(320);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                splitPanePropertyChange(evt);
            }
        });

        topPanel.setPreferredSize(new java.awt.Dimension(517, 50));

        mediaPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        mediaPanel.setMaximumSize(new java.awt.Dimension(200, 32767));

        mediaImagePanel.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.mediaImagePanel.toolTipText")); // NOI18N
        mediaImagePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                mediaImagePanelMouseClicked(evt);
            }
        });
        mediaImagePanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                mediaImagePanelComponentResized(evt);
            }
        });

        javax.swing.GroupLayout mediaImagePanelLayout = new javax.swing.GroupLayout(mediaImagePanel);
        mediaImagePanel.setLayout(mediaImagePanelLayout);
        mediaImagePanelLayout.setHorizontalGroup(
            mediaImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        mediaImagePanelLayout.setVerticalGroup(
            mediaImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        scrollPanePhotos.setPreferredSize(new java.awt.Dimension(238, 50));

        textAreaPhotos.setColumns(20);
        textAreaPhotos.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
        textAreaPhotos.setLineWrap(true);
        textAreaPhotos.setRows(4);
        textAreaPhotos.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.textAreaPhotos.text")); // NOI18N
        textAreaPhotos.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.textAreaPhotos.toolTipText")); // NOI18N
        textAreaPhotos.setWrapStyleWord(true);
        scrollPanePhotos.setViewportView(textAreaPhotos);

        scrollMediaEvent.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        scrollMediaEvent.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                scrollMediaEventMouseWheelMoved(evt);
            }
        });
        scrollMediaEvent.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollMediaEventAdjustmentValueChanged(evt);
            }
        });

        prefMediaEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/star.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(prefMediaEventButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.prefMediaEventButton.text")); // NOI18N
        prefMediaEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.prefMediaEventButton.toolTipText")); // NOI18N
        prefMediaEventButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        prefMediaEventButton.setIconTextGap(0);
        prefMediaEventButton.setPreferredSize(new java.awt.Dimension(16, 16));
        prefMediaEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prefMediaEventButtonActionPerformed(evt);
            }
        });

        addMediaEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addMediaEventButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addMediaEventButton.text")); // NOI18N
        addMediaEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addMediaEventButton.toolTipText")); // NOI18N
        addMediaEventButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addMediaEventButton.setIconTextGap(0);
        addMediaEventButton.setPreferredSize(new java.awt.Dimension(16, 16));
        addMediaEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMediaEventButtonActionPerformed(evt);
            }
        });

        delMediaEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(delMediaEventButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delMediaEventButton.text")); // NOI18N
        delMediaEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delMediaEventButton.toolTipText")); // NOI18N
        delMediaEventButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        delMediaEventButton.setIconTextGap(0);
        delMediaEventButton.setPreferredSize(new java.awt.Dimension(16, 16));
        delMediaEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delMediaEventButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout mediaPanelLayout = new javax.swing.GroupLayout(mediaPanel);
        mediaPanel.setLayout(mediaPanelLayout);
        mediaPanelLayout.setHorizontalGroup(
            mediaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mediaPanelLayout.createSequentialGroup()
                .addComponent(scrollMediaEvent, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                .addGap(2, 2, 2)
                .addComponent(prefMediaEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(addMediaEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(delMediaEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(scrollPanePhotos, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addComponent(mediaImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        mediaPanelLayout.setVerticalGroup(
            mediaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mediaPanelLayout.createSequentialGroup()
                .addComponent(mediaImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(2, 2, 2)
                .addComponent(scrollPanePhotos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(mediaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(mediaPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(delMediaEventButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(addMediaEventButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(scrollMediaEvent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(prefMediaEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        fatherButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/father.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(fatherButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.fatherButton.text")); // NOI18N
        fatherButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.fatherButton.toolTipText")); // NOI18N
        fatherButton.setPreferredSize(new java.awt.Dimension(60, 27));
        fatherButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                fatherButtonMouseClicked(evt);
            }
        });
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
        motherButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                motherButtonMouseClicked(evt);
            }
        });
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
        moreNamesButton.setIconTextGap(0);
        moreNamesButton.setPreferredSize(new java.awt.Dimension(30, 26));
        moreNamesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moreNamesButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(firstnamesLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.firstnamesLabel.text_1")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lastnameLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.lastnameLabel.text")); // NOI18N

        firstnamesCombo.setEditable(true);
        firstnamesCombo.setMaximumRowCount(19);

        lastnameCombo.setEditable(true);
        lastnameCombo.setMaximumRowCount(19);

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
        privateCheckBox.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
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
        brothersButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                brothersButtonMouseClicked(evt);
            }
        });
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
        sistersButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sistersButtonMouseClicked(evt);
            }
        });
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
        spousesButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                spousesButtonMouseClicked(evt);
            }
        });
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
        childrenButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                childrenButtonMouseClicked(evt);
            }
        });
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                        .addComponent(privateCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(namePanelLayout.createSequentialGroup()
                        .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(firstnamesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(firstnamesCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(namePanelLayout.createSequentialGroup()
                                .addComponent(lastnameLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(namePanelLayout.createSequentialGroup()
                                .addComponent(lastnameCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(moreNamesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2))))))
        );
        namePanelLayout.setVerticalGroup(
            namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(namePanelLayout.createSequentialGroup()
                .addComponent(TopButtonsdPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(firstnamesLabel)
                    .addComponent(lastnameLabel))
                .addGap(1, 1, 1)
                .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(moreNamesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(firstnamesCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lastnameCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(4, 4, 4)
                .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(privateCheckBox)
                    .addComponent(maleRadioButton)
                    .addComponent(femaleRadioButton)
                    .addComponent(unknownRadioButton))
                .addGap(5, 5, 5)
                .addComponent(BottomButtonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        scrollPaneFamily.setPreferredSize(new java.awt.Dimension(106, 113));
        scrollPaneFamily.setRequestFocusEnabled(false);
        scrollPaneFamily.setViewportView(familyTree);

        javax.swing.GroupLayout topPanelLayout = new javax.swing.GroupLayout(topPanel);
        topPanel.setLayout(topPanelLayout);
        topPanelLayout.setHorizontalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addComponent(mediaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(2, 2, 2)
                .addGroup(topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollPaneFamily, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(namePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        topPanelLayout.setVerticalGroup(
            topPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mediaPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(topPanelLayout.createSequentialGroup()
                .addComponent(namePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(scrollPaneFamily, javax.swing.GroupLayout.DEFAULT_SIZE, 189, Short.MAX_VALUE))
        );

        splitPane.setTopComponent(topPanel);

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
        eventBirtButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventBirtButtonActionPerformed(evt);
            }
        });

        eventBaptButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/baptism.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventBaptButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventBaptButton.text")); // NOI18N
        eventBaptButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventBaptButton.toolTipText")); // NOI18N
        eventBaptButton.setPreferredSize(new java.awt.Dimension(30, 26));
        eventBaptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventBaptButtonActionPerformed(evt);
            }
        });

        eventMarrButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/marr.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventMarrButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventMarrButton.text")); // NOI18N
        eventMarrButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventMarrButton.toolTipText")); // NOI18N
        eventMarrButton.setPreferredSize(new java.awt.Dimension(30, 26));
        eventMarrButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventMarrButtonActionPerformed(evt);
            }
        });

        eventDeatButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/death.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventDeatButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventDeatButton.text")); // NOI18N
        eventDeatButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventDeatButton.toolTipText")); // NOI18N
        eventDeatButton.setPreferredSize(new java.awt.Dimension(30, 26));
        eventDeatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventDeatButtonActionPerformed(evt);
            }
        });

        eventBuriButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/burial.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventBuriButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventBuriButton.text")); // NOI18N
        eventBuriButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventBuriButton.toolTipText")); // NOI18N
        eventBuriButton.setPreferredSize(new java.awt.Dimension(30, 26));
        eventBuriButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventBuriButtonActionPerformed(evt);
            }
        });

        eventOccuButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/occu.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventOccuButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventOccuButton.text")); // NOI18N
        eventOccuButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventOccuButton.toolTipText")); // NOI18N
        eventOccuButton.setActionCommand(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventOccuButton.actionCommand")); // NOI18N
        eventOccuButton.setPreferredSize(new java.awt.Dimension(30, 26));
        eventOccuButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventOccuButtonActionPerformed(evt);
            }
        });

        eventRetiButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/retirement.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventRetiButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventRetiButton.text")); // NOI18N
        eventRetiButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventRetiButton.toolTipText")); // NOI18N
        eventRetiButton.setActionCommand(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventRetiButton.actionCommand")); // NOI18N
        eventRetiButton.setPreferredSize(new java.awt.Dimension(30, 26));
        eventRetiButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventRetiButtonActionPerformed(evt);
            }
        });

        eventResiButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/residency.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventResiButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventResiButton.text")); // NOI18N
        eventResiButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventResiButton.toolTipText")); // NOI18N
        eventResiButton.setActionCommand(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventResiButton.actionCommand")); // NOI18N
        eventResiButton.setPreferredSize(new java.awt.Dimension(30, 26));
        eventResiButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventResiButtonActionPerformed(evt);
            }
        });

        eventOthersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/event.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventOthersButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventOthersButton.text")); // NOI18N
        eventOthersButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventOthersButton.toolTipText")); // NOI18N
        eventOthersButton.setActionCommand(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventOthersButton.actionCommand")); // NOI18N
        eventOthersButton.setPreferredSize(new java.awt.Dimension(30, 26));
        eventOthersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventOthersButtonActionPerformed(evt);
            }
        });

        eventRemoveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventRemoveButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventRemoveButton.text")); // NOI18N
        eventRemoveButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventRemoveButton.toolTipText")); // NOI18N
        eventRemoveButton.setActionCommand(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventRemoveButton.actionCommand")); // NOI18N
        eventRemoveButton.setPreferredSize(new java.awt.Dimension(30, 26));
        eventRemoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventRemoveButtonActionPerformed(evt);
            }
        });

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

        sourceImagePanel.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.sourceImagePanel.toolTipText")); // NOI18N
        sourceImagePanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                sourceImagePanelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout sourceImagePanelLayout = new javax.swing.GroupLayout(sourceImagePanel);
        sourceImagePanel.setLayout(sourceImagePanelLayout);
        sourceImagePanelLayout.setHorizontalGroup(
            sourceImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        sourceImagePanelLayout.setVerticalGroup(
            sourceImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        mediaSourceText.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
        mediaSourceText.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.mediaSourceText.text")); // NOI18N
        mediaSourceText.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.mediaSourceText.toolTipText")); // NOI18N

        scrollMediaSource.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        scrollMediaSource.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                scrollMediaSourceMouseWheelMoved(evt);
            }
        });
        scrollMediaSource.addAdjustmentListener(new java.awt.event.AdjustmentListener() {
            public void adjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {
                scrollMediaSourceAdjustmentValueChanged(evt);
            }
        });

        addMediaSourceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addMediaSourceButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addMediaSourceButton.text")); // NOI18N
        addMediaSourceButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addMediaSourceButton.toolTipText")); // NOI18N
        addMediaSourceButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addMediaSourceButton.setIconTextGap(0);
        addMediaSourceButton.setPreferredSize(new java.awt.Dimension(16, 16));
        addMediaSourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addMediaSourceButtonActionPerformed(evt);
            }
        });

        delMediaSourceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(delMediaSourceButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delMediaSourceButton.text")); // NOI18N
        delMediaSourceButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delMediaSourceButton.toolTipText")); // NOI18N
        delMediaSourceButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        delMediaSourceButton.setIconTextGap(0);
        delMediaSourceButton.setPreferredSize(new java.awt.Dimension(16, 16));
        delMediaSourceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delMediaSourceButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout sourcePanelLayout = new javax.swing.GroupLayout(sourcePanel);
        sourcePanel.setLayout(sourcePanelLayout);
        sourcePanelLayout.setHorizontalGroup(
            sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sourceImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(sourcePanelLayout.createSequentialGroup()
                .addComponent(scrollMediaSource, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addMediaSourceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(delMediaSourceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(mediaSourceText)
        );
        sourcePanelLayout.setVerticalGroup(
            sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourcePanelLayout.createSequentialGroup()
                .addComponent(sourceImagePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(mediaSourceText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addGroup(sourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(scrollMediaSource, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(delMediaSourceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addMediaSourceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                        .addComponent(eventMarrButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventDeatButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventBuriButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(eventLeftLayout.createSequentialGroup()
                        .addComponent(eventOccuButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventResiButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventRetiButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventOthersButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventRemoveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(eventLeftLayout.createSequentialGroup()
                .addGroup(eventLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(eventLeftLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(modificationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(eventScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(sourcePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE))
                .addGap(2, 2, 2))
        );
        eventLeftLayout.setVerticalGroup(
            eventLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventLeftLayout.createSequentialGroup()
                .addGroup(eventLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(eventBirtButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventBaptButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventMarrButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventDeatButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventBuriButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(eventLeftLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(eventOccuButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventRetiButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventResiButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventOthersButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventRemoveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addComponent(eventScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sourcePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modificationLabel))
        );

        eventSplitPane.setLeftComponent(eventLeft);

        eventRight.setPreferredSize(new java.awt.Dimension(300, 106));

        eventTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        eventTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(eventTitle, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventTitle.text")); // NOI18N

        eventDescriptionCombo.setEditable(true);
        eventDescriptionCombo.setMaximumRowCount(19);
        eventDescriptionCombo.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventDescriptionText.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(datelabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.datelabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(dayOfWeek, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.dayOfWeek.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ageAtEvent, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.ageAtEvent.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(timelabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.timelabel.text")); // NOI18N

        eventTime.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        eventTime.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventTime.text")); // NOI18N
        eventTime.setMinimumSize(new java.awt.Dimension(12, 15));
        eventTime.setPreferredSize(new java.awt.Dimension(78, 22));

        org.openide.awt.Mnemonics.setLocalizedText(placeLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.placeLabel.text")); // NOI18N

        eventPlaceCombo.setEditable(true);
        eventPlaceCombo.setMaximumRowCount(19);

        eventPlaceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/place.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventPlaceButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventPlaceButton.text")); // NOI18N
        eventPlaceButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventPlaceButton.toolTipText")); // NOI18N
        eventPlaceButton.setPreferredSize(new java.awt.Dimension(30, 26));
        eventPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eventPlaceButtonActionPerformed(evt);
            }
        });

        eventNotePanel.setPreferredSize(new java.awt.Dimension(256, 60));

        org.openide.awt.Mnemonics.setLocalizedText(noteLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.noteLabel.text")); // NOI18N

        addNoteEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addNoteEventButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addNoteEventButton.text")); // NOI18N
        addNoteEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addNoteEventButton.toolTipText")); // NOI18N
        addNoteEventButton.setBorderPainted(false);
        addNoteEventButton.setIconTextGap(0);
        addNoteEventButton.setPreferredSize(new java.awt.Dimension(22, 22));
        addNoteEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNoteEventButtonActionPerformed(evt);
            }
        });

        replaceNoteEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/replace.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(replaceNoteEventButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.replaceNoteEventButton.text")); // NOI18N
        replaceNoteEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.replaceNoteEventButton.toolTipText")); // NOI18N
        replaceNoteEventButton.setIconTextGap(0);
        replaceNoteEventButton.setPreferredSize(new java.awt.Dimension(22, 22));
        replaceNoteEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceNoteEventButtonActionPerformed(evt);
            }
        });

        delNoteEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(delNoteEventButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delNoteEventButton.text")); // NOI18N
        delNoteEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delNoteEventButton.toolTipText")); // NOI18N
        delNoteEventButton.setIconTextGap(0);
        delNoteEventButton.setPreferredSize(new java.awt.Dimension(22, 22));
        delNoteEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delNoteEventButtonActionPerformed(evt);
            }
        });

        maxNoteEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/maximize.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(maxNoteEventButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.maxNoteEventButton.text")); // NOI18N
        maxNoteEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.maxNoteEventButton.toolTipText")); // NOI18N
        maxNoteEventButton.setPreferredSize(new java.awt.Dimension(22, 22));
        maxNoteEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxNoteEventButtonActionPerformed(evt);
            }
        });

        eventNote.setColumns(20);
        eventNote.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
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
                .addComponent(noteLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(replaceNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(delNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(maxNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(eventNotePanelLayout.createSequentialGroup()
                .addComponent(eventNoteScrollPane)
                .addGap(2, 2, 2)
                .addComponent(scrollNotesEvent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        eventNotePanelLayout.setVerticalGroup(
            eventNotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventNotePanelLayout.createSequentialGroup()
                .addGroup(eventNotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(noteLabel)
                    .addComponent(addNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(replaceNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(delNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(eventNotePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(eventNoteScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 25, Short.MAX_VALUE)
                    .addGroup(eventNotePanelLayout.createSequentialGroup()
                        .addComponent(scrollNotesEvent, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                        .addGap(2, 2, 2))))
        );

        org.openide.awt.Mnemonics.setLocalizedText(sourceLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.sourceLabel.text")); // NOI18N

        addSourceEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addSourceEventButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addSourceEventButton.text")); // NOI18N
        addSourceEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addSourceEventButton.toolTipText")); // NOI18N
        addSourceEventButton.setIconTextGap(0);
        addSourceEventButton.setPreferredSize(new java.awt.Dimension(22, 22));
        addSourceEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSourceEventButtonActionPerformed(evt);
            }
        });

        replaceSourceEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/replace.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(replaceSourceEventButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.replaceSourceEventButton.text")); // NOI18N
        replaceSourceEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.replaceSourceEventButton.toolTipText")); // NOI18N
        replaceSourceEventButton.setIconTextGap(0);
        replaceSourceEventButton.setPreferredSize(new java.awt.Dimension(22, 22));
        replaceSourceEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceSourceEventButtonActionPerformed(evt);
            }
        });

        delSourceEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(delSourceEventButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delSourceEventButton.text")); // NOI18N
        delSourceEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delSourceEventButton.toolTipText")); // NOI18N
        delSourceEventButton.setIconTextGap(0);
        delSourceEventButton.setPreferredSize(new java.awt.Dimension(22, 22));
        delSourceEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delSourceEventButtonActionPerformed(evt);
            }
        });

        maxSourceEventButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/maximize.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(maxSourceEventButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.maxSourceEventButton.text")); // NOI18N
        maxSourceEventButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.maxSourceEventButton.toolTipText")); // NOI18N
        maxSourceEventButton.setPreferredSize(new java.awt.Dimension(22, 22));
        maxSourceEventButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maxSourceEventButtonActionPerformed(evt);
            }
        });

        eventSourceTitle.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventSourceTitle.text")); // NOI18N
        eventSourceTitle.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventSourceTitle.toolTipText")); // NOI18N

        eventSourceText.setColumns(20);
        eventSourceText.setFont(new java.awt.Font("Tahoma", 0, 13)); // NOI18N
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
        repoText.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.repoText.toolTipText")); // NOI18N

        repoEditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/repository.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(repoEditButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.repoEditButton.text")); // NOI18N
        repoEditButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.repoEditButton.toolTipText")); // NOI18N
        repoEditButton.setIconTextGap(0);
        repoEditButton.setPreferredSize(new java.awt.Dimension(30, 26));
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

        javax.swing.GroupLayout eventSourcePanelLayout = new javax.swing.GroupLayout(eventSourcePanel);
        eventSourcePanel.setLayout(eventSourcePanelLayout);
        eventSourcePanelLayout.setHorizontalGroup(
            eventSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventSourcePanelLayout.createSequentialGroup()
                .addGroup(eventSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(eventSourcePanelLayout.createSequentialGroup()
                        .addComponent(sourceLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addSourceEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(replaceSourceEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(delSourceEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(maxSourceEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(eventSourceScrollPane)
                    .addComponent(repoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(eventSourceTitle))
                .addGap(2, 2, 2)
                .addComponent(scrollSourcesEvent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        eventSourcePanelLayout.setVerticalGroup(
            eventSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollSourcesEvent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(eventSourcePanelLayout.createSequentialGroup()
                .addGroup(eventSourcePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(sourceLabel)
                    .addComponent(addSourceEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(replaceSourceEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(delSourceEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxSourceEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addComponent(eventSourceTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(eventSourceScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 25, Short.MAX_VALUE)
                .addGap(2, 2, 2)
                .addComponent(repoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        assoComboBox.setMaximumRowCount(20);
        assoComboBox.setModel(cbModel);
        assoComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.assoComboBox.toolTipText")); // NOI18N

        assoEditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/association.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(assoEditButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.assoEditButton.text")); // NOI18N
        assoEditButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.assoEditButton.toolTipText")); // NOI18N
        assoEditButton.setIconTextGap(0);
        assoEditButton.setPreferredSize(new java.awt.Dimension(30, 26));
        assoEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                assoEditButtonActionPerformed(evt);
            }
        });

        assoEditIndi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/editindi.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(assoEditIndi, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.assoEditIndi.text")); // NOI18N
        assoEditIndi.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.assoEditIndi.toolTipText")); // NOI18N
        assoEditIndi.setIconTextGap(0);
        assoEditIndi.setPreferredSize(new java.awt.Dimension(30, 26));
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
                .addGap(4, 4, 4)
                .addGroup(eventRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(assoPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(eventRightLayout.createSequentialGroup()
                        .addComponent(datelabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dayOfWeek)
                        .addGap(18, 18, 18)
                        .addComponent(ageAtEvent)
                        .addGap(18, 18, 18)
                        .addComponent(timelabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(45, Short.MAX_VALUE))
                    .addGroup(eventRightLayout.createSequentialGroup()
                        .addComponent(eventTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventDescriptionCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(2, 2, 2))
                    .addGroup(eventRightLayout.createSequentialGroup()
                        .addComponent(placeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventPlaceCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(2, 2, 2)
                        .addComponent(eventPlaceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(eventNotePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 316, Short.MAX_VALUE)
                    .addComponent(eventSourcePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        eventRightLayout.setVerticalGroup(
            eventRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventRightLayout.createSequentialGroup()
                .addGroup(eventRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eventTitle)
                    .addComponent(eventDescriptionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addComponent(datelabel)
                .addGroup(eventRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dayOfWeek)
                    .addComponent(ageAtEvent)
                    .addComponent(timelabel)
                    .addComponent(eventTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(eventRightLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(placeLabel)
                    .addComponent(eventPlaceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventPlaceCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addComponent(eventNotePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                .addGap(2, 2, 2)
                .addComponent(eventSourcePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(2, 2, 2)
                .addComponent(assoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        eventSplitPane.setRightComponent(eventRight);

        splitPane.setBottomComponent(eventSplitPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(splitPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(stickyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(focusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(warningButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(56, 56, 56)
                        .addComponent(indiAddButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(title, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(indiDelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(title)
                    .addComponent(indiAddButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(indiDelButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(warningButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stickyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(focusButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitPane)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void maleRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maleRadioButtonActionPerformed
        triggerChange();
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (event.eventMediaSet == null || event.eventMediaSet.isEmpty()) {
            setEventMedia(event, null, PropertySex.MALE);
        }
    }//GEN-LAST:event_maleRadioButtonActionPerformed

    private void femaleRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_femaleRadioButtonActionPerformed
        triggerChange();
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (event.eventMediaSet == null || event.eventMediaSet.isEmpty()) {
            setEventMedia(event, null, PropertySex.FEMALE);
        }
    }//GEN-LAST:event_femaleRadioButtonActionPerformed

    private void unknownRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unknownRadioButtonActionPerformed
        triggerChange();
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (event.eventMediaSet == null || event.eventMediaSet.isEmpty()) {
            setEventMedia(event, null, PropertySex.UNKNOWN);
        }
    }//GEN-LAST:event_unknownRadioButtonActionPerformed

    private void privateCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_privateCheckBoxActionPerformed
        triggerChange();
    }//GEN-LAST:event_privateCheckBoxActionPerformed

    private void scrollMediaEventAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_scrollMediaEventAdjustmentValueChanged
        if (isBusyEventMedia) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        int i = scrollMediaEvent.getValue();
        if (event.eventMediaSet != null && !event.eventMediaSet.isEmpty() && i >= 0 && i < event.eventMediaSet.size() && i != event.eventMediaIndex) {
            event.eventMediaIndex = scrollMediaEvent.getValue();
            displayEventMedia(event);
        }
    }//GEN-LAST:event_scrollMediaEventAdjustmentValueChanged

    private void addMediaEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMediaEventButtonActionPerformed
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (chooseMedia(event, event.eventMediaSet.size())) {
            displayEventMedia(event);
            textAreaPhotos.requestFocus();
        }
    }//GEN-LAST:event_addMediaEventButtonActionPerformed

    private void delMediaEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delMediaEventButtonActionPerformed
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (event.eventMediaSet != null && !event.eventMediaSet.isEmpty() && (event.eventMediaIndex >= 0) && (event.eventMediaIndex < event.eventMediaSet.size())) {
            MediaWrapper media = event.eventMediaSet.get(event.eventMediaIndex);
            event.eventMediaRemovedSet.add(media);
            event.eventMediaSet.remove(event.eventMediaIndex);
            event.eventMediaIndex--;
            if (event.eventMediaIndex < 0) {
                event.eventMediaIndex = 0;
            }
            triggerChange();
        }
        displayEventMedia(event);
    }//GEN-LAST:event_delMediaEventButtonActionPerformed

    private void brothersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_brothersButtonActionPerformed
        //xxx
    }//GEN-LAST:event_brothersButtonActionPerformed

    private void sistersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sistersButtonActionPerformed
        //xxx
    }//GEN-LAST:event_sistersButtonActionPerformed

    private void spousesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spousesButtonActionPerformed
        //xxx
    }//GEN-LAST:event_spousesButtonActionPerformed

    private void childrenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_childrenButtonActionPerformed
        //xxx
    }//GEN-LAST:event_childrenButtonActionPerformed

    private void fatherButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fatherButtonActionPerformed
        //xxx
    }//GEN-LAST:event_fatherButtonActionPerformed

    private void motherButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_motherButtonActionPerformed
        //xxx
    }//GEN-LAST:event_motherButtonActionPerformed

    private void moreNamesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moreNamesButtonActionPerformed
        DialogManager.create(NbBundle.getMessage(getClass(), "IndiPanel.moreNamesButton.toolTipText"), nameDetails).setMessageType(DialogManager.PLAIN_MESSAGE).setOptionType(OK_ONLY_OPTION).setDialogId("cygnus_names").show();
        String f = nameDetails.getFirstName();
        String l = nameDetails.getLastName();
        if (!firstnamesText.getText().equals(f)) {
            firstnamesText.setText(f);
        }
        if (!lastnameText.getText().equals(l)) {
            lastnameText.setText(l);
        }
    }//GEN-LAST:event_moreNamesButtonActionPerformed

    private void eventBuriButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventBuriButtonActionPerformed
        createOrPreSelectEvent("BURI");
        selectEvent(getRowFromIndex(eventIndex));
        eventDescriptionText.requestFocus();
    }//GEN-LAST:event_eventBuriButtonActionPerformed

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

    private void scrollMediaEventMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollMediaEventMouseWheelMoved
        int notches = evt.getWheelRotation();
        if (isBusyEventMedia) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (event.eventMediaSet != null && !event.eventMediaSet.isEmpty()) {
            int i = event.eventMediaIndex + notches;
            if (i >= event.eventMediaSet.size()) {
                i = event.eventMediaSet.size() - 1;
            }
            if (i < 0) {
                i = 0;
            }
            event.eventMediaIndex = i;
            displayEventMedia(event);
        }
    }//GEN-LAST:event_scrollMediaEventMouseWheelMoved

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
            triggerChange();
        }
        displayEventNote(event);
    }//GEN-LAST:event_delNoteEventButtonActionPerformed

    private void eventSplitPanePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_eventSplitPanePropertyChange
        registry.put("cygnuseventSplitDividerLocation", eventSplitPane.getDividerLocation());
        imagePanel.redraw();
    }//GEN-LAST:event_eventSplitPanePropertyChange

    private void addSourceEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSourceEventButtonActionPerformed
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (chooseEventSource(event, event.eventSourceSet.size())) {
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
            triggerChange();
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
        repoText.requestFocus();

    }//GEN-LAST:event_repoEditButtonActionPerformed

    private void assoEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_assoEditButtonActionPerformed
        if (manageAssociations()) {
            displayAssociationsComboBox();
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
            SelectionDispatcher.fireSelection(new Context(asso.assoIndi));     // fireselection because we are navigating to another entity
        }
    }//GEN-LAST:event_assoEditIndiActionPerformed

    private void eventBirtButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventBirtButtonActionPerformed
        createOrPreSelectEvent("BIRT");
        selectEvent(getRowFromIndex(eventIndex));
        eventDescriptionText.requestFocus();
    }//GEN-LAST:event_eventBirtButtonActionPerformed

    private void eventBaptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventBaptButtonActionPerformed
        createOrPreSelectEvent("CHR");
        selectEvent(getRowFromIndex(eventIndex));
        eventDescriptionText.requestFocus();
    }//GEN-LAST:event_eventBaptButtonActionPerformed

    private void eventMarrButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventMarrButtonActionPerformed
        showPopupEventMenu(eventMarrButton, "MARR", "EventMenu_AddMarr", "EventMenu_DisplayNextMarr");
    }//GEN-LAST:event_eventMarrButtonActionPerformed

    private void eventDeatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventDeatButtonActionPerformed
        createOrPreSelectEvent("DEAT");
        selectEvent(getRowFromIndex(eventIndex));
        eventDescriptionText.requestFocus();
    }//GEN-LAST:event_eventDeatButtonActionPerformed

    private void eventOccuButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventOccuButtonActionPerformed
        showPopupEventMenu(eventOccuButton, "OCCU", "EventMenu_AddOccu", "EventMenu_DisplayNextOccu");
    }//GEN-LAST:event_eventOccuButtonActionPerformed

    private void eventRetiButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventRetiButtonActionPerformed
        showPopupEventMenu(eventRetiButton, "RETI", "EventMenu_AddReti", "EventMenu_DisplayNextReti");
    }//GEN-LAST:event_eventRetiButtonActionPerformed

    private void eventResiButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventResiButtonActionPerformed
        showPopupEventMenu(eventResiButton, "RESI", "EventMenu_AddResi", "EventMenu_DisplayNextResi");
    }//GEN-LAST:event_eventResiButtonActionPerformed

    private void eventRemoveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventRemoveButtonActionPerformed
        EventWrapper event = getCurrentEvent();
        eventRemovedSet.add(event);
        eventSet.remove(eventIndex);
        int row = getRowFromIndex(eventIndex);
        if (row == eventSet.size()) {
            row--;
        }
        if (row < 0) {
            row = 0;
        }
        displayEventTable();
        selectEvent(row);
        triggerChange();
    }//GEN-LAST:event_eventRemoveButtonActionPerformed

    private void eventOthersButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventOthersButtonActionPerformed
        showPopupEventMenu(eventOthersButton);
    }//GEN-LAST:event_eventOthersButtonActionPerformed

    private void warningButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_warningButtonActionPerformed
        showWarningsAndErrors();
    }//GEN-LAST:event_warningButtonActionPerformed

    private void indiAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indiAddButtonActionPerformed
        IndiCreator indiCreator = new IndiCreator(IndiCreator.CREATION, indi, IndiCreator.REL_NONE, null, null);
        //getEditorTopComponent().setContext(new Context(indiCreator.getIndi()));
        SelectionDispatcher.fireSelection(new Context(indiCreator.getIndi()));
    }//GEN-LAST:event_indiAddButtonActionPerformed

    private void indiDelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indiDelButtonActionPerformed
        if (DialogManager.YES_OPTION == DialogManager.create(NbBundle.getMessage(getClass(), "TITL_WARNING_Delete_Indi"), NbBundle.getMessage(getClass(), "MSG_WARNING_Delete_Indi", indi.toString())).setMessageType(DialogManager.WARNING_MESSAGE).setOptionType(DialogManager.YES_NO_OPTION).show()) {
            new IndiCreator(IndiCreator.DESTROY, indi, IndiCreator.REL_NONE, null, null);
        }
    }//GEN-LAST:event_indiDelButtonActionPerformed

    private void eventPlaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eventPlaceButtonActionPerformed
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (chooseEventPlace(event)) {
            PropertyLatitude lat = event.place.getLatitude(true);
            PropertyLongitude lon = event.place.getLongitude(true);
            eventPlaceText.setText(event.place.getDisplayValue());  // this generates a remove and an insert events ; the remove event deletes the coordinates ; let's put them back
            if (lat != null && lon != null) {
                event.place.setCoordinates(lat.getValue(), lon.getValue());
            } else {
                 event.place.setCoordinates("", "");
            }
            eventPlaceText.setCaretPosition(0);
            eventPlaceText.requestFocus();
            triggerChange();   //FL: 2017-05-24 : should be triggered by changing the field, but force change just in case (on one user, change was not triggered)
        }
    }//GEN-LAST:event_eventPlaceButtonActionPerformed

    private void addMediaSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMediaSourceButtonActionPerformed
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (chooseSourceMedia(event, event.eventSourceIndex, true)) {
            displayEventSource(event);
            mediaSourceText.requestFocus();
        }
    }//GEN-LAST:event_addMediaSourceButtonActionPerformed

    private void delMediaSourceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delMediaSourceButtonActionPerformed
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        SourceWrapper source = event.getEventSource();
        if (source == null) {
            return;
        }
        if (source.deleteMedia()) {
            triggerChange();
            displayEventSource(event);
            mediaSourceText.requestFocus();
        }
    }//GEN-LAST:event_delMediaSourceButtonActionPerformed

    private void scrollMediaSourceAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_scrollMediaSourceAdjustmentValueChanged
        if (isBusyEventSource) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        SourceWrapper source = event.getEventSource();
        if (source == null) {
            return;
        }
        int i = scrollMediaSource.getValue();
        if (source.sourceMediaSet != null && !source.sourceMediaSet.isEmpty() && i >= 0 && i < source.sourceMediaSet.size() && i != source.sourceMediaIndex) {
            source.sourceMediaIndex = scrollMediaSource.getValue();
            setMediaSource(source);
        }
    }//GEN-LAST:event_scrollMediaSourceAdjustmentValueChanged

    private void scrollMediaSourceMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollMediaSourceMouseWheelMoved
        if (isBusyEventSource) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        SourceWrapper source = event.getEventSource();
        if (source == null) {
            return;
        }
        if (source.sourceMediaSet != null && !source.sourceMediaSet.isEmpty()) {
            int notches = evt.getWheelRotation();
            int i = source.sourceMediaIndex + notches;
            if (i >= source.sourceMediaSet.size()) {
                i = source.sourceMediaSet.size() - 1;
            }
            if (i < 0) {
                i = 0;
            }
            source.sourceMediaIndex = i;
            setMediaSource(source);
        }
    }//GEN-LAST:event_scrollMediaSourceMouseWheelMoved

    private void mediaImagePanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_mediaImagePanelMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1 && !((evt.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK)) {
            EventWrapper event = getCurrentEvent();
            if (event == null) {
                return;
            }
            if (chooseMedia(event, event.eventMediaIndex)) {
                displayEventMedia(event);
                textAreaPhotos.requestFocus();
            }
        } else if (evt.getButton() == MouseEvent.BUTTON1 && ((evt.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK)) {
            photoPanel.cropAndSave();
            EventWrapper event = getCurrentEvent();
            event.eventMediaSet.get(event.eventMediaIndex).setInputSource(photoPanel.getInput());
            triggerChange();
        }
    }//GEN-LAST:event_mediaImagePanelMouseClicked

    private void sourceImagePanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sourceImagePanelMouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1 && !((evt.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK)) {
            EventWrapper event = getCurrentEvent();
            if (event == null) {
                return;
            }
            if (chooseSourceMedia(event, event.eventSourceIndex, false)) {
                displayEventSource(event);
                mediaSourceText.requestFocus();
            }
        } else if (evt.getButton() == MouseEvent.BUTTON1 && ((evt.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK)) {
            imagePanel.cropAndSave();
            EventWrapper event = getCurrentEvent();
            event.setSourceFile(imagePanel.getInput(), false);
            triggerChange();
        }
    }//GEN-LAST:event_sourceImagePanelMouseClicked

    private void replaceNoteEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceNoteEventButtonActionPerformed
        if (!replaceNoteEventButton.isEnabled()) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (chooseEventNote(event, event.eventNoteIndex)) {
            displayEventNote(event);
            eventNote.requestFocus();
        }
    }//GEN-LAST:event_replaceNoteEventButtonActionPerformed

    private void replaceSourceEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceSourceEventButtonActionPerformed
        if (!replaceSourceEventButton.isEnabled()) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        if (chooseEventSource(event, event.eventSourceIndex)) {
            displayEventSource(event);
            eventSourceTitle.requestFocus();
        }
    }//GEN-LAST:event_replaceSourceEventButtonActionPerformed

    private void maxNoteEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxNoteEventButtonActionPerformed
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        textDetails.setText(eventNote.getText());
        DialogManager.create(NbBundle.getMessage(getClass(), "IndiPanel.maxNoteEventButton.toolTipText"), textDetails).setMessageType(DialogManager.PLAIN_MESSAGE).setOptionType(OK_ONLY_OPTION).show();
        String noteStr = textDetails.getText();
        if (!eventNote.getText().equals(noteStr)) {
            eventNote.setText(noteStr);
        }
    }//GEN-LAST:event_maxNoteEventButtonActionPerformed

    private void maxSourceEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maxSourceEventButtonActionPerformed
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        textDetails.setText(eventSourceText.getText());
        DialogManager.create(NbBundle.getMessage(getClass(), "IndiPanel.maxSourceEventButton.toolTipText"), textDetails).setMessageType(DialogManager.PLAIN_MESSAGE).setOptionType(OK_ONLY_OPTION).show();
        String sourceStr = textDetails.getText();
        if (!eventSourceText.getText().equals(sourceStr)) {
            eventSourceText.setText(sourceStr);
        }
    }//GEN-LAST:event_maxSourceEventButtonActionPerformed

    private void mediaImagePanelComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_mediaImagePanelComponentResized
        photoPanel.redraw();
    }//GEN-LAST:event_mediaImagePanelComponentResized

    private void stickyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stickyButtonActionPerformed
        triggerSticky();
    }//GEN-LAST:event_stickyButtonActionPerformed

    private void prefMediaEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prefMediaEventButtonActionPerformed
        // make this media the first one (the preferred one)
        if (isBusyEventMedia) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event == null || event.eventMediaSet.isEmpty()) {
            return;
        }
        MediaWrapper photo = event.eventMediaSet.get(event.eventMediaIndex);
        event.eventMediaSet.remove(photo);
        event.eventMediaSet.add(0, photo);
        event.eventMediaIndex = 0;
        displayEventMedia(event);
        triggerChange();
    }//GEN-LAST:event_prefMediaEventButtonActionPerformed

    private void focusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_focusButtonActionPerformed
        triggerFocus();
    }//GEN-LAST:event_focusButtonActionPerformed

    private void splitPanePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_splitPanePropertyChange
        registry.put("cygnustopSplitDividerLocation", splitPane.getDividerLocation());
    }//GEN-LAST:event_splitPanePropertyChange

    private void fatherButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fatherButtonMouseClicked
        showPopupFamilyMenu(fatherButton, evt.getButton() == 3, IndiCreator.REL_FATHER, indi.getBiologicalFather(), null);
    }//GEN-LAST:event_fatherButtonMouseClicked

    private void motherButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_motherButtonMouseClicked
        showPopupFamilyMenu(motherButton, evt.getButton() == 3, IndiCreator.REL_MOTHER, indi.getBiologicalMother(), null);
    }//GEN-LAST:event_motherButtonMouseClicked

    private void brothersButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_brothersButtonMouseClicked
        showPopupFamilyMenu(brothersButton, evt.getButton() == 3, IndiCreator.REL_BROTHER, null, indi.getBrothers(true));
    }//GEN-LAST:event_brothersButtonMouseClicked

    private void sistersButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sistersButtonMouseClicked
        showPopupFamilyMenu(sistersButton, evt.getButton() == 3, IndiCreator.REL_SISTER, null, indi.getSisters(true));
    }//GEN-LAST:event_sistersButtonMouseClicked

    private void spousesButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_spousesButtonMouseClicked
        showPopupFamilyMenu(spousesButton, evt.getButton() == 3, IndiCreator.REL_PARTNER, null, indi.getPartners());
    }//GEN-LAST:event_spousesButtonMouseClicked

    private void childrenButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_childrenButtonMouseClicked
        showPopupFamilyMenu(childrenButton, evt.getButton() == 3, IndiCreator.REL_CHILD, null, indi.getChildren());
    }//GEN-LAST:event_childrenButtonMouseClicked

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
    private javax.swing.JPanel TopButtonsdPanel;
    private javax.swing.JButton addMediaEventButton;
    private javax.swing.JButton addMediaSourceButton;
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
    private javax.swing.JButton delMediaEventButton;
    private javax.swing.JButton delMediaSourceButton;
    private javax.swing.JButton delNoteEventButton;
    private javax.swing.JButton delSourceEventButton;
    private javax.swing.JButton eventBaptButton;
    private javax.swing.JButton eventBirtButton;
    private javax.swing.JButton eventBuriButton;
    private javax.swing.JButton eventDeatButton;
    private javax.swing.JComboBox eventDescriptionCombo;
    private javax.swing.JPanel eventLeft;
    private javax.swing.JButton eventMarrButton;
    private javax.swing.JTextArea eventNote;
    private javax.swing.JPanel eventNotePanel;
    private javax.swing.JScrollPane eventNoteScrollPane;
    private javax.swing.JButton eventOccuButton;
    private javax.swing.JButton eventOthersButton;
    private javax.swing.JButton eventPlaceButton;
    private javax.swing.JComboBox eventPlaceCombo;
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
    private javax.swing.JTextField eventTime;
    private javax.swing.JLabel eventTitle;
    private javax.swing.JTree familyTree;
    private javax.swing.JButton fatherButton;
    private javax.swing.JRadioButton femaleRadioButton;
    private javax.swing.JComboBox firstnamesCombo;
    private javax.swing.JLabel firstnamesLabel;
    private javax.swing.JToggleButton focusButton;
    private javax.swing.JLabel idLabel;
    private javax.swing.JButton indiAddButton;
    private javax.swing.JButton indiDelButton;
    private javax.swing.JComboBox lastnameCombo;
    private javax.swing.JLabel lastnameLabel;
    private javax.swing.JRadioButton maleRadioButton;
    private javax.swing.JButton maxNoteEventButton;
    private javax.swing.JButton maxSourceEventButton;
    private javax.swing.JPanel mediaImagePanel;
    private javax.swing.JPanel mediaPanel;
    private javax.swing.JTextField mediaSourceText;
    private javax.swing.JLabel modificationLabel;
    private javax.swing.JButton moreNamesButton;
    private javax.swing.JButton motherButton;
    private javax.swing.JPanel namePanel;
    private javax.swing.JLabel noteLabel;
    private javax.swing.JLabel placeLabel;
    private javax.swing.JButton prefMediaEventButton;
    private javax.swing.JCheckBox privateCheckBox;
    private javax.swing.JButton replaceNoteEventButton;
    private javax.swing.JButton replaceSourceEventButton;
    private javax.swing.JButton repoEditButton;
    private javax.swing.JPanel repoPanel;
    private javax.swing.JTextField repoText;
    private javax.swing.JScrollBar scrollMediaEvent;
    private javax.swing.JScrollBar scrollMediaSource;
    private javax.swing.JScrollBar scrollNotesEvent;
    private javax.swing.JScrollPane scrollPaneFamily;
    private javax.swing.JScrollPane scrollPanePhotos;
    private javax.swing.JScrollBar scrollSourcesEvent;
    private javax.swing.JButton sistersButton;
    private javax.swing.JLabel sosaLabel;
    private javax.swing.JPanel sourceImagePanel;
    private javax.swing.JLabel sourceLabel;
    private javax.swing.JPanel sourcePanel;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JButton spousesButton;
    private javax.swing.JButton stickyButton;
    private javax.swing.JTextArea textAreaPhotos;
    private javax.swing.JLabel timelabel;
    private javax.swing.JLabel title;
    private javax.swing.JPanel topPanel;
    private javax.swing.JRadioButton unknownRadioButton;
    private javax.swing.JButton warningButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public Component getEditorComponent() {
        return this;
    }

    @Override
    public ViewContext getContext() {
        Property prop = getCurrentEvent().eventProperty;
        if (indi.contains(prop)) {
            context = new Context(prop);
        } else {
            context = new Context(indi); // newly created properties are not yet attached to indi and point to a temporary gedcom
        }
        return new ViewContext(context);
    }

    @Override
    public void setGedcomHasChanged(boolean flag) {
        // Force Reload
        reloadData = true;

        // Remember selections
        EventWrapper ew = getCurrentEvent();
        if (ew != null) {
            savedEventTagDateDesc = ew.getEventKey(flag);
            savedEventMediaIndex = ew.eventMediaIndex;
            savedEventNoteIndex = ew.eventNoteIndex;
            savedEventSourceIndex = ew.eventSourceIndex;
            savedEventSourceMediaIndex = 0;
            if (ew.eventSourceSet != null && !ew.eventSourceSet.isEmpty()) {
                SourceWrapper sw = ew.eventSourceSet.get(ew.eventSourceIndex);
                savedEventSourceMediaIndex = sw.sourceMediaIndex;
            }
        }
        savedFocusedControl = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    }

    @Override
    protected void setContextImpl(Context context) {
        LOG.finer(TimingUtility.getInstance().getTime() + ": setContextImpl().start");

        // force data reload if to be reloaded or if entity selected is different
        if (reloadData || (this.context != null && context != null && !this.context.equals(context) && this.context.getEntity() != context.getEntity())) {
            reloadData = true;
        }

        this.context = context;
        this.gedcom = context.getGedcom();
        if (gedcom == null) {
            stickyButton.setSelected(false);
            return;
        }

        Utilities.setCursorWaiting(this);
        Entity entity = context.getEntity();
        if (entity != null && (entity instanceof Indi)) {

            // If sticky is off, refresh any context (new indi), otherwise stick to current indi and refresh it at most
            if (!stickyButton.isSelected()) {
                this.indi = (Indi) entity;
            }

            if (reloadData) {  // do not reload data when not necessary, for performance reasons when selecting properties in Gedcom editor for instance
                loadData();
                warningButton.setVisible(passControls());
                reloadData = false;
            }

            if (!listernersOn) {
                addListeners();
                listernersOn = true;
            }

            // Focus saved focused field, or else on firstnames
            if (isGrabFocus()) {
                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                    @Override
                    public void run() {
                        if (savedFocusedControl == null || !savedFocusedControl.isFocusable()) {
                            firstnamesText.setCaretPosition(firstnamesText.getText().length());
                            firstnamesText.requestFocus();
                        } else {
                            if (savedFocusedControl instanceof JTextField) {
                                JTextField jtf = (JTextField) savedFocusedControl;
                                jtf.setCaretPosition(jtf.getText().length());
                            }
                            savedFocusedControl.requestFocus();
                            savedFocusedControl = null;
                        }
                    }
                });
            }

            // Overwrite and select default context property if any
            selectPropertyContext(context);

            // Reset change flag
            changes.setChanged(false);

        }

        Utilities.setCursorNormal(this);
        LOG.finer(TimingUtility.getInstance().getTime() + ": setContextImpl().finish");
    }

    /**
     * Select event corresponding to property - if context is of different
     * entity - if context is an event, select it
     *
     * @param context
     */
    private void selectPropertyContext(Context context) {
        // Select event selected when last saved (it if not necessarily a property in case it is being created for instance)
        if (!savedEventTagDateDesc.equals("-1") && eventSet != null) {
            selectEvent(savedEventTagDateDesc);
            savedEventTagDateDesc = "-1";
            scrollMediaEvent.setValue(savedEventMediaIndex);
            savedEventMediaIndex = -1;
            scrollNotesEvent.setValue(savedEventNoteIndex);
            savedEventNoteIndex = -1;
            scrollSourcesEvent.setValue(savedEventSourceIndex);
            savedEventSourceIndex = -1;
            scrollMediaSource.setValue(savedEventSourceMediaIndex);
            savedEventSourceMediaIndex = -1;
            return;
        }

        // Else select property if any (coming from fire Selection)
        Property propertyToDisplay = context.getProperty();
        boolean found = false;
        if (propertyToDisplay != null && eventSet != null) {
            Property loopProp = propertyToDisplay;
            while (loopProp != null) {
                for (EventWrapper event : eventSet) {
                    if (event.eventProperty.equals(loopProp)) {
                        int index = eventSet.indexOf(event);
                        if (index != -1) {
                            selectEvent(getRowFromIndex(index));
                            scrollToProperty(event, propertyToDisplay);
                            loopProp = null;
                            found = true;
                            break;
                        }
                    }
                } // end for
                if (!found) {
                    loopProp = loopProp.getParent();
                }
            }
        }

        // Else select first row if eventSet not empty
        if (!found) {
            selectEvent(0);
        }

        // Select corresponding context line in family tree in case of FAM property
        Property p = propertyToDisplay != null ? propertyToDisplay.getEntity() : null;
        if (p instanceof Fam) {
            Fam fam = (Fam) p;
            Object o = familyTree.getModel().getRoot();
            if (o instanceof DefaultMutableTreeNode) {
                Enumeration<TreeNode> e = ((DefaultMutableTreeNode) o).depthFirstEnumeration();
                while (e.hasMoreElements()) {
                    TreeNode tn = e.nextElement();
                    if (tn instanceof DefaultMutableTreeNode) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tn;
                        NodeWrapper nodewrapper = (NodeWrapper) node.getUserObject();
                        if (nodewrapper != null && nodewrapper.getType() == NodeWrapper.SPOUSE) {
                            Fam nodeFam = (Fam) nodewrapper.getCurrentFamily(indi);
                            if (nodeFam == fam) {
                                TreePath tp = new TreePath(node.getPath());
                                familyTree.setSelectionPath(tp);
                                familyTree.expandPath(tp);
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            familyTree.clearSelection();
        }

    }

    private void selectEvent(String key) {
        if (eventTable.getRowCount() == 0) {
            return;
        }
        for (EventWrapper event : eventSet) {
            if (key.equals(event.getEventKey())) {
                selectEvent(getRowFromIndex(eventSet.indexOf(event)));
                return;
            }
        }
        selectEvent(0);
    }

    private void selectEvent(int row) {
        if (eventTable.getRowCount() == 0) {
            return;
        }
        if (row < 0 || row >= eventTable.getRowCount()) {
            row = 0;
        }
        eventTable.setRowSelectionInterval(row, row);
        eventTable.scrollRectToVisible(new Rectangle(eventTable.getCellRect(row, 0, true)));
        eventIndex = eventTable.convertRowIndexToModel(eventTable.getSelectedRow());
    }

    private int getRowFromIndex(int index) {
        if (eventTable != null && eventTable.getRowCount() > 0) {
            return eventTable.convertRowIndexToView(index);
        }
        return -1;
    }

    private void scrollToProperty(EventWrapper event, Property property) {
        Property parent = property.getParent();
        if (parent == null) {
            return;
        }
        TagPath tagPathRoot = TagPath.get(event.eventProperty);
        TagPath tagPath = TagPath.get(property);
        if (tagPathRoot.length() > (tagPath.length() - 1)) {
            return;
        }
        boolean isSource = tagPath.get(tagPathRoot.length()).equals("SOUR");

        // If media in individual panel
        if (!isSource && tagPath.contains("OBJE")) {
            for (MediaWrapper mw : event.eventMediaSet) {
                if (belongsTo(mw.getHostingProperty(), property)) {
                    event.eventMediaIndex = event.eventMediaSet.indexOf(mw);
                    displayEventMedia(event);
                    return;
                }
            }
            return;
        }

        // If note in note panel
        if (!isSource && tagPath.contains("NOTE")) {
            for (NoteWrapper nw : event.eventNoteSet) {
                if (belongsTo(nw.getHostingProperty(), property)) {
                    event.eventNoteIndex = event.eventNoteSet.indexOf(nw);
                    displayEventNote(event);
                    return;
                }
            }
            return;
        }

        // If source in source panel
        if (isSource && !tagPath.contains("OBJE")) {
            for (SourceWrapper sw : event.eventSourceSet) {
                if (belongsTo(sw.getHostingProperty(), property)) {
                    event.eventSourceIndex = event.eventSourceSet.indexOf(sw);
                    displayEventSource(event);
                    return;
                }
            }
            return;
        }

        // If media in source panel
        if (isSource && tagPath.contains("OBJE")) {
            for (SourceWrapper sw : event.eventSourceSet) {
                if (belongsTo(sw.getHostingProperty(), parent)) {
                    event.eventSourceIndex = event.eventSourceSet.indexOf(sw);
                    for (MediaWrapper mw : sw.sourceMediaSet) {
                        if (belongsTo(mw.getHostingProperty(), property)) {
                            sw.sourceMediaIndex = sw.sourceMediaSet.indexOf(mw);
                        }
                    }
                    displayEventSource(event);
                    return;
                }
            }
            return;
        }

    }

    private boolean belongsTo(Property host, Property child) {
        while (child != null) {
            if (child == host) {
                return true;
            }
            child = child.getParent();
        }
        return false;
    }

    @Override
    public void commit() throws GedcomException {
        boolean nouveau = indi.isNew();
        indi.setOld();
        saveData();

        // Detect if ask for it and new or any time.
        if ((GedcomOptions.getInstance().getDetectDuplicate() && nouveau) || GedcomOptions.getInstance().getDuplicateAnyTime()) {
            SwingUtilities.invokeLater(new IndiDuplicatesFinder(indi));
        }
    }

    private void triggerSticky() {
        stickyButton.setSelected(!stickyButton.isSelected());
        stickyButton.setIcon(stickyButton.isSelected()
                ? (new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/StickOn.png")))
                : (new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/StickOff.png"))));
    }

    private void triggerFocus() {
        registry.put("focus", focusButton.isSelected());

    }

    /**
     * Check whether editor should grab focus or not
     */
    private boolean isGrabFocus() {
        return focusButton.isSelected();
    }

    private void triggerChange() {
        changes.setChanged(true);
        // If auto commit is on, users may expect data to be saved at every change... That is not done : data is saved at change of contexxt only
        // (I haven't found a way to do it properly with all the listeners around...)
        //        if (ConfirmChangeWidget.getAutoCommit()) {
        //            savedata();
        //        }
    }

    private CygnusTopComponent getEditorTopComponent() {
        for (CygnusTopComponent editorTopComponent : (List<CygnusTopComponent>) AncestrisPlugin.lookupAll(CygnusTopComponent.class)) {
            if (editorTopComponent.getEditor() != null && editorTopComponent.getEditor() == this) {
                return editorTopComponent;
            }
        }
        return null;
    }

    @Override
    public Entity getEditedEntity() {
        return indi;
    }

    public ChangeSupport getChangeSupport() {
        return changes;
    }

    private void loadData() {
        String str = "";
        int i = 0;
        boolean privateTagFound = false;

        // Title
        title.setText("<html> " + indi.getFirstName() + " " + indi.getLastName() + " </html> ");

        // IDs
        idLabel.setText(NbBundle.getMessage(IndiPanel.class, "IndiPanel.idLabel.text") + " " + indi.getId());
        str = indi.getSosaString();
        if (str.isEmpty()) {
            str = NO_SOSA;
        }
        sosaLabel.setText(NbBundle.getMessage(IndiPanel.class, "IndiPanel.sosaLabel.text") + " " + str);

        // Names
        allFirstNames = PropertyName.getFirstNames(gedcom, true);
        AutoCompletion.reset(firstnamesCombo, allFirstNames);
        allLastNames = PropertyName.getLastNames(gedcom, true);
        AutoCompletion.reset(lastnameCombo, allLastNames);
        firstnamesText.setText(indi.getFirstName());
        lastnameText.setText(indi.getLastName());
        nameDetails.setDetails(indi);

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

        // Family tree (parents, siblings, mariages and corresponding childrens)
        createFamilyNodes(indi);
        familyTree.repaint();

        // Places
        allPlaces = gedcom.getReferenceSet("PLAC").getKeys(gedcom.getCollator());
        AutoCompletion.reset(eventPlaceCombo, allPlaces);

        // Events (with description, date, place, note, source)
        if (eventSet != null) {
            eventSet.clear();
            eventSet = null;
        }
        if (eventRemovedSet != null) {
            eventRemovedSet.clear();
            eventRemovedSet = null;
        }
        eventSet = getEvents(indi);
        eventRemovedSet = new ArrayList<>();
        displayEventTable();
        eventIndex = 0;

        // Associations
        if (assoSet != null) {
            assoSet.clear();
            assoSet = null;
        }
        if (assoRemovedSet != null) {
            assoRemovedSet.clear();
            assoRemovedSet = null;
        }
        assoSet = getAssociations(indi);
        assoRemovedSet = new ArrayList<>();
        displayAssociationsComboBox();

        // Modification timestamp
        modificationLabel.setText(NbBundle.getMessage(IndiPanel.class, "IndiPanel.modificationLabel.text") + " : " + (indi.getLastChange() != null ? indi.getLastChange().getDisplayValue() : ""));
        //

        // ID on father and mother button
        Indi dad = indi.getBiologicalFather();
        if (dad != null) {
            final String dadName = dad.getName();
            if (dadName.length() > 15) {
                fatherButton.setText(dadName.substring(0, 15) + " (...)");
            } else {
                fatherButton.setText(dadName);
            }
        } else {
            fatherButton.setText("");
        }
        Indi mom = indi.getBiologicalMother();
        if (mom != null) {
            final String momName = mom.getName();
            if (momName.length() > 15) {
                motherButton.setText(momName.substring(0, 15) + " (...)");
            } else {
                motherButton.setText(momName);
            }
        } else {
            motherButton.setText("");
        }

    }

    private void addListeners() {
        // Main
        firstnamesText.getDocument().addDocumentListener(this);
        lastnameText.getDocument().addDocumentListener(this);
        nameDetails.addListeners(this);
        eventDate.addChangeListener(new EventDateListener());
        eventTime.getDocument().addDocumentListener(new EventTimeListener());
        eventPlaceText.getDocument().addDocumentListener(new EventPlaceListener());

        // Events
        textAreaPhotos.getDocument().addDocumentListener(new PhotoTitleListener());
        eventNote.getDocument().addDocumentListener(new EventNoteTextListener());
        EventSourceTextListener estl = new EventSourceTextListener();
        eventSourceTitle.getDocument().addDocumentListener(estl);
        eventSourceText.getDocument().addDocumentListener(estl);
        mediaSourceText.getDocument().addDocumentListener(estl);

    }

    private void saveData() {

        // Save names
        nameDetails.saveNameDetails(indi, firstnamesText.getText(), lastnameText.getText());

        // Save gender
        saveSex();

        // Save privacy
        savePrivacy();

        // Save Events
        saveEvents();

        // Save assocs
        saveAssociations();

        // End
    }

    /**
     * *************************************************************************
     * Sex
     */
    private int getSex() {
        return maleRadioButton.isSelected() ? PropertySex.MALE : femaleRadioButton.isSelected() ? PropertySex.FEMALE : PropertySex.UNKNOWN;
    }

    /**
     * *************************************************************************
     * Family tree
     */
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
        Fam[] fams = indi.getFamiliesWhereSpouse(true);
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

    /**
     * *************************************************************************
     * Events
     */
    private List<EventWrapper> getEvents(Indi indi) {
        List<EventWrapper> ret = new ArrayList<>();

        // Start adding the general event which will only hold general notes and sources for the individual
        ret.add(new EventWrapper(indi, indi, null));

        // Look for all individual events
        // - INDIVIDUAL_EVENT_STRUCTURE (birth, etc.)
        // - INDIVIDUAL_ATTRIBUTE_STRUCTURE (occu, resi, etc.)
        //
        String[] INDI_TAGS = EventUsage.getTags(eventUsages, "INDI");
        for (String tag : INDI_TAGS) {
            Property[] eventProps = indi.getProperties(tag);
            for (Property prop : eventProps) {
                if (prop != null) {
                    ret.add(new EventWrapper(prop, indi, null));
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
                        ret.add(new EventWrapper(prop, indi, fam));
                    }
                }
            }
        }

        return ret;
    }

    private void saveSex() {
        PropertySex p = (PropertySex) indi.getProperty("SEX", false);
        if (p == null) {
            indi.setSex(getSex());
            return;
        }

        // Quit if nothing has changed
        if (p.getSex() == getSex()) {
            return;
        }

        indi.setSex(getSex());
    }

    private void savePrivacy() {
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
    }

    private void saveEvents() {
        //eventSet
        for (EventWrapper event : eventSet) {
            event.update(indi);
        }

        // Remove events updating associations at the same time
        Set<AssoWrapper> tmpList = new HashSet<AssoWrapper>();
        for (EventWrapper event : eventRemovedSet) {
            for (AssoWrapper asso : assoSet) {
                if (asso.assoProp.getTargetParent() == event.eventProperty) {
                    tmpList.add(asso);
                }
            }
            event.remove(indi);
        }
        for (AssoWrapper asso : tmpList) {
            assoSet.remove(asso);
        }
    }

    private void displayEventTable() {
        // Init table
        if (eventTableListener == null) {
            eventTableListener = new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting() && !isBusyEvent && eventTable.getSelectedRow() != -1) {
                        eventIndex = eventTable.convertRowIndexToModel(eventTable.getSelectedRow());
                        displayEvent();
                        displayAssociationsComboBox();
                    }
                }
            };
            eventTable.setShowHorizontalLines(false);
            eventTable.setShowVerticalLines(false);
            eventTable.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);

        }

        // Clear table
        if (eventTableModel != null) {
            eventTableModel.clear();
        }
        if (eventTableListener != null) {
            eventTable.getSelectionModel().removeListSelectionListener(eventTableListener);
        }

        // Refresh table
        eventTableModel = new EventTableModel(eventSet);
        eventTable.setModel(eventTableModel);
        eventTable.setAutoCreateRowSorter(true);

        int maxWidth = 0;
        for (EventWrapper event : eventSet) {
            maxWidth = Math.max(maxWidth, getFontMetrics(getFont()).stringWidth(event.eventLabel.getShortLabel()));
        }
        eventTable.getColumnModel().getColumn(0).setPreferredWidth(maxWidth + 15); // add icon size
        eventTable.getColumnModel().getColumn(1).setPreferredWidth(getFontMetrics(getFont()).stringWidth(" 9999 "));
        eventTable.getColumnModel().getColumn(2).setPreferredWidth(getFontMetrics(getFont()).stringWidth(" 99.9 "));

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

        eventTable.getSelectionModel().addListSelectionListener(eventTableListener);

        sortEventTable();

    }

    private void sortEventTable() {
        TableRowSorter sorter = new TableRowSorter<>((EventTableModel) eventTable.getModel());
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
                    Double d1 = new DecimalFormat(EventWrapper.AGE_FORMAT).parse(s1).doubleValue();
                    Double d2 = new DecimalFormat(EventWrapper.AGE_FORMAT).parse(s2).doubleValue();
                    return d1.compareTo(d2);
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                    return 0;
                }
            }
        });
        eventTable.setRowSorter(sorter);
        List<SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(2, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();
    }

    private void showGeneralInformation(boolean show) {
        //eventTitle.setVisible(show);
        eventDescriptionCombo.setVisible(show);
        datelabel.setVisible(show);
        eventDate.setVisible(show);
        dayOfWeek.setVisible(show);
        ageAtEvent.setVisible(show);
        timelabel.setVisible(show);
        eventTime.setVisible(show);
        placeLabel.setVisible(show);
        eventPlaceCombo.setVisible(show);
        eventPlaceButton.setVisible(show);
    }

    private void displayEvent() {
        isBusyEvent = true;
        eventDate.removeChangeListener(changes);

        EventWrapper event = getCurrentEvent();
        boolean cursorHasChanged = Utilities.setCursorWaiting(this);
        if (event != null) {

            showGeneralInformation(!event.isGeneral);
            eventRemoveButton.setEnabled(eventIndex != 0);

            eventTitle.setText(event.title);

            if (!event.isGeneral) {
                // Descriptions : list of items in combo box depens on event type
                eventDescriptionText.getDocument().removeDocumentListener(edl);
                allDescriptions = gedcom.getReferenceSet(event.getTag()).getKeys(gedcom.getCollator());
                AutoCompletion.reset(eventDescriptionCombo, allDescriptions);
                eventDescriptionText.getDocument().addDocumentListener(edl);
                // Title
                eventDescriptionText.setText(event.description);
                eventDescriptionText.setCaretPosition(0);

                // Date
                if (event.date != null) {
                    eventDate.setPropertyImpl(event.date);
                } else {
                    eventDate.setPropertyImpl(null);
                }
                ageAtEvent.setText(event.age);
                dayOfWeek.setText(event.dayOfWeek);

                // Time
                eventTime.setText(event.time);

                // Place
                if (event.place != null) {
                    eventPlaceText.setText(event.place.getDisplayValue());
                } else {
                    eventPlaceText.setText("");
                }
                eventPlaceText.setCaretPosition(0);

            }

            // Media
            scrollMediaEvent.setMinimum(0);
            scrollMediaEvent.setBlockIncrement(1);
            scrollMediaEvent.setUnitIncrement(1);
            event.eventMediaIndex = 0;
            displayEventMedia(event);

            // Notes
            scrollNotesEvent.setMinimum(0);
            scrollNotesEvent.setBlockIncrement(1);
            scrollNotesEvent.setUnitIncrement(1);
            event.eventNoteIndex = 0;
            displayEventNote(event);

            // Sources - Media & Text & Repo
            scrollSourcesEvent.setMinimum(0);
            scrollSourcesEvent.setBlockIncrement(1);
            scrollSourcesEvent.setUnitIncrement(1);
            event.eventSourceIndex = 0;
            displayEventSource(event);

        }

        if (cursorHasChanged) {
            Utilities.setCursorNormal(this);
        }
        isBusyEvent = false;
        eventDate.addChangeListener(changes);
    }

    /**
     * *************************************************************************
     * Place
     */
    private boolean chooseEventPlace(EventWrapper event) {

        boolean b = false;

        JButton OKButton = new JButton(NbBundle.getMessage(getClass(), "Button_Ok"));
        JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
        Object[] options = new Object[]{OKButton, cancelButton};

        if (placeEditor == null) {
            placeEditor = new PlaceEditorPanel();
        }
        placeEditor.set(gedcom, event.place, false);
        placeEditor.setOKButton(OKButton);

        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_ChoosePlaceTitle"), placeEditor).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
        placeEditor.close();
        if (o == OKButton) {
            placeEditor.copyValue(event.place);
            b = true;
        }

        return b;
    }

    /**
     * *************************************************************************
     * Media
     */
    private void displayEventMedia(EventWrapper event) {
        isBusyEventMedia = true;
        if (event.eventMediaSet != null && !event.eventMediaSet.isEmpty() && (event.eventMediaIndex >= 0) && (event.eventMediaIndex < event.eventMediaSet.size())) {
            setEventMedia(event, event.eventMediaSet.get(event.eventMediaIndex), indi.getSex());
        } else {
            setEventMedia(event, null, getSex());
        }
        isBusyEventMedia = false;
    }

    private void setEventMedia(EventWrapper event, MediaWrapper media, int sex) {

        InputSource is = null;
        String localTitle = "";

        if (media != null) {
            is = media.getInputSource();
            localTitle = media.getTitle();
        }

        // Photo
        textAreaPhotos.setFont(new Font("DejaVu sans", Font.PLAIN, 11));
        textAreaPhotos.setForeground(Color.BLACK);
        textAreaPhotos.setEditable(true);

        // Image
        if (is != null) {
            photoPanel.setMedia(is, getSexImage(sex));
            prefMediaEventButton.setEnabled(true);
        } else {
            // try to display main indi photo rather than default grey one
            if (eventSet != null && !eventSet.isEmpty()
                    && eventSet.get(0) != null
                    && eventSet.get(0).eventMediaSet != null
                    && !eventSet.get(0).eventMediaSet.isEmpty()
                    && eventSet.get(0).eventMediaSet.get(0) != null
                    && eventSet.get(0).eventMediaSet.get(0).getInputSource() != null) {
                InputSource f0 = eventSet.get(0).eventMediaSet.get(0).getInputSource();
                photoPanel.setMedia(f0, getSexImage(sex));
                localTitle = NbBundle.getMessage(getClass(), "IndiPanel.Photo_default");
                textAreaPhotos.setFont(new Font("DejavVu sans", Font.ITALIC, 9));
                textAreaPhotos.setForeground(Color.GRAY);
                textAreaPhotos.setEditable(false);
            } else {
                photoPanel.setMedia(null, getSexImage(sex));
            }
            prefMediaEventButton.setEnabled(false);
        }

        // Title
        textAreaPhotos.setText(localTitle);
        textAreaPhotos.setCaretPosition(0);

        // Update scroll
        scrollMediaEvent.setValues(event.eventMediaIndex, 1, 0, event.eventMediaSet.size());
        scrollMediaEvent.setToolTipText(getScrollPhotosLabel(event));
    }

    private String getScrollPhotosLabel(EventWrapper event) {
        return String.valueOf(event.eventMediaSet.size() > 0 ? event.eventMediaIndex + 1 : event.eventMediaIndex) + "/" + String.valueOf(event.eventMediaSet.size());
    }

    private BufferedImage getSexImage(int sex) {
        return (sex == PropertySex.MALE ? PHOTO_MALE : (sex == PropertySex.FEMALE ? PHOTO_FEMALE : PHOTO_UNKNOWN));
    }

    private boolean chooseMedia(EventWrapper event, int index) {
        boolean b = false;
        boolean exists = (event.eventMediaSet != null) && (!event.eventMediaSet.isEmpty()) && (index >= 0) && (index < event.eventMediaSet.size());

        JButton mediaButton = new JButton(NbBundle.getMessage(getClass(), "Button_ChooseMedia"));
        JButton fileButton = new JButton(NbBundle.getMessage(getClass(), "Button_LookForFile"));
        JButton externFileButton = new JButton(NbBundle.getMessage(getClass(), "Button_LookForInternetFile"));
        JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
        Object[] options = new Object[]{mediaButton, fileButton, externFileButton, cancelButton};
        MediaChooser mediaChooser = new MediaChooser(gedcom, exists ? event.eventMediaSet.get(index).getInputSource() : null,
                exists ? getImageFromFile(event.eventMediaSet.get(index).getInputSource(), getClass()) : getSexImage(getSex()),
                exists ? event.eventMediaSet.get(index).getTitle() : "",
                exists ? event.eventMediaSet.get(index) : null,
                mediaButton, cancelButton,
                false
        );
        int size = mediaChooser.getNbMedia();
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_ChooseMediaTitle", size), mediaChooser).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
        if (o == mediaButton) {
            InputSource file = mediaChooser.getSelectedInput();
            String mediaTitle = mediaChooser.getSelectedTitle();
            if (mediaChooser.isSelectedEntityMedia()) {
                Media entity = (Media) mediaChooser.getSelectedEntity();
                if (exists) {
                    event.eventMediaSet.get(index).setTargetEntity(entity);
                    event.eventMediaSet.get(index).setTitle(mediaTitle);
                    event.eventMediaIndex = index;
                } else {
                    MediaWrapper media = new MediaWrapper(entity);
                    media.setTitle(mediaTitle);
                    event.eventMediaSet.add(media);
                    event.eventMediaIndex = event.eventMediaSet.size() - 1;
                }
                triggerChange();
                b = true;
            } else {
                if (exists) {
                    event.eventMediaSet.get(index).setInputSource(file);
                    event.eventMediaSet.get(index).setTitle(mediaTitle);
                    event.eventMediaIndex = index;
                } else {
                    MediaWrapper media = new MediaWrapper(file, mediaTitle);
                    event.eventMediaSet.add(media);
                    event.eventMediaIndex = event.eventMediaSet.size() - 1;
                }
                triggerChange();
                b = true;
            }
        } else if (o == fileButton) {
            return chooseFileImage(event, index);
        } else if (o == externFileButton) {
            return chooseInternetFile(event, index);
        }

        return b;
    }

    private boolean chooseFileImage(EventWrapper event, int index) {
        boolean b = false;
        boolean exists = (event.eventMediaSet != null) && (!event.eventMediaSet.isEmpty()) && (index >= 0) && (index < event.eventMediaSet.size());

        File file = new FileChooserBuilder(IndiPanel.class.getCanonicalName() + "Images")
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(getClass(), "FileChooserTitle"))
                .setApproveText(NbBundle.getMessage(getClass(), "FileChooserOKButton"))
                .setDefaultExtension(FileChooserBuilder.getImageFilter().getExtensions()[0])
                .addFileFilter(FileChooserBuilder.getImageFilter())
                .addFileFilter(FileChooserBuilder.getSoundFilter())
                .addFileFilter(FileChooserBuilder.getVideoFilter())
                .addFileFilter(FileChooserBuilder.getPdfFilter())
                .setAcceptAllFileFilterUsed(false)
                .setFileHiding(true)
                .setDefaultPreviewer()
                .showOpenDialog();
        if (file != null) {
            InputSource is = InputSource.get(file).get();
            if (exists) {
                event.eventMediaSet.get(index).setInputSource(is);
                event.eventMediaIndex = index;
            } else {
                MediaWrapper media = new MediaWrapper(is);
                event.eventMediaSet.add(media);
                event.eventMediaIndex = event.eventMediaSet.size() - 1;
            }
            triggerChange();
            b = true;
        } else {
            textAreaPhotos.requestFocus();
        }
        return b;
    }

    private boolean chooseInternetFile(EventWrapper event, int index) {
        boolean b = false;
        boolean exists = (event.eventMediaSet != null) && (!event.eventMediaSet.isEmpty()) && (index >= 0) && (index < event.eventMediaSet.size());

        DialogManager.InputLine link = DialogManager.create(NbBundle.getMessage(getClass(), "Button_LookForInternetFile"), NbBundle.getMessage(getClass(), "LookForInternetFile"), theUrl);
        theUrl = link.show();
        if (theUrl != null && !theUrl.isEmpty()) {
            URL url;
            try {
                url = new URL(theUrl);
            } catch (MalformedURLException e) {
                LOG.log(Level.FINE, "Unable to get media URL : " + theUrl, e);
                return b;
            }
            InputSource is = InputSource.get(url).get();
            if (exists) {
                event.eventMediaSet.get(index).setInputSource(is);
                event.eventMediaIndex = index;
            } else {
                MediaWrapper media = new MediaWrapper(is);
                event.eventMediaSet.add(media);
                event.eventMediaIndex = event.eventMediaSet.size() - 1;
            }
            triggerChange();
            b = true;
        } else {
            textAreaPhotos.requestFocus();
        }

        return b;
    }

    /**
     * *************************************************************************
     * Notes
     */
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

        // Icon button
        boolean enabled = (event != null) && (event.eventNoteSet.size() > 0) && (event.eventNoteIndex < event.eventNoteSet.size()) && ((event.eventNoteSet.get(event.eventNoteIndex)).isRecord());
        replaceNoteEventButton.setEnabled(enabled);
        replaceNoteEventButton.setToolTipText(NbBundle.getMessage(IndiPanel.class, enabled ? "IndiPanel.replaceNoteEventButton.toolTipText" : "IndiPanel.replaceNoteEventButton.toolTipTextOff"));
        eventNote.setCaretPosition(0);

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
        Object[] options = new Object[]{noteButton, cancelButton};
        NoteChooser noteChooser = new NoteChooser(gedcom, exists ? event.eventNoteSet.get(index) : null, noteButton, cancelButton);
        int size = noteChooser.getNbNotes();
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_ChooseNoteTitle", size), noteChooser).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
        if (o == noteButton) {
            String noteText = noteChooser.getSelectedText();
            if (noteChooser.isSelectedEntityNote()) {
                Note entity = (Note) noteChooser.getSelectedEntity();
                if (exists) {
                    event.setNote(entity, noteText, index);
                } else {
                    event.addNote(entity, noteText);
                }
                triggerChange();
                b = true;
            } else {
                if (exists) {
                    event.setNote(noteText, index);
                } else {
                    event.addNote(noteText);
                }
                triggerChange();
                b = true;
            }
        }

        return b;
    }

    /**
     * *************************************************************************
     * Sources
     */
    private void displayEventSource(EventWrapper event) {
        if (event.eventSourceSet != null && !event.eventSourceSet.isEmpty() && (event.eventSourceIndex >= 0) && (event.eventSourceIndex < event.eventSourceSet.size())) {
            setEventSource(event, event.eventSourceSet.get(event.eventSourceIndex));
        } else {
            setEventSource(event, null);
        }
    }

    private void setEventSource(EventWrapper event, SourceWrapper source) {
        isBusyEventSource = true;

        // Icon button
        boolean enabled = (event != null) && (event.eventSourceSet.size() > 0) && (event.eventSourceIndex < event.eventSourceSet.size()) && ((event.eventSourceSet.get(event.eventSourceIndex)).isRecord());
        replaceSourceEventButton.setEnabled(enabled);
        repoEditButton.setEnabled(enabled);
        replaceSourceEventButton.setToolTipText(NbBundle.getMessage(IndiPanel.class, enabled ? "IndiPanel.replaceSourceEventButton.toolTipText" : "IndiPanel.replaceSourceEventButton.toolTipTextOff"));
        repoEditButton.setToolTipText(NbBundle.getMessage(IndiPanel.class, enabled ? "IndiPanel.repoEditButton.toolTipText" : "IndiPanel.repoEditButton.toolTipTextOff"));
        eventNote.setCaretPosition(0);

        // Title
        eventSourceTitle.setText(source == null ? "" : source.getTitle());
        eventSourceTitle.setCaretPosition(0);

        // Text
        eventSourceText.setText(source == null ? "" : source.getText());
        eventSourceText.setCaretPosition(0);

        // Media
        setMediaSource(source);
        isBusyEventSource = true;

        // Repositoryname
        repoText.setText(source != null ? source.getRepoName() : "");
        repoText.setCaretPosition(0);

        // Update scroll
        scrollSourcesEvent.setValues(event.eventSourceIndex, 1, 0, event.eventSourceSet.size());
        scrollSourcesEvent.setToolTipText(getScrollEventSourcesLabel(event));
        isBusyEventSource = false;
    }

    private void setMediaSource(SourceWrapper source) {
        isBusyEventSource = true;
        imagePanel.setMedia(source != null ? source.getMediaFile() : null, SOURCE_UNKNOWN);
        mediaSourceText.setText(source != null ? source.getMediaTitle() : "");
        if (source != null && source.sourceMediaSet != null) {
            scrollMediaSource.setValues(source.sourceMediaIndex, 1, 0, source.sourceMediaSet.size());
            scrollMediaSource.setToolTipText(getScrollMediaSourcesLabel(source));
        } else {
            scrollMediaSource.setValues(0, 1, 0, 0);
            scrollMediaSource.setToolTipText("0/0");
        }
        isBusyEventSource = false;
    }

    private String getScrollEventSourcesLabel(EventWrapper event) {
        return String.valueOf(event.eventSourceSet.size() > 0 ? event.eventSourceIndex + 1 : event.eventSourceIndex) + "/" + String.valueOf(event.eventSourceSet.size());
    }

    private String getScrollMediaSourcesLabel(SourceWrapper source) {
        return String.valueOf(source.sourceMediaSet.size() > 0 ? source.sourceMediaIndex + 1 : source.sourceMediaIndex) + "/" + String.valueOf(source.sourceMediaSet.size());
    }

    public boolean chooseEventSource(EventWrapper event, int index) {
        boolean b = false;
        boolean exists = (event.eventSourceSet != null) && (!event.eventSourceSet.isEmpty()) && (index >= 0) && (index < event.eventSourceSet.size());

        JButton sourceButton = new JButton(NbBundle.getMessage(getClass(), "Button_ChooseSource"));
        JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
        Object[] options = new Object[]{sourceButton, cancelButton};
        SourceChooser sourceChooser = new SourceChooser(gedcom, exists ? event.getEventSource() : null, sourceButton, cancelButton);
        int size = sourceChooser.getNbSource();
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_ChooseSourceTitle", size), sourceChooser).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
        if (o == sourceButton) {
            if (sourceChooser.isSelectedEntitySource()) {
                Source entity = (Source) sourceChooser.getSelectedEntity();
                if (exists) {
                    event.setSource(entity, index);
                } else {
                    event.addSource(entity);
                }
                triggerChange();
                b = true;
            }
        }

        return b;
    }

    private boolean chooseSourceMedia(EventWrapper event, int index, boolean addMedia) {
        boolean b = false;
        boolean exists = (event.eventSourceSet != null) && (!event.eventSourceSet.isEmpty()) && (index >= 0) && (index < event.eventSourceSet.size());
        MediaWrapper readMedia = null;
        InputSource f = null;
        if (exists) { // source exists
            SourceWrapper source = event.eventSourceSet.get(event.eventSourceIndex);
            if (source.sourceMediaSet != null && !source.sourceMediaSet.isEmpty()) {
                readMedia = source.sourceMediaSet.get(source.sourceMediaIndex);
            }
        }
        f = (readMedia != null ? readMedia.getInputSource() : null);

        JButton mediaButton = new JButton(NbBundle.getMessage(getClass(), "Button_ChooseMedia"));
        JButton fileButton = new JButton(NbBundle.getMessage(getClass(), "Button_LookForFile"));
        JButton externFileButton = new JButton(NbBundle.getMessage(getClass(), "Button_LookForInternetFile"));
        JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
        Object[] options = new Object[]{mediaButton, fileButton, externFileButton, cancelButton};
        MediaChooser mediaChooser = new MediaChooser(gedcom, exists ? f : null,
                exists ? getImageFromFile(f, getClass()) : null,
                exists ? (readMedia != null ? readMedia.getTitle() : "") : "",
                exists ? readMedia : null,
                mediaButton, cancelButton,
                true
        );
        int size = mediaChooser.getNbMedia();
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_ChooseMediaTitle", size), mediaChooser).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
        if (o == mediaButton) {
            InputSource file = mediaChooser.getSelectedInput();
            String mediaTitle = mediaChooser.getSelectedTitle();
            MediaWrapper media = null;
            if (mediaChooser.isSelectedEntityMedia()) {
                media = new MediaWrapper((Media) mediaChooser.getSelectedEntity());
            } else {
                media = new MediaWrapper(file, mediaTitle);
            }
            if (exists) {
                event.setSourceMedia(media, addMedia);
                if (!addMedia) {
                    media.setHostingProperty(readMedia != null ? readMedia.getHostingProperty() : null);
                }
            } else {
                event.addSourceMedia(media);
            }
            triggerChange();
            b = true;
        } else if (o == fileButton) {
            return chooseSourceFile(event, index, addMedia);
        } else if (o == externFileButton) {
            return chooseSourceInternetFile(event, index, addMedia);
        }

        return b;
    }

    private boolean chooseSourceInternetFile(EventWrapper event, int index, boolean addMedia) {
        boolean b = false;
        boolean exists = (event.eventSourceSet != null) && (!event.eventSourceSet.isEmpty()) && (index >= 0) && (index < event.eventSourceSet.size());

        DialogManager.InputLine link = DialogManager.create(NbBundle.getMessage(getClass(), "Button_LookForInternetFile"), NbBundle.getMessage(getClass(), "LookForInternetFile"), theUrl);
        theUrl = link.show();
        if (theUrl != null && !theUrl.isEmpty()) {
            URL url;
            try {
                url = new URL(theUrl);
            } catch (MalformedURLException e) {
                LOG.log(Level.FINE, "Unable to get media URL : " + theUrl, e);
                return b;
            }
            InputSource is = InputSource.get(url).get();
            if (exists) {
                event.setSourceFile(is, addMedia);
            } else {
                event.addSourceFile(is);
            }
            triggerChange();
            b = true;
        }
        return b;
    }

    private boolean chooseSourceFile(EventWrapper event, int index, boolean addMedia) {
        boolean b = false;
        boolean exists = (event.eventSourceSet != null) && (!event.eventSourceSet.isEmpty()) && (index >= 0) && (index < event.eventSourceSet.size());

        File file = new FileChooserBuilder(IndiPanel.class.getCanonicalName() + "Sources")
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(NbBundle.getMessage(getClass(), "FileChooserTitle"))
                .setApproveText(NbBundle.getMessage(getClass(), "FileChooserOKButton"))
                .setDefaultExtension(FileChooserBuilder.getImageFilter().getExtensions()[0])
                .addFileFilter(FileChooserBuilder.getImageFilter())
                .addFileFilter(FileChooserBuilder.getSoundFilter())
                .addFileFilter(FileChooserBuilder.getVideoFilter())
                .addFileFilter(FileChooserBuilder.getPdfFilter())
                .setAcceptAllFileFilterUsed(false)
                .setFileHiding(true)
                .setDefaultPreviewer()
                .showOpenDialog();
        if (file != null) {
            InputSource is = InputSource.get(file).get();
            if (exists) {
                event.setSourceFile(is, addMedia);
            } else {
                event.addSourceFile(is);
            }
            triggerChange();
            b = true;
        }
        return b;
    }

    public boolean chooseRepository(EventWrapper event) {
        boolean b = false;
        boolean exists = (event.eventSourceSet != null) && (!event.eventSourceSet.isEmpty()) && (event.eventSourceIndex >= 0) && (event.eventSourceIndex < event.eventSourceSet.size());

        JButton selectButton = new JButton(NbBundle.getMessage(getClass(), "Button_ChooseRepo"));
        JButton unselectButton = new JButton(NbBundle.getMessage(getClass(), "Button_UnchooseRepo"));
        unselectButton.setEnabled(exists);
        JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
        Object[] options = new Object[]{selectButton, unselectButton, cancelButton};
        SourceWrapper source = event.eventSourceSet.isEmpty() ? null : event.eventSourceSet.get(event.eventSourceIndex);
        RepoChooser repoChooser = new RepoChooser(gedcom, source, selectButton, cancelButton);
        int size = repoChooser.getNbRepos();
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_ChooseRepoTitle", size), repoChooser).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
        if (o == selectButton) {
            if (repoChooser.isSelectedEntityRepo()) {
                Repository repo = (Repository) repoChooser.getSelectedEntity();
                if (exists) {
                    event.setSourceRepository(repo);
                } else {
                    event.addSourceRepository(repo);
                }
                triggerChange();
                b = true;
            }
        } else if (o == unselectButton) {
            if (repoChooser.isSelectedEntityRepo()) {
                if (exists) {
                    event.setSourceRepository(null);
                } else {
                    //nothing
                }
                triggerChange();
                b = true;
            }
        }
        return b;
    }

    /**
     * *************************************************************************
     * Associations
     */
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

    private void displayAssociationsComboBox() {
        if (eventSet == null || eventSet.isEmpty() || assoSet == null) {
            return;
        }

        // Empty list
        cbModel.removeAllElements();

        // Build new list with only event related associations
        EventWrapper event = getCurrentEvent();
        for (AssoWrapper asso : assoSet) {
            if (event.isGeneral || asso.targetEvent == event) {
                cbModel.addElement(asso);
            }
        }

        // Set combo box list
        if (cbModel.getSize() == 0) {
            cbModel.addElement(new AssoWrapper(NbBundle.getMessage(getClass(), "No_Association_Text")));
        }
        assoComboBox.setModel(cbModel);
        assoComboBox.setSelectedIndex(0);

        assoEditButton.setEnabled(!(eventSet.isEmpty() && assoSet.isEmpty()));
    }

    private boolean manageAssociations() {
        boolean b = false;
        JButton okButton = new JButton(NbBundle.getMessage(getClass(), "Button_Ok"));
        JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
        Object[] options = new Object[]{okButton, cancelButton};
        AssoManager assoManager = new AssoManager(indi, eventSet, assoSet, (AssoWrapper) assoComboBox.getSelectedItem(), okButton, cancelButton);
        String localTitle = NbBundle.getMessage(getClass(), "TITL_AssoManagerTitle", assoManager.getIndi());
        Object o = DialogManager.create(localTitle, assoManager).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
        if (o == okButton && assoManager.hasChanged()) {
            for (AssoWrapper asso : assoSet) {
                if (!assoManager.contains(asso)) {
                    assoRemovedSet.add(asso);
                }
            }
            assoSet = assoManager.clone(assoManager.getSet());
            triggerChange();
            b = true;
        }
        return b;
    }

    private void saveAssociations() {
        //assoSet
        for (AssoWrapper asso : assoSet) {
            asso.update();
        }
        for (AssoWrapper asso : assoRemovedSet) {
            asso.remove();
        }
    }

    /**
     * *************************************************************************
     * Updaters (user has made a change to in a field or control, data is stored
     * in data structure)
     */
    private void updateEventDescription(DocumentEvent e) {
        if (isBusyEvent) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event != null) {
            event.setDescription(eventDescriptionText.getText());
            triggerChange();
        }
    }

    private void updateEventPlace(DocumentEvent e) {
        if (isBusyEvent) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event != null) {
            event.setPlace(eventPlaceText.getText()); 
            triggerChange();
        }
    }

    private void updateEventDate(ChangeEvent e) {
        if (isBusyEvent) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event != null) {
            event.setDate(eventDate);
        }
    }

    private void updateEventTime(DocumentEvent e) {
        if (isBusyEvent) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event != null) {
            event.setTime(eventTime.getText());
            triggerChange();
        }
    }

    private void updatePhotoTitle() {
        if (isBusyEvent || isBusyEventMedia) {
            return;
        }
        EventWrapper event = getCurrentEvent();
        if (event == null) {
            return;
        }
        String photoTitle = textAreaPhotos.getText();
        if ((event.eventMediaSet != null) && (!event.eventMediaSet.isEmpty()) && (event.eventMediaIndex >= 0) && (event.eventMediaIndex < event.eventMediaSet.size())) {
            event.eventMediaSet.get(event.eventMediaIndex).setTitle(photoTitle);
            event.setMedia(photoTitle);
        } else {
            event.addMedia(photoTitle);
        }
        Entity ent = event.eventMediaSet.get(event.eventMediaIndex).getTargetMedia();
        if (ent instanceof Media) {
            propagateMedia((Media) ent, photoTitle);
        }

        triggerChange();
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
            event.setNote(noteText);
        } else {
            event.addNote(noteText);
        }
        Entity ent = event.eventNoteSet.get(event.eventNoteIndex).getTargetNote();
        if (ent instanceof Note) {
            propagateNote((Note) ent, noteText);
        }

        triggerChange();
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
        String mediaTitle = mediaSourceText.getText();
        if ((event.eventSourceSet != null) && (!event.eventSourceSet.isEmpty()) && (event.eventSourceIndex >= 0) && (event.eventSourceIndex < event.eventSourceSet.size())) {
            event.setSource(sourceTitle, sourceText, mediaTitle);
        } else {
            event.addSource(sourceTitle, sourceText, mediaTitle);
        }
        SourceWrapper sourceW = event.eventSourceSet.get(event.eventSourceIndex);
        Entity ent = sourceW.getTargetSource();
        if (ent instanceof Source) {
            propagateSource((Source) ent, sourceTitle, sourceText);
        }
        if (sourceW.sourceMediaSet != null && !sourceW.sourceMediaSet.isEmpty()) {
            MediaWrapper mediaW = sourceW.sourceMediaSet.get(sourceW.sourceMediaIndex);
            if (mediaW != null) {
                propagateMedia((Media) mediaW.getTargetMedia(), mediaTitle);
                propagateSourceMedia((Media) mediaW.getTargetMedia(), mediaTitle);
            }
        }
        triggerChange();
    }

    /**
     * Propagators
     *
     * - lookup all events for entities of same type and propage the same change
     *
     */
    public void propagateMedia(Media media, String text) {
        if (media == null) {
            return;
        }
        for (EventWrapper event : eventSet) {
            if (event.eventMediaSet == null) {
                continue;
            }
            for (MediaWrapper mediaW : event.eventMediaSet) {
                if (mediaW.getTargetMedia() != media) {
                    continue;
                }
                mediaW.setTitle(text);
            }
        }
    }

    public void propagateNote(Note note, String text) {
        if (note == null) {
            return;
        }
        for (EventWrapper event : eventSet) {
            if (event.eventNoteSet == null) {
                continue;
            }
            for (NoteWrapper noteW : event.eventNoteSet) {
                if (noteW.getTargetNote() != note) {
                    continue;
                }
                noteW.setText(text);
            }
        }
    }

    public void propagateSource(Source source, String title, String text) {
        if (source == null) {
            return;
        }
        for (EventWrapper event : eventSet) {
            if (event.eventSourceSet == null) {
                continue;
            }
            for (SourceWrapper sourceW : event.eventSourceSet) {
                if (sourceW.getTargetSource() != source) {
                    continue;
                }
                sourceW.setTitle(title);
                sourceW.setText(text);
            }
        }
    }

    public void propagateSourceMedia(Media media, String text) {
        if (media == null) {
            return;
        }
        for (EventWrapper event : eventSet) {
            if (event.eventSourceSet == null) {
                continue;
            }
            for (SourceWrapper sourceW : event.eventSourceSet) {
                if (sourceW.sourceMediaSet == null) {
                    continue;
                }
                for (MediaWrapper mediaW : sourceW.sourceMediaSet) {
                    if (mediaW.getTargetMedia() != media) {
                        continue;
                    }
                    mediaW.setTitle(text);
                }
            }
        }
    }

    /**
     * *************************************************************************
     * Family buttons navigation
     */
    private void showPopupFamilyMenu(JButton button, boolean isPopup, final int relation, Indi familyMember, Indi[] familyMembers) {

        ImageIcon displaIcon = new ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/editindi.png"));
        ImageIcon createIcon = new ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/ico_create.png"));
        ImageIcon attachIcon = new ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/ico_attach.png"));
        ImageIcon detachIcon = new ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/ico_detach.png"));

        // If modifications not saved, add "save and " in front of each label
        String prefixLabel = changes.hasChanged() ? NbBundle.getMessage(getClass(), "SaveAnd") + " " : "";

        // Initiate menu
        JPopupMenu menu = new JPopupMenu("");
        JMenuItem menuItem = null;
        boolean putSeparator = false;

        // Build popup menu with items
        // - (save and ) create <family member> : only if <family member> does not exist or more than one can be created
        // - (save and ) attach <family member> : only if <family member> does not exist or more than one can be created
        // - (save and ) detach <family member> : only if <family member> exists, with sub-menu if several exist
        // create father or mother 
        if ((relation == IndiCreator.REL_FATHER || relation == IndiCreator.REL_MOTHER) && familyMember != null) {
            String label = NbBundle.getMessage(getClass(), "DisplaIndi_" + IndiCreator.RELATIONS[relation], familyMember.getName());
            menuItem = new JMenuItem(prefixLabel + (changes.hasChanged() ? label.toLowerCase() : label), displaIcon);
            menu.add(menuItem);
            putSeparator = true;
            final Indi fThisIndi = indi;
            final Indi fFamilyIndi = familyMember;
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    if (fThisIndi != null) {
                        if (changes.hasChanged()) {
                            changes.fireChangeEvent(true);
                        }
                    }
                    SelectionDispatcher.fireSelection(new Context(fFamilyIndi));   // fireselection because we are navigating to another entity
                }

            });
        }
        if ((relation == IndiCreator.REL_FATHER || relation == IndiCreator.REL_MOTHER) && familyMember == null) {
            if (putSeparator) {
                menu.addSeparator();
            }
            String label = NbBundle.getMessage(getClass(), "CreateIndi_" + IndiCreator.RELATIONS[relation]);
            menuItem = new JMenuItem(prefixLabel + (changes.hasChanged() ? label.toLowerCase() : label), createIcon);
            menu.add(menuItem);
            putSeparator = true;
            menuItem.addActionListener(new ActionCreation(getEditorTopComponent(), IndiCreator.CREATION, relation, indi));
        }
        // attach father or mother 
        if ((relation == IndiCreator.REL_FATHER || relation == IndiCreator.REL_MOTHER) && familyMember == null) {
            if (putSeparator) {
                menu.addSeparator();
            }
            Indi[] potentialFamilyMembers = Utils.getPotentialFamilyMembers(indi, relation);
            for (Indi potMember : potentialFamilyMembers) {
                String label = "";
                if (potMember != null) {
                    label = NbBundle.getMessage(getClass(), "AttachIndi_" + IndiCreator.RELATIONS[relation], Utils.getDetails(potMember));
                } else {
                    if (potentialFamilyMembers.length >= 2) {
                        label = NbBundle.getMessage(getClass(), "AttachIndi_" + IndiCreator.RELATIONS[relation] + "_others");
                    } else {
                        label = NbBundle.getMessage(getClass(), "AttachIndi_" + IndiCreator.RELATIONS[relation] + "_other");
                    }
                }
                menuItem = new JMenuItem(prefixLabel + (changes.hasChanged() ? label.toLowerCase() : label), attachIcon);
                menu.add(menuItem);
                putSeparator = true;
                final Indi fIndi = potMember;
                menuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        Indi indiToAttach = fIndi;
                        if (indiToAttach == null) {
                            indiToAttach = getIndiFromUser(indi, relation == IndiCreator.REL_FATHER ? indi.getLastName() : "", relation);
                        }
                        if (indiToAttach != null) {
                            if (changes.hasChanged()) {
                                changes.fireChangeEvent(true);
                            }
                            IndiCreator indiCreator = new IndiCreator(IndiCreator.ATTACH, indi, relation, null, indiToAttach);
                            getEditorTopComponent().setContext(new Context(indiCreator.getIndi()));
                        }
                    }

                });
            }
        }
        // detach father or mother 
        if ((relation == IndiCreator.REL_FATHER || relation == IndiCreator.REL_MOTHER) && familyMember != null) {
            if (putSeparator) {
                menu.addSeparator();
            }
            String label = NbBundle.getMessage(getClass(), "DetachIndi_" + IndiCreator.RELATIONS[relation], familyMember.getName());
            menuItem = new JMenuItem(prefixLabel + (changes.hasChanged() ? label.toLowerCase() : label), detachIcon);
            menu.add(menuItem);
            final Indi fIndi = familyMember;
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    if (changes.hasChanged()) {
                        changes.fireChangeEvent(true);
                    }
                    new IndiCreator(IndiCreator.DETACH, indi, relation, null, fIndi);
                    getEditorTopComponent().setContext(new Context(indi));
                }
            });
        }

        putSeparator = false;
        final Fam currentFam = Utils.getCurrentFamily(indi, familyTree);

        // create family members
        if (relation != IndiCreator.REL_FATHER && relation != IndiCreator.REL_MOTHER && familyMembers != null && familyMembers.length != 0) {
            for (Indi i : familyMembers) {
                String label = NbBundle.getMessage(getClass(), "DisplaIndi_" + IndiCreator.RELATIONS[relation], i.getName());
                menuItem = new JMenuItem(prefixLabel + (changes.hasChanged() ? label.toLowerCase() : label), displaIcon);
                menu.add(menuItem);
                putSeparator = true;
                final Indi fIndi = i;
                menuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        if (changes.hasChanged()) {
                            changes.fireChangeEvent(true);
                        }
                        SelectionDispatcher.fireSelection(new Context(fIndi));   // fireselection because we are navigating to another entity
                    }
                });
            }
        }
        if (relation != IndiCreator.REL_FATHER && relation != IndiCreator.REL_MOTHER) {
            if (putSeparator) {
                menu.addSeparator();
            }
            String label = NbBundle.getMessage(getClass(), "CreateIndi_" + IndiCreator.RELATIONS[relation]);

            if (relation == IndiCreator.REL_CHILD) {
                if (currentFam != null) {
                    Indi currentSpouse = currentFam.getOtherSpouse(indi);
                    if (currentSpouse != null) {
                        label += " " + NbBundle.getMessage(getClass(), "CreateIndi_CHILD_spouse", currentSpouse);
                    } else {
                        label += " " + NbBundle.getMessage(getClass(), "CreateIndi_CHILD_selectedSpouse");
                    }
                } else {
                    label += " ";
                }
            }
            menuItem = new JMenuItem(prefixLabel + (changes.hasChanged() ? label.toLowerCase() : label), createIcon);
            menu.add(menuItem);
            menuItem.addActionListener(new ActionCreation(getEditorTopComponent(), IndiCreator.CREATION, relation, indi, currentFam));
            // Now, for child only, if there was one valid spouse, create another menu item to create a child from unknown spouse without creating the spouse
            if (relation == IndiCreator.REL_CHILD && currentFam != null) {
                label = NbBundle.getMessage(getClass(), "CreateIndi_" + IndiCreator.RELATIONS[relation]);
                label += " " + NbBundle.getMessage(getClass(), "CreateIndi_CHILD_unknownSpouse");
                menuItem = new JMenuItem(prefixLabel + (changes.hasChanged() ? label.toLowerCase() : label), createIcon);
                menu.add(menuItem);
                menuItem.addActionListener(new ActionCreation(getEditorTopComponent(), IndiCreator.CREATION, relation, indi, null));
            }
            putSeparator = true;
        }
        // attach family members
        if (relation != IndiCreator.REL_FATHER && relation != IndiCreator.REL_MOTHER) {
            if (putSeparator) {
                menu.addSeparator();
            }
            Indi[] potentialFamilyMembers = Utils.getPotentialFamilyMembers(indi, relation);
            for (Indi potMember : potentialFamilyMembers) {
                String label = "";
                if (potMember != null) {
                    label = NbBundle.getMessage(getClass(), "AttachIndi_" + IndiCreator.RELATIONS[relation], Utils.getDetails(potMember));
                } else {
                    if (potentialFamilyMembers.length >= 2) {
                        label = NbBundle.getMessage(getClass(), "AttachIndi_" + IndiCreator.RELATIONS[relation] + "_others");
                    } else {
                        label = NbBundle.getMessage(getClass(), "AttachIndi_" + IndiCreator.RELATIONS[relation] + "_other");
                    }
                }
                menuItem = new JMenuItem(prefixLabel + (changes.hasChanged() ? label.toLowerCase() : label), attachIcon);
                menu.add(menuItem);
                putSeparator = true;
                final Indi fIndi = potMember;
                menuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        Indi indiToAttach = fIndi;
                        if (indiToAttach == null) {
                            String filter = "";
                            if (relation == IndiCreator.REL_BROTHER || relation == IndiCreator.REL_SISTER || (relation == IndiCreator.REL_CHILD && indi.getSex() == PropertySex.MALE)) {
                                filter = indi.getLastName();
                            }
                            indiToAttach = getIndiFromUser(indi, filter, relation);
                        }
                        if (indiToAttach != null) {
                            if (changes.hasChanged()) {
                                changes.fireChangeEvent(true);
                            }
                            IndiCreator indiCreator = new IndiCreator(IndiCreator.ATTACH, indi, relation, currentFam, indiToAttach);
                            getEditorTopComponent().setContext(new Context(indiCreator.getIndi()));
                        }
                    }

                });
            }
        }
        // detach family members
        if (relation != IndiCreator.REL_FATHER && relation != IndiCreator.REL_MOTHER && familyMembers != null && familyMembers.length != 0) {
            if (putSeparator) {
                menu.addSeparator();
            }
            for (Indi i : familyMembers) {
                String label = NbBundle.getMessage(getClass(), "DetachIndi_" + IndiCreator.RELATIONS[relation], i.getName());
                menuItem = new JMenuItem(prefixLabel + (changes.hasChanged() ? label.toLowerCase() : label), detachIcon);
                menu.add(menuItem);
                final Indi fIndi = i;
                menuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        if (changes.hasChanged()) {
                            changes.fireChangeEvent(true);
                        }
                        new IndiCreator(IndiCreator.DETACH, indi, relation, null, fIndi);
                        getEditorTopComponent().setContext(new Context(indi));
                    }
                });
            }
        }

        // Show menu
        if (isPopup) {
            menu.show(button, 3, button.getHeight() - 5);
        } else {
            JMenuItem item = (JMenuItem) menu.getSubElements()[0];
            item.doClick();
        }

    }

    private Indi getIndiFromUser(Indi tmpIndi, String filter, int relation) {
        // Init variables
        JButton okButton = new JButton(NbBundle.getMessage(getClass(), "Button_Ok"));
        JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
        Object[] options = new Object[]{okButton, cancelButton};
        String str = NbBundle.getMessage(getClass(), "TITL_" + IndiCreator.RELATIONS[relation]);

        // Create chooser
        IndiChooser indiChooser = new IndiChooser(tmpIndi, filter, relation, okButton);

        // Open dialog
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_ChooseIndiTitle", str, tmpIndi.toString(true)), indiChooser).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
        if (o == okButton) {
            return indiChooser.getIndi();
        } else {
            return null;
        }
    }

    /**
     * *************************************************************************
     * Event buttons manipulation and navigation
     */
    private EventWrapper getCurrentEvent() {
        EventWrapper event = null;
        if (eventSet != null && !eventSet.isEmpty() && (eventIndex >= 0) && (eventIndex < eventSet.size())) {
            event = eventSet.get(eventIndex);
        }
        return event;
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

    private void createOrPreSelectEvent(String tag) {
        int index = getEvent(tag);

        if (index == -1) { // Create event if it does not exists
            // Need to use properties attached to a gedcom, with same grammar, in order to be able to display icons
            Gedcom tmpGedcom = new Gedcom();
            tmpGedcom.setGrammar(gedcom.getGrammar());
            Entity tmpIndi = null;
            try {
                tmpIndi = tmpGedcom.createEntity(Gedcom.INDI);
            } catch (GedcomException ex) {
                return;
            }
            Property prop = tmpIndi.addProperty(tag, "");
            createEvent(prop, null);
        } else { // else select it
            eventIndex = index;
        }
    }

    private void createEvent(Property prop, Fam fam) {
        eventSet.add(new EventWrapper(prop, indi, fam));
        displayEventTable();
        eventIndex = eventSet.size() - 1;
        triggerChange();
    }

    private int getEvent(String tag) {
        for (EventWrapper event : eventSet) {
            if (event.eventProperty.getTag().equals(tag)) {
                return eventSet.indexOf(event);
            }
        }
        return -1;
    }

    private int getNextEvent(String tag) {

        int row = eventTable.getSelectedRow() + 1;
        if (row >= eventTable.getRowCount()) {
            row = 0;
        }
        for (int r = row; r < eventTable.getRowCount(); r++) {
            int i = eventTable.convertRowIndexToModel(r);
            if (eventSet.get(i).eventProperty.getTag().equals(tag)) {
                return i;
            }
        }
        for (int r = 0; r < row; r++) {
            int i = eventTable.convertRowIndexToModel(r);
            if (eventSet.get(i).eventProperty.getTag().equals(tag)) {
                return i;
            }
        }
        return -1;
    }

    private int getEventNb(String tag) {
        int nb = 0;
        for (EventWrapper event : eventSet) {
            if (event.eventProperty.getTag().equals(tag)) {
                nb++;
            }
        }
        return nb;
    }

    private void showPopupEventMenu(JButton button, final String tag, String createLabel, String displayNextLabel) {

        // Need to use properties attached to a gedcom, with same grammar, in order to be able to display icons
        boolean isFam = false;
        Gedcom tmpGedcom = new Gedcom();
        tmpGedcom.setGrammar(gedcom.getGrammar());
        Entity tmpIndi = null, tmpFam = null;
        Property prop = null;
        try {
            tmpIndi = tmpGedcom.createEntity(Gedcom.INDI);
            tmpFam = tmpGedcom.createEntity(Gedcom.FAM);
        } catch (GedcomException ex) {
            return;
        }
        prop = tmpIndi.addProperty(tag, "");
        if (!tmpIndi.getMetaProperty().allows(tag)) {
            prop = tmpFam.addProperty(tag, "");
            isFam = true;
        }
        final Property fProp = prop;
        final Fam currentFam = isFam ? Utils.getCurrentFamily(indi, familyTree) : null;

        // if tag does not exist, create it and return
        int nbEvent = getEventNb(tag);
        if (nbEvent == 0) {
            createEvent(fProp, currentFam);
            selectEvent(getRowFromIndex(eventIndex));
            eventDescriptionText.requestFocus();
            return;
        }
        String nextLabel = nbEvent == 1 ? displayNextLabel : displayNextLabel + "_many";

        JPopupMenu menu = new JPopupMenu("");   // title in popup would be nice but L&F does not display it
        JMenuItem menuItem = new JMenuItem(NbBundle.getMessage(getClass(), createLabel));
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                createEvent(fProp, currentFam);
                selectEvent(getRowFromIndex(eventIndex));
                eventDescriptionText.requestFocus();
            }
        });
        menuItem = new JMenuItem(NbBundle.getMessage(getClass(), nextLabel));
        menu.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int index = getNextEvent(tag);
                if (index != -1) {
                    selectEvent(getRowFromIndex(index));
                    eventDescriptionText.requestFocus();
                }
            }
        });
        menu.show(button, 3, button.getHeight() - 5);
    }

    private void showPopupEventMenu(JButton button) {
        JPopupMenu menu = new JPopupMenu("");   // title in popup would be nice but L&F does not display it
        JMenuItem menuItem = null;

        // Need to use properties attached to a gedcom, with same grammar, in order to be able to display icons
        boolean isFam = false;
        Gedcom tmpGedcom = new Gedcom();
        tmpGedcom.setGrammar(gedcom.getGrammar());
        Entity tmpIndi = null, tmpFam = null;
        Property prop = null;
        try {
            tmpIndi = tmpGedcom.createEntity(Gedcom.INDI);
            tmpFam = tmpGedcom.createEntity(Gedcom.FAM);
        } catch (GedcomException ex) {
            return;
        }

        // Loop on all other events to build list of sorted items
        SortedMap<String, Property> names = new TreeMap<String, Property>();
        for (final String tag : EventUsage.otherEventsList) {
            prop = tmpIndi.addProperty(tag, "");
            if (!tmpIndi.getMetaProperty().allows(tag)) {
                prop = tmpFam.addProperty(tag, "");
            }
            names.put(prop.getPropertyName(), prop);
        }

        // Retrieve list in sorted order and build menu items
        for (String name : names.keySet()) {
            final Property fProp = names.get(name);
            final Fam currentFam = (!tmpIndi.getMetaProperty().allows(fProp.getTag())) ? Utils.getCurrentFamily(indi, familyTree) : null;
            menuItem = new JMenuItem(fProp.getPropertyName(), fProp.getImage());
            menuItem.setToolTipText("<html><table width=200><tr><td>" + fProp.getPropertyInfo() + "</td></tr></table></html");
            menu.add(menuItem);
            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    createEvent(fProp, currentFam);
                    selectEvent(getRowFromIndex(eventIndex));
                    eventDescriptionText.requestFocus();
                }
            });
        }
        // End loop

        // Show menu
        menu.show(button, 3, button.getHeight() - 5);
    }

    /**
     * *************************************************************************
     * Management of errors
     */
    private Validator getValidator() {
        Validator validator = null;
        Collection<? extends AncestrisPlugin> plugins = Lookup.getDefault().lookupAll(AncestrisPlugin.class);
        for (AncestrisPlugin plugin : plugins) {
            if (plugin.getPluginName().equals("ancestris.modules.gedcom.gedcomvalidate")) {
                validator = (Validator) plugin;
                break;
            }
        }
        return validator;
    }

    private boolean passControls() {

        // Refresh errorSet
        if (errorSet == null) {
            errorSet = new ArrayList<ViewContext>();
        }
        errorSet.clear();

        // If validator loaded, use it
        Validator validator = getValidator();
        if (validator != null) {
            List<ViewContext> errors = new ArrayList<ViewContext>();

            // Control INDI
            errors = validator.start(indi);
            if (errors != null) {
                addErrors(errors);
            }

            // Control parents of INDI
            errors = validator.start(indi.getFamilyWhereBiologicalChild());
            if (errors != null) {
                addErrors(errors);
            }

            // Control spouse of INDI
            for (Fam fam : indi.getFamiliesWhereSpouse()) {
                errors = validator.start(fam);
                if (errors != null) {
                    addErrors(errors);
                }
            }

            return errorSet != null && !errorSet.isEmpty();
        }

        // No validator found, used basic default one detecting only negative ages
        for (EventWrapper event : eventSet) {

            // negative age
            if (event.isAgeNegative()) {
                String str = event.eventProperty.getPropertyName();
                if (str.contains(" ")) {
                    str = str.substring(0, str.indexOf(" ")); // only take first word
                }
                String msg = NbBundle.getMessage(getClass(), "MSG_WARNING_Control_01", event.date.getDisplayValue(), str);
                errorSet.add(new ViewContext(event.eventProperty).setText(msg));
            }
        }
        return !errorSet.isEmpty();
    }

    private void addErrors(List<ViewContext> errors) {
        for (ViewContext error : errors) {
            if (error.getEntity().equals(indi)) {
                errorSet.add(error);
            }
        }
    }

    private void showWarningsAndErrors() {
        if (errorSet == null || errorSet.isEmpty()) {
            return;
        }

        ErrorPanel ep = new ErrorPanel(errorSet, getValidator() != null);
        DialogManager.create(
                NbBundle.getMessage(getClass(), "TITL_WARNING_Control", indi.toString()), ep)
                .setMessageType(DialogManager.WARNING_MESSAGE)
                .setOptionType(DialogManager.OK_ONLY_OPTION)
                .show();
        warningButton.setVisible(passControls());
    }

    /**
     * Document listener methods for name, etc information
     */
    public void insertUpdate(DocumentEvent e) {
        if (!isBusyEvent) {
            triggerChange();
        }
    }

    public void removeUpdate(DocumentEvent e) {
        if (!isBusyEvent) {
            triggerChange();
        }
    }

    public void changedUpdate(DocumentEvent e) {
        if (!isBusyEvent) {
            triggerChange();
        }
    }

    private void removeEnterKeyBindingsFromButtons() {
        KeyStroke enterStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

        stickyButton.getInputMap().put(enterStroke, "none");
        warningButton.getInputMap().put(enterStroke, "none");

        indiAddButton.getInputMap().put(enterStroke, "none");
        indiDelButton.getInputMap().put(enterStroke, "none");
        fatherButton.getInputMap().put(enterStroke, "none");
        motherButton.getInputMap().put(enterStroke, "none");
        moreNamesButton.getInputMap().put(enterStroke, "none");
        brothersButton.getInputMap().put(enterStroke, "none");
        sistersButton.getInputMap().put(enterStroke, "none");
        spousesButton.getInputMap().put(enterStroke, "none");
        childrenButton.getInputMap().put(enterStroke, "none");

        addMediaEventButton.getInputMap().put(enterStroke, "none");
        addMediaSourceButton.getInputMap().put(enterStroke, "none");
        addNoteEventButton.getInputMap().put(enterStroke, "none");
        addSourceEventButton.getInputMap().put(enterStroke, "none");
        delMediaEventButton.getInputMap().put(enterStroke, "none");
        delMediaSourceButton.getInputMap().put(enterStroke, "none");
        delNoteEventButton.getInputMap().put(enterStroke, "none");
        delSourceEventButton.getInputMap().put(enterStroke, "none");

        assoEditButton.getInputMap().put(enterStroke, "none");
        assoEditIndi.getInputMap().put(enterStroke, "none");

        eventBaptButton.getInputMap().put(enterStroke, "none");
        eventBirtButton.getInputMap().put(enterStroke, "none");
        eventBuriButton.getInputMap().put(enterStroke, "none");
        eventDeatButton.getInputMap().put(enterStroke, "none");
        eventMarrButton.getInputMap().put(enterStroke, "none");
        eventOccuButton.getInputMap().put(enterStroke, "none");
        eventOthersButton.getInputMap().put(enterStroke, "none");
        eventPlaceButton.getInputMap().put(enterStroke, "none");
        eventRemoveButton.getInputMap().put(enterStroke, "none");
        eventResiButton.getInputMap().put(enterStroke, "none");
        eventRetiButton.getInputMap().put(enterStroke, "none");

        maxNoteEventButton.getInputMap().put(enterStroke, "none");
        maxSourceEventButton.getInputMap().put(enterStroke, "none");
        replaceNoteEventButton.getInputMap().put(enterStroke, "none");
        replaceSourceEventButton.getInputMap().put(enterStroke, "none");
        repoEditButton.getInputMap().put(enterStroke, "none");

    }

    /**
     * ****************** CLASSES ********************************************
     */
    /**
     * Document listener methods for Main pictures
     */
    private class PhotoTitleListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            updatePhotoTitle();
        }

        public void removeUpdate(DocumentEvent e) {
            updatePhotoTitle();
        }

        public void changedUpdate(DocumentEvent e) {
            updatePhotoTitle();
        }
    }

    /**
     * Document listener methods for Event description
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
     * Document listener methods for Event description
     */
    private class EventTimeListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            updateEventTime(e);
        }

        public void removeUpdate(DocumentEvent e) {
            updateEventTime(e);
        }

        public void changedUpdate(DocumentEvent e) {
            updateEventTime(e);
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
     * Document listener methods for Event Places
     */
    public class EventNoteTextListener implements DocumentListener {

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
     * Document listener methods for Event notes
     */
    private class EventPlaceListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            updateEventPlace(e);
        }

        public void removeUpdate(DocumentEvent e) {
            updateEventPlace(e);
        }

        public void changedUpdate(DocumentEvent e) {
            updateEventPlace(e);
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
                if (treeNode == null) {
                    return;
                }
                NodeWrapper node = (NodeWrapper) treeNode.getUserObject();
                if (node.getEntity() != null) {
                    SelectionDispatcher.fireSelection(new Context(node.getEntity()));   // fireselection because we are navigating to another entity
                }
            }
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

    }

    private class IconTextCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                setIcon(((EventLabel) value).getIcon());
                setText(((EventLabel) value).getTableLabel());
            }
            return this;
        }
    }

    private class DoubleCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null && (value instanceof String)) {
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
