package ancestris.modules.editors.standard;

import ancestris.modules.editors.standard.tools.FamilyTreeRenderer;
import ancestris.api.editor.Editor;
import ancestris.gedcom.privacy.standard.Options;
import ancestris.modules.editors.standard.tools.EventTableModel;
import ancestris.modules.editors.standard.tools.EventWrapper;
import ancestris.modules.editors.standard.tools.MediaChooser;
import ancestris.modules.editors.standard.tools.MediaWrapper;
import ancestris.modules.editors.standard.tools.NodeWrapper;
import ancestris.modules.editors.standard.tools.NoteChooser;
import ancestris.modules.editors.standard.tools.NoteWrapper;
import ancestris.modules.editors.standard.tools.Utils;
import static ancestris.modules.editors.standard.tools.Utils.getImageFromFile;
import static ancestris.modules.editors.standard.tools.Utils.getResizedIcon;
import ancestris.util.TimingUtility;
import ancestris.util.swing.DialogManager;
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
import genj.gedcom.PropertyMedia;
import genj.gedcom.PropertySex;
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
import java.util.List;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
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
    private boolean listernersOn = false;
    
    
    private Image PHOTO_MALE = null;
    private Image PHOTO_FEMALE = null;
    private Image PHOTO_UNKNOWN = null;
    
    private Context context;
    private Gedcom gedcom;
    private Indi indi;
    
    private DefaultMutableTreeNode familyTop = null;

    private String SOSA_TAG = "_SOSA";
    private final static String NO_SOSA = NbBundle.getMessage(IndiPanel.class, "noSosa");
    
    // Media
    private List<MediaWrapper> mediaSet = null;
    private int mediaIndex = 0;
    private boolean isBusyMedia = false;
    private List<MediaWrapper> mediaRemovedSet = null;
    
    // Notes
    private List<NoteWrapper> noteSet = null;
    private int noteIndex = 0;
    private boolean isBusyNote = false;
    private List<NoteWrapper> noteRemovedSet = null;
    
    // Events
    private List<EventWrapper> eventSet = null;
    private int eventIndex = 0;
    private boolean isBusyEvent = false;
    private List<EventWrapper> eventRemovedSet = null;
    
    // Event Notes
    private List<NoteWrapper> eventNoteSet = null;
    private int eventNoteIndex = 0;
    private boolean isBusyEventNote = false;
    private List<NoteWrapper> eventNoteRemovedSet = null;
    
    
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
        familyTop = new DefaultMutableTreeNode(new NodeWrapper(NodeWrapper.PARENTS, null));
        initComponents();
        familyTree.setCellRenderer(new FamilyTreeRenderer());
        familyTree.addMouseListener(new FamilyTreeMouseListener());
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
        idLabel = new javax.swing.JLabel();
        sosaLabel = new javax.swing.JLabel();
        firstnamesLabel = new javax.swing.JLabel();
        firstnamesText = new javax.swing.JTextField();
        lastnameLabel = new javax.swing.JLabel();
        lastnameText = new javax.swing.JTextField();
        moreNamesButton = new javax.swing.JButton();
        photos = new javax.swing.JLabel();
        scrollPanePhotos = new javax.swing.JScrollPane();
        textAreaPhotos = new javax.swing.JTextArea();
        scrollPhotos = new javax.swing.JScrollBar();
        addMediaButton = new javax.swing.JButton();
        delMediaButton = new javax.swing.JButton();
        maleRadioButton = new javax.swing.JRadioButton();
        femaleRadioButton = new javax.swing.JRadioButton();
        unknownRadioButton = new javax.swing.JRadioButton();
        privateCheckBox = new javax.swing.JCheckBox();
        fatherButton = new javax.swing.JButton();
        motherButton = new javax.swing.JButton();
        brothersButton = new javax.swing.JButton();
        sistersButton = new javax.swing.JButton();
        spousesButton = new javax.swing.JButton();
        childrenButton = new javax.swing.JButton();
        scrollPaneFamily = new javax.swing.JScrollPane();
        familyTree = new javax.swing.JTree(familyTop);
        scrollPaneNotes = new javax.swing.JScrollPane();
        textAreaNotes = new javax.swing.JTextArea();
        scrollNotes = new javax.swing.JScrollBar();
        addNoteButton = new javax.swing.JButton();
        delNoteButton = new javax.swing.JButton();
        separator = new javax.swing.JSeparator();
        eventBirtButton = new javax.swing.JButton();
        eventBaptButton = new javax.swing.JButton();
        eventOccuButton = new javax.swing.JButton();
        eventMarrButton = new javax.swing.JButton();
        eventRetiButton = new javax.swing.JButton();
        eventDeatButton = new javax.swing.JButton();
        eventBuriButton = new javax.swing.JButton();
        eventResiButton = new javax.swing.JButton();
        eventOthersButton = new javax.swing.JButton();
        eventRemoveButton = new javax.swing.JButton();
        eventTitle = new javax.swing.JLabel();
        eventDescription = new javax.swing.JTextField();
        eventScrollPane = new javax.swing.JScrollPane();
        eventTable = new javax.swing.JTable();
        datelabel = new javax.swing.JLabel();
        eventDate = new genj.edit.beans.DateBean();
        placeLabel = new javax.swing.JLabel();
        eventPlace = new javax.swing.JTextField();
        eventDetailsButton = new javax.swing.JButton();
        eventNoteScrollPane = new javax.swing.JScrollPane();
        eventNote = new javax.swing.JTextArea();
        dayOfWeek = new javax.swing.JLabel();
        ageAtEvent = new javax.swing.JLabel();
        sourceImage = new javax.swing.JLabel();
        sourceScrollPane1 = new javax.swing.JScrollPane();
        sourceText = new javax.swing.JTextArea();
        scrollSource = new javax.swing.JScrollBar();
        addSourceButton = new javax.swing.JButton();
        delSourceButton = new javax.swing.JButton();
        repoText = new javax.swing.JTextField();
        repoEditButton = new javax.swing.JButton();
        modificationLabel = new javax.swing.JLabel();
        assoComboBox = new javax.swing.JComboBox();
        assoEditButton = new javax.swing.JButton();
        delNoteEventButton = new javax.swing.JButton();
        addNoteEventButton = new javax.swing.JButton();
        scrollNotesEvent = new javax.swing.JScrollBar();

        setMaximumSize(new java.awt.Dimension(32767, 500));
        setPreferredSize(new java.awt.Dimension(557, 800));

        title.setFont(new java.awt.Font("DejaVu Sans", 1, 14)); // NOI18N
        title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(title, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.title.text")); // NOI18N

        idLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(idLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.idLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(sosaLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.sosaLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(firstnamesLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.firstnamesLabel.text_1")); // NOI18N

        firstnamesText.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.firstnamesText.text")); // NOI18N
        firstnamesText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firstnamesTextActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lastnameLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.lastnameLabel.text")); // NOI18N

        lastnameText.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.lastnameText.text")); // NOI18N

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

        photos.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(photos, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.photos.text")); // NOI18N
        photos.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.photos.toolTipText")); // NOI18N
        photos.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        photos.setPreferredSize(new java.awt.Dimension(104, 134));
        photos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                photosMouseClicked(evt);
            }
        });

        textAreaPhotos.setColumns(20);
        textAreaPhotos.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
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

        fatherButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/father.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(fatherButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.fatherButton.text")); // NOI18N
        fatherButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.fatherButton.toolTipText")); // NOI18N
        fatherButton.setPreferredSize(new java.awt.Dimension(105, 27));
        fatherButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fatherButtonActionPerformed(evt);
            }
        });

        motherButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/mother.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(motherButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.motherButton.text")); // NOI18N
        motherButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.motherButton.toolTipText")); // NOI18N
        motherButton.setPreferredSize(new java.awt.Dimension(105, 27));
        motherButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                motherButtonActionPerformed(evt);
            }
        });

        brothersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/brother.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(brothersButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.brothersButton.text")); // NOI18N
        brothersButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.brothersButton.toolTipText")); // NOI18N
        brothersButton.setPreferredSize(new java.awt.Dimension(105, 27));
        brothersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                brothersButtonActionPerformed(evt);
            }
        });

        sistersButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/sister.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(sistersButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.sistersButton.text")); // NOI18N
        sistersButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.sistersButton.toolTipText")); // NOI18N
        sistersButton.setPreferredSize(new java.awt.Dimension(105, 27));
        sistersButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sistersButtonActionPerformed(evt);
            }
        });

        spousesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/spouse.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(spousesButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.spousesButton.text")); // NOI18N
        spousesButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.spousesButton.toolTipText")); // NOI18N
        spousesButton.setPreferredSize(new java.awt.Dimension(105, 27));
        spousesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spousesButtonActionPerformed(evt);
            }
        });

        childrenButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/children.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(childrenButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.childrenButton.text")); // NOI18N
        childrenButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.childrenButton.toolTipText")); // NOI18N
        childrenButton.setPreferredSize(new java.awt.Dimension(105, 27));
        childrenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                childrenButtonActionPerformed(evt);
            }
        });

        scrollPaneFamily.setViewportView(familyTree);

        textAreaNotes.setColumns(20);
        textAreaNotes.setLineWrap(true);
        textAreaNotes.setRows(4);
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

        eventTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        eventTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(eventTitle, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventTitle.text")); // NOI18N

        eventDescription.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventDescription.text")); // NOI18N
        eventDescription.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventDescription.toolTipText")); // NOI18N

        eventTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        eventScrollPane.setViewportView(eventTable);

        org.openide.awt.Mnemonics.setLocalizedText(datelabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.datelabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(placeLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.placeLabel.text")); // NOI18N

        eventPlace.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventPlace.text")); // NOI18N

        eventDetailsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/place.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(eventDetailsButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventDetailsButton.text")); // NOI18N
        eventDetailsButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.eventDetailsButton.toolTipText")); // NOI18N

        eventNote.setColumns(20);
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

        org.openide.awt.Mnemonics.setLocalizedText(dayOfWeek, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.dayOfWeek.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(ageAtEvent, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.ageAtEvent.text")); // NOI18N

        sourceImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(sourceImage, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.sourceImage.text")); // NOI18N
        sourceImage.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        sourceText.setColumns(20);
        sourceText.setRows(5);
        sourceText.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.sourceText.text")); // NOI18N
        sourceText.setWrapStyleWord(true);
        sourceScrollPane1.setViewportView(sourceText);

        addSourceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addSourceButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addSourceButton.text")); // NOI18N
        addSourceButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.addSourceButton.toolTipText")); // NOI18N
        addSourceButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        addSourceButton.setIconTextGap(0);
        addSourceButton.setPreferredSize(new java.awt.Dimension(16, 16));

        delSourceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(delSourceButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delSourceButton.text")); // NOI18N
        delSourceButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.delSourceButton.toolTipText")); // NOI18N
        delSourceButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        delSourceButton.setIconTextGap(0);
        delSourceButton.setPreferredSize(new java.awt.Dimension(16, 16));

        repoText.setText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.repoText.text")); // NOI18N

        repoEditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/repository.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(repoEditButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.repoEditButton.text")); // NOI18N
        repoEditButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.repoEditButton.toolTipText")); // NOI18N
        repoEditButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        repoEditButton.setIconTextGap(0);
        repoEditButton.setPreferredSize(new java.awt.Dimension(18, 18));

        modificationLabel.setFont(new java.awt.Font("DejaVu Sans", 0, 10)); // NOI18N
        modificationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        org.openide.awt.Mnemonics.setLocalizedText(modificationLabel, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.modificationLabel.text")); // NOI18N

        assoComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "asso 1", "asso 2", "asso 3", "asso 4" }));

        assoEditButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/association.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(assoEditButton, org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.assoEditButton.text")); // NOI18N
        assoEditButton.setToolTipText(org.openide.util.NbBundle.getMessage(IndiPanel.class, "IndiPanel.assoEditButton.toolTipText")); // NOI18N
        assoEditButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        assoEditButton.setIconTextGap(0);
        assoEditButton.setPreferredSize(new java.awt.Dimension(18, 18));

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(photos, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scrollPanePhotos, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scrollPhotos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(3, 3, 3)
                        .addComponent(addMediaButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addComponent(delMediaButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(firstnamesText, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(fatherButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(idLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(firstnamesLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lastnameLabel)
                                .addGap(18, 18, 18)
                                .addComponent(moreNamesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(sosaLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGap(69, 69, 69)
                                    .addComponent(motherButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(lastnameText)))
                        .addGap(8, 8, 8))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(scrollPaneFamily)
                        .addContainerGap())))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(title, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(116, 116, 116)
                .addComponent(brothersButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sistersButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spousesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(3, 3, 3)
                .addComponent(childrenButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(8, 8, 8))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modificationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(sourceImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(eventScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(eventBirtButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(eventMarrButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(eventRetiButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(eventBaptButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(eventOccuButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(eventResiButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(eventOthersButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(eventDeatButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(eventBuriButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(eventRemoveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(0, 19, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(eventNoteScrollPane, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sourceScrollPane1, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                                    .addComponent(delSourceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(addSourceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(scrollSource, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(delNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(addNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(scrollNotesEvent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(5, 5, 5))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(assoEditButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(1, 1, 1)
                                .addComponent(placeLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(eventPlace)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(eventDetailsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(datelabel)
                                .addGap(10, 10, 10)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(dayOfWeek)
                                        .addGap(18, 18, 18)
                                        .addComponent(ageAtEvent))
                                    .addComponent(eventDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(assoComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(repoText, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(repoEditButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(eventTitle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eventDescription)
                        .addGap(6, 6, 6))))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(116, 116, 116)
                        .addComponent(maleRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(femaleRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(unknownRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(privateCheckBox))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(scrollPaneNotes)
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(delNoteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addNoteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(scrollNotes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(separator)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addComponent(photos, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(title)
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(fatherButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(idLabel)
                            .addComponent(sosaLabel)
                            .addComponent(motherButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(4, 4, 4)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(firstnamesLabel)
                            .addComponent(lastnameLabel)
                            .addComponent(moreNamesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lastnameText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(firstnamesText, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(privateCheckBox)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(brothersButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(spousesButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sistersButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(childrenButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(maleRadioButton)
                                .addComponent(femaleRadioButton)
                                .addComponent(unknownRadioButton)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scrollPanePhotos, javax.swing.GroupLayout.DEFAULT_SIZE, 81, Short.MAX_VALUE)
                        .addGap(3, 3, 3)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(scrollPhotos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(delMediaButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(addMediaButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(8, 8, 8))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scrollPaneFamily, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scrollNotes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(3, 3, 3)
                        .addComponent(addNoteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addComponent(delNoteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrollPaneNotes, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(separator, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(eventBirtButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventBaptButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventOccuButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventDeatButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventBuriButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventTitle)
                    .addComponent(eventDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(eventMarrButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventRetiButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventResiButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventOthersButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventRemoveButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(eventDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(datelabel))
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(eventScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(dayOfWeek)
                            .addComponent(ageAtEvent))
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(placeLabel)
                            .addComponent(eventPlace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(eventDetailsButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(scrollNotesEvent, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addGap(3, 3, 3)
                                .addComponent(addNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)
                                .addComponent(delNoteEventButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(eventNoteScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 83, Short.MAX_VALUE))
                        .addGap(1, 1, 1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(scrollSource, javax.swing.GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
                                .addGap(3, 3, 3)
                                .addComponent(addSourceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)
                                .addComponent(delSourceButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(9, 9, 9))
                            .addComponent(sourceScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                            .addComponent(repoEditButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(repoText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(sourceImage, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(assoComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(assoEditButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(modificationLabel))
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

    private void delNoteEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delNoteEventButtonActionPerformed
        if (eventNoteSet != null && !eventNoteSet.isEmpty() && (eventNoteIndex >= 0) && (eventNoteIndex < eventNoteSet.size())) {
            NoteWrapper note = eventNoteSet.get(eventNoteIndex);
            eventNoteRemovedSet.add(note);
            eventNoteSet.remove(eventNoteIndex);
            eventNoteIndex--;
            if (eventNoteIndex < 0) {
                eventNoteIndex = 0;
            }
            changes.setChanged(true);
        }
        displayEventNote();        
    }//GEN-LAST:event_delNoteEventButtonActionPerformed

    private void addNoteEventButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNoteEventButtonActionPerformed
        if (chooseEventNote(eventNoteSet.size())) {
            displayEventNote();
            eventNote.requestFocus();
        }
    }//GEN-LAST:event_addNoteEventButtonActionPerformed

    private void scrollNotesEventAdjustmentValueChanged(java.awt.event.AdjustmentEvent evt) {//GEN-FIRST:event_scrollNotesEventAdjustmentValueChanged
        if (isBusyEventNote) {
            return;
        }
        int i = scrollNotesEvent.getValue();
        if (eventNoteSet != null && !eventNoteSet.isEmpty() && i >= 0 && i < eventNoteSet.size() && i != eventNoteIndex) {
            eventNoteIndex = scrollNotesEvent.getValue();
            displayEventNote();
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

    private void scrollNotesEventMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollNotesEventMouseWheelMoved
        int notches = evt.getWheelRotation();
        scrollEventNotes(notches);
    }//GEN-LAST:event_scrollNotesEventMouseWheelMoved

    
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
        if (eventNoteSet != null && !eventNoteSet.isEmpty()) {
            int i = eventNoteIndex + notches;
            if (i >= eventNoteSet.size()) {
                i = eventNoteSet.size() - 1;
            }
            if (i < 0) {
                i = 0;
            }
            eventNoteIndex = i;
            displayEventNote();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addMediaButton;
    private javax.swing.JButton addNoteButton;
    private javax.swing.JButton addNoteEventButton;
    private javax.swing.JButton addSourceButton;
    private javax.swing.JLabel ageAtEvent;
    private javax.swing.JComboBox assoComboBox;
    private javax.swing.JButton assoEditButton;
    private javax.swing.JButton brothersButton;
    private javax.swing.ButtonGroup buttonGender;
    private javax.swing.JButton childrenButton;
    private javax.swing.JLabel datelabel;
    private javax.swing.JLabel dayOfWeek;
    private javax.swing.JButton delMediaButton;
    private javax.swing.JButton delNoteButton;
    private javax.swing.JButton delNoteEventButton;
    private javax.swing.JButton delSourceButton;
    private javax.swing.JButton eventBaptButton;
    private javax.swing.JButton eventBirtButton;
    private javax.swing.JButton eventBuriButton;
    private genj.edit.beans.DateBean eventDate;
    private javax.swing.JButton eventDeatButton;
    private javax.swing.JTextField eventDescription;
    private javax.swing.JButton eventDetailsButton;
    private javax.swing.JButton eventMarrButton;
    private javax.swing.JTextArea eventNote;
    private javax.swing.JScrollPane eventNoteScrollPane;
    private javax.swing.JButton eventOccuButton;
    private javax.swing.JButton eventOthersButton;
    private javax.swing.JTextField eventPlace;
    private javax.swing.JButton eventRemoveButton;
    private javax.swing.JButton eventResiButton;
    private javax.swing.JButton eventRetiButton;
    private javax.swing.JScrollPane eventScrollPane;
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
    private javax.swing.JLabel modificationLabel;
    private javax.swing.JButton moreNamesButton;
    private javax.swing.JButton motherButton;
    private javax.swing.JLabel photos;
    private javax.swing.JLabel placeLabel;
    private javax.swing.JCheckBox privateCheckBox;
    private javax.swing.JButton repoEditButton;
    private javax.swing.JTextField repoText;
    private javax.swing.JScrollBar scrollNotes;
    private javax.swing.JScrollBar scrollNotesEvent;
    private javax.swing.JScrollPane scrollPaneFamily;
    private javax.swing.JScrollPane scrollPaneNotes;
    private javax.swing.JScrollPane scrollPanePhotos;
    private javax.swing.JScrollBar scrollPhotos;
    private javax.swing.JScrollBar scrollSource;
    private javax.swing.JSeparator separator;
    private javax.swing.JButton sistersButton;
    private javax.swing.JLabel sosaLabel;
    private javax.swing.JLabel sourceImage;
    private javax.swing.JScrollPane sourceScrollPane1;
    private javax.swing.JTextArea sourceText;
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
    protected void setContextImpl(Context context) {
        LOG.finer(TimingUtility.geInstance().getTime() + ": setContextImpl().start");
        
        this.context = context;
        Entity entity = context.getEntity();
        if (entity != null && (entity instanceof Indi)) {
            this.indi = (Indi) entity;
            this.gedcom = indi.getGedcom();

            loadData();

            if (!listernersOn) {
                addListeners();
                listernersOn = true;
            }
        }

        LOG.finer(TimingUtility.geInstance().getTime() + ": setContextImpl().finish");
    }

    
    @Override
    public void commit() throws GedcomException {
        saveData();
    }


    
    
    
    
    
    /**
     * Document listener methods
     */
    public void insertUpdate(DocumentEvent e) {
        changes.setChanged(true);
    }

    public void removeUpdate(DocumentEvent e) {
        changes.setChanged(true);
    }

    public void changedUpdate(DocumentEvent e) {
        changes.setChanged(true);
    }

    
    

    
    
    
    
    
    
    
    
    
    
    
    private void loadData() {
        String str = "";
        int i = 0;
        boolean privateTagFound = false;
        
        // Title
        title.setText(indi.getFirstName() + " " + indi.getLastName());

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
        
        
        //
        //
        //
        //
        modificationLabel.setText(NbBundle.getMessage(IndiPanel.class, "IndiPanel.modificationLabel.text") + " : " + indi.getLastChange().getDisplayValue());
        //
        //.......................................
        
    }

    private void addListeners() {
        firstnamesText.getDocument().addDocumentListener(this);
        lastnameText.getDocument().addDocumentListener(this);
        textAreaPhotos.getDocument().addDocumentListener(new PhotoTitleListener());
        textAreaNotes.getDocument().addDocumentListener(new NoteTextListener());
        eventDescription.getDocument().addDocumentListener(this);
        eventDate.addChangeListener(changes);
        eventPlace.getDocument().addDocumentListener(this);
        eventNote.getDocument().addDocumentListener(new EventNoteTextListener());
    }

    
    
    private void saveData() {
        indi.setName(firstnamesText.getText(), lastnameText.getText());
        //
        indi.setSex(getSex());
        //
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
        //
        putMedia();
        //
        putNotes();
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

    
    private void putMedia() {
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

        photos.setIcon(getResizedIcon(imageIcon, 104, 134));   // preferred size of photo label but getPreferredSize() does not return those values...
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
        
        Registry registry = Registry.get(getClass());
        JFileChooser jfc = new JFileChooser();
        jfc.setDialogTitle(NbBundle.getMessage(getClass(), "FileChooserTitle"));
        FileNameExtensionFilter imageFileFilter = Utils.getImageFilter();
        FileNameExtensionFilter videoFileFilter = Utils.getVideoFilter();
        FileNameExtensionFilter soundFileFilter = Utils.getSoundFilter();
        jfc.addChoosableFileFilter(imageFileFilter);
        jfc.addChoosableFileFilter(videoFileFilter);
        jfc.addChoosableFileFilter(soundFileFilter);
        jfc.setAcceptAllFileFilterUsed(true);
        jfc.setSelectedFile(exists ? mediaSet.get(index).getFile() : new File(registry.get("mediaPath", ".")));
        int ret = jfc.showDialog(jfc, NbBundle.getMessage(getClass(), "FileChooserOKButton"));
        if (ret == JFileChooser.APPROVE_OPTION) {
            File f = jfc.getSelectedFile();
            registry.put("mediaPath", f);
            if (f != null) {
                if (exists) {
                    mediaSet.get(index).setFile(f);
                    mediaIndex = index;
                } else {
                    MediaWrapper media = new MediaWrapper(f);  
                    mediaSet.add(media);
                    mediaIndex = mediaSet.size()-1;
                }
                changes.setChanged(true);
                b = true;
            }
        } else {
            textAreaPhotos.requestFocus();
        }
        return b;
    }

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

    
    private void putNotes() {
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
        String[] INDI_TAGS = { "BIRT","CHR","DEAT","BURI","CREM","ADOP","BAPM","BARM","BASM","BLES","CHRA","CONF","FCOM",   // attributes with type and no description
            "ORDN","NATU","EMIG","IMMI","CENS","PROB","WILL","GRAD","RETI","EVEN",                                          // attributes with type and no description, including EVEN
            "CAST","DSCR","EDUC","IDNO","NATI","NCHI","NMR","OCCU","PROP","RELI","RESI","SSN","TITL","FACT" };              // attributes with type and a description
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
        String[] FAM_TAGS = { "ANUL","CENS","DIV","DIVF","ENGA","MARR","MARB","MARC","MARL","MARS","EVEN", "RESI" };        // attributes with type and no description, except EVEN
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

    
    private void putEvents() {
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
        sorter.setComparator(0, new Comparator<JLabel>() {
            public int compare(JLabel l1, JLabel l2) {
                return l1.getText().compareTo(l2.getText());
            }
        });
        sorter.setComparator(2, new Comparator<String>() {
            public int compare(String s1, String s2) {
                try {
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
        eventTable.getRowSorter().toggleSortOrder(2);
        
        int maxWidth = 0;
        for (EventWrapper event : eventSet) {
            maxWidth = Math.max(maxWidth, getFontMetrics(getFont()).stringWidth(event.eventLabel.getText()));
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
                }
            }
        });
        if (eventTable.getRowCount() > 0) {
            eventTable.setRowSelectionInterval(0, 0);
        }
    }

    
    private void displayEvent() {
        isBusyEvent = true;
        if (eventSet != null && !eventSet.isEmpty() && (eventIndex >= 0) && (eventIndex < eventSet.size())) {        
            EventWrapper event = eventSet.get(eventIndex);
            
            // Title
            eventTitle.setText(event.title);
            eventDescription.setText(event.description);
            
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

            // Notes
            if (eventNoteSet != null) {
                eventNoteSet.clear();
                eventNoteSet = null;
            }
            if (eventNoteRemovedSet != null) {
                eventNoteRemovedSet.clear();
                eventNoteRemovedSet = null;
            }
            eventNoteSet = getEventNotes(event.eventProperty);
            eventNoteRemovedSet = new ArrayList<NoteWrapper>();
            scrollNotesEvent.setMinimum(0);
            scrollNotesEvent.setBlockIncrement(1);
            scrollNotesEvent.setUnitIncrement(1);
            eventNoteIndex = 0;
            displayEventNote();
        
            
            // Sources - Media
            
            // Sources - Text
            
            // Sources - Repositories
            
            
            
            
        }
        isBusyEvent = false;
    }

    

    private List<NoteWrapper> getEventNotes(Property event) {
        List<NoteWrapper> ret = new ArrayList<NoteWrapper>();
                
        // Look for only general notes directly attached to indi
        Property[] noteProps = event.getProperties("NOTE");
        for (Property prop : noteProps) {
            if (prop != null && !prop.getDisplayValue().trim().isEmpty()) {
                ret.add(new NoteWrapper(prop));
            }
        }
        return ret;
    }

    
    private void putEventNotes() {
//        //noteSet
//        for (NoteWrapper note : eventNoteSet) {
//            note.update(indi);
//        }
//        for (NoteWrapper note : noteRemovedSet) {
//            note.remove(indi);
//        }
    }

    
    private void displayEventNote() {
        isBusyEventNote = true;
        if (eventNoteSet != null && !eventNoteSet.isEmpty() && (eventNoteIndex >= 0) && (eventNoteIndex < eventNoteSet.size())) {        
            setEventNote(eventNoteSet.get(eventNoteIndex));
        } else {
            setEventNote(null);
        }
        isBusyEventNote = false;
    }
    
    private void setEventNote(NoteWrapper note) {
        
        String localText = "";
        
        if (note != null) {
            localText = note.getText();
        }
        
        // Text
        eventNote.setText(localText);
        eventNote.setCaretPosition(0);
        
        // Update scroll
        scrollNotesEvent.setValues(eventNoteIndex, 1, 0, eventNoteSet.size());
        scrollNotesEvent.setToolTipText(getScrollEventNotesLabel());
    }


    private String getScrollEventNotesLabel() {
        return String.valueOf(eventNoteSet.size() > 0 ? eventNoteIndex + 1 : eventNoteIndex) + "/" + String.valueOf(eventNoteSet.size());
    }

    
    private void updateEventNoteText(int index) {
        if (isBusyEventNote) {
            return;
        }
        String noteText = eventNote.getText();
        if ((eventNoteSet != null) && (!eventNoteSet.isEmpty()) && (index >= 0) && (index < eventNoteSet.size())) {
            eventNoteSet.get(index).setText(noteText);
            eventNoteIndex = index;
        } else {
            NoteWrapper note = new NoteWrapper(noteText);
            eventNoteSet.add(note);
            eventNoteIndex = eventNoteSet.size()-1;
        }
        changes.setChanged(true);
    }

    
    private boolean chooseEventNote(int index) {
        boolean b = false;
        boolean exists = (eventNoteSet != null) && (!eventNoteSet.isEmpty()) && (index >= 0) && (index < eventNoteSet.size());
        
        JButton noteButton = new JButton(NbBundle.getMessage(getClass(), "Button_ChooseNote"));
        JButton cancelButton = new JButton(NbBundle.getMessage(getClass(), "Button_Cancel"));
        Object[] options = new Object[] { noteButton, cancelButton };
        NoteChooser noteChooser = new NoteChooser(gedcom, exists ? eventNoteSet.get(index) : null, noteButton, cancelButton);
        int size = noteChooser.getNbNotes();
        Object o = DialogManager.create(NbBundle.getMessage(getClass(), "TITL_ChooseNoteTitle", size), noteChooser).setMessageType(DialogManager.PLAIN_MESSAGE).setOptions(options).show();
        if (o == noteButton) {
            String noteText = noteChooser.getSelectedText();
            if (noteChooser.isSelectedEntityNote()) {
                Note entity = (Note) noteChooser.getSelectedEntity();
                if (exists) {
                    eventNoteSet.get(index).setTargetEntity(entity);
                    eventNoteSet.get(index).setText(noteText);
                    eventNoteIndex = index;
                } else {
                    NoteWrapper note = new NoteWrapper(entity);
                    note.setText(noteText);
                    eventNoteSet.add(note);
                    eventNoteIndex = eventNoteSet.size() - 1;
                }
                changes.setChanged(true);
                b = true;
            } else {
                if (exists) {
                    eventNoteSet.get(index).setText(noteText);
                    eventNoteIndex = index;
                } else {
                    NoteWrapper note = new NoteWrapper(noteText);
                    eventNoteSet.add(note);
                    eventNoteIndex = eventNoteSet.size() - 1;
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

    private class EventNoteTextListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            updateEventNoteText(eventNoteIndex);
        }

        public void removeUpdate(DocumentEvent e) {
            updateEventNoteText(eventNoteIndex);
        }

        public void changedUpdate(DocumentEvent e) {
            updateEventNoteText(eventNoteIndex);
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
            setIcon(((JLabel)value).getIcon());
            setText(((JLabel)value).getText());
        return this;
        }
    }
    
    
    private class DoubleCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
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
            return this;
        }
    }
    
}
