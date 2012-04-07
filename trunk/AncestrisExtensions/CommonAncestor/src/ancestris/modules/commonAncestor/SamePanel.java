/*
 * SamePanel.java
 *
 * Created on 26 juin 2011, 18:06:04
 */
package ancestris.modules.commonAncestor;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.util.EnvironmentChecker;
import genj.util.Registry;
import genj.util.swing.Action2;
import genj.util.swing.DialogHelper;
import ancestris.modules.commonAncestor.quicksearch.module.AbstractQuickSearchComboBar;
import ancestris.modules.commonAncestor.quicksearch.module.QuickSearchComboBar;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.filechooser.FileFilter;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 *
 * <br>Parameters stored in module configuration :
 * <br>FileTypeName  : pdf,svg or png
 * <br> 
 * <br> Parameters stored in each gedcom configuration <br>
 * <br>  PREFERRED_ID.individu1 
 * <br>  PREFERRED_ID.individu2
 * 
 * @author michel
 */
public class SamePanel extends javax.swing.JPanel implements AncestorListener {

    private static final String PREFERRED_ID = "SamePanel";
    protected static final String DEFAULT_FILE_TYPE_NAME = "FileTypeName";
    private static final String QUICKSEARCH_CATEGORY_INDIVIDU_1 = "Individu1";
    private static final String QUICKSEARCH_CATEGORY_INDIVIDU_2 = "Individu2";
    private Context context;
    private Indi currentIndi;
    private Indi individu1;
    private Indi individu2;
    private CommonAncestorTree commonAncestorTree = new CommonAncestorTree();
    private DefaultListModel ancestorListModel = new DefaultListModel();
    protected Registry registry;
    Component owner;
    protected PreviewTopComponent previewTopComponent = null;


    /** Creates new form SamePanel */
    public SamePanel( ) {
        
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
        jCheckBoxAutoPreview.setSelected(false);

        // Provider individu 1
        QuickSearchProvider searchProvider1 = new QuickSearchProvider();
        searchProvider1.setSamePanel(this);
        String categoryDisplayName1 = org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.individu1.category.displayName");
        AbstractQuickSearchComboBar quickSearchIndividu1 = new QuickSearchComboBar(QUICKSEARCH_CATEGORY_INDIVIDU_1, categoryDisplayName1, searchProvider1, null, null);
        jPanelSearch1.add(quickSearchIndividu1, BorderLayout.CENTER);

        // Provider individu 2
        QuickSearchProvider searchProvider2 = new QuickSearchProvider();
        searchProvider2.setSamePanel(this);
        String categoryDisplayName2 = org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.individu2.category.displayName");
        AbstractQuickSearchComboBar quickSearchIndividu2 = new QuickSearchComboBar(QUICKSEARCH_CATEGORY_INDIVIDU_2, categoryDisplayName2, searchProvider2, null, null); //KeyStroke.getKeyStroke("F7")
        jPanelSearch2.add(quickSearchIndividu2, BorderLayout.CENTER);

        // j'intialise la combobox de l'option "marie/femme au centre"
        jComboBoxHusbandOrWife.setModel(new javax.swing.DefaultComboBoxModel(new String[]{
                    org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.husband"),
                    org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.wife")}));

        // j'intialise la combobox avec la liste des noms des types de fichiers
        jComboBoxFileType.setModel(new javax.swing.DefaultComboBoxModel(commonAncestorTree.getFileTypeNames().toArray()));
        // j'affiche le type de fichier enregistrée pendant la session précédente
        if (jComboBoxFileType.getModel().getSize() > 0) {
            // je selectionne la valeur par defaut enregistrée pendant la session précédente
            // si la valeur par defaut n'existe pas , je selectionne le premier element de la liste
            jComboBoxFileType.setSelectedItem(registry.get(DEFAULT_FILE_TYPE_NAME, jComboBoxFileType.getModel().getElementAt(0).toString()));
        } else {
            // s'il n'y a aucun type de fichier disponible,je desactive l'export fichier
            jComboBoxFileType.setEnabled(false);
        }
        
        // je met à jour l'individu courant 
        updateCurrentIndividu(context.getEntity());
        
        // j'affiche les deux individus de la session precedente (relatif au fichier GEDCOM)
        if (context != null && context.getGedcom() != null) {
            String id1 = context.getGedcom().getRegistry().get(PREFERRED_ID + ".individu1", "");
            if (!id1.equalsIgnoreCase("")) {
                individu1 = (Indi) context.getGedcom().getEntity("INDI", id1);
                if (individu1 != null) {
                    jTextFieldIndividu1.setText(individu1.toString());
                }
            }
            String id2 = context.getGedcom().getRegistry().get(PREFERRED_ID + ".individu2", "");
            if (!id2.equalsIgnoreCase("")) {
                individu2 = (Indi) context.getGedcom().getEntity("INDI", id2);
                if (individu2 != null) {
                    jTextFieldIndividu2.setText(individu2.toString());
                }
            }
        } else {
            individu1 = null;
            individu2 = null;
            jTextFieldIndividu1.setText("");
            jTextFieldIndividu2.setText("");
        }
        // je recherche et j'affiche l'ancetre commun
        findCommonAncestors();
        // refresh panel display
        revalidate();
        
        // 
        jCheckBoxAutoPreview.setSelected(true);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                openPreview();
            }
        });

    }

