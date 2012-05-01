package ancestris.modules.releve.editor;

import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.ReleveTopComponent;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

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
    /**
     * Cree une nouvelle fenetre
     * Recupere la taille et la position de la session précédente
     *
     * Remarque : A sa fermeture (windowClosing) la fenetre enregistre sa taille
     * et sa position et appelle ReleveTopComponent.setStandaloneEditor(false)
     * pour signaler sa fermeture
     */
    public StandaloneEditor() {
        //super(new javax.swing.JFrame(), false);
        initComponents();
        ImageIcon icon = new ImageIcon(ReleveTopComponent.class.getResource("/ancestris/modules/releve/images/Releve.png"));
        setIconImage(icon.getImage());
        setAlwaysOnTop(true);
        // je configure les editeurs
        birthEditor.setStandaloneMode();
        marriageEditor.setStandaloneMode();
        deathEditor.setStandaloneMode();
        miscEditor.setStandaloneMode();

        // je configure la taille de la fenetre
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        String size = NbPreferences.forModule(ReleveTopComponent.class).get("StandaloneEditorSize", "200,400,0,0");
        String[] dimensions = size.split(",");
        if ( dimensions.length >= 4 ) {
            int width = Integer.parseInt(dimensions[0]);
            int height = Integer.parseInt(dimensions[1]);
            int x = Integer.parseInt(dimensions[2]);
            int y = Integer.parseInt(dimensions[3]);
            if ( width > 100 && height > 100 ) {
                setSize(width, height);
            }
            if ( x < 10 || x > screen.width -10) {
                x = (screen.width / 2) - (width / 2);
            }
            if ( y < 10 || y > screen.height -10) {
                y = (screen.height / 2) - (height / 2);
            }
            setBounds(x, y, width, height);
        } else {
            setBounds(screen.width / 2 -100, screen.height / 2- 100,200, 400);
        }

        // listener pour intercepter l'evenement de fermeture de la fenetre.
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // j'enregistre la taille dans les preferences
                String size ;
                size= String.valueOf(e.getWindow().getWidth())+","
                        + String.valueOf(e.getWindow().getHeight()) + ","
                        + String.valueOf(e.getWindow().getLocation().x + ","
                        + String.valueOf(e.getWindow().getLocation().y)
                        );
                
                NbPreferences.forModule(ReleveTopComponent.class).put("StandaloneEditorSize", size);
                TopComponent tc = WindowManager.getDefault().findTopComponent("ReleveTopComponent");
                ((ReleveTopComponent)tc).standaloneEditorClosed();
            }
        });


        


    }

    /**
     * Initialise les modeles de donnée des 4 panneaux.
     *
     * Cette methode doit être appelée systematiquemnt  apres le contructeur
     * StandaloneEditor() pour que la fenetre puisse afficher les données d'un
     * modele.
     *
     * @param releveBirthModel
     * @param releveMarriageModel
     * @param releveDeathModel
     * @param releveMiscModel
     */
    public void setDataManager(DataManager dataManager) {
        birthEditor.setModel(dataManager, DataManager.ModelType.birth);
        marriageEditor.setModel(dataManager, DataManager.ModelType.marriage);
        deathEditor.setModel(dataManager, DataManager.ModelType.death);
        miscEditor.setModel(dataManager, DataManager.ModelType.misc);

        // je selection le premier releve.
        selectRecord(0, 0, 0, 0, 0);

        // je crée les raccourcis pour créer un noueau relevé
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt N"), jTabbedPane1);
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt M"), jTabbedPane1);
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt D"), jTabbedPane1);
        jTabbedPane1.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt V"), jTabbedPane1);
        jTabbedPane1.getActionMap().put(jTabbedPane1, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if ( actionEvent.getActionCommand().equals("n") ) {
                    jTabbedPane1.setSelectedComponent(birthEditor);
                    birthEditor.createRecord();
                } else if ( actionEvent.getActionCommand().equals("m") ) {
                    jTabbedPane1.setSelectedComponent(marriageEditor);
                    marriageEditor.createRecord();
                } else if ( actionEvent.getActionCommand().equals("d") ) {
                    jTabbedPane1.setSelectedComponent(deathEditor);
                    deathEditor.createRecord();
                } else if ( actionEvent.getActionCommand().equals("d") ) {
                    jTabbedPane1.setSelectedComponent(miscEditor);
                    miscEditor.createRecord();
                }
            }
        });
    }

    /**
     * initialise le titre de la fenetre
     * @param fileName
     */
    @Override
    public void setTitle(String fileName) {
        super.setTitle(fileName);
    }


    /**
     * selectionne les releves a afficher dans l'editeur
     *
     * @param recordBirthIndex
     * @param recordMarriageIndex
     * @param recordDeathIndex
     * @param recordMiscIndex
     */
    public void selectRecord(int recordBirthIndex, int recordMarriageIndex, int recordDeathIndex, int recordMiscIndex, int selectedPanel) {
        birthEditor.selectRecord(recordBirthIndex);
        marriageEditor.selectRecord(recordMarriageIndex);
        deathEditor.selectRecord(recordDeathIndex);
        miscEditor.selectRecord(recordMiscIndex);
        // je selectionne l'onglet
        jTabbedPane1.setSelectedIndex(selectedPanel);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        birthEditor = new ancestris.modules.releve.editor.ReleveEditor();
        marriageEditor = new ancestris.modules.releve.editor.ReleveEditor();
        deathEditor = new ancestris.modules.releve.editor.ReleveEditor();
        miscEditor = new ancestris.modules.releve.editor.ReleveEditor();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        birthEditor.setPreferredSize(new java.awt.Dimension(180, 100));
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.birthEditor.TabConstraints.tabTitle"), birthEditor); // NOI18N

        marriageEditor.setPreferredSize(new java.awt.Dimension(180, 100));
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.marriageEditor.TabConstraints.tabTitle"), marriageEditor); // NOI18N
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.deathEditor.TabConstraints.tabTitle"), deathEditor); // NOI18N
        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(StandaloneEditor.class, "StandaloneEditor.miscEditor.TabConstraints.tabTitle"), miscEditor); // NOI18N

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ancestris.modules.releve.editor.ReleveEditor birthEditor;
    private ancestris.modules.releve.editor.ReleveEditor deathEditor;
    private javax.swing.JTabbedPane jTabbedPane1;
    private ancestris.modules.releve.editor.ReleveEditor marriageEditor;
    private ancestris.modules.releve.editor.ReleveEditor miscEditor;
    // End of variables declaration//GEN-END:variables

}
