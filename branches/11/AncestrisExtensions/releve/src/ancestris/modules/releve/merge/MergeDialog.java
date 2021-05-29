/*
 * MergeDialog.java
 *
 * Created on 30 avr. 2012, 18:55:26
 */

package ancestris.modules.releve.merge;

import ancestris.modules.releve.dnd.TransferableRecord;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.text.MessageFormat;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * Cette classe est le point d'entrée du package.
 * Elle permet d'insérer un relevé dans une entité d'un fichier GEDCOM.
 * @author Michel
 */
public class MergeDialog extends javax.swing.JFrame implements ListDataListener {
    private final MergeManager m_mergeManager;
    private final JPopupMenu m_popupMenu = new JPopupMenu();

    /**
    * factory de la fenetre
    * @param location
    * @param selectedEntity
    * @param record
    */
    public static MergeDialog show(Component sourceComponent, final Gedcom gedcom, final Entity selectedEntity, final TransferableRecord.TransferableData transferableData, boolean visible) {

        final MergeDialog dialog = new MergeDialog(sourceComponent, gedcom, selectedEntity, transferableData);

        try {
            dialog.initData();
            dialog.setVisible(visible);
            return dialog;
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            //dialog.componentClosed();
            dialog.dispose();
            Toolkit.getDefaultToolkit().beep();
            String title = "MergeDialog";
            if (ex.getMessage() == null) {
                ex.printStackTrace(System.out);
                JOptionPane.showMessageDialog(sourceComponent, ex.getClass().getName() + " See console log", title, JOptionPane.ERROR_MESSAGE);
            } else {
                ex.printStackTrace(System.out);
                JOptionPane.showMessageDialog(sourceComponent, ex.getMessage(), title, JOptionPane.ERROR_MESSAGE);
            }
            return null;
        }

    }

