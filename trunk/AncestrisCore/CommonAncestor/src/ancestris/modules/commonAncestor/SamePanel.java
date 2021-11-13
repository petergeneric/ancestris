/*
 * SamePanel.java
 *
 * Created on 26 juin 2011, 18:06:04
 */
package ancestris.modules.commonAncestor;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.modules.commonAncestor.quicksearch.module.AbstractQuickSearchComboBar;
import ancestris.modules.commonAncestor.quicksearch.module.QuickSearchComboBar;
import ancestris.modules.commonAncestor.quicksearch.module.QuickSearchPopup;
import ancestris.util.swing.FileChooserBuilder;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.util.Registry;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 *
 * <br>Parameters stored in module configuration :
 * <br>FileTypeName : pdf,svg or png
 * <br>
 * <br> Parameters stored in each gedcom configuration <br>
 * <br> PREFERRED_ID.individu1
 * <br> PREFERRED_ID.individu2
 *
 * @author michel
 */
public class SamePanel extends javax.swing.JPanel implements AncestorListener {

    private static final Logger LOG = Logger.getLogger("ancestris.app");

    private static final String PREFERRED_ID = "SamePanel";
    protected static final String AUTOPREVIEW = "autopreview";
    protected static final String SEPARATED_WINDOW = "separatedWindow";
    protected static final String DISPLAY_ID = "displayIDs";
    protected static final String PRIVINFO = "privinfo";
    protected static final String HUSBWIFE = "husbwife";
    protected static final String DEFAULT_FILE_TYPE_NAME = "FileTypeName";
    private static final String QUICKSEARCH_CATEGORY_INDIVIDU_1 = "Individu1";
    private static final String QUICKSEARCH_CATEGORY_INDIVIDU_2 = "Individu2";
    private Context context;
    private Indi currentIndi;
    private Indi individu1;
    private Indi individu2;
    private final CommonAncestorTree commonAncestorTree = new CommonAncestorTree();
    private final DefaultListModel<Indi> ancestorListModel = new DefaultListModel<>();
    protected Registry registry;
    Component owner;
    protected PreviewTopComponent previewTopComponent = null;

    /**
     * Creates new form SamePanel
     */
    public SamePanel() {

    }

