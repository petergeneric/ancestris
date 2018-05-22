/*
 * MergePanel.java
 *
 */

package ancestris.modules.releve.merge;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 */
public class MergePanel extends javax.swing.JPanel  {

    private Proposal currentProposal = null;

    private JPopupMenu popupMenu;
    private JMenuItem menuItemImportClipboard = new JMenuItem(NbBundle.getMessage(MergePanel.class, "MergePanel.menu.copyToClipboard"));
    private boolean firstPanel = true;


    /**
     * le construteur initialise l'affichage
     */
    public MergePanel() {
        initComponents();

        ActionListener popupActionListener = new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                if (menuItemImportClipboard.equals(e.getSource())) {
                    // je copie le relevé
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    StringSelection sel = new StringSelection(currentProposal.getEventComment() );
                    clipboard.setContents(sel, sel);
                }
            }

        };

        //je cree le popupmenu
        popupMenu = new JPopupMenu();
        menuItemImportClipboard.addActionListener(popupActionListener);
        //menuItemImportClipboard.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/NewFile.png")));
        popupMenu.add(menuItemImportClipboard);

        // je branche le clic du bouton droit de la souris sur l'afffichage
        // du popupmenu
        mergeTable1.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

    }

    /**
     * affiche les modeles contenant les comparaisons du releve avec les entites
     * qui peuvent être concernées par le relevé,
     * et selectionne le premier modele de la liste classée par ordre decroissant de pertinence.
     *
     * @param models    liste des modeles
     * @param selectedEntity  entite selectonne
     */
    protected void initData (MergeManager.ProposalList proposalList, boolean firstPanel) {
        this.firstPanel = firstPanel;

        // je vide le panneau
        jPanelChoice.removeAll();

        // je dimensionne le panneau avec la taille choisie precedemment
        String splitHeight = NbPreferences.forModule(MergePanel.class).get("MergeDialogSplitHeight"+ (firstPanel? "1" :"2"), "90");
        jSplitPane.setDividerLocation(Integer.parseInt(splitHeight));

        // j'ajoute un radio bouton pour chaque proposition
        buttonGroupChoiceModel=new javax.swing.ButtonGroup();
        for(int i= 0; i< proposalList.getSize(); i++) {
            addRadioButton(i, proposalList.getElementAt(i), proposalList.containsSelectedEntity(i) );
        }

        if ( buttonGroupChoiceModel.getButtonCount() >0 ) {
            // j'ajoute un label pour occuper le bas du panel s'il y a au moins une proposition
            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = buttonGroupChoiceModel.getButtonCount();
            gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weighty = 1.0;
            JLabel jLabelEnd = new javax.swing.JLabel();
            jPanelChoice.add(jLabelEnd, gridBagConstraints);

            // je selectionne la premiere proposition
            JRadioButton radioButton0 = (JRadioButton)buttonGroupChoiceModel.getElements().nextElement();
            radioButton0.doClick();
        }

        this.revalidate();
        this.repaint();
    }

    protected void componentClosed() {
        if( firstPanel ) {
            NbPreferences.forModule(MergePanel.class).put("MergeDialogSplitHeight"+ (firstPanel? "1" :"2") , String.valueOf(jSplitPane.getDividerLocation()));
            mergeTable1.componentClosed();
        }
    }

    /**
     * affiche un radio bouton
     * @param entity
     * @param record
     * @param selected
     */
    private void addRadioButton(int position, final Proposal proposal, boolean containsSelectedEntity) {
        // je cree le label a afficher en tete du panneau
        String labelText = " " + Integer.toString(proposal.getDisplayRuleList().getNbEqual() )+ " ";
        JLabel jLabelNbMatch =  new JLabel();
        jLabelNbMatch.setText(labelText);
        jLabelNbMatch.setBorder(null);
        jLabelNbMatch.setVerticalTextPosition(SwingConstants.TOP);
        jLabelNbMatch.setVerticalAlignment(SwingConstants.TOP);
        if (proposal.getDisplayRuleList().getNbConflict()> 0) {
            jLabelNbMatch.setBackground(Color.PINK);
            jLabelNbMatch.setOpaque(true);
        }
        jLabelNbMatch.addMouseListener(new ToolMouseAdapter() );

        // je cree le radiobutton
        JRadioButton jRadioButton =  new JRadioButton();
        jRadioButton.setVerticalTextPosition(SwingConstants.TOP);
        jRadioButton.setText(proposal.getSummary(false));
         if( !containsSelectedEntity ) {
             // j'affiche en bleu si c'est une proposition avec une autre entité
             // que celle sélectionnées par l'utilisateur
             jRadioButton.setFont(jRadioButton.getFont().deriveFont(Font.ITALIC));
             jRadioButton.setForeground(Color.blue);
        }
        jRadioButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        jRadioButton.setPreferredSize(null);
        jRadioButton.setSelected(false);
        jRadioButton.setToolTipText(proposal.getSummary(true) );
        jRadioButton.addMouseListener( mouseAdapter );
        jRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectProposal(proposal, (JRadioButton) evt.getSource());
            }
        });

        // j'ajoute le bouton dans le groupe de boutons pour activer la selection exlusive
        buttonGroupChoiceModel.add(jRadioButton);

        // j'affiche le radiobutton dans la deuxième colonne
        java.awt.GridBagConstraints gridBagConstraints;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = position;
        gridBagConstraints.weightx = 1;
        //gridBagConstraints.weighty = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelChoice.add(jRadioButton,gridBagConstraints);

        // j'affiche le nombre de champs egaux dans la premiere colonne
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = position;
        gridBagConstraints.weightx = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.NONE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        jPanelChoice.add(jLabelNbMatch, gridBagConstraints);
    }

    /**
     * selectionne le modele de données et l'affiche dans la fenetre
     * Cette methode est appelee par le panneau de choix des individus
     * @param entity
     * @param record
     */
    private void selectProposal(Proposal proposal, JRadioButton radioButton) {
        this.currentProposal = proposal;
        mergeTable1.setModel(currentProposal.getDisplayRuleList());
        // j'affiche les données du modele dans la table
        currentProposal.getDisplayRuleList().fireTableDataChanged();
        // j'affiche  dans l'arbre l'entité proposée par le modele
        SelectionManager.setRootEntity(currentProposal.getMainEntity());
    }

    Proposal getCurrentProposal() {
        return currentProposal;
    }

    static private ToolMouseAdapter mouseAdapter = new ToolMouseAdapter();

    static private class ToolMouseAdapter extends MouseAdapter {

        final int defaultDismissTimeout = ToolTipManager.sharedInstance().getDismissDelay();
        final int dismissDelayMinutes = (int) TimeUnit.MINUTES.toMillis(10); // 10 minutes

        @Override
        public void mouseEntered(MouseEvent me) {
            ToolTipManager.sharedInstance().setDismissDelay(dismissDelayMinutes);
        }

        @Override
        public void mouseExited(MouseEvent me) {
            ToolTipManager.sharedInstance().setDismissDelay(defaultDismissTimeout);
        }
    };

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupChoiceModel = new javax.swing.ButtonGroup();
        jSplitPane = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanelChoice = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        mergeTable1 = new ancestris.modules.releve.merge.MergeTable();

        setLayout(new java.awt.BorderLayout());

        jSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBar(null);

        jPanelChoice.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelChoice.setRequestFocusEnabled(false);
        jPanelChoice.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(jPanelChoice);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jSplitPane.setTopComponent(jPanel1);

        jScrollPane2.setViewportView(mergeTable1);

        jSplitPane.setBottomComponent(jScrollPane2);

        add(jSplitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupChoiceModel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelChoice;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane;
    private ancestris.modules.releve.merge.MergeTable mergeTable1;
    // End of variables declaration//GEN-END:variables

}
