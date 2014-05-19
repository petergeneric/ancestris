/*
 * MergeDialog.java
 *
 * Created on 30 avr. 2012, 18:55:26
 */

package ancestris.modules.releve.dnd;

import ancestris.modules.releve.dnd.MergeRecord.MergeParticipantType;
import ancestris.modules.releve.dnd.ViewWrapperManager.AbstractViewWrapper;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.Source;
import genj.gedcom.UnitOfWork;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.openide.util.NbPreferences;

/**
 * Cette classe est le point d'entrée du package.
 * Elle permet d'insérer un relevé dans une entité d'un fichier GEDCOM.
 * @author Michel
 */
public class MergeDialog extends javax.swing.JFrame implements EntityActionManager {

    private AbstractViewWrapper dndSourceComponent = null;
    private Entity selectedEntity = null;
    private Gedcom gedcom=null;
    private MergeRecord mergeRecord = null;
    private boolean showAllParents = false;
    List<MergeModel> mergeModelList;

    /**
    * factory de la fenetre
    * @param location
    * @param selectedEntity
    * @param record
    */
    public static MergeDialog show(Component sourceComponenent, final Gedcom gedcom, final Entity selectedEntity, final MergeRecord mergeRecord, boolean visible) {

        final MergeDialog dialog = new MergeDialog();
        try {
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    dialog.setVisible(false);
                    dialog.componentClosed();
                    dialog.dispose();
                }
            });
            dialog.setVisible(visible);
            dialog.initData(sourceComponenent, mergeRecord, gedcom, selectedEntity);
            return dialog;
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            //dialog.componentClosed();
            dialog.dispose();
            Toolkit.getDefaultToolkit().beep();
            String title = "";
            if (ex.getMessage() == null) {
                JOptionPane.showMessageDialog(sourceComponenent, ex.getClass().getName()+ " See console log", title, JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(sourceComponenent, ex.getMessage(), title, JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }

    }

    /**
     * Constructeur 
     */
    protected MergeDialog() {
        //super(SwingUtilities.windowForComponent(parent));
        setLayout(new java.awt.BorderLayout());
        initComponents();
        setAlwaysOnTop(true);

        // je configure la taille de la fenetre
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        String size = NbPreferences.forModule(MergeDialog.class).get("MergeDialogSize", "560,600,0,0");
        String[] dimensions = size.split(",");
        if ( dimensions.length >= 4 ) {
            int width = Integer.parseInt(dimensions[0]);
            int height = Integer.parseInt(dimensions[1]);
            int x = Integer.parseInt(dimensions[2]);
            int y = Integer.parseInt(dimensions[3]);
            if ( width < 100 ) {
                width = 100;
            }
            if ( height < 100 ) {
                height = 100;
            }
            if ( x < 10 || x > screen.width -10) {
                x = (screen.width / 2) - (width / 2);
            }
            if ( y < 10 || y > screen.height -10) {
                y = (screen.height / 2) - (height / 2);
            }
            setBounds(x, y, width, height);
        } else {
            setBounds(screen.width / 2 -100, screen.height / 2- 100, 300, 450);
        }

        //String splitHeight = NbPreferences.forModule(MergeDialog.class).get("MergeDialogSplitHeight", "90");
        //jSplitPane0.setDividerLocation(Integer.parseInt(splitHeight));
        showAllParents =  Boolean.parseBoolean(NbPreferences.forModule(MergeDialog.class).get("MergeDialogShowAllParents", "false"));
    }

    /**
     * cette methode est appelée à la fermeture de la fenetre
     * Elle enregistre les preference de l'utilisateur.
     */
    protected void componentClosed() {
        // j'enregistre les preferences de la table
        mergePanel1.componentClosed();

        // j'enregistre la taille et la position
        String size;
        size = String.valueOf(getWidth()) + ","
                + String.valueOf(getHeight()) + ","
                + String.valueOf(getLocation().x + ","
                + String.valueOf(getLocation().y));

        NbPreferences.forModule(MergeDialog.class).put("MergeDialogSize", size);
        NbPreferences.forModule(MergeDialog.class).put("MergeDialogSplitHeight", String.valueOf(jSplitPane0.getDividerLocation()));
        NbPreferences.forModule(MergeDialog.class).put("MergeDialogShowAllParents", String.valueOf(showAllParents));
    }

    /**
     * Initialise le modèle de données du comparateur
     * @param selectedEntity
     * @param record
     */
    protected void initData(Component sourceComponent, MergeRecord mergeRecord, Gedcom gedcom, Entity selectedEntity ) throws Exception {
        this.dndSourceComponent = ViewWrapperManager.createViewWrapper(sourceComponent);
        this.mergeRecord = mergeRecord;
        this.gedcom = gedcom;
        this.selectedEntity = selectedEntity;

        // j'affiche l'icone correspondant type de relevé de la fenetre
        String ressourceName ;
        switch (mergeRecord.getType()) {
            case Birth:
                ressourceName="/ancestris/modules/releve/images/Birth.png";
                break;
            case Marriage:
                ressourceName="/ancestris/modules/releve/images/Marriage.png";
                break;
            case Death:
                ressourceName="/ancestris/modules/releve/images/Death.png";
                break;
            default:
                ressourceName="/ancestris/modules/releve/images/misc.png";
                break;
        }
        ImageIcon icon = new ImageIcon(MergeDialog.class.getResource(ressourceName));
        setIconImage(icon.getImage());

        // je recupere les modeles contenant les entites compatibles avec le relevé
        mergeModelList = MergeModel.createMergeModel(mergeRecord, gedcom, selectedEntity, showAllParents);
        // j'affiche les modeles et selectionne le premier modele de la liste
        // (j'affiche le panel1 en dernier pour que le partipant 1 soit selectionné dans l'arbre)
        mergePanel2.initData( mergeModelList, selectedEntity, this, MergeParticipantType.participant2);
        mergePanel1.initData( mergeModelList, selectedEntity, this, MergeParticipantType.participant1);
        if (mergePanel2.getCurrentModel() == null ) {
            // si le deuxième panneau est vide , j'affiche le premier panneau dans toute la fenetre.
            jSplitPane0.setDividerLocation(getHeight());
        } else {
            // j'affiche chaque panneau dans chaque moitié de la fenetre
            jSplitPane0.setDividerLocation((getHeight()-jPanelButton.getHeight()*2)/2);
        }
        
    }

    /**
     * Re-initialise le modele de données du comparateur
     * @param selectedEntity
     * @param record
     */
    protected void updateData( boolean showNewParents ) throws Exception {
        this.showAllParents = showNewParents;
        List<MergeModel> models;

        // je recupere les modeles contenant les entites compatibles avec le relevé
        models = MergeModel.createMergeModel(mergeRecord, gedcom, selectedEntity, showNewParents);
        // j'affiche les modeles et selectionne le premier modele de la liste
        mergePanel1.initData(models, selectedEntity, this, MergeParticipantType.participant1);
        mergePanel2.initData( models, selectedEntity, this, MergeParticipantType.participant2);
        if (mergePanel2.getCurrentModel() == null ) {
            jSplitPane0.setDividerLocation(getHeight());
        } else {
            jSplitPane0.setDividerLocation((getHeight()-jPanelButton.getHeight())/2);
        }
    }

    /**
     * accesseur a la propriete showAllParents
     * @return
     */
    boolean getShowAllParents() {
        return showAllParents;
    }



    
    /**
     * affiche l'entité dans le dnd source
     * @param entity
     * @param setRoot positionne l'entité comme racine de l'arbre si la source de Dnd est un arbre
     */
    @Override
    public void setRoot(Entity entity) {
        // je declare l'entité comme racine de l'arbre
        dndSourceComponent.setRoot(entity);
    }
    
    /**
     * affiche l'entité dans le dnd source
     * @param entity
     * @param setRoot positionne l'entité comme racine de l'arbre
     */
    @Override
    public void show(Entity entity) {
        dndSourceComponent.show(entity);
    }
    
    /**
     * sélectionne une source pour remplacer celle qui est proposée dans les propositions
     */
    @Override
    public void selectSource() {
        final MergeModel currentModel1 = mergePanel1.getCurrentModel();
        Object entityObject = currentModel1.getRow(MergeModel.RowType.EventSource).entityObject;
        String sourceTitle = currentModel1.record.getEventSourceTitle();
        if (entityObject instanceof Source ) {
            sourceTitle = ((Source) entityObject).getTitle();
        }
        Source source = RecordSourceConfigDialog.show(this, currentModel1.record.getFileName(), sourceTitle, gedcom);
        if (source != null ) {
            mergeRecord.calculateSourceTitle();
            
            // je copie la source dans tous les modeles car la source est commune à tous les modeles
            for( MergeModel mergeModel : mergeModelList) {                
                mergeModel.addRowSource(source);
                mergeModel.fireTableDataChanged();
            }
            // j'enregistre la paire fileName,source dans les preferences        
            MergeOptionPanel.SourceModel.getModel().add(currentModel1.record.getFileName(), source.getTitle());
            MergeOptionPanel.SourceModel.getModel().savePreferences();
        }
        
    }
    
    /**
     * pour lancer le test avec junit
     * @throws Exception
     */
    protected void copyRecordToEntity() throws Exception {
        final MergeModel currentModel1 = mergePanel1.getCurrentModel();
        final MergeModel currentModel2 = mergePanel2.getCurrentModel();
        
        currentModel1.getGedcom().doUnitOfWork(new UnitOfWork() {
            @Override
            public void perform(Gedcom gedcom) {
                long beforeChange = currentModel1.getGedcom().getLastChange()!=null ? currentModel1.getGedcom().getLastChange().getTime() : 0;
                try {
                    Property associatedProperty1 = currentModel1.copyRecordToEntity();
                    if (currentModel2 != null ) {
                        Property associatedProperty2 = currentModel2.copyRecordToEntity();
                        currentModel2.copyAssociation(associatedProperty1, associatedProperty2);
                    }
                } catch (Exception throwable) {
                    // je constitue la commande pour annuler les modifications
                    long afterChange = currentModel1.getGedcom().getLastChange()!=null ? currentModel1.getGedcom().getLastChange().getTime() : 0;
                    if ( afterChange > beforeChange ) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                currentModel1.getGedcom().undoUnitOfWork(false);
                            }
                        });
                        
                    }
                    throwable.printStackTrace();
                    throw new RuntimeException(throwable);
                }
            }
        });
    }

   /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane0 = new javax.swing.JSplitPane();
        mergePanel1 = new ancestris.modules.releve.dnd.MergePanel();
        mergePanel2 = new ancestris.modules.releve.dnd.MergePanel();
        jPanelButton = new javax.swing.JPanel();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jSplitPane0.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane0.setMaximumSize(null);

        mergePanel1.setPreferredSize(new java.awt.Dimension(300, 100));
        jSplitPane0.setTopComponent(mergePanel1);

        mergePanel2.setPreferredSize(new java.awt.Dimension(300, 100));
        jSplitPane0.setBottomComponent(mergePanel2);

        getContentPane().add(jSplitPane0, java.awt.BorderLayout.CENTER);

        jButtonOK.setText(org.openide.util.NbBundle.getMessage(MergeDialog.class, "MergeDialog.jButtonOK.text")); // NOI18N
        jButtonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOKActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonOK);

        jButtonCancel.setText(org.openide.util.NbBundle.getMessage(MergeDialog.class, "MergeDialog.jButtonCancel.text")); // NOI18N
        jButtonCancel.setRolloverEnabled(false);
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonCancel);

        getContentPane().add(jPanelButton, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        componentClosed();
        setVisible(false);
        dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    /**
     * copie les données du relevé dans l'entité sélectionnée
     * puis affiche l'entité dans l'arbre dynamique (si la cible du dnd est l'arbre)
     * puis ferme la fenêtre 
     * @param evt
     */
    private void jButtonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOKActionPerformed
        try {
            // je copie les données du releve dans gedcom
            copyRecordToEntity();

            // j'affiche l'entité dans l'arbre dynamic
            final MergeModel currentModel = mergePanel1.getCurrentModel();
            if (currentModel.getSelectedEntity() != null) {
                if (currentModel.getSelectedEntity() instanceof Indi) {
                    Indi selectedIndi = (Indi) currentModel.getSelectedEntity();
                    if (selectedIndi.getFamilyWhereBiologicalChild() != null) {
                        // je centre sur la famille des parents
                        setRoot(selectedIndi.getFamilyWhereBiologicalChild());
                        if (mergeRecord.getType() == MergeRecord.RecordType.Birth
                                || mergeRecord.getType() == MergeRecord.RecordType.Death) {
                            // j'affiche l'individu
                            show(selectedIndi);
                        }

                    } else {
                        // je centre l'arbre sur l'entité
                        setRoot(currentModel.getSelectedEntity());
                    }
                } else {
                    // je centre l'arbre sur l'entité
                    setRoot(currentModel.getSelectedEntity());
                }    
            }
            componentClosed();
            setVisible(false);
            dispose();
        } catch (Exception throwable) {
            // je ferme la fenetre avant d'afficher le message d'erreur
            componentClosed();
            setVisible(false);
            dispose();
            Toolkit.getDefaultToolkit().beep();
            String title = "";
            throwable.printStackTrace();
            if (throwable.getMessage() == null) {
                JOptionPane.showMessageDialog(null, throwable.getClass().getName()+ " See console log", title, JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, throwable.getMessage(), title, JOptionPane.ERROR_MESSAGE);
            }
        }


    }//GEN-LAST:event_jButtonOKActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JPanel jPanelButton;
    private javax.swing.JSplitPane jSplitPane0;
    private ancestris.modules.releve.dnd.MergePanel mergePanel1;
    private ancestris.modules.releve.dnd.MergePanel mergePanel2;
    // End of variables declaration//GEN-END:variables

   
}