    /**
     * Constructeur
     */
    protected MergeDialog(Component sourceComponent, Gedcom gedcom, Entity selectedEntity, TransferableRecord.TransferableData transferableData) {
        m_mergeManager = new MergeManager(transferableData, gedcom, selectedEntity);

        setLayout(new java.awt.BorderLayout());
        initComponents();
        setAlwaysOnTop(true);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                setVisible(false);
                componentClosed();
                dispose();
            }
        });

        JMenuItem menuItemShowLog   = new JMenuItem(NbBundle.getMessage(MergeDialog.class, "MergeDialog.menu.showLog"));
        menuItemShowLog.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/log-16.png")));
        menuItemShowLog.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateLog(true);
            }
        });
        m_popupMenu.add(menuItemShowLog);

        JMenuItem menuItemCopyLogFileName = new JMenuItem(NbBundle.getMessage(MergeDialog.class, "MergeDialog.menu.copyLogFileName"));
        menuItemCopyLogFileName.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/clipboard-16.png")));
        menuItemCopyLogFileName.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateLog(false);
            }
        });
        m_popupMenu.add(menuItemCopyLogFileName);

        JCheckBoxMenuItem menuItemShowAllParents = new JCheckBoxMenuItem(NbBundle.getMessage(MergeDialog.class, "MergePanel.jToggleButtonShowAllParents.toolTipText"));
        menuItemShowAllParents.setState(MergeManager.getShowAllParents());
        menuItemShowAllParents.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateOption( ((JCheckBoxMenuItem) (evt.getSource()) ).isSelected() );
            }
        });
        m_popupMenu.add(menuItemShowAllParents);

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

    }

    /**
     * cette methode est appelée à la fermeture de la fenetre
     * Elle enregistre les preference de l'utilisateur.
     */
    private void componentClosed() {
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
        NbPreferences.forModule(MergeDialog.class).put("MergeDialogShowAllParents", String.valueOf(MergeManager.getShowAllParents()) );
    }

    /**
     * Initialise le modèle de données du comparateur
     * @param selectedEntity
     * @param record
     */
    private void initData() throws Exception {
        // je cree les propositions
        m_mergeManager.createProposals();

        // j'affiche l'icone correspondant type de relevé de la fenetre
        String ressourceName ;
        switch (m_mergeManager.getMergeRecordType()) {
            case BIRTH:
                ressourceName="/ancestris/modules/releve/images/Birth.png";
                break;
            case MARRIAGE:
                ressourceName="/ancestris/modules/releve/images/Marriage.png";
                break;
            case DEATH:
                ressourceName="/ancestris/modules/releve/images/Death.png";
                break;
            default:
                ressourceName="/ancestris/modules/releve/images/misc.png";
                break;
        }
        ImageIcon icon = new ImageIcon(MergeDialog.class.getResource(ressourceName));
        setIconImage(icon.getImage());

        // j'affiche le titre de la fenetre
        setTitle( makeTitle() );

        // je reseigne JLabelTitle
        if ( m_mergeManager.getProposalList1().getNbAllProposal() > 0 ) {
            if( m_mergeManager.getSelectedEntity() == null ) {
                // j'affiche le nombre de propositions
                JLabelProposalList.setText(NbBundle.getMessage(MergePanel.class, "MergeDialog.ProposalList.all", m_mergeManager.getProposalList1().getSize() )); // NOI18N
                jCheckBoxShowOtherProposal.setVisible(false);
            } else {
                JLabelProposalList.setText(NbBundle.getMessage(MergePanel.class,"MergeDialog.ProposalList.selectedIndi", m_mergeManager.getProposalList1().getNbProposalWithSelectedEntity(), m_mergeManager.getSelectedEntity() )); // NOI18N
                if( m_mergeManager.getProposalList1().getNbBestProposal() > 0 ) {
                    // voir %d autres propositions dont %s meilleures
                    jCheckBoxShowOtherProposal.setVisible(true);
                    jCheckBoxShowOtherProposal.setSelected(false);
                    jCheckBoxShowOtherProposal.setText(NbBundle.getMessage(MergePanel.class, "MergeDialog.ProposalList.better",
                           m_mergeManager.getProposalList1().getNbAllProposal() - m_mergeManager.getProposalList1().getNbProposalWithSelectedEntity(),
                           m_mergeManager.getProposalList1().getNbBestProposal()  ));
                } else if( m_mergeManager.getProposalList1().getNbAllProposal() > m_mergeManager.getProposalList1().getNbProposalWithSelectedEntity() ) {
                    // voir %d autres proppositions
                    jCheckBoxShowOtherProposal.setVisible(true);
                    jCheckBoxShowOtherProposal.setSelected(false);
                    jCheckBoxShowOtherProposal.setText(NbBundle.getMessage(MergePanel.class,"MergeDialog.ProposalList.other",
                           m_mergeManager.getProposalList1().getNbAllProposal() - m_mergeManager.getProposalList1().getNbProposalWithSelectedEntity() ));
                } else {
                    // il n'y a pas d'autre proposition
                    jCheckBoxShowOtherProposal.setVisible(false);
                }
            }
        } else {
            // s'il n'y a aucune proposition, j'affiche un message d'information dans le premier panneau
            JLabelProposalList.setText("<html>" + NbBundle.getMessage(MergePanel.class, "MergeDialog.ProposalList.empty") + "</html>");// je désactive le bouton OK s'il n'y a pas de proposition
            jButtonOK.setEnabled(false);
            jCheckBoxShowOtherProposal.setVisible(false);
        }

        // je declare les listener
        m_mergeManager.getProposalList1().addListDataListener(this);
        m_mergeManager.getProposalList2().addListDataListener(this);

        // je notitifie les listeners pour afficher les données
        m_mergeManager.getProposalList1().fireContentsChanged();
        m_mergeManager.getProposalList2().fireContentsChanged();


    }

    void updateData() {
        // j'affiche les modeles et selectionne le premier modele de la liste
        // (j'affiche le panel1 en dernier pour que le partipant 1 soit selectionné dans l'arbre)
        mergePanel2.initData( m_mergeManager.getProposalList2(), false);
        mergePanel1.initData( m_mergeManager.getProposalList1(), true);
        if (m_mergeManager.getProposalList2().getSize() > 0 ) {
            // j'affiche chaque panneau dans chaque moitié de la fenetre
            jSplitPane0.setDividerLocation((getHeight()-jPanelButton.getHeight()*2)/2);
        } else {
            // si le deuxième panneau est vide , j'affiche le premier panneau dans toute la fenetre.
            jSplitPane0.setDividerLocation(getHeight());
            // je masque le deuxième panneau
            jSplitPane0.getBottomComponent().setVisible(false);
            jSplitPane0.setDividerSize(0);
        }

    }

    /**
     * Re-initialise le modele de données du comparateur
     * @param selectedEntity
     * @param record
     */
    protected void updateOption( boolean showNewParents ) {
        try {
            m_mergeManager.setShowAllParents(showNewParents);
            // je notitifie les listeners pour afficher les données
            m_mergeManager.getProposalList1().fireContentsChanged();
            m_mergeManager.getProposalList2().fireContentsChanged();
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            Toolkit.getDefaultToolkit().beep();
            String title = "MergeDialog.updateData";
            if (ex.getMessage() == null) {
                JOptionPane.showMessageDialog(this, ex.getClass().getName() + " See console log", title, JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, ex.getMessage(), title, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implements  ListModel<MergeModel>
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void intervalAdded(ListDataEvent event) {
        updateData();
    }

    @Override
    public void intervalRemoved(ListDataEvent event) {
        updateData();
    }

    @Override
    public void contentsChanged(ListDataEvent event) {
        updateData();
    }

    /**
     * Re-initialise le modele de données du comparateur
     */
    protected void generateLog(boolean showLog){
        try {
            m_mergeManager.generateLog();
            MergeLogger.copyFileNameToClipboard();
            if(showLog) {
                MergeLogger.showLog();
            }
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            Toolkit.getDefaultToolkit().beep();
            String title = "MergeDialog.generateLog";
            if (ex.getMessage() == null) {
                JOptionPane.showMessageDialog(this, ex.getClass().getName() + " See console log", title, JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, ex.getMessage(), title, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showPopupMenu(Component invoker, int x, int y) {
        m_popupMenu.show(invoker, x, y);
    }

    /**
     * retourne les noms du releve pour constituer le titre de la fenetre principale
     * @return
     */
    private String makeTitle() {
        MergeRecord record = m_mergeManager.getMergeRecord();
        if( record == null) {
            return "";
        }

        switch (m_mergeManager.getMergeRecord().getEventTypeTag() )  {
            case BIRT: {
                String message = record.getIndi().getFirstName() + " " + record.getIndi().getLastName() + " " + record.getEventDate().getDisplayValue();
                return NbBundle.getMessage(MergeDialog.class, "MergeDialog.title.birth", message);
            }
            case DEAT:
            {
                String message = record.getIndi().getFirstName() + " " + record.getIndi().getLastName() + " " + record.getEventDate().getDisplayValue();
                return NbBundle.getMessage(MergeDialog.class, "MergeDialog.title.death", message);
            }
            case MARR:
            {
                String husband = record.getIndi().getFirstName() + " "+ record.getIndi().getLastName();
                String wife = record.getWife().getFirstName() + " "+ record.getWife().getLastName()+ " " + record.getEventDate().getDisplayValue();
                return  NbBundle.getMessage(MergeDialog.class, "MergeDialog.title.marriage", husband, wife);
            }
            case MARC:
            case MARB:
            case MARL:
            {
                String husband = record.getIndi().getFirstName() + " " + record.getIndi().getLastName();
                String wife = record.getWife().getFirstName() + " " + record.getWife().getLastName() + " " + record.getEventDate().getDisplayValue();
                return MessageFormat.format("{0}: {1} x {2}", record.getEventType(), husband, wife);
            }
            case WILL: {
                String message = record.getIndi().getFirstName() + " " + record.getIndi().getLastName() + " " + record.getEventDate().getDisplayValue();
                return NbBundle.getMessage(MergeDialog.class, "MergeDialog.title.miscWill", message);
            }
            case EVEN:
            default:
            {
                String message1 = record.getIndi().getFirstName() + " " + record.getIndi().getLastName();
                String message2 = record.getWife().getFirstName() + " " + record.getWife().getLastName() + " " + record.getEventDate().getDisplayValue();
                return NbBundle.getMessage(MergeDialog.class, "MergeDialog.title.misc", record.getEventType(), message1, message2);
            }
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

        jPanelToolbar = new javax.swing.JPanel();
        JLabelProposalList = new javax.swing.JLabel();
        jButtonMenu = new javax.swing.JButton();
        jCheckBoxShowOtherProposal = new javax.swing.JCheckBox();
        jSplitPane0 = new javax.swing.JSplitPane();
        mergePanel1 = new ancestris.modules.releve.merge.MergePanel();
        mergePanel2 = new ancestris.modules.releve.merge.MergePanel();
        jPanelButton = new javax.swing.JPanel();
        jButtonOK = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanelToolbar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanelToolbar.setLayout(new java.awt.GridBagLayout());

        JLabelProposalList.setText(org.openide.util.NbBundle.getMessage(MergeDialog.class, "MergeDialog.ProposalList.all")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        jPanelToolbar.add(JLabelProposalList, gridBagConstraints);

        jButtonMenu.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/hamb1-16.png"))); // NOI18N
        jButtonMenu.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButtonMenu.setPreferredSize(new java.awt.Dimension(20, 20));
        jButtonMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonMenuActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanelToolbar.add(jButtonMenu, gridBagConstraints);

        jCheckBoxShowOtherProposal.setFont(jCheckBoxShowOtherProposal.getFont().deriveFont((jCheckBoxShowOtherProposal.getFont().getStyle() | java.awt.Font.ITALIC)));
        jCheckBoxShowOtherProposal.setForeground(java.awt.Color.blue);
        jCheckBoxShowOtherProposal.setText(org.openide.util.NbBundle.getMessage(MergeDialog.class, "MergeDialog.ProposalList.other")); // NOI18N
        jCheckBoxShowOtherProposal.setHideActionText(true);
        jCheckBoxShowOtherProposal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxShowOtherProposalActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelToolbar.add(jCheckBoxShowOtherProposal, gridBagConstraints);

        getContentPane().add(jPanelToolbar, java.awt.BorderLayout.NORTH);

        jSplitPane0.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane0.setMaximumSize(null);

        mergePanel1.setPreferredSize(new java.awt.Dimension(300, 100));
        jSplitPane0.setTopComponent(mergePanel1);

        mergePanel2.setPreferredSize(new java.awt.Dimension(300, 100));
        jSplitPane0.setBottomComponent(mergePanel2);

        getContentPane().add(jSplitPane0, java.awt.BorderLayout.CENTER);

        jButtonOK.setText(org.openide.util.NbBundle.getMessage(MergeDialog.class, "MergeDialog.OK.text")); // NOI18N
        jButtonOK.setName("OK"); // NOI18N
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
        jPanelButton.getAccessibleContext().setAccessibleName("");

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
            m_mergeManager.copyRecordToEntity(mergePanel1.getCurrentProposal(), mergePanel2.getCurrentProposal());
            componentClosed();
            setVisible(false);
            dispose();
        } catch (Exception throwable) {
            // je ferme la fenetre avant d'afficher le message d'erreur
            componentClosed();
            setVisible(false);
            dispose();
            Toolkit.getDefaultToolkit().beep();
            String title = "MergeDialog.OK";
            //throwable.printStackTrace();
            if (throwable.getMessage() == null) {
                JOptionPane.showMessageDialog(null, throwable.getClass().getName()+ " See console log", title, JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, throwable.getMessage(), title, JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_jButtonOKActionPerformed

    private void jButtonMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMenuActionPerformed
        showPopupMenu(jButtonMenu, 0, jButtonMenu.getHeight());
    }//GEN-LAST:event_jButtonMenuActionPerformed

    private void jCheckBoxShowOtherProposalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxShowOtherProposalActionPerformed
        m_mergeManager.getProposalList1().showAllProposal(jCheckBoxShowOtherProposal.isSelected());
    }//GEN-LAST:event_jCheckBoxShowOtherProposalActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel JLabelProposalList;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonMenu;
    private javax.swing.JButton jButtonOK;
    private javax.swing.JCheckBox jCheckBoxShowOtherProposal;
    private javax.swing.JPanel jPanelButton;
    private javax.swing.JPanel jPanelToolbar;
    private javax.swing.JSplitPane jSplitPane0;
    private ancestris.modules.releve.merge.MergePanel mergePanel1;
    private ancestris.modules.releve.merge.MergePanel mergePanel2;
    // End of variables declaration//GEN-END:variables


}
