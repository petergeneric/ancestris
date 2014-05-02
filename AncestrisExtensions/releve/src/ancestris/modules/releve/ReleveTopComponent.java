package ancestris.modules.releve;

import ancestris.modules.releve.imageBrowser.ImageViewActionProvider;
import ancestris.modules.releve.file.FileManager;
import ancestris.modules.releve.file.ReleveFileExport;
import ancestris.modules.releve.file.ReleveFileDialog;
import ancestris.modules.releve.model.RecordModel;
import ancestris.modules.releve.editor.StandaloneEditor;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.GedcomDirectory;
import ancestris.modules.releve.table.ErrorBuffer;
import ancestris.modules.releve.table.TableModelRecordCheck;
import ancestris.modules.releve.table.ResultDialog;
import ancestris.modules.releve.file.FileBuffer;
import ancestris.modules.releve.file.ReleveFileAncestrisV2;
import ancestris.modules.releve.file.ReleveFileGedcom;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordMisc;
import genj.gedcom.Context;
import genj.util.EnvironmentChecker;
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.modules.releve.dnd.ViewWrapperManager;
import ancestris.util.swing.DialogManager;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.swing.filechooser.FileFilter;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.netbeans.api.javahelp.Help;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.util.*;
import org.openide.windows.RetainLocation;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@RetainLocation("explorer")
// je declare la classe ServiceProvider pour que ses instances soient visibles
//@ServiceProvider(service=ReleveTopComponent.class)
public final class ReleveTopComponent extends TopComponent implements MenuCommandProvider {    
    private static final String PREFERRED_ID = "ReleveTopComponent";
    private static final String FILE_DIRECTORY = "FileDirectory";
    protected Registry registry;
    private DataManager dataManager;
    private JPopupMenu popup;
    private final JMenuItem menuItemNewFile   = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.new"));
    private final JMenuItem menuItemLoadFile  = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.open"));
    private final JMenuItem menuItemSave      = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.save"));
    private final JMenuItem menuItemSaveAs    = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.saveas"));
    private final JMenuItem menuItemImport    = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.import"));
    private final JMenuItem menuItemImportClipboard = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveClipboard.title"));
    private final JMenuItem menuItemExport    = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.export"));
    private final JMenuItem menuItemDelete   = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.delete"));

    private final JMenuItem menuItemInsert      = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.insert"));
    private final JMenuItem menuItemSwapNext    = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.swapnext"));
    private final JMenuItem menuItemSwapPrevious= new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.swapprevious"));
    private final JMenuItem menuItemReorder     = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.reorder"));
    private final JCheckBoxMenuItem menuItemGedcomLink  = new JCheckBoxMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.gedcomLink"));
    
    private final JMenuItem menuItemCheck     = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.check"));
    private final JMenuItem menuItemStatistics= new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.statistics"));
    private final JMenuItem menuItemDemoFile  = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.demo"));
    private final JMenuItem menuItemHelp      = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.help"));
    
    private StandaloneEditor standaloneEditor;
    private File currentFile = null;
    private ImageViewActionProvider ImageViewActionProvider;
    
