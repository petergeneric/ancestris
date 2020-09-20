package ancestris.modules.releve.editor;

import ancestris.modules.releve.MenuCommandProvider;
import ancestris.modules.releve.RelevePanel;
import ancestris.modules.releve.imageBrowser.BrowserOptionsPanel;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.Record;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import org.openide.util.NbPreferences;

/*
 * StandaloneEditor.java
 *
 * Created on 1 avr. 2012, 17:54:42
 */

/**
 *
 * @author Michel
 */
public class StandaloneEditor extends javax.swing.JFrame {

    private boolean browserVisible = false;
    private boolean editorVisible = false;
    //private int editorWidth ;
    private int browserWidth ;


    /**
     * Cree une nouvelle fenetre
     * Recupere la taille et la position de la session précédente
     *
     * Remarque : A sa fermeture (windowClosing) la fenetre enregistre sa taille
     * et sa position et appelle ReleveTopComponent.setStandaloneEditor(false)
     * pour signaler sa fermeture
     */
    public StandaloneEditor() {
        initComponents();

        ImageIcon icon = new ImageIcon(StandaloneEditor.class.getResource("/ancestris/modules/releve/images/Releve.png"));
        setIconImage(icon.getImage());
        // J'applique un poids=1 pour que seule la largeur du composant de gauche soit modifiée quand on change la taille de la fenetre
        jSplitPane1.setResizeWeight(1.0);

        //setAlwaysOnTop(true);
        // je configure les editeurs
        panelBirth.setStandaloneMode();
        panelMarriage.setStandaloneMode();
        panelDeath.setStandaloneMode();
        panelMisc.setStandaloneMode();
        panelAll.setStandaloneMode();

        // je configure la taille de la fenetre
        browserVisible = BrowserOptionsPanel.getImageBrowserVisible();
        int editorWidth = Integer.parseInt(NbPreferences.forModule(StandaloneEditor.class).get("StandaloneEditorWidth", "300"));
        browserWidth = Integer.parseInt(NbPreferences.forModule(StandaloneEditor.class).get("StandaloneBrowserWidth", "600"));
        String size = NbPreferences.forModule(StandaloneEditor.class).get("StandaloneEditorSize", "300,450,0,0");
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        String[] dimensions = size.split(",");
        if ( dimensions.length >= 4 ) {
            int width = Integer.parseInt(dimensions[0]);
            if ( width > screen.width ) {
                width = screen.width -1;
            }
            if (width < 300) {
                width = 300;
            }
            int height = Integer.parseInt(dimensions[1]);
            if ( height > screen.height ) {
                height = screen.height -1;
            } 
            if ( height < 450) {
                height = 450;
            }
            int x = Integer.parseInt(dimensions[2]) - width ;
            if ( x + width > screen.width) {
                x = screen.width - width;
            } 
            if (x < 0 ) {
                x = (screen.width - width) /2;
            }
            int y = Integer.parseInt(dimensions[3]);
            if ( y + height >  screen.height ) {
                y = screen.height - height;
            }
            if ( height < 0 ) {
                height = (screen.height -height) / 2 ;
            }
            this.setBounds(x, y, width, height);
            this.validate();
        } else {
            this.setBounds(screen.width / 2 -100, screen.height / 2 - 100, 300, 450);
            this.validate();
        }
        jSplitPane1.getRightComponent().setSize(editorWidth, jSplitPane1.getRightComponent().getHeight());
        jSplitPane1.getLeftComponent().setSize(browserWidth, jSplitPane1.getLeftComponent().getHeight());

        // j'applique la taille de la fenetre avant de dimensionner jSplitPane1
        setBounds();

        // listener pour intercepter l'evenement de fermeture de la fenetre.

        addWindowListener( new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                componentClosed();
            }
        });

        // je crée les raccourcis pour créer un nouveau relevé
        String shortCut = "StandaloneShortcut";
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt N"), shortCut);
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt M"), shortCut);
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt D"), shortCut);
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt V"), shortCut);
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt T"), shortCut);
        jTabbedPane1.getActionMap().put(shortCut, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().toUpperCase().equals("N")) {
                    jTabbedPane1.setSelectedComponent(panelBirth);
                    panelBirth.createRecord();
                } else if (actionEvent.getActionCommand().toUpperCase().equals("M")) {
                    jTabbedPane1.setSelectedComponent(panelMarriage);
                    panelMarriage.createRecord();
                } else if (actionEvent.getActionCommand().toUpperCase().equals("D")) {
                    jTabbedPane1.setSelectedComponent(panelDeath);
                    panelDeath.createRecord();
                } else if (actionEvent.getActionCommand().toUpperCase().equals("V")) {
                    jTabbedPane1.setSelectedComponent(panelMisc);
                    panelMisc.createRecord();
                } else if (actionEvent.getActionCommand().toUpperCase().equals("T")) {
                    jTabbedPane1.setSelectedComponent(panelAll);
                    panelAll.createRecord();
                }

            }
        });

    }

     /**
     * sauvegarde de la configuration a la fermeture du composant
     */
    public void closeComponent() {
       componentClosed();
       dispose();
    }

    /**
     * sauvegarde de la configuration a la fermeture du composant
     */
    public void componentClosed() {
        // Chaque panel sauvegarde la largeur des colonnes
        panelBirth.componentClosed();
        panelMarriage.componentClosed();
        panelDeath.componentClosed();
        panelMisc.componentClosed();
        panelAll.componentClosed();

        // j'affiche la fenetre dans le mode normal pour récuperer la
        // position et la taille
        if (getExtendedState() != JFrame.NORMAL) {
            setExtendedState(JFrame.NORMAL);
        }
        // j'enregistre la taille dans les preferences
        int editorWidth = jSplitPane1.getRightComponent().getWidth();
        browserWidth = jSplitPane1.getLeftComponent().getWidth();
        String size = String.valueOf(this.getWidth()) + ","
                + String.valueOf(this.getHeight()) + ","
                + String.valueOf(this.getX() + this.getWidth()) + "," 
                + String.valueOf(this.getY());

        NbPreferences.forModule(StandaloneEditor.class).put("StandaloneEditorSize", size);
        NbPreferences.forModule(StandaloneEditor.class).put("StandaloneEditorWidth", String.valueOf(editorWidth));
        NbPreferences.forModule(StandaloneEditor.class).put("StandaloneBrowserWidth", String.valueOf(browserWidth));

        browserPanel1.componentClosed();
        
        this.setVisible(false);
    }

    /**
     * Initialise les modeles de donnée des 4 panneaux.
     *
     * Cette methode doit être appelée systematiquemnt  apres le contructeur
     * StandaloneEditor() pour que la fenetre puisse afficher les données d'un
     * modele.
     *
     */
    public void setDataManager(DataManager dataManager, MenuCommandProvider menuCommandProvider,
        int recordBirthIndex, int recordMarriageIndex, int recordDeathIndex, int recordMiscIndex, int recordAllIndex, int selectedPanel ) {

        panelBirth.setModel(dataManager, RelevePanel.PanelType.birth, menuCommandProvider);
        panelMarriage.setModel(dataManager, RelevePanel.PanelType.marriage, menuCommandProvider);
        panelDeath.setModel(dataManager, RelevePanel.PanelType.death, menuCommandProvider);
        panelMisc.setModel(dataManager, RelevePanel.PanelType.misc, menuCommandProvider);
        panelAll.setModel(dataManager, RelevePanel.PanelType.all, menuCommandProvider);

        // je selectionne le panel
        jTabbedPane1.setSelectedIndex(selectedPanel);
        // je selectionne le relevé
        panelBirth.selectRecord(recordBirthIndex);
        panelMarriage.selectRecord(recordMarriageIndex);
        panelDeath.selectRecord(recordDeathIndex);
        panelMisc.selectRecord(recordMiscIndex);
        panelAll.selectRecord(recordAllIndex);

    }

    /**
     * selection un relevé
     *  et affiche la photo si le browser est visible
     * @param record
     * @param recordIndex
     */
    public void selectRecord(DataManager dataManager, int panelIndex, int recordIndex) {

        Record record = dataManager.getRecord(recordIndex);
        if (record != null) {
            jTabbedPane1.setSelectedIndex(panelIndex);

            switch (panelIndex) {
                case 0:
                    panelBirth.selectRecord(recordIndex);
                    break;
                case 1:
                    panelMarriage.selectRecord(recordIndex);
                    break;
                case 2:
                    panelDeath.selectRecord(recordIndex);
                    break;
                case 3:
                    panelMisc.selectRecord(recordIndex);
                    break;
                default:
                    panelAll.selectRecord(recordIndex);
                    break;
            }
            if (browserVisible) {
                browserPanel1.showImage(dataManager.getCityName(), record.getFieldValue(FieldType.cote), record.getFieldValue(FieldType.freeComment));                
            }
        }
    }

    /**
     * show image
     */
    public void showImage(DataManager dataManager) {
        if (browserVisible) {
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
            Record record = dataManager.getRecord(recordIndex);
            if (record != null) {
                browserPanel1.selectImage(dataManager.getCityName(), record.getFieldValue(FieldType.cote), record.getFieldValue(FieldType.freeComment));
            }
        }
    }


