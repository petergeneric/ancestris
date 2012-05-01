/*
 * ReleveEditor.java
 *
 * Created on 18 mars 2012, 10:43:03
 */
package ancestris.modules.releve.editor;

import ancestris.modules.releve.ConfigPanel;
import ancestris.modules.releve.model.BeanField;
import ancestris.modules.releve.model.Field.FieldType;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldSex;
import ancestris.modules.releve.model.FieldTitle;
import ancestris.modules.releve.model.ModelAbstract;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.ReleveTopComponent;
import ancestris.modules.releve.ReleveEditorListener;
import ancestris.modules.releve.TableSelectionListener;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.FieldEventType;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.GridBagConstraints;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Michel
 */
public class ReleveEditor extends javax.swing.JPanel implements FocusListener, ReleveEditorListener, TableSelectionListener, TableModelListener {

    DataManager dataManager = null;
    ModelAbstract recordModel = null;
    int currentRecordIndex = -1;
    boolean standaloneMode = false;
    /**
     * bean ayant le focus par defaut (utilisé a la creation d'un releve
     */
    Bean defaultBeanFocus = null;

    public ReleveEditor() {
        initComponents();
        jButtonPrevious.setVisible(false);
        jTextFielRecordNo.setVisible(false);
        jButtonNext.setVisible(false);
        jButtonDelete.setEnabled(false);

        jScrollPane1.getVerticalScrollBar().setUnitIncrement(20);

        // je crée les raccourcis pour créer les nouveaux relevés
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt S"), this);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt Z"), this);
        
        // je crée le racourci pour reseigner le nom de l'indiividu dans le nom du père de l'individu
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt X"), this);
        // je crée le racourci pour copier le nom de l'épouse dans le nom du père de l'épouse
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt Y"), this);
        // je crée le racourci pour copier la date de l'evenement dans la date de naissaance
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt B"), this);
        // je crée le racourci pour copier la meme valeur que celle de l'enregistrement précédent
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS , InputEvent.ALT_MASK), this);

        getActionMap().put(this, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if ( actionEvent.getActionCommand().equals("c")
                    || actionEvent.getActionCommand().equals("m")
                    || actionEvent.getActionCommand().equals("d")
                    || actionEvent.getActionCommand().equals("v") ) {
                    jButtonNewActionPerformed(actionEvent);
                } else if ( actionEvent.getActionCommand().equals("s") ) {
                    jButtonDeleteActionPerformed(actionEvent);
                } else if ( actionEvent.getActionCommand().equals("z") ) {
                    Record record = recordModel.undo();
                    if (record != null ) {
                        selectRecord(recordModel.getIndex(record));
                    } else {
                        selectRecord(-1);
                    }
                    recordModel.fireTableDataChanged();
                } else if ( actionEvent.getActionCommand().equals("x") ) {
                    copyIndiNameToIndiFatherName();
                } else if ( actionEvent.getActionCommand().equals("y") ) {
                    copyWifeNameToWifeFatherName();
                } else if ( actionEvent.getActionCommand().equals("b") ) {
                    copyEventDateToIndiBirthDate();
                } else if ( actionEvent.getActionCommand().equals("=") ) {
                    copyPreviousRecordField();
                }
            }

        });
    }

    /**
     * Cette methode doit etre appelee apres le constructeur pour fixer le 
     * modele de données utilisé par l'editeur. 
     * @param dataManager
     * @param modelType
     */
    public void setModel(DataManager dataManager, DataManager.ModelType modelType) {
        this.dataManager = dataManager;
        this.recordModel = dataManager.getModel(modelType);
        if( standaloneMode) {
            recordModel.addTableModelListener(this);
        } else {
            // je branche la selection de releve de la table vers l'editeur
            recordModel.addReleveValidationListener(this);
        }

        // je complete le tooltip du bouton "créer" en fonction du modele
        String toolTipText = org.openide.util.NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.jButtonNew.toolTipText");

        switch (modelType) {
            case birth:
                jButtonNew.setToolTipText(toolTipText+ " (ALT-N)");
                break;
            case marriage:
                jButtonNew.setToolTipText(toolTipText+ " (ALT-M)");
                break;
            case death:
                jButtonNew.setToolTipText(toolTipText+ " (ALT-D)");
                break;
            case misc:
                jButtonNew.setToolTipText(toolTipText+ " (ALT-V)");
                break;
        }
    }


    /**
     * Copie le nom de l'individu dans le nom du pere de l'individu
     * et deplace le focus sur le nom du pere
     */
    private void copyIndiNameToIndiFatherName() {
        
        Field indiLastNameField = null;
        Field indiFatherLastNameField = null;
        Bean indiFatherNameBean = null;

        // je cherche le champ contenant le nom de l'individu
        for(Component component : fieldsPanel.getComponents()) {
            if ( component instanceof Bean) {
                Bean bean = ((Bean) component);
                if ( bean.getBeanField().getFieldType() == Field.FieldType.indiLastName ) {
                    indiLastNameField = bean.getBeanField().getField();
                    break;
                }
            }
        }

        // je cherche le champ contenant le nom du pere de l'individu
        for(Component component : fieldsPanel.getComponents()) {
            if ( component instanceof Bean) {
               Bean bean = ((Bean) component);
               if ( bean.getBeanField().getFieldType() == Field.FieldType.indiFatherLastName ) {
                    indiFatherLastNameField = bean.getBeanField().getField();
                    indiFatherNameBean = bean;
                    break;
                }
            }
        }

        // je copie le nom
        if ( indiLastNameField != null && indiFatherLastNameField != null ) {
            recordModel.fieldChanged(indiFatherNameBean.getBeanField().getRecord(), indiFatherLastNameField, indiLastNameField.getValue());
            indiFatherLastNameField.setValue(indiLastNameField.getValue());
            // je refraichis l'affichage
            indiFatherNameBean.setContext(indiFatherNameBean.getBeanField());
            // je donne le focus au bean indiFatherNameBean
            indiFatherNameBean.requestFocusInWindow();
        } else {
            // j'emets un beep
            Toolkit.getDefaultToolkit().beep();
        }
    }


    /**
     * Copie le nom de l'epouse dans le nom du pere de l'epouse
     * et deplace le focus sur le nom du pere
     */
    private void copyWifeNameToWifeFatherName() {
        Field wifeLastNameField = null;
        Field wifeFatherLastNameField = null;
        Bean wifeFatherNameBean = null;

        // je cherche le champ contenant le nom de l'individu
        for(Component component : fieldsPanel.getComponents()) {
            if ( component instanceof Bean) {
                Bean bean = ((Bean) component);
                if ( bean.getBeanField().getFieldType() == Field.FieldType.wifeLastName ) {
                    wifeLastNameField = bean.getBeanField().getField();
                    break;
                }
            }
        }
        
        // je cherche le champ contenant le nom du pere de la femme
        for(Component component : fieldsPanel.getComponents()) {
            if ( component instanceof Bean) {
                Bean bean = ((Bean) component);
               if ( bean.getBeanField().getFieldType() == Field.FieldType.wifeFatherLastName ) {
                    wifeFatherLastNameField = bean.getBeanField().getField();
                    wifeFatherNameBean = bean;
                    break;
                }
            }
        }

        // je copie le nom
        if ( wifeLastNameField != null && wifeFatherLastNameField != null ) {
            recordModel.fieldChanged(wifeFatherNameBean.getBeanField().getRecord(), wifeFatherLastNameField, wifeLastNameField.getValue());
            wifeFatherLastNameField.setValue(wifeLastNameField.getValue());
        
            // je refraichis l'affichage
            wifeFatherNameBean.setContext(wifeFatherNameBean.getBeanField());

            // je donne le focus au bean wifeFatherNameBean
            wifeFatherNameBean.requestFocusInWindow();
        } else {
            // j'emets un beep
            Toolkit.getDefaultToolkit().beep();
        }

    }
    
    /**
     * Copie la date de l'evenement dans la date de naissance
     * et deplace le focus sur la date de naissance
     */
    private void copyEventDateToIndiBirthDate() {
        Field eventDate = null;
        Field indiBirthDate = null;
        Bean indiBirthDateBean = null;

        // je cherche le champ contenant le nom de l'individu
        for(Component component : fieldsPanel.getComponents()) {
            if ( component instanceof Bean) {
                Bean bean = ((Bean) component);
                if ( bean.getBeanField().getFieldType() == Field.FieldType.eventDate ) {
                    eventDate = bean.getBeanField().getField();
                    break;
                }
            }
        }
        
        // je cherche le champ contenant le nom du pere de la femme
        for(Component component : fieldsPanel.getComponents()) {
            if ( component instanceof Bean) {
                Bean bean = ((Bean) component);
               if ( bean.getBeanField().getFieldType() == Field.FieldType.indiBirthDate ) {
                    indiBirthDate = bean.getBeanField().getField();
                    indiBirthDateBean = bean;
                    break;
                }
            }
        }

        // je copie le nom
        if ( eventDate != null && indiBirthDate != null ) {
            recordModel.fieldChanged(indiBirthDateBean.getBeanField().getRecord(), eventDate, indiBirthDate.getValue());
            indiBirthDate.setValue(eventDate.getValue());
        
            // je refraichis l'affichage
            indiBirthDateBean.setContext(indiBirthDateBean.getBeanField());

            // je donne le focus au bean wifeFatherNameBean
            indiBirthDateBean.requestFocusInWindow();
        } else {
            // j'emets un beep
            Toolkit.getDefaultToolkit().beep();
        }
    }



    /**
     * Copie le contenu du meme champ du releve precedemment saisi
     * 
     */
    private void copyPreviousRecordField() {
        // je recupere le releve precedent
        Record previousRecord = recordModel.getPreviousRecord(recordModel.getRecord(currentRecordIndex));

        if (previousRecord == null) {
            // j'emets un beep
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        // je cherche le champ courant qui a le focus
        Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if ( focused == null ) {
            // j'emets un beep
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        // je recupere le bean parent
        Component parent = focused.getParent();
        while ((parent != null) && !(parent instanceof Bean)) {
            parent = parent.getParent();
        }
        Bean bean = (Bean) parent;
        // je recupere le champ qui a le focus
        Field previousField = previousRecord.getField(bean.getBeanField().getFieldType());
        // je copie la donnée du meme champ du releve precedent dans le bean
        // Attention : je ne copie pas la donnée dans le champ du releve courant pour
        // que les controles puissent s'effectuer comme si l'utilisateur avait saisi
        // la nouvelle valeur dans le bean
        bean.replaceValue(previousField);
    }

    /**
     * cree un nouveau relevé
     * Cette methode est appelée
     */
     public void createRecord() {
        // avant de creer le nouveau releve , je verifie la coherence du releve courant
        String errorMessage = recordModel.verifyRecord(currentRecordIndex);
        if (errorMessage.isEmpty()) {
            // je cree un nouveau releve
            Record record = recordModel.createRecord();
            int recordIndex = dataManager.addRecord(record);
            if (dataManager.getCopyFreeCommentEnabled() ) {
                // je valorise le numero de photo avec la valeur par defaut
                String defaultValue = dataManager.getDefaultFreeComment();
                record.setFreeComment(defaultValue);
            }

            // je memorise l'index du nouveau releve
            // ATTENTION : currentRecordIndex sert a distinguer l'editeur normal
            // de l'editeur indépendant dans tableChanged()
            currentRecordIndex = recordIndex;
            // je previens les listeners (table et editeur)
            recordModel.fireTableRowsInserted(recordIndex, recordIndex);
        } else {
            // j'affiche le message d'erreur
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, errorMessage, "Relevé", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * affiche un relevé
     * si le releve est null, nettoie l'affichage
     * @param record
     */
    public void selectRecord(int recordIndex) {
        
        //TODO ajouter l'age des témoins

        //TODO nom du nouveau fichier avec la commune

        fieldsPanel.setVisible(false);
        fieldsPanel.setFocusTraversalPolicyProvider(true);
        fieldsPanel.setFocusCycleRoot(true);
        fieldsPanel.resetKeyboardActions();
        fieldsPanel.removeAll();
        fieldsPanel.setPreferredSize(new Dimension(200,0));

        if (recordModel != null) {
            BeanField[] beanFields = recordModel.getFieldList(recordIndex);
            int lineNo = 0;
            KeyStroke keyStroke = null;
            defaultBeanFocus = null;

            // j'ajoute la permiere ligne avec le nom de la commune
            addRow(lineNo, dataManager.getCityName() + "," + dataManager.getCityCode() + "," + dataManager.getCountyName(), null, null);
            lineNo++;

            // j'affiche les beans en focntion du type de champ
            for (int recordNo = 0; recordNo < beanFields.length; recordNo++) {
                BeanField beanField = beanFields[recordNo];
                Field field = beanField.getField();
                String label = beanField.getLabel();
                Bean bean = null;

                switch (beanField.getFieldType()) {
                    case title:
                        // label separateur de rubrique
                        keyStroke = ((FieldTitle) field).getKeyStroke();
                        break;

                    case eventType:
                        bean = new BeanEventType(dataManager.getCompletionProvider());
                        break;

                    case indiBirthDate:
                        label = label.substring(0,5)+". (Alt-B)";
                        bean = new BeanDate();
                        bean.setPreferredSize(new java.awt.Dimension(200, 20));
                        break;
                    case eventDate:
                    case wifeBirthDate:
                        bean = new BeanDate();
                        bean.setPreferredSize(new java.awt.Dimension(200, 20));
                        break;

                    case indiLastName:
                    case indiMarriedLastName:
                    case indiMotherLastName:
                    case wifeLastName:
                    case wifeMarriedLastName:
                    case wifeMotherLastName:
                    case witness1LastName:
                    case witness2LastName:
                    case witness3LastName:
                    case witness4LastName:
                        // j'affiche le bean d'edition du nom
                        bean = new BeanLastName(dataManager.getCompletionProvider());
                        break;
                    case indiFatherLastName:
                        label += " (Alt-X)";
                        bean = new BeanFirstName(dataManager.getCompletionProvider());
                        break;
                    case wifeFatherLastName:
                        label += " (Alt-Y)";
                        bean = new BeanFirstName(dataManager.getCompletionProvider());
                        break;

                    case indiFirstName:
                    case indiMarriedFirstName:
                    case indiFatherFirstName:
                    case indiMotherFirstName:
                    case wifeFirstName:
                    case wifeMarriedFirstName:
                    case wifeFatherFirstName:
                    case wifeMotherFirstName:
                    case witness1FirstName:
                    case witness2FirstName:
                    case witness3FirstName:
                    case witness4FirstName:
                        bean = new BeanFirstName(dataManager.getCompletionProvider());
                        break;

                    case indiSex:
                    //case indiMarriedSex:
                    case wifeSex:
                    //case wifeMarriedSex:
                        bean = new BeanSex();
                        break;

                    case indiMarriedDead:
                    case indiFatherDead:
                    case indiMotherDead:
                    case wifeMarriedDead:
                    case wifeFatherDead:
                    case wifeMotherDead:
                        bean = new BeanDead();
                        break;

                    case indiOccupation:
                    case indiMarriedOccupation:
                    case indiFatherOccupation:
                    case indiMotherOccupation:
                    case wifeOccupation:
                    case wifeMarriedOccupation:
                    case wifeFatherOccupation:
                    case wifeMotherOccupation:
                    case witness1Occupation:
                    case witness2Occupation:
                    case witness3Occupation:
                    case witness4Occupation:
                        bean = new BeanOccupation(dataManager.getCompletionProvider());
                        break;

                    case indiPlace:
                    case wifePlace:
                        bean = new BeanPlace();
                        break;

                    case indiAge:
                    case wifeAge:
                    case indiComment:
                    case indiMarriedComment:
                    case indiFatherComment:
                    case indiMotherComment:
                    case wifeComment:
                    case wifeMarriedComment:
                    case wifeFatherComment:
                    case wifeMotherComment:
                    case witness1Comment:
                    case witness2Comment:
                    case witness3Comment:
                    case witness4Comment:
                    case cote:
                    case parish:
                    case generalComment:
                        bean = new BeanSimpleValue();
                        break;

                    case freeComment:
                        bean = new BeanFreeComment();
                        break;

                    case notary:
                        bean = new BeanSimpleValue();
                        break;
                }

                addRow(lineNo, label, bean, keyStroke);
                
                if (beanField.getFieldType() != FieldType.title) {
                    bean.setContext(beanField);
                    keyStroke = null;
                }
                lineNo++;
            }

            // j'ajoute un label pour occuper le bas du panel
            GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = lineNo;
            gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weighty = 1.0;
            JLabel jLabelEnd = new javax.swing.JLabel();
            fieldsPanel.add(jLabelEnd, gridBagConstraints);
            // je change la taille du panel pour forcer la mise à jour des scrollbars
            fieldsPanel.setPreferredSize(new Dimension(200, 20 * lineNo));

            // je memorise le numero du releve
            currentRecordIndex = recordIndex;
            // j'affiche le numero du relevé.
            jTextFielRecordNo.setText(String.valueOf(currentRecordIndex+1));
            // j'active le bouton Delete si le releve courant est valide
            if ( currentRecordIndex == -1 ) {
                jButtonDelete.setEnabled(false);
            } else {
                jButtonDelete.setEnabled(true);
            }
        }
        fieldsPanel.setVisible(true);
        fieldsPanel.revalidate();
    }

     private void addRow(int lineNo, String label, final Bean bean, KeyStroke keyStroke) {

        if (bean != null) {
            GridBagConstraints gridBagConstraints;

            // j'ajoute le label dans la colonne 0
            JLabel jLabel1 = new javax.swing.JLabel();
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = lineNo;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            jLabel1.setText(label);
            fieldsPanel.add(jLabel1, gridBagConstraints);
            // j'ajoute le bean dans la colonne 1
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = lineNo;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            fieldsPanel.add(bean, gridBagConstraints);
            this.addLastFocusListeners(bean);

            // j'ajoute le raccourci clavier
            if (keyStroke != null) {
                getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, bean);
                getActionMap().put(bean, new AbstractAction() {

                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        bean.requestFocusInWindow();
                    }
                });
            }

            // je donne le focus au premier bean editable
            if (defaultBeanFocus == null ) {
                defaultBeanFocus = bean;
            }

        } else {
            // j'ajoute le label étalé dans les colonnes 0 et 1
            GridBagConstraints gridBagConstraints;
            JLabel jLabel1 = new javax.swing.JLabel();
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = lineNo;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            if (keyStroke == null) {
                jLabel1.setText(label);
            } else {
                jLabel1.setText(label + "   ( Alt-" + String.valueOf((char) keyStroke.getKeyCode()) + " )");
            }

            jLabel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
            fieldsPanel.add(jLabel1, gridBagConstraints);
        }
    }
    
    public void setStandaloneMode() {
        jButtonStandalone.setVisible(false);
        jButtonPrevious.setVisible(true);
        jButtonNext.setVisible(true);
        jTextFielRecordNo.setVisible(true);
        standaloneMode = true;        
    }


    ///////////////////////////////////////////////////////////////////////////
    // Implement TableModelListener
    ///////////////////////////////////////////////////////////////////////////
    /**
     * affiche le releve qui vient d'être ajouté ou supprimé dans le modele
     */
    /**
     * Cette methode est appelée par le modele de données qunand
     * ATTENTION : elle n'est utilisée que par l'editeur independant
     * (L'éditeur normal est mis à jour par ReleveTable.TableChanged() pour
     * prendree en compte le tri choisi par l'utilisateur, voir rowSelected() )
     * @param e e.getFirstRow() contient l'index du nouveau releve
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        switch (e.getType()) {

            case TableModelEvent.INSERT:
                // je verifie si c'est le meme qui est source de la notification 
                if ( e.getFirstRow() == currentRecordIndex ) {
                    selectRecord(e.getFirstRow());
                    // je place le curseur sur le premier champ de l'editeur quand on cree un nouveau releve
                    if (defaultBeanFocus != null) {
                        defaultBeanFocus.requestFocusInWindow();
                    }
                }
                break;
            case TableModelEvent.DELETE:
                if ( currentRecordIndex >0 ) {
                    selectRecord(currentRecordIndex -1);
                } else {
                    if (recordModel.getRowCount() > 0  ) {
                        selectRecord(currentRecordIndex );
                    } else {
                        selectRecord(-1);
                    }
                }
                break;
            case TableModelEvent.UPDATE :
                // TODO : afficher les modifications si c'est le meme releve courant dans l'editeur
                break;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implement TableSelectionListener
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Affiche le releve sélectionné dans l'editeur
     * Cette methode est appelee par ReleveTable a chaque nouvelle selection dans la table du releve
     * @param modelRow
     */
    @Override
    public void rowSelected(int recordIndex , boolean isNewRecord) {
        selectRecord( recordIndex);
        if (isNewRecord && defaultBeanFocus != null) {
            defaultBeanFocus.requestFocusInWindow();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implement ReleveEditorListener
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public int getCurrentRecordIndex() {
        return currentRecordIndex;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Implement ReleveEditorListener
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Met en surbrillance un bean qui récupère le focus
     * Cette méthode est appelée par le systeme quand un bean recupere le focus.
     * Je change la couleur de fond du bean (bleu clair).
     * Je scrolle la vue de l'éditeur pour que le bean qui recupere le focus soit
     * visible dans le viewport du panel de l'editeur
     * @param focusEvent
     */
    @Override
    public void focusGained(FocusEvent focusEvent) {
        final JComponent focused = (JComponent) focusEvent.getSource();

        // j'active la surbrillance du champ
        focused.setBackground(new Color(200, 255, 255));

        // je scolle la fenetre de l'editeur pour voir le champ
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Rectangle rect = focused.getBounds();
                // je rejoute une marge de 20 pixels au dessous et au dessus
                // pour voir les champs precedents et suivants
                rect.y -= 20;
                rect.height +=40;
                ((JComponent) focused.getParent()).scrollRectToVisible(rect);
            }
        });
    }


    ///////////////////////////////////////////////////////////////////////////
    // Implement FocusListener
    ///////////////////////////////////////////////////////////////////////////

    /**
     * controle et commite les données d'un bean qui perd le focus.
     * Cette méthode est appelée par le systeme quand un bean perd le focus.
     * Je rétablis la couleur de fond par defaut (blanc).
     * Si la valeur du bean a changé :
     *  - je memorise l'ancienne valeur pour undo
     *
     * - je commite le champ modifié (mise a jour du modele)
     *
     * - j'ajoute le nouveau prenom pour la détermination automatique du sexe associé au prénom
     * - je vérifie s'il y a un nouveau doublon.
     * - je met à jour le sexe de l'ex conjoint de l'individu, si c'est un mariage, décès ou divers
     * - je met à jour le sexe de l'ex conjoint de la femme de l'individu, si c'est un mariage ou divers
     * - je memorise la nouvelle valeur du numero de photo
     *
     * @param focusEvent parametre non utilisé
     */
    @Override
    public void focusLost(FocusEvent focusEvent) {
        //TODO ajouter le controle sur une plage de date
        final JComponent focused = (JComponent) focusEvent.getSource();
        // je supprime la surbrillance du champ
        focused.setBackground(new Color(255, 255, 255));

        // je recupere le bean parent
        Component parent = focused.getParent();
        while ((parent != null) && !(parent instanceof Bean)) {
            parent = parent.getParent();
        }
        Bean bean = (Bean) parent;
        // j'applique les modifications
        if (bean.hasChanged()) {

            Record record = bean.getBeanField().getRecord();
            FieldType fieldType =  bean.getBeanField().getFieldType();

            // je memorise l'ancienne valeur
            String oldValue = bean.getBeanField().getField().toString();
            
            // je commite le champ modifié (mise a jour du modele)
            bean.commit();

            // je demande confirmation à l'utilisateur si c'est un nouveau nom, prénom, profession ou TypeEventTag
            String newValue = bean.getBeanField().getField().toString();
            if (dataManager.getNewValueControlEnabled() && !newValue.isEmpty()) {
                List completionList = null;
                // je determine la liste de completion qui doit etre utilisee
                switch (fieldType) {
                    case indiFirstName :
                    case indiMarriedFirstName :
                    case indiFatherFirstName :
                    case indiMotherFirstName :
                    case wifeFirstName :
                    case wifeMarriedFirstName :
                    case wifeFatherFirstName :
                    case wifeMotherFirstName :
                    case witness1FirstName :
                    case witness2FirstName :
                    case witness3FirstName :
                    case witness4FirstName :
                        completionList = dataManager.getCompletionProvider().getFirstNames();                       
                        break;
                    case indiLastName :
                    case indiMarriedLastName :
                    case indiFatherLastName :
                    case indiMotherLastName :
                    case wifeLastName :
                    case wifeMarriedLastName :
                    case wifeFatherLastName :
                    case wifeMotherLastName :
                    case witness1LastName :
                    case witness2LastName :
                    case witness3LastName :
                    case witness4LastName :
                        completionList = dataManager.getCompletionProvider().getLastNames();
                        break;
                    case indiOccupation:
                    case indiMarriedOccupation:
                    case indiFatherOccupation:
                    case indiMotherOccupation:
                    case wifeOccupation:
                    case wifeMarriedOccupation:
                    case wifeFatherOccupation:
                    case wifeMotherOccupation:
                    case witness1Occupation:
                    case witness2Occupation:
                    case witness3Occupation:
                    case witness4Occupation:
                        completionList = dataManager.getCompletionProvider().getOccupations();
                        break;
                    case eventType:
                        completionList = dataManager.getCompletionProvider().getEventTypes();
                        break;
                }

                //je verifie si c'est une nouvelle valeur
                if ( completionList!= null && ! completionList.contains(newValue)) {
                    // je demande à l'utilisateur s'il veut enregistrer cette nouvelle valeur
                    Toolkit.getDefaultToolkit().beep();
                    int choice = JOptionPane.showConfirmDialog(this,
                            String.format("Confirmez-vous la saisie de ce nouveau %s : %s ", bean.getBeanField().getLabel(), newValue),
                            NbBundle.getMessage(ConfigPanel.class, "ConfigPanel.jCheckBoxNewValueControl.text"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);

                    if (choice == 1 ) {
                        // l'utisateur a refusé de confirmer la nouvelle valeur
                        // j'annule le commit
                        bean.getBeanField().getField().setValue(oldValue);
                        // je rafraichis l'affichage
                        bean.setContext(bean.getBeanField());
                        // je redonne le focus au champ
                        bean.requestFocusInWindow();
                        // j'interromps le tratitement
                        return;
                    }
                }
            }

            // je memorise l'ancienne valeur pour undo
            if (recordModel != null) {
                recordModel.fieldChanged(record, bean.getField(), oldValue);
            }

            // je mets a jour les statistiques des sexes/prénoms
            switch (fieldType) {
                case indiFirstName :
                    if ( record.getIndiFirstName() != null &&  !record.getIndiFirstName().isEmpty()) {
                        record.getIndiSexField().setSex( dataManager.getCompletionProvider().getFirstNameSex(record.getIndiFirstName().getValue())) ;
                        refreshBeanField(fieldType.indiSex);
                    }
                    dataManager.getCompletionProvider().updateFirstNameSex(oldValue, record.getIndiSexField().getValue(), record.getIndiFirstName().getValue(), record.getIndiSexField().getValue());
                    break;
                case indiSex :
                    dataManager.getCompletionProvider().updateFirstNameSex(record.getIndiFirstName().getValue(), oldValue, record.getIndiFirstName().getValue(), record.getIndiSexField().getValue());
                    break;
                case indiMarriedFirstName :
//                    if ( record.getIndiMarriedFirstName() != null &&  !record.getIndiMarriedFirstName().isEmpty() && record.getIndiMarriedSex().getFirstNameSex() == FieldSex.UNKNOWN) {
//                        record.getIndiMarriedSex().setSex( dataManager.getCompletionProvider().getFirstNameSex(record.getIndiMarriedFirstName())) ;
//                        refreshBeanField(fieldType.indiMarriedSex);
//                    }
                    dataManager.getCompletionProvider().updateFirstNameSex(oldValue, record.getIndiSexField().getValue(), record.getIndiMarriedFirstName().getValue(), record.getIndiSexField().getOppositeString());
                    break;
//                case indiMarriedSex :
//                    dataManager.getCompletionProvider().updateFirstNameSex(record.getIndiMarriedFirstName(), oldValue, record.getIndiMarriedFirstName(), record.getIndiMarriedSex().getValue());
//                    break;
                case indiFatherFirstName :
                    dataManager.getCompletionProvider().updateFirstNameSex(oldValue, FieldSex.MALE_STRING, record.getIndiFatherFirstName().getValue(), FieldSex.MALE_STRING);
                    break;
                case indiMotherFirstName :
                    dataManager.getCompletionProvider().updateFirstNameSex(oldValue, FieldSex.FEMALE_STRING, record.getIndiFirstName().getValue(), FieldSex.FEMALE_STRING);
                    break;
                case wifeFirstName :
                    if ( record.getWifeFirstName() != null &&  !record.getWifeFirstName().isEmpty() ) {
                        record.getWifeSexField().setSex( dataManager.getCompletionProvider().getFirstNameSex(record.getWifeFirstName().getValue())) ;
                        refreshBeanField(fieldType.wifeSex);
                    }
                    dataManager.getCompletionProvider().updateFirstNameSex(oldValue, record.getWifeSexField().getValue(), record.getWifeFirstName().getValue(), record.getWifeSexField().getValue());
                    break;
                case wifeSex :
                    dataManager.getCompletionProvider().updateFirstNameSex(record.getWifeFirstName().getValue(), oldValue, record.getWifeFirstName().getValue(), record.getWifeSexField().getValue());
                    break;
                case wifeMarriedFirstName :
//                    if ( record.getWifeMarriedFirstName() != null &&  !record.getWifeMarriedFirstName().isEmpty() && record.getWifeMarriedSex().getFirstNameSex() == FieldSex.UNKNOWN) {
//                        record.getWifeMarriedSex().setSex( dataManager.getCompletionProvider().getFirstNameSex(record.getWifeMarriedFirstName())) ;
//                        refreshBeanField(fieldType.wifeMarriedSex);
//                    }
                    dataManager.getCompletionProvider().updateFirstNameSex(oldValue, record.getWifeSexField().getOppositeString(), record.getWifeMarriedFirstName().getValue(), record.getWifeSexField().getOppositeString());
                    break;
//                case wifeMarriedSex :
//                    dataManager.getCompletionProvider().updateFirstNameSex(record.getWifeMarriedFirstName(), oldValue, record.getWifeMarriedFirstName(), record.getWifeMarriedSex().getValue());
//                    break;
                case wifeFatherFirstName :
                    dataManager.getCompletionProvider().updateFirstNameSex(oldValue, FieldSex.MALE_STRING, record.getWifeFatherFirstName().getValue(),  FieldSex.MALE_STRING);
                    break;
                case wifeMotherFirstName :
                    dataManager.getCompletionProvider().updateFirstNameSex(oldValue, FieldSex.FEMALE_STRING, record.getWifeMotherFirstName().getValue(), FieldSex.FEMALE_STRING);
                    break;
            }


            //je mets à jour la liste de completion des noms, prénoms, professions et type d'évènements
            switch (fieldType) {
                case indiFirstName :
                case indiMarriedFirstName :
                case indiFatherFirstName :
                case indiMotherFirstName :
                case wifeFirstName :
                case wifeMarriedFirstName :
                case wifeFatherFirstName :
                case wifeMotherFirstName :
                case witness1FirstName :
                case witness2FirstName :
                case witness3FirstName :
                case witness4FirstName :
                    //je mets à jour la completion des prénoms
                    dataManager.getCompletionProvider().updateFirstName(bean.getBeanField().getField(), oldValue);
                    break;
                case indiLastName :
                case indiMarriedLastName :
                case indiFatherLastName :
                case indiMotherLastName :
                case wifeLastName :
                case wifeMarriedLastName :
                case wifeFatherLastName :
                case wifeMotherLastName :
                case witness1LastName :
                case witness2LastName :
                case witness3LastName :
                case witness4LastName :
                    //je mets à jour la completion des noms
                    dataManager.getCompletionProvider().updateLastName(bean.getBeanField().getField(), oldValue);
                    break;
                case indiOccupation:
                case indiMarriedOccupation:
                case indiFatherOccupation:
                case indiMotherOccupation:
                case wifeOccupation:
                case wifeMarriedOccupation:
                case wifeFatherOccupation:
                case wifeMotherOccupation:
                case witness1Occupation:
                case witness2Occupation:
                case witness3Occupation:
                case witness4Occupation:
                    //je mets à jour la completion des professions
                    dataManager.getCompletionProvider().updateOccupation(bean.getBeanField().getField(), oldValue);
                    break;
                case eventType:
                    //je mets à jour la completion des types d'évènement
                    dataManager.getCompletionProvider().updateEventType((FieldEventType)bean.getBeanField().getField(), oldValue);
                    break;
            }

            // Si l'utilisateur vient de changer la date ou le nom de l'individu
            // je vérifie s'il y a un nouveau doublon.
            if ( fieldType.equals(Field.FieldType.eventDate)
                ||fieldType.equals(Field.FieldType.indiFirstName)
                ||fieldType.equals(Field.FieldType.indiLastName) ) {
                
                Record[] duplicateRecords = recordModel.findDuplicateRecord(record);
                if (duplicateRecords.length > 0) {
                    // j'affiche un message d'avertissement
                    String message = String.format(NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.duplicateRecord"));
                    message += "\n id=";
                    for ( int index =0; index <duplicateRecords.length; index ++ ) {
                        Record duplicate = duplicateRecords[index];
                        message += Integer.toString(duplicate.getRecordNo());
                        if ( index < duplicateRecords.length-1) {
                           message += ", ";
                        }
                    }
                    String title = "Informations";
                    JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);

                }
            }

            // je memorise la nouvelle valeur du numero de photo
            if (fieldType == Field.FieldType.freeComment && dataManager.getCopyFreeCommentEnabled() == true  ) {
                // je copie le numero de photo dans la valeur par defaut
                String defaultValue = bean.getField().toString();
                dataManager.setDefaultFreeComment(defaultValue);
            }

            if (recordModel != null) {
                recordModel.fireTableRowsUpdated(currentRecordIndex, currentRecordIndex);
            }
        }
    }

    /**
     * J'ajoute le listener de focus pour les TextField et Jcombox qui composent les beans
     * @param container
     */
    public void addLastFocusListeners(Container container) {
        Component[] components = container.getComponents();
        for (int i = 0; i < components.length; i++) {
            Component component = components[i];
            if ((component instanceof JTextField) || (component instanceof JComboBox || (component instanceof JCheckBox))) {
                component.addFocusListener(this);
            }
            if (component instanceof Container) {
                addLastFocusListeners((Container) component);
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

        editorBar = new javax.swing.JPanel();
        jButtonNew = new javax.swing.JButton();
        jButtonDelete = new javax.swing.JButton();
        jButtonPrevious = new javax.swing.JButton();
        jTextFielRecordNo = new javax.swing.JTextField();
        jButtonNext = new javax.swing.JButton();
        jButtonStandalone = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        fieldsPanel = new javax.swing.JPanel();

        setMinimumSize(new java.awt.Dimension(200, 300));
        setPreferredSize(new java.awt.Dimension(0, 0));
        setLayout(new java.awt.BorderLayout());

        editorBar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        editorBar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jButtonNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/NewRecord.png"))); // NOI18N
        jButtonNew.setText(org.openide.util.NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.jButtonNew.text")); // NOI18N
        jButtonNew.setToolTipText(org.openide.util.NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.jButtonNew.toolTipText")); // NOI18N
        jButtonNew.setActionCommand(org.openide.util.NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.jButtonNew.actionCommand")); // NOI18N
        jButtonNew.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewActionPerformed(evt);
            }
        });
        editorBar.add(jButtonNew);

        jButtonDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/DeleteRecord.png"))); // NOI18N
        jButtonDelete.setText(org.openide.util.NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.jButtonDelete.text")); // NOI18N
        jButtonDelete.setToolTipText(org.openide.util.NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.jButtonDelete.toolTipText")); // NOI18N
        jButtonDelete.setActionCommand(org.openide.util.NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.jButtonDelete.actionCommand")); // NOI18N
        jButtonDelete.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });
        editorBar.add(jButtonDelete);

        jButtonPrevious.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Back.png"))); // NOI18N
        jButtonPrevious.setText(org.openide.util.NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.jButtonPrevious.text")); // NOI18N
        jButtonPrevious.setToolTipText(org.openide.util.NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.jButtonPrevious.toolTipText")); // NOI18N
        jButtonPrevious.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrevious.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPreviousActionPerformed(evt);
            }
        });
        editorBar.add(jButtonPrevious);

        jTextFielRecordNo.setEditable(false);
        jTextFielRecordNo.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFielRecordNo.setText(org.openide.util.NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.jTextFielRecordNo.text")); // NOI18N
        jTextFielRecordNo.setPreferredSize(new java.awt.Dimension(26, 20));
        editorBar.add(jTextFielRecordNo);

        jButtonNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Forward.png"))); // NOI18N
        jButtonNext.setText(org.openide.util.NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.jButtonNext.text")); // NOI18N
        jButtonNext.setToolTipText(org.openide.util.NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.jButtonNext.toolTipText")); // NOI18N
        jButtonNext.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNextActionPerformed(evt);
            }
        });
        editorBar.add(jButtonNext);

        jButtonStandalone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/editor.png"))); // NOI18N
        jButtonStandalone.setText(org.openide.util.NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.jButtonStandalone.text")); // NOI18N
        jButtonStandalone.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonStandalone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStandaloneActionPerformed(evt);
            }
        });
        editorBar.add(jButtonStandalone);

        add(editorBar, java.awt.BorderLayout.NORTH);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(0, 0));

        fieldsPanel.setPreferredSize(new java.awt.Dimension(170, 0));
        fieldsPanel.setLayout(new java.awt.GridBagLayout());
        jScrollPane1.setViewportView(fieldsPanel);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewActionPerformed
        createRecord();
	}//GEN-LAST:event_jButtonNewActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        int choice = JOptionPane.showConfirmDialog(this,
                "Confirmez-vous la suppression ?",
                "Relevé",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
        switch (choice)  {
            case 0: // YES
                dataManager.removeRecord(recordModel.getRecord(currentRecordIndex));
                break;
            default: // CANCEL
                //rien à faire
        }
    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jButtonPreviousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPreviousActionPerformed
        // avant de creer de changer de releve , je verifie la coherence du releve courant
        String errorMessage = recordModel.verifyRecord(currentRecordIndex);
        if (errorMessage.isEmpty()) {
            if (currentRecordIndex > 0) {
                selectRecord(currentRecordIndex -1);
            } else {
                Toolkit.getDefaultToolkit().beep();
                selectRecord(recordModel.getRowCount() -1);
            }

        } else {
            // j'affiche le message d'erreur
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, errorMessage, "Relevé", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_jButtonPreviousActionPerformed

    private void jButtonNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNextActionPerformed
        // avant de changer de releve , je verifie la coherence du releve courant
        String errorMessage = recordModel.verifyRecord(currentRecordIndex);
        if (errorMessage.isEmpty()) {
            if (currentRecordIndex < recordModel.getRowCount() -1 ) {
                selectRecord(currentRecordIndex +1);
            } else {
                Toolkit.getDefaultToolkit().beep();
                selectRecord(0);
            }

        } else {
            // j'affiche le message d'erreur
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(this, errorMessage, "Relevé", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonNextActionPerformed

    private void jButtonStandaloneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStandaloneActionPerformed
        TopComponent tc = WindowManager.getDefault().findTopComponent("ReleveTopComponent");
        ((ReleveTopComponent)tc).setStandaloneEditor(true);

    }//GEN-LAST:event_jButtonStandaloneActionPerformed

   
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel editorBar;
    private javax.swing.JPanel fieldsPanel;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonNew;
    private javax.swing.JButton jButtonNext;
    private javax.swing.JButton jButtonPrevious;
    private javax.swing.JButton jButtonStandalone;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextFielRecordNo;
    // End of variables declaration//GEN-END:variables

    /**
     * donne le focus au champ du type demandé
     * @param fieldType type du champ
     */
    public void selectField(FieldType fieldType) {
        // je cherche le champ du type demandé et je lui donne le focus
        for(Component component : fieldsPanel.getComponents()) {
            if ( component instanceof Bean) {
                Bean bean = ((Bean) component);
                if ( bean.getBeanField().getFieldType() == fieldType ) {
                    bean.requestFocusInWindow();
                    break;
                }
            }
        }
    }

    /**
     * rafraichis l'affichage de la valeur d'un champ dans son bean 
     * 
     * @param fieldType
     */
    private void refreshBeanField(FieldType fieldType) {
        // je cherche le champ contenant le nom de l'individu
        for(Component component : fieldsPanel.getComponents()) {
            if ( component instanceof Bean) {
                Bean bean = ((Bean) component);
                if ( bean.getBeanField().getFieldType() == fieldType ) {
                    bean.setContext(bean.getBeanField());
                    break;
                }
            }
        }
    }

}
