/*
 * ReleveEditor.java
 *
 * Created on 18 mars 2012, 10:43:03
 */
package ancestris.modules.releve.editor;

import ancestris.modules.releve.MenuCommandProvider;
import ancestris.modules.releve.editor.EditorConfigGroupDialog.EditorConfigListener;
import ancestris.modules.releve.model.PlaceListener;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldSex;
import ancestris.modules.releve.model.RecordModel;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.DataManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.ActionEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Michel
 */
@ServiceProvider(service=ancestris.modules.releve.editor.ReleveEditor.class)
public class ReleveEditor extends javax.swing.JPanel implements FocusListener, PlaceListener, EditorConfigListener {

    private DataManager dataManager = null;
    private RecordModel recordModel = null;
    private MenuCommandProvider menuCommandeProvider = null;
    private Bean currentFocusedBean = null;
    private Record currentRecord = null;
    private final ArrayList<KeyStroke> recordKeyStrokeList = new ArrayList<KeyStroke>();
    
    public ReleveEditor() {
        initComponents();

        jScrollPane1.getVerticalScrollBar().setUnitIncrement(20);
        //jScrollPane1.getActionMap().remove(KeyStroke.getKeyStroke(KeyEvent.VK_UP,0));
        //jScrollPane1.getActionMap().remove(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,0));
        jScrollPane1.getActionMap().remove(KeyStroke.getKeyStroke(KeyEvent.VK_UP,InputEvent.ALT_MASK));
        jScrollPane1.getActionMap().remove(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,InputEvent.ALT_MASK));