//
//    public void udpdateEditorVisibleField() {
//        panelBirth.selectRecord(panelBirth.getCurrentRecordIndex());
//        panelMarriage.selectRecord(panelMarriage.getCurrentRecordIndex());
//        panelDeath.selectRecord(panelDeath.getCurrentRecordIndex());
//        panelMisc.selectRecord(panelMisc.getCurrentRecordIndex());
//        panelAll.selectRecord(panelAll.getCurrentRecordIndex());
//    }


    

    /**
     * initialise le titre de la fenetre
     * @param fileName
     */
    @Override
    public void setTitle(String fileName) {
        super.setTitle(fileName);
    }

    public void setBrowserVisible(boolean visible) {
        browserVisible = visible;
        setBounds();
    }
    
    public void toggleBrowserVisible() {
        // j'inverse la visbilité de la visionneuse d'image
        browserVisible = ! browserVisible ;
        setBounds();
    }

    private void setBounds() {
        //if( browserVisible) {
        if( browserVisible) {
            int dividerSize = 5;
            int leftx =this.getX() + this.getWidth();
            int externalBorderWidth = 2;
            int width = jSplitPane1.getRightComponent().getWidth() + browserWidth + dividerSize + externalBorderWidth ;
            int x = leftx - width;
            // j'affiche le browser d'image
            jSplitPane1.getLeftComponent().setVisible(true);
            jSplitPane1.setDividerSize(dividerSize);
            setBounds(x, this.getY(), width, this.getHeight());
        } else {
            browserWidth = jSplitPane1.getLeftComponent().getWidth();
            int leftx =this.getX() + this.getWidth();
            int editorWidth = jSplitPane1.getRightComponent().getWidth();
            int externalBorderWidth = this.getWidth() - this.getContentPane().getWidth() + 2;
            int width = editorWidth + jSplitPane1.getDividerSize() + externalBorderWidth;
            int x = leftx - width;
            // je masque le browser
            jSplitPane1.getLeftComponent().setVisible(false);
            jSplitPane1.setDividerSize(0);
            jSplitPane1.getRightComponent().setSize(editorWidth, jSplitPane1.getRightComponent().getHeight());
            setBounds(x, this.getY(), width, this.getHeight());
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

        jSplitPane1 = new javax.swing.JSplitPane();
        browserPanel1 = new ancestris.modules.releve.imageBrowser.BrowserPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        panelBirth = new ancestris.modules.releve.RelevePanel();
        panelMarriage = new ancestris.modules.releve.RelevePanel();
        panelDeath = new ancestris.modules.releve.RelevePanel();
        panelMisc = new ancestris.modules.releve.RelevePanel();
        panelAll = new ancestris.modules.releve.RelevePanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

        jSplitPane1.setLeftComponent(browserPanel1);

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.birthEditor.TabConstraints.tabTitle"), panelBirth); // NOI18N
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.marriageEditor.TabConstraints.tabTitle"), panelMarriage); // NOI18N
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.deathEditor.TabConstraints.tabTitle"), panelDeath); // NOI18N
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.miscEditor.TabConstraints.tabTitle"), panelMisc); // NOI18N
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.panelAll.TabConstraints.tabTitle"), panelAll); // NOI18N

        jSplitPane1.setRightComponent(jTabbedPane1);

        getContentPane().add(jSplitPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.releve.imageBrowser.BrowserPanel browserPanel1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private ancestris.modules.releve.RelevePanel panelAll;
    private ancestris.modules.releve.RelevePanel panelBirth;
    private ancestris.modules.releve.RelevePanel panelDeath;
    private ancestris.modules.releve.RelevePanel panelMarriage;
    private ancestris.modules.releve.RelevePanel panelMisc;
    // End of variables declaration//GEN-END:variables

}