    public ReleveTopComponent() {
        super();
        initComponents();
        setName(NbBundle.getMessage(ReleveTopComponent.class, "CTL_ReleveTopComponent"));
        setToolTipText(NbBundle.getMessage(ReleveTopComponent.class, "HINT_ReleveTopComponent"));
        setIcon(ImageUtilities.loadImage("ancestris/modules/releve/images/Releve.png", true));

        //je cree le popupmenu
        popup = new JPopupMenu();
        PopupMouseHandler popupMouseHandler = new PopupMouseHandler();
        menuItemNewFile.addActionListener(popupMouseHandler);
        menuItemNewFile.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/NewFile.png")));
        popup.add(menuItemNewFile);

        menuItemLoadFile.addActionListener(popupMouseHandler);
        menuItemLoadFile.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/OpenFile.png")));
        popup.add(menuItemLoadFile);
        menuItemSave.addActionListener(popupMouseHandler);
        menuItemSave.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/SaveFile.png")));
        popup.add(menuItemSave);
        menuItemSaveAs.addActionListener(popupMouseHandler);
        popup.add(menuItemSaveAs);

        // import , export
        popup.addSeparator();
        menuItemImport.addActionListener(popupMouseHandler);
        menuItemImport.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/ImportFile16.png")));
        popup.add(menuItemImport);
        menuItemImportClipboard.addActionListener(popupMouseHandler);
        menuItemImportClipboard.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/ImportFile16.png")));
        //popup.add(menuItemImportClipboard);
        menuItemExport.addActionListener(popupMouseHandler);
        menuItemExport.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/ExportFile16.png")));
        popup.add(menuItemExport);

        // create, insert,  delete
        popup.addSeparator();
        menuItemInsert.addActionListener(popupMouseHandler);
        menuItemInsert.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/NewRecord.png")));
        popup.add(menuItemInsert);
        menuItemDelete.addActionListener(popupMouseHandler);
        menuItemDelete.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/DeleteRecord.png")));
        popup.add(menuItemDelete);

        // swap, statistics, demo records, help
        popup.addSeparator();
        menuItemSwapPrevious.addActionListener(popupMouseHandler);
        menuItemSwapPrevious.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/arrowup16.png")));
        popup.add(menuItemSwapPrevious);
        menuItemSwapNext.addActionListener(popupMouseHandler);
        menuItemSwapNext.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/arrowdown16.png")));
        popup.add(menuItemSwapNext);
        menuItemReorder.addActionListener(popupMouseHandler);
        menuItemReorder.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/reorder16.png")));
        popup.add(menuItemReorder);
        menuItemGedcomLink.addActionListener(popupMouseHandler);
        menuItemGedcomLink.setSelected(false);
       // menuItemGedcomLink.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/gedcomLink.png")));
        popup.add(menuItemGedcomLink);

        menuItemCheck.addActionListener(popupMouseHandler);
        menuItemCheck.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/check16.png")));
        popup.add(menuItemCheck);
        menuItemStatistics.addActionListener(popupMouseHandler);
        menuItemStatistics.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/information.png")));
        popup.add(menuItemStatistics);
        menuItemDemoFile.addActionListener(popupMouseHandler);
        menuItemDemoFile.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/OpenFile.png")));
        popup.add(menuItemDemoFile);
        menuItemHelp.addActionListener(popupMouseHandler);
        menuItemHelp.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/information.png")));
        popup.add(menuItemHelp);

        // je branche le clic du bouton droit de la souris sur l'afffichage
        // du popupmenu
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
        // j'ajoute le listener du popmenu sur tous les panneaux
        panelBirth.addMouseListener(mouseAdapter);
        panelMarriage.addMouseListener(mouseAdapter);
        panelDeath.addMouseListener(mouseAdapter);
        panelMisc.addMouseListener(mouseAdapter);
        panelAll.addMouseListener(mouseAdapter);
        jTabbedPane1.addMouseListener(mouseAdapter);
    }
     
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    protected String preferredID() {
        String id = PREFERRED_ID;
        return id;
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        // je crée les raccourcis pour créer un nouveau relevé
        String shortCut = "MainShortcut";
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt N"), shortCut);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt M"), shortCut);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt D"), shortCut);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt T"), shortCut);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt V"), shortCut);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt G"), shortCut);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt K"), shortCut);

        getActionMap().put(shortCut, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if ( actionEvent.getActionCommand().toUpperCase().equals("N") ) {
                    jTabbedPane1.setSelectedComponent(panelBirth);
                        panelBirth.createRecord();
                } else if ( actionEvent.getActionCommand().toUpperCase().equals("M") ) {
                    jTabbedPane1.setSelectedComponent(panelMarriage);
                    panelMarriage.createRecord();
                } else if ( actionEvent.getActionCommand().toUpperCase().equals("D") ) {
                    jTabbedPane1.setSelectedComponent(panelDeath);
                    panelDeath.createRecord();
                } else if ( actionEvent.getActionCommand().toUpperCase().equals("V") ) {
                    jTabbedPane1.setSelectedComponent(panelMisc);
                    panelMisc.createRecord();
                } else if ( actionEvent.getActionCommand().toUpperCase().equals("T") ) {
                    jTabbedPane1.setSelectedComponent(panelAll);
                    panelAll.createRecord();
                } else if ( actionEvent.getActionCommand().toUpperCase().equals("K") ) {
                    //CopyFamPanel.showStatistics();
                } else if ( actionEvent.getActionCommand().toUpperCase().equals("G") ) {
                    // load current gedcom
                    boolean saveResult = true;
                    if (dataManager.isDirty()) {
                        // je demande s'il faut sauvegarder les données
                        saveResult = askSaveData();
                    }
                    if ( saveResult ) {
                        // je convertis le fichier GEDCOM courant en releve
                        //Context context = Utilities.actionsGlobalContext().lookup(Context.class);
                        Context context = GedcomDirectory.getDefault().getContexts().get(0);
                        if (context != null && context.getGedcom() != null) {
                            try {
                                FileBuffer  fileBuffer = ReleveFileGedcom.loadFile(context.getGedcom());
                                String defaultPlace = "";
                                if (fileBuffer.getPlaces().size() == 1 ) {
                                    defaultPlace = fileBuffer.getPlaces().get(0);
                                } else if ( fileBuffer.getPlaces().size() > 1 ) {
                                    defaultPlace = askSelectDefaultPlace(fileBuffer.getPlaces());
                                }
                                dataManager.setPlace(defaultPlace);
                                // Je copie les données dans les modeles
                                dataManager.addRecords(fileBuffer, false, defaultPlace, 1 );
                            } catch (Exception ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
        });

        AncestrisPlugin.register(this);
        // je cree le modele de données
        dataManager = new DataManager();
        panelBirth.setModel(dataManager, RelevePanel.PanelType.birth, this);
        panelMarriage.setModel(dataManager, RelevePanel.PanelType.marriage, this);
        panelDeath.setModel(dataManager, RelevePanel.PanelType.death, this);
        panelMisc.setModel(dataManager, RelevePanel.PanelType.misc,  this);
        panelAll.setModel(dataManager, RelevePanel.PanelType.all,  this);

        setCurrentFile(null);
        
        // je charge le fichier de la session précédente
        String lastFileName =  NbPreferences.forModule(ReleveTopComponent.class).get(
                    "LastFileName", "");
        if (!lastFileName.isEmpty() ) {
            File lastFile = new File(lastFileName);
            if (lastFile.exists() ) {
                loadFile(lastFile, false);

            } 
        }       

        //LookupTreeView lo = new LookupTreeView();
        //lo.init();
        
        ImageViewActionProvider = new ImageViewActionProvider();
        
        
        // j'active le DnD pour les Treeview
        ViewWrapperManager.addTreeViewListener();  
        
    }

    /**
     * Cette fonction est appelée par le système avant la fermeture du composant.
     * Si des modifications des données n'ont pas été sauvegardées elle demande
     * à l'utilisateur s'il veut les sauvegarder.
     * @return false si l'utilisateur veut interrompre la fermeture
     */
    @Override
    public boolean canClose() {
        boolean result = true;
        if(dataManager.isDirty()) {
            // je demande s'il faut sauvegarder les données
            // result = false si l'action doit être abandonnée
            result = askSaveData();
        }

        if (result) {
            // Remarque : j'ajoute donc l'enregistrement des preferences ici 
            // au lieu de componentClosed() car Netbeans n'appelle pas componentClosed()
            // quand on ferme l'application ancestris sans avoir fermé le TopComponent
            
            // Chaque panel sauvegarde la largeur des colonnes
            panelBirth.componentClosed();
            panelMarriage.componentClosed();
            panelDeath.componentClosed();
            panelMisc.componentClosed();
            panelAll.componentClosed();
            
            // j'enregistre le nom du fichier courant
            if (currentFile != null) {
                NbPreferences.forModule(ReleveTopComponent.class).put(
                        "LastFileName",
                        currentFile.getAbsolutePath());
            }

            // je ferme l'editeur independant
            if ( standaloneEditor != null) {
                standaloneEditor.closeComponent();
            }
        }
        return result;
    }

    /**
     * Cette methode est appelée par le système a la fermeture du composant
     * après l'appel a canClose.
     * Elle enregistre les parametres du composant et ferme la fenetre de
     * l'éditeur independant.
     */
    @Override
    public void componentClosed() {
        // je ferme l'editeur independant
        if ( standaloneEditor != null) {
            standaloneEditor.closeComponent();
        }
        
        // j'arrete le listener des vues
        ViewWrapperManager.removeTreeViewListener();
        
        // je désactive l'action
        ImageViewActionProvider = null;
        
        //
        AncestrisPlugin.unregister(this);
    }

    DataManager getDataManager() {
       return dataManager;
    }

    /**
     * Cette méthode est appelée par ReleveQuickSearch
     * Elle permet de sélectionner le champ qui est choisi dans l'outil de recherche
     * @param record
     * @param fieldType
     */
    void selectField(Record record, Field.FieldType fieldType) {
        if (record instanceof RecordBirth) {
            if (panelBirth.verifyCurrentRecord()) {
                jTabbedPane1.setSelectedComponent(panelBirth);
                panelBirth.selectRecord(dataManager.getDataModel().getIndex(record));
                panelBirth.selectField(fieldType);
            }
        } else if (record instanceof RecordMarriage) {
            if (panelMarriage.verifyCurrentRecord()) {
                jTabbedPane1.setSelectedComponent(panelMarriage);
                panelMarriage.selectRecord(dataManager.getDataModel().getIndex(record));
                panelMarriage.selectField(fieldType);
            }
        } else if (record instanceof RecordDeath) {
            if (panelDeath.verifyCurrentRecord()) {
                jTabbedPane1.setSelectedComponent(panelDeath);
                panelDeath.selectRecord(dataManager.getDataModel().getIndex(record));
                panelDeath.selectField(fieldType);
            }
        } else if (record instanceof RecordMisc) {
            if (panelMisc.verifyCurrentRecord()) {
                jTabbedPane1.setSelectedComponent(panelMisc);
                panelMisc.selectRecord(dataManager.getDataModel().getIndex(record));
                panelMisc.selectField(fieldType);
            }
        }
    }

    /**
     * Cette méthode est appelée par EditorBeanGroup
     * pour afficher les champs dans l'éditeur 
     * après avoir modifié la liste des champs visibles
     */
    public void udpdateEditorVisibleField() {
        // je rafraichis l'affichage des editeurs de la fentre principale
        panelBirth.selectRecord(panelBirth.getCurrentRecordIndex());
        panelMarriage.selectRecord(panelMarriage.getCurrentRecordIndex());
        panelDeath.selectRecord(panelDeath.getCurrentRecordIndex());
        panelMisc.selectRecord(panelMisc.getCurrentRecordIndex());
        panelAll.selectRecord(panelAll.getCurrentRecordIndex());
        
        // je rafraichis l'affichage des editeurs de la fentre volante
        if ( standaloneEditor != null) {
            standaloneEditor.udpdateEditorVisibleField();
        }        
    }



    ///////////////////////////////////////////////////////////////////////////
    //  Traite les actions du popup menu
    ///////////////////////////////////////////////////////////////////////////

    /**
     * cette methode est utilisée pour afficher le popupemenu depuis la toolbar de
     * l'editeur
     * @param invoker
     * @param x
     * @param y
     */
    @Override
    public void showPopupMenu(Component invoker, int x, int y ) {
        popup.show(invoker, x, y);
    }

   
    /**
     * Traite les evenements de souris du topcompoent
     */
    private class PopupMouseHandler implements ActionListener {
        /**
         * traite les évènements du popumenu
         * @param e
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (menuItemNewFile.equals(e.getSource())) {
                createFile();
            } else if (menuItemLoadFile.equals(e.getSource())) {
                loadFile();
            } else if (menuItemSave.equals(e.getSource())) {
                saveFile();
            } else if (menuItemSaveAs.equals(e.getSource())) {
                saveFileAs();
            } else if (menuItemImport.equals(e.getSource())) {
                importFile();
            } else if (menuItemImportClipboard.equals(e.getSource())) {
                importClipboard();
            } else if (menuItemExport.equals(e.getSource())) {
                exportFile();
            } else if (menuItemInsert.equals(e.getSource())) {
                insertRecord();
            } else if (menuItemDelete.equals(e.getSource())) {
                deleteRecord();
            } else if (menuItemSwapNext.equals(e.getSource())) {
                swapRecordNext();
            } else if (menuItemSwapPrevious.equals(e.getSource())) {
                swapRecordPrevious();
             } else if (menuItemReorder.equals(e.getSource())) {
                renumberRecords();
             } else if (menuItemGedcomLink.equals(e.getSource())) {
                showGedcomLink(menuItemGedcomLink.isSelected());
             } else if (menuItemCheck.equals(e.getSource())) {
                showCheck();
            } else if (menuItemStatistics.equals(e.getSource())) {
                showStatistics();
            } else if (menuItemDemoFile.equals(e.getSource())) {
                loadFileDemo();
            } else if (menuItemHelp.equals(e.getSource())) {
                showHelp();
            }
        }
    }

    public void insertRecord() {
        switch (jTabbedPane1.getSelectedIndex()) {
            case 0:
                panelBirth.insertRecord();
                break;
            case 1:
                panelMarriage.insertRecord();
                break;
            case 2:
                panelDeath.insertRecord();
                break;
            case 3:
                panelMisc.insertRecord();
                break;
            default:
                panelAll.insertRecord();
                break;
        }
    }

    public void deleteRecord() {
        switch (jTabbedPane1.getSelectedIndex()) {
            case 0:
                panelBirth.removeRecord();
                break;
            case 1:
                panelMarriage.removeRecord();
                break;
            case 2:
                panelDeath.removeRecord();
                break;
            case 3:
                panelMisc.removeRecord();
                break;
            default:
                panelAll.removeRecord();
                break;
        }
    }

    public void swapRecordNext() {
         switch (jTabbedPane1.getSelectedIndex()) {
            case 0:
                panelBirth.swapRecordNext();
                break;
            case 1:
                panelMarriage.swapRecordNext();
                break;
            case 2:
                panelDeath.swapRecordNext();
                break;
            case 3:
                panelMisc.swapRecordNext();
                break;
            default:
                panelAll.swapRecordNext();
        }
    }

    public void swapRecordPrevious() {
         switch (jTabbedPane1.getSelectedIndex()) {
            case 0:
                panelBirth.swapRecordPrevious();
                break;
            case 1:
                panelMarriage.swapRecordPrevious();
                break;
            case 2:
                panelDeath.swapRecordPrevious();
                break;
            case 3:
                panelMisc.swapRecordPrevious();
                break;
            default:
                panelAll.swapRecordPrevious();
        }
    }

    public void renumberRecords() {
        if ( jTabbedPane1.getSelectedComponent().equals(panelAll)) {
            panelAll.renumberRecords();            
        } else {
            
        }
    }

    public void showGedcomLink(boolean state) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            dataManager.showGedcomLink(state);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setCursor(Cursor.getDefaultCursor());
    }

    public void showCheck() {
        ErrorBuffer errorBuffer = new ErrorBuffer();
        TableModelRecordCheck modelCheck = new TableModelRecordCheck(dataManager.getDataModel());
        ResultDialog.show(null, this, modelCheck, errorBuffer, currentFile);
    }

    public void showStatistics() {
        ReleveStatistic.showStatistics(dataManager);
    }

    /**
     * Affiche le panneau contenant les options
     */
    @Override
    public void showOptionPanel() {
        OptionsDisplayer.getDefault().open("Extensions/Releve");
    }

    /**
     * Affiche les informations du registre
     */
    @Override
    public void showConfigPanel() {
        ReleveConfigDialog.show(WindowManager.getDefault().getMainWindow(), dataManager);
    }

    /**
     * Affiche la fenetre principale au premier plan
     */
    @Override
    public void showToFront() {
        this.toFront();
    }

    /**
     * Affiche les informations du relevé courant
     */
    void showHelp() {
        String id = "Releve.about";
        Help help = Lookup.getDefault().lookup(Help.class);
        if (help != null && help.isValidID(id, true).booleanValue()) {
            help.showHelp(new HelpCtx(id));
        }
    }

    /**
     *
     */
    protected void createFile() {
        boolean result = true; 
        if(dataManager.isDirty()) {
            // je demande s'il faut sauvegarder les données
            result = askSaveData();
        }

        // je passe à la suite
        if ( result ) {
            dataManager.removeAll();
            setCurrentFile(null);
        }
    }


    /**
     *
     */
    protected void loadFile() {
        boolean result = true;
        if(dataManager.isDirty()) {
            // je demande s'il faut sauvegarder les données
            result = askSaveData();
        }

        // je passe à la suite
        if ( result ) {
            String title = java.util.ResourceBundle.getBundle("ancestris/modules/releve/Bundle").getString("LOAD_FILE");

            // je demande a l'utilisateur de choisir un nom de ficher
            File releveFile = getFileFromUserForOpen(this, title, AbstractAncestrisAction.TXT_OK, "", true, "txt");
            loadFile(releveFile, false);
        }
    }


    /**
     *
     * @param releveFile1
     * @param append
     */
    private void loadFile(File releveFile, boolean append) {
        String title = java.util.ResourceBundle.getBundle("ancestris/modules/releve/Bundle").getString("LOAD_FILE");

        // je desactive le listener UNDO
        RecordModel.setUndoEnabled(false);

        try {

            if (releveFile != null) {                
                // je prepare un modele temporaire 
                
                // je charge le fichier
                FileBuffer fileBuffer = FileManager.loadFile(releveFile);

                // je controle le nombre de releves lus.
//                if ((newBirthModel.getRowCount() + newMarriageModel.getRowCount()
//                    + newDeathModel.getRowCount() + newMiscModel.getRowCount()) == 0) {
//                    throw new Exception(String.format("%s \n Fichier vide", releveFile.getName()));
//                }

                //boolean append = false;

//                if (currentFile != null) {
//                    // j'affiche le resultat du chargement
//                    // et je demande à l'utilsateur s'il vaut importer les données
//                    // en les ajoutant ou a la place des données existantes
//
//
//                    String message = String.format(
//                            java.util.ResourceBundle.getBundle("ancestris/modules/releve/Bundle").getString("INFO_FILE"),
//                            releveFile.getName(),
//                            newDataManager.getReleveBirthModel().getRowCount(),
//                            newDataManager.getReleveMarriageModel().getRowCount(),
//                            newDataManager.getReleveDeathModel().getRowCount(),
//                            newDataManager.getReleveMiscModel().getRowCount());
//                    message += ("\nVoulez vous les remplacer ou les ajouter aux données existantes ?");
//                    Object[] possibleValues = {"Remplacer", "Ajouter", Action2.TXT_CANCEL};
//                    // j'affiche la fenetre
//                    int addModel = JOptionPane.showOptionDialog(this, message, title,
//                            JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, (Icon) null,
//                            possibleValues, possibleValues[0]);
//
//                    //int addModel = ReleveFileDialog.show(getTopLevelAncestor(), title, message, sb);
//                    switch (addModel) {
//                        case 0:
//                            append = false;
//                            break;
//                        case 1:
//                            append = true;
//                            break;
//                        default:
//                            append = true;
//                            //  l'utilsateur annule l'operation
//                            RecordModel.setUndoEnabled(true);
//                            return;
//                    }
//                }


                // j'affiche le résultat
                if (fileBuffer.getError().length() >0) {
                    String message = String.format(
                                java.util.ResourceBundle.getBundle("ancestris/modules/releve/Bundle").getString("INFO_FILE"),
                                releveFile.getName(),
                                fileBuffer.getBirthCount(),
                                fileBuffer.getMarriageCount(),
                                fileBuffer.getDeathCount(),
                                fileBuffer.getMiscCount());
                    ReleveFileDialog.show(WindowManager.getDefault().getMainWindow(), title, message, fileBuffer.getError());
                }

                // je traite les releves qui ont un lieu different du lieu par defaut
                List<String> places = fileBuffer.getPlaces();
                //places.addAll(newCompletionProvider.getPlaces());
                String defaultPlace = dataManager.getPlace();
                if (append == false ) {
                    defaultPlace = "";
                }
                int forceDefaultPlace = 0;
                if ( places.size() > 0) {
                    // le fichier n'est pas vide
                    if (places.size() > 1) {
                        // il y a plusieurs lieux dans le fichier
                        if (append == true) {
                            if (defaultPlace.isEmpty()) {
                                // il n'y a pas de lieu par defaut
                                // je demande quel doit etre le lieu par defaut
                                defaultPlace = askSelectDefaultPlace(places);
                            }
                            if ( defaultPlace != null ) {
                                // Je demande s'il faut rejeter ou forcer le lieu
                                // des releves qui ont des lieux differents
                                forceDefaultPlace = askDifferentPlace(places, defaultPlace);
                            }
                        } else {
                            // mode replace
                            // je demande quel doit être le lieu par defaut
                            defaultPlace = askSelectDefaultPlace(places);
                            if ( defaultPlace != null ) {
                                // Je demande s'il faut rejeter ou forcer le lieu
                                // des releves qui ont des lieux differents
                                forceDefaultPlace = askDifferentPlace(places, defaultPlace);
                            }
                        }
                    } else if (places.size() == 1 ) {
                        // il y a un seul lieu dans le fichier
                        if (append == true) {
                            // mode ajout
                            if (defaultPlace.isEmpty()) {
                                // il n'y a pas de lieu par defaut
                                // je memorise le lieu du premier du fichier comme lieu par defaut
                               defaultPlace = places.toArray()[0].toString();
                            } else {
                                // il y a un lieu par defaut
                                if (places.toArray()[0].equals(defaultPlace)) {
                                    // les lieux de tous les releves sont identiques au lieu par defaut
                                    // rien a faire
                                }  else  {
                                    // le lieu est différent du lieu par défaut
                                    // Je demande s'il faut rejeter ou forcer
                                    forceDefaultPlace = askDifferentPlace(places, defaultPlace);
                                }
                            }
                        } else {
                            // mode replace
                            // je memorise le lieu du premier releve comme lieu par defaut
                            defaultPlace = places.toArray()[0].toString();
                        }
                    } else {
                        // le fichier est vide. 
                        // rien a faire
                    }
                }

                if (defaultPlace != null && forceDefaultPlace != -1) {
                    dataManager.setPlace(defaultPlace);
                    // Je copie les données dans les modeles
                    dataManager.addRecords(fileBuffer, append, defaultPlace, forceDefaultPlace );

                    // je selectionne le premier releve dans chaque table, s'il n'y a
                    // pas de releve deja selectionne
                    if (!append ) {    
                        panelBirth.selectRow(0);
                        panelMarriage.selectRow(0);
                        panelDeath.selectRow(0);
                        panelMisc.selectRow(0);
                        panelAll.selectRow(0);
                    }

                    // je selectionne le premier onglet
                    jTabbedPane1.setSelectedComponent(panelBirth);
                    
                    if ( append == false ) {
                        // je memorise le nom du fichier seulement en mode "replace"
                        setCurrentFile(releveFile);
                    }
                }
            }

        } catch (Exception ex) {
            String message = ex.getMessage();
            ex.printStackTrace(System.err);
            Toolkit.getDefaultToolkit().beep();
            if (ex.getMessage() == null || message.isEmpty()) {
                JOptionPane.showMessageDialog(this, ex.getClass().getName()+ " See console log", title, JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, ex.getMessage(), title, JOptionPane.ERROR_MESSAGE);
            }
        }
        // je retablis le listener UNDO
        RecordModel.setUndoEnabled(true);
    }

    /**
     *
     * @param places
     * @return
     */
    private String askSelectDefaultPlace(List<String> places) {
        String defaultPlace;
        String title = NbBundle.getMessage(ReleveTopComponent.class, "LOAD_FILE");
        Object result = JOptionPane.showInputDialog(this,
                "Choisir le lieu par défaut :",
                title,
                JOptionPane.QUESTION_MESSAGE,
                (Icon) null,
                places.toArray(),
                places.toArray()[0]
            );

        if (result != null) {
            // je memorise le lieu par defaut
            defaultPlace = result.toString();
        } else {
            // l'utilsateur n'a pas répondu a la question
            defaultPlace = null;
        }

        return defaultPlace;
    }

    /**
     *
     * @param places
     * @param defaultPlace
     * @return
     */
    private int askDifferentPlace (List<String> places, String defaultPlace) {
        int result;
        String title = NbBundle.getMessage(ReleveTopComponent.class, "LOAD_FILE");
        String message = String.format("Ce fichier contient des relevés qui ont des lieux différents de %s", defaultPlace);
        for (String place : places) {
            message += "\n   " + place;
        }
        message += "\n";
        message += String.format("Voulez-vous les ignorer, ou remplacer leur lieu par %s ?", defaultPlace);


        String[] options = { "Ignorer", "Remplacer" , "Abandonner"};
        result = JOptionPane.showOptionDialog(this,
                message,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                (Icon) null,
                options,
                options[0]);

        switch (result) {
            case 0:
                result = 0;
                break;
            case 1:
                result = 1;
                break;
            default:
                // l'utilsateur n'a pas répondu a la question
                result = -1;
        }
        return result;
    }

    /**
     * Demande à l'utilsateur s'il veut sauvegarder les données du fichier courant
     * @return true si l'utisateur accepte de continuer l'action, ou false si l'utilisateur abandonne l'action
     */
    private boolean askSaveData() {
        String title = NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.save");
        String fileName;
        if (currentFile != null) {
            fileName = currentFile.getName();
        } else {
            fileName = dataManager.getCityName();
            if (!fileName.isEmpty()) {
                fileName += ".txt";
            }
        }
        int choice = JOptionPane.showConfirmDialog(this,
                String.format(NbBundle.getMessage(ReleveTopComponent.class, "message.saveFile"), fileName),
                title,
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
        boolean result;

        switch (choice)  {
            case 0: // YES
                saveFile();
                result = true;
                break;
            case 1: // NO
                result = true;
                // rien a faire
                break;
            default: // CANCEL
                result = false;
        }

        return result;
    }

    /**
     * Choix d'un fichier à ouvrir
     *
     * @param title  file dialog title
     * @param button  file dialog OK button text
     * @param askForOverwrite  whether to confirm overwriting files
     * @param extension  extension of files to display
     * @return FILE handle , or null if user cancel action
     */
    private static File getFileFromUserForOpen(Component component, String title, String buttonLabel, String defaultFileName, boolean askForOverwrite, String extension) {

        // show filechooser
        String defaultDir = EnvironmentChecker.getProperty("user.home", ".", "looking for report dir to let the user choose from");
        String dir = NbPreferences.forModule(ReleveTopComponent.class).get(FILE_DIRECTORY, defaultDir);
        JFileChooser chooser = new JFileChooser(dir);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle(title);
        chooser.setSelectedFile(new File(defaultFileName));
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        if (extension != null) {
            chooser.setFileFilter(new FileExtensionFilter(extension));
        }

        int rc = chooser.showDialog(component, buttonLabel);

        // check resultFile
        File resultFile = chooser.getSelectedFile();
        if (rc != JFileChooser.APPROVE_OPTION || resultFile == null) {
            return null;
        }

        // choose an existing file?
        if (resultFile.exists() && askForOverwrite) {
            //rc = DialogHelper.openDialog(title, DialogHelper.WARNING_MESSAGE, NbBundle.getMessage(ReleveTopComponent.class, "message.fileExits"), Action2.yesNo(), this);
            if (rc != 0) {
                return null;
            }
        }

        // keep it as new default directory
        if (resultFile != null) {
            NbPreferences.forModule(ReleveTopComponent.class).put(FILE_DIRECTORY, resultFile.getParent().toString());
        }
        return resultFile;
    }


   /**
     * user choose output file name
     *
     */
    protected void saveFile() {
        if ( currentFile != null) {
            StringBuilder saveResult = FileManager.saveFile(dataManager, dataManager, currentFile, FileManager.FileFormat.FILE_TYPE_ANCESTRISV4);
            if (saveResult.toString().isEmpty()) {
                // je met a zero l'indicateur des modifications
                dataManager.resetDirty();
            } else {
                // j'affiche les erreurs rencontrées
                String message = saveResult.toString();
                String title  = "Enregistrer";
                JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
            }
        } else {
            saveFileAs();
        }
    }

    /**
     * user choose output file name
     *
     */
    protected void saveFileAs() {

        String buttonLabel = "Enregistrer";
        boolean askForOverwrite = true;
        Component component = this;
        String title  = "Enregistrer";
        String extension = "txt";

        String fileName;
        if (currentFile != null) {
            fileName = currentFile.getName();
        } else {
            fileName = dataManager.getCityName();
            if (!fileName.isEmpty()) {
                fileName += ".txt";
            }
        }
        // show filechooser
        String defaultDir = EnvironmentChecker.getProperty("user.home", ".", "looking for report dir to let the user choose from");
        String dir = NbPreferences.forModule(ReleveTopComponent.class).get(FILE_DIRECTORY, defaultDir);
        JFileChooser chooser = new JFileChooser(dir);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle(title);
        chooser.setSelectedFile(new File(fileName));
        if (extension != null) {
            chooser.setFileFilter(new FileExtensionFilter(extension));
        }

        int rc = chooser.showDialog(component, buttonLabel);


        // check resultFile
        File resultFile = chooser.getSelectedFile();
        if (rc != JFileChooser.APPROVE_OPTION || resultFile == null) {
            return ;
        }

         // j'ajoute l'extension par defaut si l'utilisateur n'a pas mis d'extension
        if (!resultFile.getName().contains(".")) {
            resultFile = new File(resultFile.getAbsolutePath() + "." + extension);
        }

        // choose an existing file?
        if (resultFile.exists() && askForOverwrite) {
            Toolkit.getDefaultToolkit().beep();
            if (DialogManager.YES_OPTION !=
                    DialogManager.createYesNo(title, NbBundle.getMessage(ReleveTopComponent.class, "message.fileExits"))
                    .setMessageType(DialogManager.WARNING_MESSAGE).show()){
                return;
            }
        }

        if (resultFile != null) {
            // je memorise  le répertoire du fichier
            // remarque : je memorise le répertoire du fichier avant d'enregistrer le fichier
            // afin de pouvoir le ré-utiliser meme si l'enregistrement s'est mal passé.
            NbPreferences.forModule(ReleveTopComponent.class).put(FILE_DIRECTORY, resultFile.getParent().toString());
            // j'enregistre les données dans le fichier
            StringBuilder saveResult = FileManager.saveFile(dataManager, dataManager, resultFile, FileManager.FileFormat.FILE_TYPE_ANCESTRISV4);

             if (saveResult.toString().isEmpty()) {
                // je met a zero l'indicateur des modifications
                dataManager.resetDirty();
                // je memorise le nom du fichier
                setCurrentFile(resultFile);
            } else {
                // j'affiche les erreurs rencontrées
                String message = saveResult.toString();
                JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * importe un releve d'un fichier et les ajoute aux releves dejà présent
     *
     */
    protected void importFile() {
        String title = NbBundle.getMessage(ReleveTopComponent.class, "LOAD_FILE");

        // je demande a l'utilisateur de choisir un nom de ficher
        File releveFile = getFileFromUserForOpen(this, title, AbstractAncestrisAction.TXT_OK, "", true, "txt");
        loadFile(releveFile, true);
    }

    /**
     * importe les données du presse papier 
     *
     */
    protected void importClipboard()  {
        if(dataManager.isDirty()) {
            // je demande s'il faut sauvegarder les données
            if ( false == askSaveData() ) {
                return;
            }
        }
        int result;
        String title = NbBundle.getMessage(ReleveTopComponent.class, "ReleveClipboard.title");
        String message = NbBundle.getMessage(ReleveTopComponent.class, "ReleveClipboard.message");
        String[] options = {
            NbBundle.getMessage(ReleveTopComponent.class, "ReleveClipboard.import"),
            NbBundle.getMessage(ReleveTopComponent.class, "ReleveClipboard.cancel")};
        result = JOptionPane.showOptionDialog(this,
                message,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                (Icon) null,
                options,
                options[0]);
        switch (result) {
            case 0:
                result = 0;
                break;
            case 1:
                result = 1;
                break;
            default:
                // l'utilisateur n'a pas répondu a la question
                result = -1;
        }
        if (result != 0) {
            return;
        }

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        //odd: the Object param of getContents is not currently used
        Transferable contents = clipboard.getContents(null);

        boolean hasTransferableText =
                (contents != null)
                && contents.isDataFlavorSupported(DataFlavor.stringFlavor);
        String inputData = "";
        if (hasTransferableText) {
            try {
                inputData = (String) contents.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException ex) {
                //highly unlikely since we are using a standard DataFlavor
                System.out.println(ex);
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
        inputData = inputData.replaceAll(";", ",");
        inputData = inputData.replaceAll("\t", ";");

        // je lis la premiere ligne du fichier
        if (inputData==null || inputData.isEmpty() ) {
            //throw new Exception(String.format(java.util.ResourceBundle.getBundle("ancestris/modules/releve/file/Bundle").getString("file.EmptyFile"), ""));
            return;
        }

        File inputFile = new File(System.getProperty("user.home") + File.separator + "clipboard.txt");
        try {
            FileWriter writer = null;
            boolean append = false;
            writer = new FileWriter(inputFile, append);
            writer.write(inputData);
            writer.close();

//            setCurrentFile(null);
            loadFile(inputFile, false);
        } catch (Exception ex) {
            // j'intercepte l'exception pour fermer le fichier
            inputFile.delete();
            // TODO afficher un message d'erreur
        }
    }

    /**
     * exporte les releves dans un format different de ANCESTRIS :
     *  c'est a dire  EGMT ou NIMEGUE
     *
     */
    protected void exportFile() {
        String buttonLabel = "Enregistrer";
        boolean askForOverwrite = true;
        Component component = this;
        String title  = "Enregistrer";       

        // show filechooser
        String defaultDir = EnvironmentChecker.getProperty("user.home", ".", "looking for report dir to let the user choose from");
        String dir = NbPreferences.forModule(ReleveTopComponent.class).get(FILE_DIRECTORY, defaultDir);
        final JFileChooser chooser = new JFileChooser(dir);
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogTitle(title);        

        GridBagConstraints gridBagConstraints;
        javax.swing.JPanel panelExport;
        javax.swing.JPanel jPanelFormat;
        javax.swing.JPanel jPanelModel;
        javax.swing.ButtonGroup buttonGroupFormat;
        final javax.swing.JRadioButton jRadioButtonAll;
        final javax.swing.JRadioButton jRadioButtonEgmt;
        final javax.swing.JRadioButton jRadioButtonNimegue;
        final javax.swing.JRadioButton jRadioButtonPdf;
        javax.swing.ButtonGroup buttonGroupModel;
        final javax.swing.JRadioButton jRadioButtonBirth;
        final javax.swing.JRadioButton jRadioButtonDeath;
        final javax.swing.JRadioButton jRadioButtonMarriage;
        final javax.swing.JRadioButton jRadioButtonMisc;
        panelExport = new javax.swing.JPanel();

        jPanelFormat = new javax.swing.JPanel();
        jRadioButtonEgmt = new javax.swing.JRadioButton();
        jRadioButtonNimegue = new javax.swing.JRadioButton();
        jRadioButtonPdf = new javax.swing.JRadioButton();
        buttonGroupFormat = new javax.swing.ButtonGroup();
        buttonGroupFormat.add(jRadioButtonEgmt);
        buttonGroupFormat.add(jRadioButtonNimegue);
        buttonGroupFormat.add(jRadioButtonPdf);

        jRadioButtonAll = new javax.swing.JRadioButton();
        jRadioButtonBirth = new javax.swing.JRadioButton();
        jRadioButtonMarriage = new javax.swing.JRadioButton();
        jRadioButtonDeath = new javax.swing.JRadioButton();
        jRadioButtonMisc = new javax.swing.JRadioButton();
        buttonGroupModel = new javax.swing.ButtonGroup();
        buttonGroupModel.add(jRadioButtonAll);
        buttonGroupModel.add(jRadioButtonBirth);
        buttonGroupModel.add(jRadioButtonMarriage);
        buttonGroupModel.add(jRadioButtonDeath);
        buttonGroupModel.add(jRadioButtonMisc);

        jPanelModel = new javax.swing.JPanel();

        panelExport.setLayout(new java.awt.GridBagLayout());
        jPanelFormat.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jPanelFormat.border.title"))); // NOI18N
        jPanelFormat.setLayout(new java.awt.GridLayout(2, 1, 2, 2));
        jRadioButtonEgmt.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonEgmt.text")); // NOI18N
        jPanelFormat.add(jRadioButtonEgmt);
        jRadioButtonNimegue.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonNimegue.text")); // NOI18N
        jPanelFormat.add(jRadioButtonNimegue);
        jRadioButtonPdf.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonPdf.text")); // NOI18N
        jPanelFormat.add(jRadioButtonPdf);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        panelExport.add(jPanelFormat, gridBagConstraints);

        jPanelModel.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jPanelModel.border.title"))); // NOI18N
        jPanelModel.setLayout(new java.awt.GridLayout(5, 1, 2, 2));
        jRadioButtonAll.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonAll.text")); // NOI18N
        jPanelModel.add(jRadioButtonAll);
        jRadioButtonBirth.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonBirth.text")); // NOI18N
        jPanelModel.add(jRadioButtonBirth);
        jRadioButtonMarriage.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonMarriage.text")); // NOI18N
        jPanelModel.add(jRadioButtonMarriage);
        jRadioButtonDeath.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonDeath.text")); // NOI18N
        jPanelModel.add(jRadioButtonDeath);
        jRadioButtonMisc.setText(org.openide.util.NbBundle.getMessage(ReleveFileExport.class, "ReleveFileExport.jRadioButtonMisc.text")); // NOI18N
        jPanelModel.add(jRadioButtonMisc);

        // je declare l'action pour proposer un nom de fichier par defaut correspondant aux choix de l'utilisateur
        ActionListener rbActionListener = new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                String extension = "txt";
                String cityName = dataManager.getCityName();
                if ( cityName.isEmpty()) {
                    cityName = "";
                }
                String recordType;
                if (jRadioButtonAll.isSelected()) {
                    recordType = "";
                } else if (jRadioButtonBirth.isSelected()) {
                    recordType = "_B";
                } else if (jRadioButtonMarriage.isSelected()) {
                    recordType = "_M";
                }  else if (jRadioButtonDeath.isSelected()) {
                    recordType = "_D";
                }  else if (jRadioButtonMisc.isSelected()) {
                    recordType = "_V";
                } else {
                    recordType = "";
                }
                String format = "";
                if (jRadioButtonEgmt.isSelected()) {
                    format = "_EGMT";
                    extension = "csv";
                } else if (jRadioButtonNimegue.isSelected()) {
                    format = "_NIMEGUE";
                    extension = "txt";
                } else if (jRadioButtonPdf.isSelected()) {
                    format = "";
                    extension = "pdf";
                }
                // je cree le nom de fichier par defaut
                chooser.setSelectedFile(new File(cityName+recordType+format+"."+extension));
            }
        };

        jRadioButtonEgmt.addActionListener(rbActionListener);
        jRadioButtonNimegue.addActionListener(rbActionListener);
        jRadioButtonAll.addActionListener(rbActionListener);
        jRadioButtonBirth.addActionListener(rbActionListener);
        jRadioButtonMarriage.addActionListener(rbActionListener);
        jRadioButtonDeath.addActionListener(rbActionListener);
        jRadioButtonMisc.addActionListener(rbActionListener);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        panelExport.add(jPanelModel, gridBagConstraints);

        // je selectionne le format EGMT par defaut
        jRadioButtonEgmt.setSelected(true);
        // je selectionne les naissances par defaut
        jRadioButtonBirth.setSelected(true);
        // je met a jour le nom du fichier par defaut en fonction du format 
        rbActionListener.actionPerformed(null);

        // j'ajoute le panneau dans la boite de dialogue
        chooser.setAccessory(panelExport);

        String extension = chooser.getDescription(chooser.getSelectedFile());
        chooser.setFileFilter(new FileExtensionFilter(extension));

        // j'affiche la boit de dialog
        int rc = chooser.showDialog(component, buttonLabel);

        // check resultFile
        File outputFile = chooser.getSelectedFile();
        if (rc != JFileChooser.APPROVE_OPTION || outputFile == null) {
            return ;
        }

        // choose an existing file?
        if (outputFile.exists() && askForOverwrite) {
            Toolkit.getDefaultToolkit().beep();
             if (DialogManager.YES_OPTION !=
                    DialogManager.createYesNo(title, NbBundle.getMessage(ReleveTopComponent.class, "message.fileExits"))
                    .setMessageType(DialogManager.WARNING_MESSAGE).show()){
                return;
            }
        }

        if (outputFile != null) {
            // j'enregistre le répertoire du fichier
            NbPreferences.forModule(ReleveTopComponent.class).put(FILE_DIRECTORY, outputFile.getParent().toString());

            // je recupere le type de fichier choisi
            FileManager.FileFormat fileFormat = FileManager.FileFormat.FILE_TYPE_UNKNOW;
            if (jRadioButtonEgmt.isSelected()) {
                fileFormat = FileManager.FileFormat.FILE_TYPE_EGMT;
            } else if (jRadioButtonNimegue.isSelected()) {
                fileFormat = FileManager.FileFormat.FILE_TYPE_NIMEGUE;
            } else if (jRadioButtonPdf.isSelected()) {
                fileFormat = FileManager.FileFormat.FILE_TYPE_PDF;
            }

            // j'ajoute l'extension par defaut si l'utilisateur n'a pas mis d'extension
            if ( ! outputFile.getName().contains(".") ) {
                outputFile = new File (outputFile.getAbsolutePath()+"."+extension);
            }

            // j'enregistre les modeles choisis la liste des modeles à enregistrer
            StringBuilder saveResult = new StringBuilder();
            if (jRadioButtonAll.isSelected()) {
                FileManager.saveFile(dataManager, dataManager, outputFile, fileFormat);
            } else if (jRadioButtonBirth.isSelected()) {
                FileManager.saveFile(dataManager, outputFile, fileFormat, dataManager.getDataModel(), DataManager.RecordType.birth);
            } else if (jRadioButtonMarriage.isSelected()) {
                FileManager.saveFile(dataManager, outputFile, fileFormat, dataManager.getDataModel(), DataManager.RecordType.marriage);
            } else if (jRadioButtonDeath.isSelected()) {
                FileManager.saveFile(dataManager, outputFile, fileFormat, dataManager.getDataModel(), DataManager.RecordType.death);
            } else if (jRadioButtonMisc.isSelected()) {
                FileManager.saveFile(dataManager, outputFile, fileFormat, dataManager.getDataModel(), DataManager.RecordType.misc);
            }
            if (! saveResult.toString().isEmpty()) {
                // j'affiche les erreurs rencontrées
                String message = saveResult.toString();
                JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * charge les données de demo
     */
    public void loadFileDemo() {
         boolean result = true;
        if(dataManager.isDirty()) {
            // je demande s'il faut sauvegarder les données
            result = askSaveData();
        }

        // je passe à la suite
        if (result) {
            FileBuffer fileBuffer;
            try {
                InputStream is = getClass().getResourceAsStream("/ancestris/modules/releve/file/bourbons.txt");
                fileBuffer = ReleveFileAncestrisV2.loadFile(is);
                dataManager.addRecords(fileBuffer, false, fileBuffer.getPlaces().get(0), 1);
                dataManager.setPlace(fileBuffer.getPlaces().get(0));
                setCurrentFile(null);
                dataManager.resetDirty();
                panelBirth.selectRow(0);
                panelMarriage.selectRow(0);
                panelDeath.selectRow(0);
                panelMisc.selectRow(0);
                panelAll.selectRow(0);

                setCurrentFile(new File(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.demoRecord")));

            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    /**
     * Memorise le nom du fichier courant
     * et affiche le nom dans le titre du TopCompenent
     */
    private void setCurrentFile ( File releveFile ) {
        currentFile = releveFile;
        dataManager.setCurrentFile(currentFile);
        String name;

        if (currentFile != null ) {
            name = currentFile.getName();
        } else {
            name = NbBundle.getMessage(ReleveTopComponent.class,"ReleveTopComponent.newRecordTitle");
        }
        setName(name);
        // je mets a jour le titre de la fenetre
        setToolTipText(NbBundle.getMessage(ReleveTopComponent.class, "HINT_ReleveTopComponent") + ": " + name);

        // je mets à jour le titre de la fenetre de l'editeur independant
        if (standaloneEditor != null) {
            standaloneEditor.setTitle(name);
        }

    }

    public File getCurrentFile() {
        return currentFile; 
    }
   
    ///////////////////////////////////////////////////////////////////////////
    // private class FileExtensionFilter
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Filters files using a specified extension.
     * used by getFileFromUser()
     */
    private static class FileExtensionFilter extends FileFilter {

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
            return f.getName().toLowerCase().endsWith("." + extension) || f.getName().toLowerCase().endsWith(".csv");
        }

        @Override
        public String getDescription() {
            return extension.toUpperCase() + " files";
        }
    }

    /**
     * affiche ou masque l'éditeur independant
     * @param show
     */
    @Override
    public void showStandalone() {        
        // je recupere l'index du releve selectionne dans la table
        int recordIndex;

        switch (jTabbedPane1.getSelectedIndex()) {
            case 0:
                recordIndex = panelBirth.getCurrentRecordIndex();
                break;
            case 1:
                recordIndex = panelMarriage.getCurrentRecordIndex();
                break;
            case 2:
                recordIndex = panelDeath.getCurrentRecordIndex();
                break;
            case 3:
                recordIndex = panelMisc.getCurrentRecordIndex();
                break;
            default:
                recordIndex = panelAll.getCurrentRecordIndex();
                break;

        }
        showStandalone(jTabbedPane1.getSelectedIndex(), recordIndex);
        
    }

    @Override
    public void showStandalone(int panelIndex, int recordIndex) {
        if (standaloneEditor == null) {
            standaloneEditor = new StandaloneEditor();
            standaloneEditor.setDataManager(dataManager, this,
                    panelBirth.getCurrentRecordIndex(),
                    panelMarriage.getCurrentRecordIndex(),
                    panelDeath.getCurrentRecordIndex(),
                    panelMisc.getCurrentRecordIndex(),
                    panelAll.getCurrentRecordIndex(),
                    jTabbedPane1.getSelectedIndex());
            // je lui donne le meme titre que ReleveTopComponent
            standaloneEditor.setTitle(this.getName());
        }

        standaloneEditor.toFront();
        standaloneEditor.setVisible(true);
        if ( standaloneEditor.getState() == java.awt.Frame.ICONIFIED) {
            standaloneEditor.setState(java.awt.Frame.NORMAL);
        }
        standaloneEditor.selectRecord(dataManager, panelIndex, recordIndex);        
    }

    /**
     * Affiche/masque le browser d'image dans l'editeur standalone
     * (transmets la commande a l'editeur standalone)
     * @param visible
     */
    @Override
    public void setBrowserVisible(boolean visible) {
        if ( standaloneEditor != null) {
            standaloneEditor.setBrowserVisible(visible);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        panelBirth = new ancestris.modules.releve.RelevePanel();
        panelMarriage = new ancestris.modules.releve.RelevePanel();
        panelDeath = new ancestris.modules.releve.RelevePanel();
        panelMisc = new ancestris.modules.releve.RelevePanel();
        panelAll = new ancestris.modules.releve.RelevePanel();

        setLayout(new java.awt.BorderLayout());

        jTabbedPane1.setVerifyInputWhenFocusTarget(false);

        panelBirth.setMinimumSize(new java.awt.Dimension(100, 100));
        panelBirth.setName(""); // NOI18N
        panelBirth.setRequestFocusEnabled(false);
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.panelBirth.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Birth.png")), panelBirth); // NOI18N

        panelMarriage.setMinimumSize(new java.awt.Dimension(100, 100));
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.panelMarriage.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Marriage.png")), panelMarriage); // NOI18N

        panelDeath.setMinimumSize(new java.awt.Dimension(100, 100));
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.panelDeath.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Death.png")), panelDeath); // NOI18N

        panelMisc.setMinimumSize(new java.awt.Dimension(100, 100));
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.panelMisc.TabConstraints.tabTitle"), new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/misc.png")), panelMisc); // NOI18N

        panelAll.setMinimumSize(new java.awt.Dimension(100, 100));
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.panelAll.TabConstraints.tabTitle"), panelAll); // NOI18N

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane jTabbedPane1;
    private ancestris.modules.releve.RelevePanel panelAll;
    private ancestris.modules.releve.RelevePanel panelBirth;
    private ancestris.modules.releve.RelevePanel panelDeath;
    private ancestris.modules.releve.RelevePanel panelMarriage;
    private ancestris.modules.releve.RelevePanel panelMisc;
    // End of variables declaration//GEN-END:variables
}