    /**
     * close also related previewTopComponent when CommonAncestorTopComponent is removed
     */
    protected void closePreview() {
        if (previewTopComponent != null) {
            previewTopComponent.close();
            previewTopComponent = null;
        }
    }

   /**
     * this method is called by previewTopComponent when user close the component
     */
    protected void onClosePreview() {
        previewTopComponent = null;
        jCheckBoxAutoPreview.setSelected(false);
        jCheckBoxSeparatedWindow.setEnabled(false);
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
                currentIndi = focusFam.getHusband();
                if (currentIndi == null) {
                    currentIndi = focusFam.getWife();
                }
            } else if (entity instanceof Indi) {
                currentIndi = (Indi) entity;
            }
        }

        if (currentIndi != null) {
            jbuttonCurrentIndi1.setToolTipText(currentIndi.getName());
            jbuttonCurrentIndi2.setToolTipText(currentIndi.getName());
            jbuttonCurrentIndi1.setEnabled(true);
            jbuttonCurrentIndi2.setEnabled(true);
        } else {
            jbuttonCurrentIndi1.setToolTipText(null);
            jbuttonCurrentIndi2.setToolTipText(null);
            jbuttonCurrentIndi1.setEnabled(false);
            jbuttonCurrentIndi2.setEnabled(false);
        }        
    }

    void openPreview() {
        int i = jListAncestors.getSelectedIndex();
        if (context != null) {

            if (previewTopComponent == null) {
                previewTopComponent = PreviewTopComponent.createInstance(this);
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
                ancestor = (Indi) ancestorListModel.getElementAt(jListAncestors.getSelectedIndex());
            }
            boolean displayRecentYears =  jCheckBoxRecentEvent.isSelected();
            boolean displayId = jCheckBoxDisplayedId.isSelected();
            commonAncestorTree.createPreview(individu1, individu2, ancestor, displayId, displayRecentYears, husband_or_wife_first, previewTopComponent);
        }
    }

    /**
     * save ancestor tree
     */
    private void saveFile() {
        if (ancestorListModel.size() > 0) {
            Indi ancestor = (Indi) ancestorListModel.getElementAt(jListAncestors.getSelectedIndex());

            int husband_or_wife_first = jComboBoxHusbandOrWife.getSelectedIndex();
            String fileTypeName = (String) jComboBoxFileType.getModel().getSelectedItem();
            String extension = commonAncestorTree.getOutputList().get(fileTypeName).getFileExtension();
            boolean displayRecentYears = jCheckBoxRecentEvent.isSelected();
            boolean displayId = jCheckBoxDisplayedId.isSelected();
            String defaultFileName = "Ancetre commun - "
                    + individu1.getFirstName() + " " + individu1.getLastName() + " - "
                    + individu2.getFirstName() + " " + individu2.getLastName();
            
            // ask filename
            File outpuFile = getFileFromUser("titre", Action2.TXT_OK, defaultFileName, true, extension);
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
     * @param evt 
     */
    public void setIndividu1(Indi indi) {
        jTextFieldIndividu1.setText(indi.toString());
        individu1 = indi;
        findCommonAncestors();
        if (individu1 != null) {
            //registry.put("individu2", individu2.getId());   
            context.getGedcom().getRegistry().put(PREFERRED_ID + ".individu1", individu1.getId());
        }
        if (jCheckBoxAutoPreview.isSelected()) {
            openPreview();
        }
    }

    /**
     * Mémorise l'individu 2
     * @param evt 
     */
    public void setIndividu2(Indi indi) {
        jTextFieldIndividu2.setText(indi.toString());
        individu2 = indi;
        findCommonAncestors();
        if (individu2 != null) {
            //registry.put("individu2", individu2.getId());   
            context.getGedcom().getRegistry().put(PREFERRED_ID + ".individu2", individu2.getId());
        }
        if (jCheckBoxAutoPreview.isSelected()) {
            openPreview();
        }
    }

    /**
     * recherche et affiche les ancetres communs
     */
    private void findCommonAncestors() {
        Set<Indi> ancestorList = commonAncestorTree.findCommonAncestors(individu1, individu2);
        ancestorListModel.clear();
        // copy ancestor list into listeModel
        for (Indi ancestor : ancestorList) {
            ancestorListModel.addElement(ancestor);
        }
        jListAncestors.setModel(ancestorListModel);

        // select default ancestor (first element) 
        if (ancestorListModel.size() > 0) {
            jListAncestors.setSelectedIndex(0);
            jButtonSaveFile.setEnabled(true);
        } else {
            jButtonSaveFile.setEnabled(false);
        }
    }

    /**
     * user choose output file name
     *
     * @param title  file dialog title
     * @param button  file dialog OK button text
     * @param askForOverwrite  whether to confirm overwriting files
     * @param extension  extension of files to display
     */
    public File getFileFromUser(String title, String buttonLabel, String defaultFileName, boolean askForOverwrite, String extension) {

        // show filechooser
        String dir = registry.get("file", EnvironmentChecker.getProperty("user.home", ".", "looking for report dir to let the user choose from"));
        JFileChooser chooser = new JFileChooser(dir);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle(title);
        chooser.setSelectedFile(new File(defaultFileName));
        if (extension != null) {
            chooser.setFileFilter(new FileExtensionFilter(extension));
        }

        int rc = chooser.showDialog(this, buttonLabel);

        // check result
        File result = chooser.getSelectedFile();
        if (rc != JFileChooser.APPROVE_OPTION || result == null) {
            return null;
        }

        // choose an existing file?
        if (result.exists() && askForOverwrite) {
            rc = DialogHelper.openDialog(title, DialogHelper.WARNING_MESSAGE, NbBundle.getMessage(SamePanel.class, "SamePanel.message.fileExits"), Action2.yesNo(), this);
            if (rc != 0) {
                return null;
            }
        }

        // keep it as new default directory
        registry.put("file", result.getParent().toString());
        return result;
    }

    ///////////////////////////////////////////////////////////////////////////
    // AncestorListener implementation
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * update jCheckBoxSeparatedWindow when previewTopComponent dock mode change 
     * @param ae 
     */
    public void ancestorAdded(AncestorEvent ae) {
        if (previewTopComponent != null) {
            Mode dockModeTemp = WindowManager.getDefault().findMode(previewTopComponent);

            if (dockModeTemp == null || (dockModeTemp != null && dockModeTemp.getName().startsWith("anonymous"))) {
                jCheckBoxSeparatedWindow.setSelected(true);
            } else {
                jCheckBoxSeparatedWindow.setSelected(false);
            }
        }
    }

    public void ancestorRemoved(AncestorEvent ae) {
        //System.out.println("ancestorRemoved" + ae.paramString());
    }

    public void ancestorMoved(AncestorEvent ae) {
        //System.out.println("ancestorMoved" + ae.paramString());
    }

    ///////////////////////////////////////////////////////////////////////////
    // private class FileExtensionFilter
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Filters files using a specified extension.
     * used by getFileFromUser()
     */
    private class FileExtensionFilter extends FileFilter {

        private String extension;

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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jButtonPreview = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabelIndividu1 = new javax.swing.JLabel();
        jTextFieldIndividu1 = new javax.swing.JTextField();
        jbuttonCurrentIndi1 = new javax.swing.JButton();
        jPanelSearch1 = new javax.swing.JPanel();
        jLabelIndividu2 = new javax.swing.JLabel();
        jTextFieldIndividu2 = new javax.swing.JTextField();
        jbuttonCurrentIndi2 = new javax.swing.JButton();
        jPanelSearch2 = new javax.swing.JPanel();
        jLabelAncestorList = new javax.swing.JLabel();
        jScrollPaneAncestortList = new javax.swing.JScrollPane();
        jListAncestors = new javax.swing.JList();
        jPanelPreview = new javax.swing.JPanel();
        jCheckBoxAutoPreview = new javax.swing.JCheckBox();
        jCheckBoxSeparatedWindow = new javax.swing.JCheckBox();
        jPanelFile = new javax.swing.JPanel();
        jPanelOptions = new javax.swing.JPanel();
        jCheckBoxDisplayedId = new javax.swing.JCheckBox();
        jCheckBoxRecentEvent = new javax.swing.JCheckBox();
        jComboBoxHusbandOrWife = new javax.swing.JComboBox();
        jPanelExportFile = new javax.swing.JPanel();
        jComboBoxFileType = new javax.swing.JComboBox();
        jButtonSaveFile = new javax.swing.JButton();

        jButtonPreview.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jButtonPreview.text")); // NOI18N
        jButtonPreview.setPreferredSize(new java.awt.Dimension(71, 32));
        jButtonPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPreviewActionPerformed(evt);
            }
        });
        jButtonPreview.getAccessibleContext().setAccessibleName(null);

        setMinimumSize(new java.awt.Dimension(260, 390));
        setPreferredSize(new java.awt.Dimension(260, 390));
        setLayout(new java.awt.GridBagLayout());

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jPanel3.border.title"))); // NOI18N
        jPanel3.setMinimumSize(new java.awt.Dimension(60, 230));
        jPanel3.setPreferredSize(new java.awt.Dimension(124, 230));
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jLabelIndividu1.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jLabelIndividu1.text")); // NOI18N
        jLabelIndividu1.setFocusable(false);
        jLabelIndividu1.setRequestFocusEnabled(false);
        jLabelIndividu1.setVerifyInputWhenFocusTarget(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabelIndividu1, gridBagConstraints);
        jLabelIndividu1.getAccessibleContext().setAccessibleName(null);

        jTextFieldIndividu1.setEditable(false);
        jTextFieldIndividu1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jTextFieldIndividu1.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jTextFieldIndividu1.setBorder(null);
        jTextFieldIndividu1.setMinimumSize(new java.awt.Dimension(50, 20));
        jTextFieldIndividu1.setPreferredSize(new java.awt.Dimension(50, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jTextFieldIndividu1, gridBagConstraints);

        jbuttonCurrentIndi1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/commonAncestor/CurrentIndi.png"))); // NOI18N
        jbuttonCurrentIndi1.setBorder(null);
        jbuttonCurrentIndi1.setBorderPainted(false);
        jbuttonCurrentIndi1.setMargin(new java.awt.Insets(0, 2, 0, 2));
        jbuttonCurrentIndi1.setMaximumSize(new java.awt.Dimension(20, 20));
        jbuttonCurrentIndi1.setMinimumSize(new java.awt.Dimension(20, 20));
        jbuttonCurrentIndi1.setPreferredSize(new java.awt.Dimension(20, 20));
        jbuttonCurrentIndi1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbuttonCurrentIndi1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel3.add(jbuttonCurrentIndi1, gridBagConstraints);

        jPanelSearch1.setBackground(new java.awt.Color(255, 255, 255));
        jPanelSearch1.setMinimumSize(new java.awt.Dimension(50, 20));
        jPanelSearch1.setPreferredSize(new java.awt.Dimension(50, 20));
        jPanelSearch1.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jPanelSearch1, gridBagConstraints);

        jLabelIndividu2.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jLabelIndividu2.text")); // NOI18N
        jLabelIndividu2.setMaximumSize(new java.awt.Dimension(42, 14));
        jLabelIndividu2.setMinimumSize(new java.awt.Dimension(42, 14));
        jLabelIndividu2.setPreferredSize(new java.awt.Dimension(42, 14));
        jLabelIndividu2.setRequestFocusEnabled(false);
        jLabelIndividu2.setVerifyInputWhenFocusTarget(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(jLabelIndividu2, gridBagConstraints);
        jLabelIndividu2.getAccessibleContext().setAccessibleName("");

        jTextFieldIndividu2.setEditable(false);
        jTextFieldIndividu2.setFont(new java.awt.Font("Tahoma", 1, 11));
        jTextFieldIndividu2.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        jTextFieldIndividu2.setBorder(null);
        jTextFieldIndividu2.setMinimumSize(new java.awt.Dimension(50, 20));
        jTextFieldIndividu2.setPreferredSize(new java.awt.Dimension(50, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        jPanel3.add(jTextFieldIndividu2, gridBagConstraints);

        jbuttonCurrentIndi2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/commonAncestor/CurrentIndi.png"))); // NOI18N
        jbuttonCurrentIndi2.setBorder(null);
        jbuttonCurrentIndi2.setBorderPainted(false);
        jbuttonCurrentIndi2.setMargin(new java.awt.Insets(0, 2, 0, 2));
        jbuttonCurrentIndi2.setMaximumSize(new java.awt.Dimension(20, 20));
        jbuttonCurrentIndi2.setMinimumSize(new java.awt.Dimension(20, 20));
        jbuttonCurrentIndi2.setPreferredSize(new java.awt.Dimension(20, 20));
        jbuttonCurrentIndi2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbuttonCurrentIndi2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        jPanel3.add(jbuttonCurrentIndi2, gridBagConstraints);

        jPanelSearch2.setBackground(new java.awt.Color(255, 255, 255));
        jPanelSearch2.setMinimumSize(new java.awt.Dimension(50, 20));
        jPanelSearch2.setPreferredSize(new java.awt.Dimension(50, 20));
        jPanelSearch2.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jPanelSearch2, gridBagConstraints);

        jLabelAncestorList.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jLabelAncestorList.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        jPanel3.add(jLabelAncestorList, gridBagConstraints);

        jScrollPaneAncestortList.setMinimumSize(new java.awt.Dimension(100, 82));
        jScrollPaneAncestortList.setPreferredSize(new java.awt.Dimension(100, 84));

        jListAncestors.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jListAncestors.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListAncestors.setMaximumSize(new java.awt.Dimension(2147483647, 2147483647));
        jListAncestors.setMinimumSize(new java.awt.Dimension(80, 80));
        jListAncestors.setPreferredSize(new java.awt.Dimension(80, 80));
        jListAncestors.setVisibleRowCount(5);
        jListAncestors.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListAncestorsValueChanged(evt);
            }
        });
        jScrollPaneAncestortList.setViewportView(jListAncestors);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(jScrollPaneAncestortList, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 0);
        add(jPanel3, gridBagConstraints);

        jPanelPreview.setLayout(new java.awt.GridLayout(1, 1, 20, 0));

        jCheckBoxAutoPreview.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jButtonPreview.text")); // NOI18N
        jCheckBoxAutoPreview.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jCheckBoxAutoPreview.setMaximumSize(new java.awt.Dimension(64000, 20));
        jCheckBoxAutoPreview.setMinimumSize(new java.awt.Dimension(61, 20));
        jCheckBoxAutoPreview.setPreferredSize(new java.awt.Dimension(50, 20));
        jCheckBoxAutoPreview.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAutoPreviewActionPerformed(evt);
            }
        });
        jPanelPreview.add(jCheckBoxAutoPreview);
        jCheckBoxAutoPreview.getAccessibleContext().setAccessibleName(null);

        jCheckBoxSeparatedWindow.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jCheckBoxSeparatedWindow.text")); // NOI18N
        jCheckBoxSeparatedWindow.setBorder(null);
        jCheckBoxSeparatedWindow.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jCheckBoxSeparatedWindow.setIconTextGap(2);
        jCheckBoxSeparatedWindow.setInheritsPopupMenu(true);
        jCheckBoxSeparatedWindow.setMargin(new java.awt.Insets(2, 2, 2, 0));
        jCheckBoxSeparatedWindow.setMaximumSize(new java.awt.Dimension(64000, 11000));
        jCheckBoxSeparatedWindow.setMinimumSize(new java.awt.Dimension(100, 20));
        jCheckBoxSeparatedWindow.setPreferredSize(new java.awt.Dimension(125, 20));
        jCheckBoxSeparatedWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxSeparatedWindowActionPerformed(evt);
            }
        });
        jPanelPreview.add(jCheckBoxSeparatedWindow);
        jCheckBoxSeparatedWindow.getAccessibleContext().setAccessibleName("");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 10);
        add(jPanelPreview, gridBagConstraints);

        jPanelFile.setMaximumSize(new java.awt.Dimension(240, 104));
        jPanelFile.setMinimumSize(new java.awt.Dimension(240, 104));
        jPanelFile.setPreferredSize(new java.awt.Dimension(240, 104));
        jPanelFile.setLayout(new java.awt.GridLayout(1, 2));

        jPanelOptions.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jPanelOptions.border.title"))); // NOI18N
        jPanelOptions.setEnabled(false);
        jPanelOptions.setMinimumSize(new java.awt.Dimension(110, 107));
        jPanelOptions.setPreferredSize(new java.awt.Dimension(110, 107));

        jCheckBoxDisplayedId.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jCheckBoxDisplayedId.text")); // NOI18N
        jCheckBoxDisplayedId.setPreferredSize(new java.awt.Dimension(70, 23));
        jCheckBoxDisplayedId.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDisplayedIdActionPerformed(evt);
            }
        });

        jCheckBoxRecentEvent.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jCheckBoxRecentEvent.text")); // NOI18N
        jCheckBoxRecentEvent.setPreferredSize(new java.awt.Dimension(70, 23));
        jCheckBoxRecentEvent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxRecentEventActionPerformed(evt);
            }
        });

        jComboBoxHusbandOrWife.setMaximumRowCount(2);
        jComboBoxHusbandOrWife.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "husband first", "wife first" }));
        jComboBoxHusbandOrWife.setPreferredSize(new java.awt.Dimension(70, 20));
        jComboBoxHusbandOrWife.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxHusbandOrWifeItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanelOptionsLayout = new javax.swing.GroupLayout(jPanelOptions);
        jPanelOptions.setLayout(jPanelOptionsLayout);
        jPanelOptionsLayout.setHorizontalGroup(
            jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jCheckBoxDisplayedId, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
            .addComponent(jCheckBoxRecentEvent, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBoxHusbandOrWife, 0, 98, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelOptionsLayout.setVerticalGroup(
            jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOptionsLayout.createSequentialGroup()
                .addComponent(jCheckBoxDisplayedId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxRecentEvent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jComboBoxHusbandOrWife, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(64, 64, 64))
        );

        jCheckBoxDisplayedId.getAccessibleContext().setAccessibleName("");
        jCheckBoxRecentEvent.getAccessibleContext().setAccessibleName("");

        jPanelFile.add(jPanelOptions);
        jPanelOptions.getAccessibleContext().setAccessibleName(null);

        jPanelExportFile.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jPanelExportFile.border.title"))); // NOI18N
        jPanelExportFile.setMaximumSize(new java.awt.Dimension(110, 107));
        jPanelExportFile.setMinimumSize(new java.awt.Dimension(110, 107));
        jPanelExportFile.setPreferredSize(new java.awt.Dimension(110, 107));

        jComboBoxFileType.setMaximumRowCount(3);
        jComboBoxFileType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "PDF", "PNG", "SVG" }));
        jComboBoxFileType.setMinimumSize(new java.awt.Dimension(50, 18));
        jComboBoxFileType.setName("jComboBoxFileType"); // NOI18N
        jComboBoxFileType.setPreferredSize(new java.awt.Dimension(50, 20));

        jButtonSaveFile.setText(org.openide.util.NbBundle.getMessage(SamePanel.class, "SamePanel.jButtonSaveFile.text")); // NOI18N
        jButtonSaveFile.setMaximumSize(null);
        jButtonSaveFile.setMinimumSize(new java.awt.Dimension(50, 24));
        jButtonSaveFile.setPreferredSize(new java.awt.Dimension(50, 32));
        jButtonSaveFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSaveFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelExportFileLayout = new javax.swing.GroupLayout(jPanelExportFile);
        jPanelExportFile.setLayout(jPanelExportFileLayout);
        jPanelExportFileLayout.setHorizontalGroup(
            jPanelExportFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelExportFileLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelExportFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonSaveFile, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 98, Short.MAX_VALUE)
                    .addComponent(jComboBoxFileType, javax.swing.GroupLayout.Alignment.LEADING, 0, 98, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelExportFileLayout.setVerticalGroup(
            jPanelExportFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelExportFileLayout.createSequentialGroup()
                .addComponent(jComboBoxFileType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonSaveFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jButtonSaveFile.getAccessibleContext().setAccessibleName("");

        jPanelFile.add(jPanelExportFile);
        jPanelExportFile.getAccessibleContext().setAccessibleName("");

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        add(jPanelFile, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    
  private void jButtonSaveFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveFileActionPerformed
      saveFile();
  }//GEN-LAST:event_jButtonSaveFileActionPerformed

  private void jButtonPreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPreviewActionPerformed
      openPreview();
}//GEN-LAST:event_jButtonPreviewActionPerformed

  private void jCheckBoxAutoPreviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAutoPreviewActionPerformed
      if (jCheckBoxAutoPreview.isSelected()) {
            openPreview();
      } else {
           closePreview();           
      }
}//GEN-LAST:event_jCheckBoxAutoPreviewActionPerformed

  private void jbuttonCurrentIndi2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbuttonCurrentIndi2ActionPerformed
      setIndividu2(currentIndi);
}//GEN-LAST:event_jbuttonCurrentIndi2ActionPerformed

  private void jListAncestorsValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListAncestorsValueChanged
      if (jCheckBoxAutoPreview.isSelected() && evt.getValueIsAdjusting() == false) {
          openPreview();
      }
}//GEN-LAST:event_jListAncestorsValueChanged

  private void jbuttonCurrentIndi1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbuttonCurrentIndi1ActionPerformed
      setIndividu1(currentIndi);
}//GEN-LAST:event_jbuttonCurrentIndi1ActionPerformed

  private void jCheckBoxDisplayedIdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDisplayedIdActionPerformed
      if (jCheckBoxAutoPreview.isSelected()) {
          openPreview();
      }
  }//GEN-LAST:event_jCheckBoxDisplayedIdActionPerformed

  private void jCheckBoxRecentEventActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxRecentEventActionPerformed
      if (jCheckBoxAutoPreview.isSelected()) {
          openPreview();
      }
  }//GEN-LAST:event_jCheckBoxRecentEventActionPerformed

  private void jComboBoxHusbandOrWifeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxHusbandOrWifeItemStateChanged
      if (jCheckBoxAutoPreview.isSelected()) {
          openPreview();
      }
  }//GEN-LAST:event_jComboBoxHusbandOrWifeItemStateChanged

  private void jCheckBoxSeparatedWindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxSeparatedWindowActionPerformed
      if ( previewTopComponent != null && jCheckBoxAutoPreview.isSelected() ) {
        previewTopComponent.setSeparatedWindowFlag(jCheckBoxSeparatedWindow.isSelected());      
      }
  }//GEN-LAST:event_jCheckBoxSeparatedWindowActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonPreview;
    private javax.swing.JButton jButtonSaveFile;
    private javax.swing.JCheckBox jCheckBoxAutoPreview;
    private javax.swing.JCheckBox jCheckBoxDisplayedId;
    private javax.swing.JCheckBox jCheckBoxRecentEvent;
    private javax.swing.JCheckBox jCheckBoxSeparatedWindow;
    private javax.swing.JComboBox jComboBoxFileType;
    private javax.swing.JComboBox jComboBoxHusbandOrWife;
    private javax.swing.JLabel jLabelAncestorList;
    private javax.swing.JLabel jLabelIndividu1;
    private javax.swing.JLabel jLabelIndividu2;
    private javax.swing.JList jListAncestors;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelExportFile;
    private javax.swing.JPanel jPanelFile;
    private javax.swing.JPanel jPanelOptions;
    private javax.swing.JPanel jPanelPreview;
    private javax.swing.JPanel jPanelSearch1;
    private javax.swing.JPanel jPanelSearch2;
    private javax.swing.JScrollPane jScrollPaneAncestortList;
    private javax.swing.JTextField jTextFieldIndividu1;
    private javax.swing.JTextField jTextFieldIndividu2;
    private javax.swing.JButton jbuttonCurrentIndi1;
    private javax.swing.JButton jbuttonCurrentIndi2;
    // End of variables declaration//GEN-END:variables
}