        String shortCut = "EditorShortcut";
        // je crée le racourci pour reseigner le nom de l'individu dans le nom du père de l'individu
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt X"), shortCut);
        // je crée le racourci pour copier le nom de l'épouse dans le nom du père de l'épouse
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt Y"), shortCut);
        // je crée le racourci pour donner le focus a l'age de l'individu
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt A"), shortCut);
        // je crée le racourci pour copier la date de l'evenement dans la date de naissance
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke("alt B"), shortCut);
        // je crée le racourci pour copier la meme valeur que celle de l'enregistrement précédent
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS , InputEvent.ALT_MASK), shortCut);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put( KeyStroke.getKeyStroke(KeyEvent.VK_PLUS , InputEvent.ALT_MASK ) , shortCut);

        getActionMap().put(shortCut, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if ( actionEvent.getActionCommand().toUpperCase().equals("X") ) {
                    copyIndiNameToIndiFatherName();
                } else if ( actionEvent.getActionCommand().toUpperCase().equals("Y") ) {
                    copyWifeNameToWifeFatherName();
                } else if ( actionEvent.getActionCommand().toUpperCase().equals("A") ) {
                    giveFocusToIndiAge();
                } else if ( actionEvent.getActionCommand().toUpperCase().equals("B") ) {
                    copyEventDateToIndiBirthDate();
                } else if ( actionEvent.getActionCommand().toLowerCase().equals("=") ) {
                    copyPreviousRecordField();
                } else if ( actionEvent.getActionCommand().toLowerCase().equals("+") ) {
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
    public void initModel(DataManager dataManager, MenuCommandProvider menuCommandeProvider) {
        if (dataManager != null) {
            //TODO modifier PlaceListener pour éviter que les listeners soient déclarés en double
            dataManager.removePlaceListener(this);

            this.dataManager = dataManager;
            this.recordModel = dataManager.getDataModel();
            this.menuCommandeProvider = menuCommandeProvider;

            // j'abonne l'editeur aux changements de lieu
            dataManager.addPlaceListener(this);
            jLabelPlace.setText(dataManager.getPlace().getValue());
            
            EditorConfigGroupDialog.addEditorConfigListener(this);
        } else {
            //TODO generer une exception
        } 
    }
    
    public void componentClosed() {
         EditorConfigGroupDialog.removeEditorConfigListener(this);
         if (dataManager != null) {
            dataManager.removePlaceListener(this);
         }
    }

     /**
     * Deplace le focus sur l'age de l'individu
     */
    private void giveFocusToIndiAge() {
        
        Bean indiAge = getBean(Record.FieldType.indiAge );
        // je deplace vers le bean
        if ( indiAge != null  ) {
            // je donne le focus au bean indiAge
            indiAge.requestFocusInWindow();
        } else {
            // j'emets un beep
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     * Copie le nom de l'individu dans le nom du pere de l'individu
     * et deplace le focus sur le nom du pere
     */
    private void copyIndiNameToIndiFatherName() {
        Bean indiLastNameBean = getBean(Record.FieldType.indiLastName );
        Bean indiFatherLastNameBean = getBean( Record.FieldType.indiFatherLastName);        

        // je copie le nom
        if ( indiLastNameBean!= null && indiFatherLastNameBean != null ) {
            // d'abord, je commite le champ en cours d'édition
            commitCurrentFocusedBean();
                        
            // je copie la valeur 
            indiFatherLastNameBean.replaceValue( indiFatherLastNameBean.getRecord().getField(indiLastNameBean.getFieldType()));
            indiFatherLastNameBean.requestFocusInWindow();
            commitCurrentFocusedBean();
                        
            // je donne le focus au bean suivant le nom du père (prénom du père) 
            Bean indiFatherFirstNameBean = getBean( Record.FieldType.indiFatherFirstName);
            if (indiFatherFirstNameBean != null) {
                // je donne le focus au bean indiFatherFirstNameBean
                indiFatherFirstNameBean.requestFocusInWindow();
            } 

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
        Bean wifeLastNameBean = getBean(Record.FieldType.wifeLastName);
        Bean wifeFatherLastNameBean = getBean(Record.FieldType.wifeFatherLastName);
        
        // je copie le nom
        if ( wifeLastNameBean != null && wifeFatherLastNameBean != null) {
            // d'abord, je commite le champ en cours d'édition
            commitCurrentFocusedBean();
            
            wifeFatherLastNameBean.replaceValue(wifeLastNameBean.getField()); 
            wifeFatherLastNameBean.requestFocusInWindow();
            commitCurrentFocusedBean();
            
            Bean wifeFatherFirstNameBean = getBean( Record.FieldType.wifeFatherFirstName);
            if (wifeFatherFirstNameBean != null) {
                // je donne le focus au bean indiFatherFirstNameBean
                wifeFatherFirstNameBean.requestFocusInWindow();
            } else {
                // je donne le focus au bean wifeFatherLastNameBean
                wifeFatherLastNameBean.requestFocusInWindow();
            }
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
        Bean eventDateBean = getBean(Record.FieldType.eventDate );
        Bean indiBirthDateBean = getBean(Record.FieldType.indiBirthDate );

       // je copie le nom
        if ( eventDateBean != null && indiBirthDateBean != null ) {
            // d'abord, je commite le champ en cours d'édition
            commitCurrentFocusedBean();
            // je copie la valeur 
            indiBirthDateBean.replaceValue( eventDateBean.getRecord().getField(eventDateBean.getFieldType()));            
            // je donne le focus au bean indiBirthDateBean
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
        

        // je cherche le champ courant qui a le focus
        Component focused = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if ( focused == null ) {
            // j'emets un beep
            Toolkit.getDefaultToolkit().beep();
            return;
        }
        
        // je recupere le releve créé précédemment
        Record previousRecord = dataManager.getDataModel().getRecord(dataManager.getPreviousRecordIndex());
        if (previousRecord == null) {
            // j'emets un beep
            Toolkit.getDefaultToolkit().beep();
            return;
        }


        // je recupere le champ du meme type dans le releve precedent
        Field previousField = previousRecord.getField(currentFocusedBean.getFieldType());
        // je copie la donnée du meme champ du releve precedent dans le bean
        // Attention : je ne copie pas la donnée directement dans le champ du releve courant pour
        // que les controles puissent s'effectuer comme si l'utilisateur avait saisi
        // la nouvelle valeur dans le bean
        currentFocusedBean.replaceValue(previousField);
    }
    
        /**
     * affiche un relevé
     * si le relevé est nul, nettoie l'affichage
     * @param record
     */
    public void selectRecord(int recordIndex) {
        selectRecord(recordModel.getRecord(recordIndex));        
    }
    
    /**
     * affiche un relevé
     * si le relevé est nul, nettoie l'affichage
     * @param record
     */
    private void selectRecord(Record record) {
        currentRecord = record;
        currentFocusedBean = null;
        fieldsPanel.setVisible(false);
        fieldsPanel.setFocusTraversalPolicyProvider(true);
        fieldsPanel.setFocusCycleRoot(true);
        fieldsPanel.resetKeyboardActions();
        for (KeyStroke keyStroke : recordKeyStrokeList) {
           getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).remove(keyStroke);
           getActionMap().remove(keyStroke.toString());
        }
        recordKeyStrokeList.clear();
        fieldsPanel.removeAll();

        if (recordModel != null) {
            int lineNo = 0;
            KeyStroke keyStroke;
            if (record != null) {
                for (EditorBeanGroup group : EditorBeanGroup.getGroups(record.getType())) {
                    if (!group.isVisible()) {
                        continue;
                    }
                    // le raccourci sera associé du premier champ du groupe qui va être créé
                    keyStroke = group.getKeystroke();
                    addRow(lineNo, group.getTitle(), keyStroke, record.getType(), group);
                    lineNo++;
                    for (EditorBeanField editorBeanField : group.getFields()) {
                        if (!editorBeanField.isVisible()) {
                            continue;
                        }
                        String label = editorBeanField.getLabel();
                        Bean bean;
                        switch (editorBeanField.getFieldType()) {
                            case eventType:
                                bean = new BeanEventType(dataManager.getCompletionProvider().getEventTypes());
                                break;

                            case indiBirthDate:
                                label = label.substring(0, 5) + ". (Alt-B)";
                                bean = new BeanDate();
                                break;
                            case eventDate:
                            case secondDate:
                            case wifeBirthDate:
                                bean = new BeanDate();
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
                                bean = new BeanLastName(dataManager.getCompletionProvider().getLastNames());
                                break;
                            case indiFatherLastName:
                                label += " (Alt-X)";
                                bean = new BeanLastName(dataManager.getCompletionProvider().getLastNames());
                                break;
                            case wifeFatherLastName:
                                label += " (Alt-Y)";
                                bean = new BeanLastName(dataManager.getCompletionProvider().getLastNames());
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
                                bean = new BeanFirstName(dataManager.getCompletionProvider().getFirstNames());
                                break;

                            case indiSex:
                                //case indiMarriedSex:
                            case wifeSex:
                                //case wifeMarriedSex:
                                bean = new BeanSex();
                                break;

                            case indiAge:
                                label += " (Alt-A)";
                                bean = new BeanAge();
                                break;

                            case indiFatherAge:
                            case indiMotherAge:
                            case wifeAge:
                            case wifeFatherAge:
                            case wifeMotherAge:
                                bean = new BeanAge();
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
                                bean = new BeanOccupation(dataManager.getCompletionProvider().getOccupations());
                                break;

                            case indiBirthPlace:
                            case wifeBirthPlace:
                            case indiResidence:
                            case indiMarriedResidence:
                            case indiFatherResidence:
                            case indiMotherResidence:
                            case wifeResidence:
                            case wifeMarriedResidence:
                            case wifeFatherResidence:
                            case wifeMotherResidence:
                                bean = new BeanPlace(dataManager.getCompletionProvider().getPlaces());
                                break;

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
                            default:
                                bean = new BeanSimpleValue();
                                break;

                            case freeComment:
                                bean = new BeanFreeComment();
                                break;

                            case notary:
                                bean = new BeanNotary(dataManager.getCompletionProvider().getNotaries());
                                break;
                        }

                        addRow(lineNo, label, bean, keyStroke);
                        bean.setContext(record, editorBeanField.getFieldType());
                        // j'ajoute le nom pour les tests unitaires
                        bean.setName(editorBeanField.getFieldType().name());
                        keyStroke = null;
                        lineNo++;
                    }
                }
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

        }
        fieldsPanel.revalidate();
        fieldsPanel.setVisible(true);
    }

    private void addRow(int lineNo, String label, final Bean bean, KeyStroke keyStroke) {

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
        //bean.setMinimumSize(new Dimension(30, 20));
        fieldsPanel.add(bean, gridBagConstraints);
        this.addLastFocusListeners(bean);

        // j'ajoute le raccourci clavier
        if (keyStroke != null) {
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, keyStroke.toString());
            recordKeyStrokeList.add(keyStroke);
            getActionMap().put(keyStroke.toString(), new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    bean.requestFocusInWindow();
                }
            });
        }

    }

    /**
     * ajoute une ligne de titre de groupe
     * @param lineNo
     * @param label
     * @param keyStroke 
     */
    private void addRow(int lineNo, String label, KeyStroke keyStroke, final Record.RecordType recordType, final EditorBeanGroup group) {

        // j'ajoute le label étalé dans les colonnes 0 et 1
        GridBagConstraints gridBagConstraints;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = lineNo;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        JLabel jLabel1 = new javax.swing.JLabel();
        
        JPanel jpanel1 = new JPanel();
        jpanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jpanel1.setLayout(new BorderLayout());
        
        
        JButton jbutton1 = new JButton();        
        jbutton1.setText("--");
        for( EditorBeanField field : group.getFields()) {
            if( field.isUsed() && !field.isVisible()) {
                jbutton1.setText("+");
                break;
            }
        }
        jbutton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jbutton1.setFocusable(false);
        //jbutton1.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        
        jbutton1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                EditorConfigGroupDialog.showEditorConfigGroupDialog(recordType, group, evt);
            }
        });
        
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        //jLabel1.setBorder(border);
        if (keyStroke == null) {
            jLabel1.setText(label);
        } else {
            jLabel1.setText(label + "   ( Alt-" + String.valueOf((char) keyStroke.getKeyCode()) + " )");
        }
        jpanel1.add(jbutton1,BorderLayout.WEST );
        jpanel1.add(jLabel1, BorderLayout.CENTER);
        fieldsPanel.add(jpanel1, gridBagConstraints);
        
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Implement EditorConfigListener
    ////////////////////////////////////////////////////////////////////////////
    /**
     * refraichit l'affichage de l'editeur
     */
    @Override
    public void onEditorConfigChanged(Record.RecordType recordType) {
        if( currentRecord != null && currentRecord.getType() == recordType) {
            // d'abord, je commite le champ en cours d'édition
            commitCurrentFocusedBean();
            // j'affiche à nouveau tous les beans visibles
            selectRecord(currentRecord);
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // Implement FocusListener
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

        // je recupere le bean parent
        Component parent = focused.getParent();
        while ((parent != null) && !(parent instanceof Bean)) {
            parent = parent.getParent();
        }
        // je mémorise le bean courant
        currentFocusedBean = (Bean) parent;

        // j'active la surbrillance du champ
        focused.setBackground(new Color(200, 255, 255));

        // je scrolle la fenetre de l'editeur pour voir le champ
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
       
        commitCurrentFocusedBean();
    }

    public boolean verifyCurrentRecord(int currentRecordIndex) {
        if (currentFocusedBean != null ) {            
            commitCurrentFocusedBean();            
            String errorMessage = dataManager.verifyRecord(currentFocusedBean.getRecord());
            if (errorMessage.isEmpty()) {
                return true;
            } else {
                // j'affiche le message d'erreur  et je demande s'il faut continuer
                Toolkit.getDefaultToolkit().beep();
                errorMessage += NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.verifyRecord.message");
                int choice = JOptionPane.showConfirmDialog(this,
                        errorMessage,
                        NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.verifyRecord.title"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE
                );

                switch (choice) {
                    case 0: // YES
                        return false;
                    case 1: // NO
                        return true;
                    default: // CANCEL
                        return true;
                }
            }
        } else {            
            return true;
        }
    }
    
    public void commitCurrentFocusedBean() {
        Bean bean = currentFocusedBean;
        // j'applique les modifications
        if (bean != null && bean.hasChanged()) {

            Record record = bean.getRecord();
            FieldType fieldType =  bean.getFieldType();

            // je memorise l'ancienne valeur
            String oldValue = bean.getFieldValue();
            
            // je commite le champ modifié (mise a jour du modele)
            bean.commit();

            // je demande confirmation à l'utilisateur si c'est un nouveau nom, prénom, profession ou TypeEventTag
            String newValue = bean.getFieldValue();
            if (DataManager.getNewValueControlEnabled() && !newValue.isEmpty()) {
                List<String> completionList = null;
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
                        completionList = dataManager.getCompletionProvider().getFirstNames().getIncluded();
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
                        completionList = dataManager.getCompletionProvider().getLastNames().getIncluded();
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
                        completionList = dataManager.getCompletionProvider().getOccupations().getIncluded();
                        break;
                    case eventType:
                        completionList = dataManager.getCompletionProvider().getEventTypes().getIncluded();
                        break;
                    case notary:
                        completionList = dataManager.getCompletionProvider().getNotaries().getIncluded();
                        break;
                }

                //je verifie si c'est une nouvelle valeur
                if ( completionList!= null && ! completionList.contains(newValue)) {
                    // je demande à l'utilisateur s'il veut enregistrer cette nouvelle valeur
                    Toolkit.getDefaultToolkit().beep();
                    int choice = JOptionPane.showConfirmDialog(this,
                            String.format(NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.confirmNewValue.question"), EditorBeanField.getLabel(bean.getFieldType()), newValue),
                            NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.confirmNewValue.title"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE);
                    // choice = 0 si l'utilisateur a cliqué sur OK 
                    // choice = 1 si l'utilisateur a cliqué sur NO
                    // choice = -1 si l'utilisateur a tapé la touche ESCAPE
                    if (choice == 1 || choice == -1) {
                        // l'utisateur a refusé de confirmer la nouvelle valeur
                        // j'annule le commit
                        bean.setFieldValue(oldValue);
                        // je rafraichis l'affichage
                        bean.refresh();
                        // je redonne le focus au champ
                        bean.requestFocusInWindow();
                        // j'interromps le tratitement
                        return;
                    }
                }
            }

            // je memorise l'ancienne valeur pour undo et notifie les listeners du changement
            if (recordModel != null && !oldValue.equals(bean.getFieldValue()) ) {
                recordModel.notiFyFieldChanged(record, bean.getFieldType(), bean.getField(), oldValue);
            }

            //je mets à jour la liste de completion des noms, prénoms, professions et type d'évènements
            // et les statistiques des sexes/prénoms
            switch (fieldType) {
                //je mets à jour la completion des prénoms
                case indiFirstName :
                    if (  !record.getFieldValue(FieldType.indiFirstName).isEmpty()) {
                    //    record.getIndi().getSex().setSex( dataManager.getCompletionProvider().getFirstNameSex(record.getIndi().getFirstName().getValue())) ;                    
                        record.setFieldValue(FieldType.indiSex, dataManager.getCompletionProvider().getFirstNameSex(record.getFieldValue(FieldType.indiFirstName))) ;
                        refreshBeanField(FieldType.indiSex);
                    }
                    dataManager.getCompletionProvider().updateFirstName(bean.getFieldValue(), record.getFieldValue(FieldType.indiSex), oldValue, record.getFieldValue(FieldType.indiSex));
                    break;
                case indiMarriedFirstName :
                    dataManager.getCompletionProvider().updateFirstName(bean.getFieldValue(), FieldSex.getOppositeString(record.getFieldValue(FieldType.indiSex)), oldValue, FieldSex.getOppositeString(record.getFieldValue(FieldType.indiSex)));
                    break;
                case indiFatherFirstName :
                    dataManager.getCompletionProvider().updateFirstName(bean.getFieldValue(), FieldSex.MALE_STRING, oldValue, FieldSex.MALE_STRING);
                    break;
                case indiMotherFirstName :
                    dataManager.getCompletionProvider().updateFirstName(bean.getFieldValue(), FieldSex.MALE_STRING, oldValue, FieldSex.MALE_STRING);
                    break;
                case wifeFirstName :
                    if ( !record.getFieldValue(FieldType.wifeFirstName).isEmpty() ) {
                        //record.getWife().getSex().setSex( dataManager.getCompletionProvider().getFirstNameSex(record.getWife().getFirstName().getValue())) ;
                        record.setFieldValue(FieldType.wifeSex, dataManager.getCompletionProvider().getFirstNameSex(record.getFieldValue(FieldType.wifeFirstName))) ;
                        refreshBeanField(FieldType.wifeSex);
                    }
                    dataManager.getCompletionProvider().updateFirstName(bean.getFieldValue(), record.getFieldValue(FieldType.wifeSex), oldValue, record.getFieldValue(FieldType.wifeSex));
                    break;
                case wifeMarriedFirstName :
                    dataManager.getCompletionProvider().updateFirstName(bean.getFieldValue(), FieldSex.getOppositeString(record.getFieldValue(FieldType.wifeSex)), oldValue, FieldSex.getOppositeString(record.getFieldValue(FieldType.wifeSex)));
                    break;
                case wifeFatherFirstName :
                    dataManager.getCompletionProvider().updateFirstName(bean.getFieldValue(), FieldSex.MALE_STRING, oldValue, FieldSex.MALE_STRING);
                    break;
                case wifeMotherFirstName :
                    dataManager.getCompletionProvider().updateFirstName(bean.getFieldValue(), FieldSex.FEMALE_STRING, oldValue, FieldSex.FEMALE_STRING);
                    break;
                case witness1FirstName :
                case witness2FirstName :
                case witness3FirstName :
                case witness4FirstName :
                    dataManager.getCompletionProvider().updateFirstName(bean.getFieldValue(), FieldSex.UNKNOWN_STRING, oldValue, FieldSex.UNKNOWN_STRING);
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
                    dataManager.getCompletionProvider().updateLastName(bean.getFieldValue(), oldValue);
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
                    dataManager.getCompletionProvider().updateOccupation(bean.getFieldValue(), oldValue);
                    break;
                case eventType:
                    //je mets à jour la completion des types d'évènement
                    dataManager.getCompletionProvider().updateEventType(bean.getFieldValue(), oldValue);
                    break;
                case notary:
                    dataManager.getCompletionProvider().updateNotary(bean.getFieldValue(), oldValue);
                    break;
                case indiBirthPlace:
                case wifeBirthPlace:
                case indiResidence:
                case indiMarriedResidence:
                case indiFatherResidence:
                case indiMotherResidence:
                case wifeResidence:
                case wifeMarriedResidence:
                case wifeFatherResidence:
                case wifeMotherResidence:
                    dataManager.getCompletionProvider().updatePlaces(bean.getFieldValue(), oldValue);
                    break;
                
            }
            
            // Si l'utilisateur vient de changer la date ou le nom de l'individu
            // je vérifie s'il y a un nouveau doublon.
            // et j'affiche un message d'avertissement si un doublon existe
            if ( fieldType.equals(Record.FieldType.eventDate)
                ||fieldType.equals(Record.FieldType.indiFirstName)
                ||fieldType.equals(Record.FieldType.indiLastName) ) {
                
                Record[] duplicateRecords = recordModel.findDuplicateRecord(record);
                if (duplicateRecords.length > 0) {
                    // j'affiche un message d'avertissement
                    String message = String.format(NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.duplicateRecord"));
                    message += "\n id=";
                    for ( int index =0; index <duplicateRecords.length; index ++ ) {
                        message += Integer.toString(recordModel.getIndex(record)+1);
                        if ( index < duplicateRecords.length-1) {
                           message += ", ";
                        }
                    }
                    String title = "Informations";
                    JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);

                }
            }

        }
    }

    /**
     * J'ajoute le listener de focus pour les TextField et Jcombox qui composent les beans
     * @param container
     */
    public void addLastFocusListeners(Container container) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            if ((component instanceof JTextField) || (component instanceof JFormattedTextField) || (component instanceof JComboBox) || (component instanceof JCheckBox)) {
                component.addFocusListener(this);
            }
            if (component instanceof Container) {
                addLastFocusListeners((Container) component);
            }
        }
    }
    
    /**
     * cherche un bean contenant un champ d'un type donné
     * @param fieldType type du champ
     */
    public Bean getBean(FieldType fieldType) {
        // je cherche le champ du type demandé et je lui donne le focus        
        for(Component component : fieldsPanel.getComponents()) {
            if ( component instanceof Bean) {
                Bean bean = ((Bean) component);
                if ( bean.getFieldType() == fieldType ) {
                    return bean; 
                }
            }
        }
        return null;
    }
    
    /**
     * donne le focus au champ du type demandé
     * @param fieldType type du champ
     */
    public void selectField(FieldType fieldType) {
        // je cherche le champ du type demandé et je lui donne le focus
        for(Component component : fieldsPanel.getComponents()) {
            if ( component instanceof Bean) {
                Bean bean = ((Bean) component);
                if ( bean.getFieldType() == fieldType ) {
                    bean.requestFocusInWindow();
                    break;
                }
            }
        }
    }

     /**
     * donne le focus au premier champ
     * @param fieldType type du champ
     */
    public void selectFirstField() {
        // je cherche le premier bean 
        for(Component component : fieldsPanel.getComponents()) {
            if ( component instanceof Bean) {
                Bean bean = ((Bean) component);
                bean.requestFocusInWindow();
                break;
            }
        }
    }


    /**
     * rafraichis l'affichage d'un champ dans son bean 
     * 
     * @param fieldType
     */
    public void refreshBeanField(FieldType fieldType) {
        // je cherche le champ contenant le nom de l'individu
        for(Component component : fieldsPanel.getComponents()) {
            if ( component instanceof Bean) {
                Bean bean = ((Bean) component);
                if ( bean.getFieldType() == fieldType ) {
                    bean.refresh();
                    break;
                }
            }
        }
    }

    /**
     * met a jour le lieu en tete de l'editeur
     * @param place
     */
    @Override
    public void updatePlace(String place) {
        jLabelPlace.setText(place);
    }
  

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        jPanelPlace = new javax.swing.JPanel();
        jLabelPlace = new javax.swing.JLabel();
        jButtonPlace = new javax.swing.JButton();
        fieldsPanel = new javax.swing.JPanel();

        setOpaque(false);
        setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanelPlace.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanelPlace.setLayout(new java.awt.BorderLayout());

        jLabelPlace.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelPlace.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jLabelPlace.setName("placeLabel"); // NOI18N
        jLabelPlace.setOpaque(true);
        jPanelPlace.add(jLabelPlace, java.awt.BorderLayout.CENTER);

        jButtonPlace.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/registre.png"))); // NOI18N
        jButtonPlace.setToolTipText(org.openide.util.NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.toolTipText")); // NOI18N
        jButtonPlace.setName(""); // NOI18N
        jButtonPlace.setPreferredSize(new java.awt.Dimension(29, 25));
        jButtonPlace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPlaceActionPerformed(evt);
            }
        });
        jPanelPlace.add(jButtonPlace, java.awt.BorderLayout.EAST);

        jPanel1.add(jPanelPlace, java.awt.BorderLayout.NORTH);

        fieldsPanel.setName(""); // NOI18N
        fieldsPanel.setLayout(new java.awt.GridBagLayout());
        jPanel1.add(fieldsPanel, java.awt.BorderLayout.CENTER);

        jScrollPane1.setViewportView(jPanel1);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonPlaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPlaceActionPerformed
        menuCommandeProvider.showConfigPanel();
    }//GEN-LAST:event_jButtonPlaceActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel fieldsPanel;
    private javax.swing.JButton jButtonPlace;
    private javax.swing.JLabel jLabelPlace;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanelPlace;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
  
}