    public void init(Context context) {
        if (this.context != null) {
            // context is already initilized
            return;
        }
        if (context == null) {
            // invalid new context
            return;
        }

        this.context = context;
        registry = new Registry(Registry.get(SamePanel.class), getClass().getName());
        initComponents();
        // j'affecte un modele à la liste
        jListAncestors.setModel(ancestorListModel);

        // Provider individu 1
        QuickSearchProvider searchProvider1 = new QuickSearchProvider();
        searchProvider1.setSamePanel(this);
        //String categoryDisplayName1 = org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.individu1.category.displayName");
        String categoryDisplayName1 = ""; // je n'affiche pas le nom de la categorie pour gagner de la place a l'ecran
        String commandPrefix1 = "";
        int maxResult = 10;
        int allMaxResult = 10000;
        AbstractQuickSearchComboBar quickSearchIndividu1 = new QuickSearchComboBar(
                QUICKSEARCH_CATEGORY_INDIVIDU_1, categoryDisplayName1, searchProvider1,
                commandPrefix1, null,
                maxResult, allMaxResult,
                QuickSearchPopup.WidthMode.HORIZONTAL_SCOLLBAR);
        jPanelSearch1.setLayout(new java.awt.BorderLayout());
        jPanelSearch1.add(quickSearchIndividu1, BorderLayout.CENTER);

        // Provider individu 2
        QuickSearchProvider searchProvider2 = new QuickSearchProvider();
        searchProvider2.setSamePanel(this);
        //String categoryDisplayName2 = org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.individu2.category.displayName");
        String categoryDisplayName2 = ""; // je n'affiche pas le nom de la categorie pour gagner de la place a l'ecran
        String commandPrefix2 = "";
        AbstractQuickSearchComboBar quickSearchIndividu2 = new QuickSearchComboBar(
                QUICKSEARCH_CATEGORY_INDIVIDU_2, categoryDisplayName2, searchProvider2,
                commandPrefix2, null,
                maxResult, allMaxResult,
                QuickSearchPopup.WidthMode.HORIZONTAL_SCOLLBAR); //KeyStroke.getKeyStroke("F7")
        jPanelSearch2.setLayout(new java.awt.BorderLayout());
        jPanelSearch2.add(quickSearchIndividu2, BorderLayout.CENTER);

        // flags
        jShowToggleButton.setSelected(false);
        jCheckBoxSeparatedWindow.setSelected(registry.get(SEPARATED_WINDOW, false));
        jCheckBoxDisplayedId.setSelected(registry.get(DISPLAY_ID, false));
        jCheckBoxRecentEvent.setSelected(registry.get(PRIVINFO, true));
        jComboBoxHusbandOrWife.setModel(new DefaultComboBoxModel<>(new String[]{
            org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.husband"),
            org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.wife")}));
        jComboBoxHusbandOrWife.setSelectedIndex(registry.get(HUSBWIFE, 0));
                
        // j'intialise la combobox avec la liste des noms des types de fichiers
        jComboBoxFileType.setModel(new DefaultComboBoxModel<>(commonAncestorTree.getFileTypeNames().toArray(new String[commonAncestorTree.getFileTypeNames().size()])));
        // j'affiche le type de fichier enregistrée pendant la session précédente
        if (jComboBoxFileType.getModel().getSize() > 0) {
            // je selectionne la valeur par defaut enregistrée pendant la session précédente
            // si la valeur par defaut n'existe pas , je selectionne le premier element de la liste
            jComboBoxFileType.setSelectedItem(registry.get(DEFAULT_FILE_TYPE_NAME, jComboBoxFileType.getModel().getElementAt(0)));
        } else {
            // s'il n'y a aucun type de fichier disponible,je desactive l'export fichier
            jComboBoxFileType.setEnabled(false);
        }

        // je met à jour l'individu courant 
        updateCurrentIndividu(context.getEntity());

        // j'affiche les deux individus de la session precedente (relatif au fichier GEDCOM)
        if (context.getGedcom() != null) {
            String id1 = context.getGedcom().getRegistry().get(PREFERRED_ID + ".individu1", "");
            if (!id1.equalsIgnoreCase("")) {
                individu1 = (Indi) context.getGedcom().getEntity("INDI", id1);
                if (individu1 != null) {
                    jLabelValueIndividu1.setText(individu1.toString());
                }
            }
            String id2 = context.getGedcom().getRegistry().get(PREFERRED_ID + ".individu2", "");
            if (!id2.equalsIgnoreCase("")) {
                individu2 = (Indi) context.getGedcom().getEntity("INDI", id2);
                if (individu2 != null) {
                    jLabelValueIndividu2.setText(individu2.toString());
                }
            }
        } else {
            individu1 = null;
            individu2 = null;
            jLabelValueIndividu1.setText("");
            jLabelValueIndividu2.setText("");
        }
        // je recherche et j'affiche l'ancetre commun
        findCommonAncestors();
        // refresh panel display
        revalidate();

        // 
        jCheckBoxAutoPreview.setSelected(registry.get(AUTOPREVIEW, false));
        if (jCheckBoxAutoPreview.isSelected()) {
            togglePreview(true);
        }


    }

    public void togglePreview(boolean set) {
        jShowToggleButton.setSelected(set);
        if (jShowToggleButton.isSelected()) {
            openPreview();
        } else {
            closePreview();
        }
    }
    
    void openPreview() {
        int i = jListAncestors.getSelectedIndex();
        if (i == -1) {
            return;
        }
        if (context != null) {

            if (previewTopComponent == null) {
                previewTopComponent = PreviewTopComponent.createInstance(this);
                if (previewTopComponent == null) {
                    return;
                }
                previewTopComponent.addAncestorListener(this);
                jCheckBoxSeparatedWindow.setSelected(previewTopComponent.getSeparatedWindowFlag());
                jCheckBoxSeparatedWindow.setEnabled(true);
            } else {
                // bring previewTopComponent to front
                previewTopComponent.requestActive();
            }

            // fill previewTopComponent with ancestor tree
            int husband_or_wife_first = jComboBoxHusbandOrWife.getSelectedIndex();
            Indi ancestor = null;
            if (jListAncestors.getSelectedIndex() >= 0) {
                ancestor = ancestorListModel.getElementAt(jListAncestors.getSelectedIndex());
            }
            boolean displayRecentYears = jCheckBoxRecentEvent.isSelected();
            boolean displayId = jCheckBoxDisplayedId.isSelected();
            commonAncestorTree.createPreview(individu1, individu2, ancestor, displayId, displayRecentYears, husband_or_wife_first, previewTopComponent);
        }
    }

    /**
     * close also related previewTopComponent when CommonAncestorTopComponent is
     * removed
     */
    protected void closePreview() {
        if (previewTopComponent != null) {
            previewTopComponent.close();
            previewTopComponent = null;
        }
    }

    /**
     * this method is called by previewTopComponent when user close the
     * component
     */
    protected void onClosePreview() {
        previewTopComponent = null;
        jShowToggleButton.setSelected(false);
    }

    public Context getContext() {
        return context;
    }

    /*
     * Mets à jour l'individu courant 
     */
    public void updateCurrentIndividu(Entity entity) {
        if (entity != null) {

            if (entity instanceof Fam) {
                if (((Fam) entity).getNoOfSpouses() == 0) {
                    return;
                }
                Fam focusFam = ((Fam) entity);
                Indi husb = focusFam.getHusband();
                Indi wife = focusFam.getWife();
                if (husb == null && wife == null) {
                    currentIndi = null;
                } else if (wife == null) {
                    currentIndi = husb;
                } else if (husb == null) {
                    currentIndi = wife;
                } else {
                    currentIndi = husb;
                    jbuttonCurrentIndi1.setToolTipText(NbBundle.getMessage(getClass(), "SamePanel.selectAsFirst", husb.getName()));
                    jbuttonCurrentIndi2.setToolTipText(NbBundle.getMessage(getClass(), "SamePanel.selectAsSecond", wife.getName()));
                    jbuttonCurrentIndi1.setEnabled(true);
                    jbuttonCurrentIndi2.setEnabled(true);
                    setIndividu1(husb);
                    setIndividu2(wife);
                    return;
                }
            } else if (entity instanceof Indi) {
                currentIndi = (Indi) entity;
            }
        }

        if (currentIndi != null) {
            jbuttonCurrentIndi1.setToolTipText(NbBundle.getMessage(getClass(), "SamePanel.selectAsFirst", currentIndi.getName()));
            jbuttonCurrentIndi2.setToolTipText(NbBundle.getMessage(getClass(), "SamePanel.selectAsSecond", currentIndi.getName()));
            jbuttonCurrentIndi1.setEnabled(true);
            jbuttonCurrentIndi2.setEnabled(true);
        } else {
            jbuttonCurrentIndi1.setToolTipText(null);
            jbuttonCurrentIndi2.setToolTipText(null);
            jbuttonCurrentIndi1.setEnabled(false);
            jbuttonCurrentIndi2.setEnabled(false);
        }
    }

    /**
     * save ancestor tree
     */
    private void saveFile() {
        if (ancestorListModel.size() > 0) {
            Indi ancestor = ancestorListModel.getElementAt(jListAncestors.getSelectedIndex());

            int husband_or_wife_first = jComboBoxHusbandOrWife.getSelectedIndex();
            String fileTypeName = (String) jComboBoxFileType.getModel().getSelectedItem();
            String extension = commonAncestorTree.getOutputList().get(fileTypeName).getFileExtension();
            boolean displayRecentYears = jCheckBoxRecentEvent.isSelected();
            boolean displayId = jCheckBoxDisplayedId.isSelected();
            String defaultFileName = "Ancetre commun - "
                    + individu1.getFirstName() + " " + individu1.getLastName() + " - "
                    + individu2.getFirstName() + " " + individu2.getLastName();

            // ask filename
            File outpuFile = getFileFromUser(NbBundle.getMessage(getClass(), "TITL_CommonAncestorsResult"), AbstractAncestrisAction.TXT_OK, defaultFileName, true, extension);
            if (outpuFile != null) {
                // Add appropriate file extension
                String suffix = "." + extension;
                if (!outpuFile.getPath().endsWith(suffix)) {
                    outpuFile = new File(outpuFile.getPath() + suffix);
                }
                commonAncestorTree.createCommonTree(individu1, individu2, ancestor, outpuFile, displayId, displayRecentYears, husband_or_wife_first, fileTypeName);
            }
            registry.put(DEFAULT_FILE_TYPE_NAME, fileTypeName);
        }
    }

    /**
     * Mémorise l'individu 1
     *
     * @param indi individual
     */
    public void setIndividu1(Indi indi) {
        jLabelValueIndividu1.setText(indi.toString());
        individu1 = indi;
        findCommonAncestors();
        if (individu1 != null) {
            //registry.put("individu2", individu2.getId());   
            context.getGedcom().getRegistry().put(PREFERRED_ID + ".individu1", individu1.getId());
        }
        if (jCheckBoxAutoPreview.isSelected()) {
            togglePreview(true);
        }
    }

    /**
     * Mémorise l'individu 2
     *
     * @param indi individual
     */
    public void setIndividu2(Indi indi) {
        jLabelValueIndividu2.setText(indi.toString());
        individu2 = indi;
        findCommonAncestors();
        if (individu2 != null) {
            //registry.put("individu2", individu2.getId());   
            context.getGedcom().getRegistry().put(PREFERRED_ID + ".individu2", individu2.getId());
        }
        if (jCheckBoxAutoPreview.isSelected()) {
            togglePreview(true);
        }
    }

    /**
     * recherche et affiche les ancetres communs
     */
    private void findCommonAncestors() {
        // Clear model and list
        ancestorListModel.clear();
        jLabelAncestorList.setText(NbBundle.getMessage(SamePanel.class, "SamePanel.jLabelAncestorList.text",
                NbBundle.getMessage(SamePanel.class, "SamePanel.jLabelAncestorList.searching")));

        // Search common ancestors
        Set<Indi> ancestorList = commonAncestorTree.findCommonAncestors(individu1, individu2);

        // Display in result field as a sorted list (do not sort before as the set has previously removed duplicates)
        List<Indi> sortedAncestorList = new ArrayList<>(ancestorList);
        Collections.sort(sortedAncestorList, (new Indi()).getDisplayComparator());
        for (Indi ancestor : sortedAncestorList) {
            ancestorListModel.addElement(ancestor);
        }
        // update scrollbars
        if (ancestorListModel.size() > 0) {
            // je recupere la hauteur de l'ensemble des lignes de la liste
            int cellHeight = jListAncestors.getCellBounds(0, ancestorListModel.getSize() - 1).height;
            // je change la hauteur preferree de la JList
            jListAncestors.setPreferredSize(new Dimension(jListAncestors.getPreferredSize().width, cellHeight));
        } else {
            // je met la hauteur preferree de la liste à zero
            jListAncestors.setPreferredSize(new Dimension(jListAncestors.getPreferredSize().width, 0));
        }
        
        // select default ancestor (first element) 
        if (ancestorListModel.size() > 0) {
            jListAncestors.setSelectedIndex(0);
            jButtonSaveFile.setEnabled(true);
        } else {
            jButtonSaveFile.setEnabled(false);
        }

        jLabelAncestorList.setText(NbBundle.getMessage(SamePanel.class, "SamePanel.jLabelAncestorList.text", ""));

    }

    /**
     * user choose output file name
     *
     * @param title file dialog title
     * @param buttonLabel
     * @param defaultFileName
     * @param askForOverwrite whether to confirm overwriting files
     * @param extension extension of files to display
     * @return
     */
    public File getFileFromUser(String title, String buttonLabel, String defaultFileName, boolean askForOverwrite, String extension) {

        File file = new FileChooserBuilder(SamePanel.class.getCanonicalName())
                .setFilesOnly(true)
                .setDefaultBadgeProvider()
                .setTitle(title)
                .setApproveText(buttonLabel)
                .setDefaultExtension(extension)
                .setFileFilter(extension != null ? new FileExtensionFilter(extension) : null)
                .setSelectedFile(new File(defaultFileName))
                .setFileHiding(true)
                .showSaveDialog();

        return file;
    }

    ///////////////////////////////////////////////////////////////////////////
    // AncestorListener implementation
    ///////////////////////////////////////////////////////////////////////////
    /**
     * update jCheckBoxSeparatedWindow when previewTopComponent dock mode change
     *
     * @param ae
     */
    @Override
    public void ancestorAdded(AncestorEvent ae) {
        if (previewTopComponent != null) {
            Mode dockModeTemp = WindowManager.getDefault().findMode(previewTopComponent);

            if (dockModeTemp == null || dockModeTemp.getName().startsWith("anonymous")) {
                jCheckBoxSeparatedWindow.setSelected(true);
            } else {
                jCheckBoxSeparatedWindow.setSelected(false);
            }
        }
    }

    @Override
    public void ancestorRemoved(AncestorEvent ae) {
        //System.out.println("ancestorRemoved" + ae.paramString());
    }

    @Override
    public void ancestorMoved(AncestorEvent ae) {
        //System.out.println("ancestorMoved" + ae.paramString());
    }

    ///////////////////////////////////////////////////////////////////////////
    // private class FileExtensionFilter
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Filters files using a specified extension. used by getFileFromUser()
     */
    private class FileExtensionFilter extends FileFilter {

        private final String extension;

        public FileExtensionFilter(String extension) {
            this.extension = extension.toLowerCase();
        }

        /**
         * Returns true if file name has the right extension.
         */
        @Override
        public boolean accept(File f) {
            if (f == null) {
                return false;
            }
            if (f.isDirectory()) {
                return true;
            }
            return f.getName().toLowerCase().endsWith("." + extension);
        }

        @Override
        public String getDescription() {
            return extension.toUpperCase() + " files";
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

        jPanel3 = new javax.swing.JPanel();
        jButtonHelp = new javax.swing.JButton();
        jLabelIndividu1 = new javax.swing.JLabel();
        jLabelValueIndividu1 = new javax.swing.JLabel();
        jPanelSearch1 = new javax.swing.JPanel();
        jbuttonCurrentIndi1 = new javax.swing.JButton();
        jLabelIndividu2 = new javax.swing.JLabel();
        jLabelValueIndividu2 = new javax.swing.JLabel();
        jbuttonCurrentIndi2 = new javax.swing.JButton();
        jPanelSearch2 = new javax.swing.JPanel();
        jLabelAncestorList = new javax.swing.JLabel();
        jScrollPaneAncestortList = new javax.swing.JScrollPane();
        jListAncestors = new javax.swing.JList<Indi>();
        jPanelExportFile = new javax.swing.JPanel();
        jComboBoxFileType = new javax.swing.JComboBox<String>();
        jButtonSaveFile = new javax.swing.JButton();
        jPanelOption = new javax.swing.JPanel();
        jCheckBoxAutoPreview = new javax.swing.JCheckBox();
        jCheckBoxSeparatedWindow = new javax.swing.JCheckBox();
        jCheckBoxDisplayedId = new javax.swing.JCheckBox();
        jCheckBoxRecentEvent = new javax.swing.JCheckBox();
        jComboBoxHusbandOrWife = new javax.swing.JComboBox<>();
        jShowToggleButton = new javax.swing.JToggleButton();

        setPreferredSize(new java.awt.Dimension(250, 630));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jPanel3.border.title"))); // NOI18N
        jPanel3.setPreferredSize(new java.awt.Dimension(232, 323));

        jButtonHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/commonAncestor/information.png"))); // NOI18N
        jButtonHelp.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jButtonHelp.text")); // NOI18N
        jButtonHelp.setToolTipText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jButtonHelp.toolTipText")); // NOI18N
        jButtonHelp.setBorderPainted(false);
        jButtonHelp.setPreferredSize(new java.awt.Dimension(26, 24));
        jButtonHelp.setRequestFocusEnabled(false);
        jButtonHelp.setRolloverEnabled(false);
        jButtonHelp.setVerifyInputWhenFocusTarget(false);
        jButtonHelp.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jButtonHelp.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        jButtonHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHelpActionPerformed(evt);
            }
        });

        jLabelIndividu1.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jLabelIndividu1.text")); // NOI18N
        jLabelIndividu1.setFocusable(false);
        jLabelIndividu1.setRequestFocusEnabled(false);
        jLabelIndividu1.setVerifyInputWhenFocusTarget(false);

        jLabelValueIndividu1.setFont(jLabelValueIndividu1.getFont().deriveFont(jLabelValueIndividu1.getFont().getStyle() | java.awt.Font.BOLD));
        jLabelValueIndividu1.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jLabelValueIndividu1.text")); // NOI18N

        jPanelSearch1.setBackground(new java.awt.Color(255, 255, 255));
        jPanelSearch1.setPreferredSize(new java.awt.Dimension(50, 20));

        javax.swing.GroupLayout jPanelSearch1Layout = new javax.swing.GroupLayout(jPanelSearch1);
        jPanelSearch1.setLayout(jPanelSearch1Layout);
        jPanelSearch1Layout.setHorizontalGroup(
            jPanelSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanelSearch1Layout.setVerticalGroup(
            jPanelSearch1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        jbuttonCurrentIndi1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/commonAncestor/CurrentIndi.png"))); // NOI18N
        jbuttonCurrentIndi1.setBorder(null);
        jbuttonCurrentIndi1.setBorderPainted(false);
        jbuttonCurrentIndi1.setPreferredSize(new java.awt.Dimension(24, 24));
        jbuttonCurrentIndi1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbuttonCurrentIndi1ActionPerformed(evt);
            }
        });

        jLabelIndividu2.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jLabelIndividu2.text")); // NOI18N
        jLabelIndividu2.setMaximumSize(new java.awt.Dimension(42, 14));
        jLabelIndividu2.setMinimumSize(new java.awt.Dimension(42, 14));
        jLabelIndividu2.setPreferredSize(new java.awt.Dimension(42, 14));
        jLabelIndividu2.setRequestFocusEnabled(false);
        jLabelIndividu2.setVerifyInputWhenFocusTarget(false);

        jLabelValueIndividu2.setFont(jLabelValueIndividu2.getFont().deriveFont(jLabelValueIndividu2.getFont().getStyle() | java.awt.Font.BOLD));
        jLabelValueIndividu2.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jLabelValueIndividu2.text")); // NOI18N

        jbuttonCurrentIndi2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/commonAncestor/CurrentIndi.png"))); // NOI18N
        jbuttonCurrentIndi2.setBorder(null);
        jbuttonCurrentIndi2.setBorderPainted(false);
        jbuttonCurrentIndi2.setPreferredSize(new java.awt.Dimension(24, 24));
        jbuttonCurrentIndi2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbuttonCurrentIndi2ActionPerformed(evt);
            }
        });

        jPanelSearch2.setBackground(new java.awt.Color(255, 255, 255));
        jPanelSearch2.setMinimumSize(new java.awt.Dimension(50, 20));
        jPanelSearch2.setPreferredSize(new java.awt.Dimension(50, 20));

        javax.swing.GroupLayout jPanelSearch2Layout = new javax.swing.GroupLayout(jPanelSearch2);
        jPanelSearch2.setLayout(jPanelSearch2Layout);
        jPanelSearch2Layout.setHorizontalGroup(
            jPanelSearch2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanelSearch2Layout.setVerticalGroup(
            jPanelSearch2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        jLabelAncestorList.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jLabelAncestorList.text")); // NOI18N

        jScrollPaneAncestortList.setMinimumSize(new java.awt.Dimension(100, 82));
        jScrollPaneAncestortList.setPreferredSize(new java.awt.Dimension(100, 84));

        jListAncestors.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListAncestors.setPreferredSize(new java.awt.Dimension(80, 80));
        jListAncestors.setVisibleRowCount(5);
        jListAncestors.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListAncestorsValueChanged(evt);
            }
        });
        jScrollPaneAncestortList.setViewportView(jListAncestors);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelIndividu1, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelIndividu2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonHelp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jScrollPaneAncestortList, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelAncestorList)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jbuttonCurrentIndi2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelSearch2, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jbuttonCurrentIndi1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanelSearch1, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE))
                    .addComponent(jLabelValueIndividu1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelValueIndividu2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabelIndividu1))
                    .addComponent(jButtonHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelValueIndividu1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanelSearch1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jbuttonCurrentIndi1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabelIndividu2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelValueIndividu2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanelSearch2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jbuttonCurrentIndi2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabelAncestorList)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPaneAncestortList, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabelIndividu1.getAccessibleContext().setAccessibleName("null");
        jLabelIndividu2.getAccessibleContext().setAccessibleName("");

        jPanelExportFile.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jPanelExportFile.border.title"))); // NOI18N
        jPanelExportFile.setPreferredSize(new java.awt.Dimension(110, 107));

        jComboBoxFileType.setMaximumRowCount(3);
        jComboBoxFileType.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "PDF", "PNG", "SVG" }));
        jComboBoxFileType.setMinimumSize(new java.awt.Dimension(50, 18));
        jComboBoxFileType.setName("jComboBoxFileType"); // NOI18N
        jComboBoxFileType.setPreferredSize(new java.awt.Dimension(30, 24));

        jButtonSaveFile.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jButtonSaveFile.text")); // NOI18N
        jButtonSaveFile.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonSaveFile.setMaximumSize(null);
        jButtonSaveFile.setMinimumSize(new java.awt.Dimension(70, 28));
        jButtonSaveFile.setPreferredSize(new java.awt.Dimension(60, 28));
        jButtonSaveFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelExportFileLayout = new javax.swing.GroupLayout(jPanelExportFile);
        jPanelExportFile.setLayout(jPanelExportFileLayout);
        jPanelExportFileLayout.setHorizontalGroup(
            jPanelExportFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelExportFileLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelExportFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBoxFileType, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButtonSaveFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelExportFileLayout.setVerticalGroup(
            jPanelExportFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelExportFileLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBoxFileType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSaveFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );

        jButtonSaveFile.getAccessibleContext().setAccessibleName("");

        jPanelOption.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jPanelOption.border.title"))); // NOI18N

        jCheckBoxAutoPreview.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jButtonPreview.text")); // NOI18N
        jCheckBoxAutoPreview.setPreferredSize(new java.awt.Dimension(50, 20));
        jCheckBoxAutoPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAutoPreviewActionPerformed(evt);
            }
        });

        jCheckBoxSeparatedWindow.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jCheckBoxSeparatedWindow.text")); // NOI18N
        jCheckBoxSeparatedWindow.setInheritsPopupMenu(true);
        jCheckBoxSeparatedWindow.setPreferredSize(new java.awt.Dimension(125, 20));
        jCheckBoxSeparatedWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSeparatedWindowActionPerformed(evt);
            }
        });

        jCheckBoxDisplayedId.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jCheckBoxDisplayedId.text")); // NOI18N
        jCheckBoxDisplayedId.setPreferredSize(new java.awt.Dimension(70, 20));
        jCheckBoxDisplayedId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDisplayedIdActionPerformed(evt);
            }
        });

        jCheckBoxRecentEvent.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jCheckBoxRecentEvent.text")); // NOI18N
        jCheckBoxRecentEvent.setPreferredSize(new java.awt.Dimension(70, 20));
        jCheckBoxRecentEvent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxRecentEventActionPerformed(evt);
            }
        });

        jComboBoxHusbandOrWife.setMaximumRowCount(2);
        jComboBoxHusbandOrWife.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "husband first", "wife first" }));
        jComboBoxHusbandOrWife.setPreferredSize(new java.awt.Dimension(70, 24));
        jComboBoxHusbandOrWife.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxHusbandOrWifeItemStateChanged(evt);
            }
        });

        jShowToggleButton.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jShowToggleButton.text")); // NOI18N
        jShowToggleButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jShowToggleButtonStateChanged(evt);
            }
        });
        jShowToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jShowToggleButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelOptionLayout = new javax.swing.GroupLayout(jPanelOption);
        jPanelOption.setLayout(jPanelOptionLayout);
        jPanelOptionLayout.setHorizontalGroup(
            jPanelOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOptionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxSeparatedWindow, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                    .addComponent(jCheckBoxDisplayedId, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jCheckBoxRecentEvent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jComboBoxHusbandOrWife, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanelOptionLayout.createSequentialGroup()
                        .addComponent(jCheckBoxAutoPreview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jShowToggleButton)))
                .addContainerGap())
        );
        jPanelOptionLayout.setVerticalGroup(
            jPanelOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOptionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxAutoPreview, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jShowToggleButton))
                .addGap(3, 3, 3)
                .addComponent(jCheckBoxSeparatedWindow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxDisplayedId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxRecentEvent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBoxHusbandOrWife, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jCheckBoxAutoPreview.getAccessibleContext().setAccessibleName("null");
        jCheckBoxSeparatedWindow.getAccessibleContext().setAccessibleName("");
        jCheckBoxDisplayedId.getAccessibleContext().setAccessibleName("");
        jCheckBoxRecentEvent.getAccessibleContext().setAccessibleName("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanelOption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanelExportFile, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanelExportFile, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanelExportFile.getAccessibleContext().setAccessibleName("");
    }// </editor-fold>//GEN-END:initComponents


  private void jButtonSaveFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveFileActionPerformed
      saveFile();
  }//GEN-LAST:event_jButtonSaveFileActionPerformed

  private void jCheckBoxAutoPreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAutoPreviewActionPerformed
      registry.put(AUTOPREVIEW, jCheckBoxAutoPreview.isSelected());
      if (jCheckBoxAutoPreview.isSelected() && jShowToggleButton.isSelected()) {  // if preview is visible and autopreview is set to on, update preview
          openPreview();
      }      
}//GEN-LAST:event_jCheckBoxAutoPreviewActionPerformed

  private void jCheckBoxDisplayedIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDisplayedIdActionPerformed
      registry.put(DISPLAY_ID, jCheckBoxDisplayedId.isSelected());
      if (jCheckBoxAutoPreview.isSelected()) {
          togglePreview(true);
      }
  }//GEN-LAST:event_jCheckBoxDisplayedIdActionPerformed

  private void jCheckBoxRecentEventActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxRecentEventActionPerformed
      registry.put(PRIVINFO, jCheckBoxRecentEvent.isSelected());
      if (jCheckBoxAutoPreview.isSelected()) {
          togglePreview(true);
      }
  }//GEN-LAST:event_jCheckBoxRecentEventActionPerformed

  private void jComboBoxHusbandOrWifeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxHusbandOrWifeItemStateChanged
      registry.put(HUSBWIFE, jComboBoxHusbandOrWife.getSelectedIndex());
      if (jCheckBoxAutoPreview.isSelected()) {
          togglePreview(true);
      }
  }//GEN-LAST:event_jComboBoxHusbandOrWifeItemStateChanged

  private void jCheckBoxSeparatedWindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSeparatedWindowActionPerformed
      registry.put(SEPARATED_WINDOW, jCheckBoxSeparatedWindow.isSelected());
      if (previewTopComponent != null && jCheckBoxAutoPreview.isSelected()) {
          previewTopComponent.setSeparatedWindowFlag(jCheckBoxSeparatedWindow.isSelected());
      }
  }//GEN-LAST:event_jCheckBoxSeparatedWindowActionPerformed

    private void jButtonHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHelpActionPerformed
        String id = NbBundle.getMessage(SamePanel.class, "SamePanel.helpPage");
        try {
            Desktop.getDesktop().browse(new URI(id));
        } catch (URISyntaxException | IOException ex) {
            LOG.log(Level.FINE, "Unable to open File", ex);
        }
    }//GEN-LAST:event_jButtonHelpActionPerformed

    private void jListAncestorsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListAncestorsValueChanged
        if (jCheckBoxAutoPreview.isSelected() && evt.getValueIsAdjusting() == false) {
            togglePreview(true);
        }
    }//GEN-LAST:event_jListAncestorsValueChanged

    private void jbuttonCurrentIndi2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbuttonCurrentIndi2ActionPerformed
        setIndividu2(currentIndi);
    }//GEN-LAST:event_jbuttonCurrentIndi2ActionPerformed

    private void jbuttonCurrentIndi1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbuttonCurrentIndi1ActionPerformed
        setIndividu1(currentIndi);
    }//GEN-LAST:event_jbuttonCurrentIndi1ActionPerformed

    private void jShowToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jShowToggleButtonActionPerformed
        if (jShowToggleButton.isSelected()) {
            openPreview();
        } else {
            closePreview();
        }
    }//GEN-LAST:event_jShowToggleButtonActionPerformed

    private void jShowToggleButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jShowToggleButtonStateChanged
        if (jShowToggleButton.isSelected()) {
            jShowToggleButton.setText(NbBundle.getMessage(this.getClass(), "SamePanel.jShowToggleButton.textalt"));
        } else {
            jShowToggleButton.setText(NbBundle.getMessage(this.getClass(), "SamePanel.jShowToggleButton.text"));
        }
    }//GEN-LAST:event_jShowToggleButtonStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonHelp;
    private javax.swing.JButton jButtonSaveFile;
    private javax.swing.JCheckBox jCheckBoxAutoPreview;
    private javax.swing.JCheckBox jCheckBoxDisplayedId;
    private javax.swing.JCheckBox jCheckBoxRecentEvent;
    private javax.swing.JCheckBox jCheckBoxSeparatedWindow;
    private javax.swing.JComboBox<String> jComboBoxFileType;
    private javax.swing.JComboBox<String> jComboBoxHusbandOrWife;
    private javax.swing.JLabel jLabelAncestorList;
    private javax.swing.JLabel jLabelIndividu1;
    private javax.swing.JLabel jLabelIndividu2;
    private javax.swing.JLabel jLabelValueIndividu1;
    private javax.swing.JLabel jLabelValueIndividu2;
    private javax.swing.JList<Indi> jListAncestors;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelExportFile;
    private javax.swing.JPanel jPanelOption;
    private javax.swing.JPanel jPanelSearch1;
    private javax.swing.JPanel jPanelSearch2;
    private javax.swing.JScrollPane jScrollPaneAncestortList;
    private javax.swing.JToggleButton jShowToggleButton;
    private javax.swing.JButton jbuttonCurrentIndi1;
    private javax.swing.JButton jbuttonCurrentIndi2;
    // End of variables declaration//GEN-END:variables
}
