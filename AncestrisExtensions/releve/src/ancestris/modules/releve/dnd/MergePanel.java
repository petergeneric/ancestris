/*
 * MergePanel.java
 *
 */

package ancestris.modules.releve.dnd;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

/**
 *
 */
public class MergePanel extends javax.swing.JPanel {

    /**
     * le construteur initialise l'affichage
     */
    public MergePanel() {
        initComponents();
    }

    /**
     * affiche les entites qui peuvent être concernées par le relevé.
     * et selection le premier de la liste
     * @param models    liste des modeles
     * @param selectedEntity  entite selectonne
     * @param mergeDialog     fenetre principale
     */
    protected void initData (List<MergeModel> models, Entity selectedEntity, final MergeDialog mergeDialog ) {

        // j'ajoute les modeles
        for(int i= 0; i< models.size(); i++) {
            addRadioButton(i, models.get(i), selectedEntity, mergeDialog);
        }
        // je coche bouton assicié au premier modele
        ((JRadioButton)buttonGroupChoiceModel.getElements().nextElement()).setSelected(true);
        // je selectionne le premier modele
        mergeDialog.selectModel(models.get(0));
    }

    /**
     * affiche un radio bouton
     * @param entity
     * @param record
     * @param mergeDialog
     * @param selected
     */
    private void addRadioButton(int position, final MergeModel model, Entity selectedEntity, final MergeDialog mergeDialog) {
        String radioButtonText;
        String labelText = Integer.toString(model.getNbMatch())+"/"+Integer.toString(model.getNbMatchMax());
        JLabel jLabelNbMatch =  new JLabel();

        if (  model.getSelectedEntity() == null ) {
            if ( model instanceof MergeModelBirth) {
                if ( selectedEntity instanceof Fam) {
                    radioButtonText = "Nouvel enfant de la famille sélectionnée";
                } else {
                    radioButtonText = "Nouvel enfant";
                    if (model.getRow(MergeModel.RowType.IndiParentFamily).entityValue != null) {
                       radioButtonText += " - "  +  ((Fam)model.getRow(MergeModel.RowType.IndiParentFamily).entityValue).getId();
                    } else {
                       radioButtonText += " - " + "Nouvelle famille";
                       if ( model.getRow(MergeModel.RowType.IndiFatherLastName).entityObject != null
                               ||  model.getRow(MergeModel.RowType.IndiMotherLastName).entityObject != null ) {
                           radioButtonText +=  " ( ";
                           if ( model.getRow(MergeModel.RowType.IndiFatherLastName).entityObject != null ) {
                               radioButtonText += model.getRow(MergeModel.RowType.IndiFatherLastName).entityObject.getId();
                           }
                           radioButtonText +=  " , ";
                           if ( model.getRow(MergeModel.RowType.IndiMotherLastName).entityObject != null ) {
                               radioButtonText += model.getRow(MergeModel.RowType.IndiMotherLastName).entityObject.getId();
                           }
                           radioButtonText +=  " )";
                       }

                    }
                }
            } else {
                radioButtonText = "Nouvelle famille";
            }
        } else {
            if ( model instanceof MergeModelBirth) {
                radioButtonText = "Modifier "+ model.getSelectedEntity().toString();
                if ( model.getSelectedEntity().equals(selectedEntity)) {
                    labelText += " "+ "(Selectionné)";
                    jLabelNbMatch.setForeground(Color.blue);

                }
            
            } else if ( model instanceof MergeModelMarriage) {
                Fam fam = (Fam) model.getSelectedEntity();
                if ( fam != null) {
                    radioButtonText =  "<html>"+ "Modifier" + " ";
                    radioButtonText += fam.getHusband().toString(true);
                    radioButtonText += "<br>";
                    radioButtonText += fam.getWife().toString(true);
                    radioButtonText += "</html>";
                } else  {
                    radioButtonText = "Modifier "+ model.getSelectedEntity().toString();
                }
                 
            } else {
               radioButtonText = "Modifier "+ model.getSelectedEntity().toString();
            }

        }
        jLabelNbMatch.setText(labelText);

        // je cree le radiobutton
        JRadioButton jRadioButton =  new JRadioButton();
        jRadioButton.setText(radioButtonText);
        jRadioButton.setMargin(new java.awt.Insets(0, 2, 0, 2));
        jRadioButton.setPreferredSize(null);
        jRadioButton.setSelected(false);
        jRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergeDialog.selectModel(model);
            }
        });

        // j'ajoute le bouton dans le groupe de boutons pour activer la selection exlusive
        buttonGroupChoiceModel.add(jRadioButton);

        // j'affiche le radiobutton dans la premiere colonne
        java.awt.GridBagConstraints gridBagConstraints;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = position;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelChoice.add(jRadioButton,gridBagConstraints);

        // j'affiche le nombre de champs egaux dans la deuxième colonne
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = position;
        gridBagConstraints.weightx = 0;
        jPanelChoice.add(jLabelNbMatch, gridBagConstraints);

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupChoiceModel = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanelChoice = new javax.swing.JPanel();

        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(MergePanel.class, "MergePanel.border.border.title")))); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(null);
        jScrollPane1.setPreferredSize(null);

        jPanelChoice.setRequestFocusEnabled(false);
        jPanelChoice.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(jPanelChoice);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupChoiceModel;
    private javax.swing.JPanel jPanelChoice;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
