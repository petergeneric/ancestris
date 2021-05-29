/*
 * RelevePanel.java
 *
 * Created on 25 mars 2012, 16:43:34
 */
package ancestris.modules.releve;

import ancestris.modules.releve.editor.ReleveEditor;
import ancestris.modules.releve.model.DataManager;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.Record.RecordType;
import ancestris.modules.releve.model.RecordModelListener;
import ancestris.modules.releve.table.ReleveTableListener;
import ancestris.modules.releve.table.TableModelRecordAbstract;
import ancestris.modules.releve.table.TableModelRecordAll;
import ancestris.modules.releve.table.TableModelRecordBirth;
import ancestris.modules.releve.table.TableModelRecordDeath;
import ancestris.modules.releve.table.TableModelRecordMarriage;
import ancestris.modules.releve.table.TableModelRecordMisc;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Michel
 */
public class RelevePanel extends javax.swing.JPanel implements ReleveTableListener {

    private boolean standaloneMode = false;
    private MenuCommandProvider menuCommandProvider;
    private DataManager dataManager = null;
    private PanelType panelType = null;
    private RecordType recordType = null;
    private int currentRecordIndex = -1;

    private static final String dialogTitle = NbBundle.getMessage(RelevePanel.class, "DialogTitle");

    static public enum PanelType {
        birth, marriage, death, misc, all
    }

    private final JPopupMenu popup = new JPopupMenu();
    private final JMenuItem menuItemInsert = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.insert"));
    private final JMenuItem menuItemDelete = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.delete"));
    private final JMenuItem menuItemSwapNext = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.swapnext"));
    private final JMenuItem menuItemSwapPrevious = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.swapprevious"));
    private final JMenuItem menuItemReorder = new JMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.reorder"));
    private final JCheckBoxMenuItem menuItemGedcomLink = new JCheckBoxMenuItem(NbBundle.getMessage(ReleveTopComponent.class, "ReleveTopComponent.menu.gedcomLink"));

    public RelevePanel() {
        initComponents();
        // J'applique un poids=1 pour que seule la largeur du composant de gauche soit modifiée quand on change la taille de la fenetre
        jSplitPane1.setResizeWeight(1);

        // je force la largeur du jButtonFile pour contenir le texte en entier et
        // et la hauteur egale aux autres boutons
        //Rectangle2D rect = jButtonFile.getFont().getStringBounds(jButtonFile.getText(), jButtonFile.getFontMetrics(jButtonFile.getFont()).getFontRenderContext());
        //jButtonFile.setPreferredSize(new Dimension((int) rect.getWidth() + jButtonFile.getMargin().left + jButtonFile.getMargin().right + 8 + jButtonFile.getInsets().left + jButtonFile.getInsets().right, 25));

        jButtonPrevious.setVisible(false);
        jTextFielRecordNo.setVisible(false);
        jButtonNext.setVisible(false);
        jButtonBrowser.setVisible(false);

        jButtonDelete.setEnabled(false);

        // je crée les raccourcis pour créer les nouveaux relevés
        String shortCut = "PanelShortcut";
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt S"), shortCut);
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("alt Z"), shortCut);

        getActionMap().put(shortCut, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().toUpperCase().equals("C")) {
                    // Create
                    jButtonNewActionPerformed(actionEvent);
                } else if (actionEvent.getActionCommand().toUpperCase().equals("S")) {
                    // Delete
                    jButtonDeleteActionPerformed(actionEvent);
                } else if (actionEvent.getActionCommand().toUpperCase().equals("Z")) {
                    // undo
                    Record record = dataManager.getDataModel().undo();
                    if (record != null) {
                        // je selectionne le relevé concerné par undo
                        selectRecord(dataManager.getDataModel().getIndex(record));
                    }
                }
            }

        });

        //je cree le popupmenu
        ActionListener popupMouseHandler = new ActionListener() {
            /**
             * traite les évènements du popumenu
             *
             * @param e
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (menuItemInsert.equals(e.getSource())) {
                    insertRecord();
                } else if (menuItemDelete.equals(e.getSource())) {
                    removeRecord();
                } else if (menuItemSwapNext.equals(e.getSource())) {
                    swapRecordNext();
                } else if (menuItemSwapPrevious.equals(e.getSource())) {
                    swapRecordPrevious();
                } else if (menuItemReorder.equals(e.getSource())) {
                    renumberRecords();
                } else if (menuItemGedcomLink.equals(e.getSource())) {
                    showGedcomLink(menuItemGedcomLink.isSelected());
                }
            }
        };

        // insert,  delete
        menuItemInsert.addActionListener(popupMouseHandler);
        menuItemInsert.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/NewRecord.png")));
        popup.add(menuItemInsert);
        menuItemDelete.addActionListener(popupMouseHandler);
        menuItemDelete.setIcon(new ImageIcon(getClass().getResource("/ancestris/modules/releve/images/DeleteRecord.png")));
        popup.add(menuItemDelete);

        // swap, reorder, gedcomlink
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

        // active le listener de la souris pour l'affichage du popupmenu quand
        // on clique avec le bouton droit de la souris
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
        this.addMouseListener(mouseAdapter);
        releveTable.addMouseListener(mouseAdapter);
        jScrollPaneTable.addMouseListener(mouseAdapter);
        tablePanel.addMouseListener(mouseAdapter);
        //jSplitPane1.addMouseListener(mouseAdapter);

    }

    public void setGedcomLinkSelected(boolean selected) {
        menuItemGedcomLink.setSelected(selected);
    }

    public void setModel(final DataManager dataManager, final PanelType panelType,
            MenuCommandProvider menuComandProvider) {

        this.menuCommandProvider = menuComandProvider;
        this.dataManager = dataManager;
        this.panelType = panelType;

        final TableModelRecordAbstract tableModel;
        switch (panelType) {
            case birth:
                tableModel = new TableModelRecordBirth(dataManager);
                break;
            case death:
                tableModel = new TableModelRecordDeath(dataManager);
                break;
            case marriage:
                tableModel = new TableModelRecordMarriage(dataManager);
                break;
            case misc:
                tableModel = new TableModelRecordMisc(dataManager);
                break;
            default:
                tableModel = new TableModelRecordAll(dataManager);

        }

        releveTable.setTableSelectionListener(this);
        releveTable.setModel(tableModel);
        releveTable.setFocusable(true);
        releveTable.requestFocus();
        releveTable.setDropMode(DropMode.USE_SELECTION);
        releveTable.setTransferHandler(new RecordTransferHandle(dataManager));
        releveTable.setDragEnabled(true);

        releveEditor.initModel(dataManager, menuComandProvider);

        RecordModelListener listener = new RecordModelListener() {

            @Override
            public void recordInserted(int firstIndex, int lastIndex) {
                // je mets à jour la table 
                tableModel.fireTableRowsInserted(firstIndex, lastIndex);
                // je mets à jour l'editeur
                if (firstIndex <= currentRecordIndex && currentRecordIndex <= lastIndex) {
                    // je refraichis l'affichage de tous les champs
                    releveEditor.selectRecord(currentRecordIndex);
                }
            }

            @Override
            public void recordDeleted(int firstIndex, int lastIndex) {
                // je mets à jour la table 
                tableModel.fireTableRowsDeleted(firstIndex, lastIndex);
                // je mets à jour l'editeur
                if (firstIndex <= currentRecordIndex && currentRecordIndex <= lastIndex) {
                    releveEditor.selectRecord(-1);
                }

            }

            /**
             * appelé quand on echange la position de deux releves (swap)
             *
             * @param firstIndex
             * @param lastIndex
             */
            @Override
            public void recordUpdated(int firstIndex, int lastIndex) {
                // je mets à jour la table 
                tableModel.fireTableRowsUpdated(firstIndex, lastIndex);
                // je mets à jour l'editeur
                if (firstIndex <= currentRecordIndex && currentRecordIndex <= lastIndex) {
                    // je refraichis l'affichage de tous les champs
                    releveEditor.selectRecord(currentRecordIndex);
                }
            }

            @Override
            public void recordUpdated(int recordIndex, Record.FieldType fieldType) {
                // je mets à jour la table 
                tableModel.fireTableRowsUpdated(recordIndex, recordIndex);
                // je mets à jour l'editeur
                if (recordIndex == currentRecordIndex) {
                    releveEditor.refreshBeanField(fieldType);
                }
            }

            @Override
            public void allChanged() {
                // je mets à jour la table 
                tableModel.fireTableDataChanged();
                // je mets à jour l'editeur
                releveEditor.selectRecord(-1);
            }

        };

        dataManager.getDataModel().addRecordModelListener(listener);

        // je recupere le toolTipText du bouton
        String toolTipText = org.openide.util.NbBundle.getMessage(ReleveEditor.class, "ReleveEditor.jButtonNew.toolTipText");

        // je complete le toolTipText du bouton "créer" en fonction du type de panel
        switch (panelType) {
            case birth:
                jButtonNew.setToolTipText(toolTipText + " (ALT-N)");
                recordType = RecordType.BIRTH;
                popup.remove(menuItemReorder);
                break;
            case marriage:
                jButtonNew.setToolTipText(toolTipText + " (ALT-M)");
                recordType = RecordType.MARRIAGE;
                popup.remove(menuItemReorder);
                break;
            case death:
                jButtonNew.setToolTipText(toolTipText + " (ALT-D)");
                recordType = RecordType.DEATH;
                popup.remove(menuItemReorder);
                break;
            case misc:
                jButtonNew.setToolTipText(toolTipText + " (ALT-V)");
                recordType = RecordType.MISC;
                popup.remove(menuItemReorder);
                break;
            default:
                jButtonNew.setToolTipText(toolTipText + " (ALT-T)");
                recordType = null;
                break;
        }

        if (standaloneMode == false) {
            int editorWidth = getEditorWidth();
            jSplitPane1.getRightComponent().setPreferredSize(new Dimension(editorWidth, jSplitPane1.getRightComponent().getHeight()));
        }

    }

    /**
     * sauvegarde de la configuration a la fermeture du composant
     */
    public void componentClosed() {
        releveEditor.componentClosed();
        releveTable.componentClosed();
        if (standaloneMode == false) {
            int editorWidth = jSplitPane1.getRightComponent().getWidth();
            putEditorWidth(editorWidth);
        }
    }

    /**
     * sélectionne une ligne en fonction de l'index de record du modèle
     *
     * @param recordIndex index du relevé
     */
    public void selectRecord(int recordIndex) {
        currentRecordIndex = recordIndex;

        // je mets à jour la toolbar du panel
        updateToolBar();
        // je mets à jour la table 
        releveTable.selectRecord(currentRecordIndex);
        // je mets à jour l'editeur
        releveEditor.selectRecord(currentRecordIndex);
    }

    /**
     * retourne l'index du releve courant
     *
     * @return
     */
    public int getCurrentRecordIndex() {
        return currentRecordIndex;
    }
    
    /**
     * retounr le nombre de lignes de la table
     * @return 
     */
    public int getRowCount() {
        return releveTable.getRowCount();
    }

    /**
     * sélectionne une ligne en fonction du numéro de ligne de la table
     *
     * @param rowIndex numéro de la ligne dans la table
     */
    public void selectRow(int rowIndex) {
        if (releveTable.getRowCount() > 0 && rowIndex != -1) {
            selectRecord(releveTable.convertRowIndexToModel(rowIndex));
        } else {
            selectRecord(-1);
        }
    }

    /**
     * active le bouton Delete si le releve courant est valide et affiche le
     * numero du relevé.
     */
    private void updateToolBar() {
        if (currentRecordIndex != -1) {
            jButtonDelete.setEnabled(true);
            jTextFielRecordNo.setText(String.valueOf(currentRecordIndex + 1));
        } else {
            jButtonDelete.setEnabled(false);
            jTextFielRecordNo.setText("");
        }
    }

    public void setStandaloneMode() {
        jButtonFile.setVisible(false);
        jButtonPrevious.setVisible(true);
        jButtonNext.setVisible(true);
        jButtonBrowser.setVisible(true);
        jButtonStandalone.setVisible(false);
        jTextFielRecordNo.setVisible(true);
        standaloneMode = true;

        // je masque la table
        tablePanel.setVisible(false);
        jSplitPane1.setDividerSize(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        tablePanel = new javax.swing.JPanel();
        jScrollPaneTable = new javax.swing.JScrollPane();
        releveTable = new ancestris.modules.releve.table.ReleveTable();
        editorPanel = new javax.swing.JPanel();
        editorBar = new javax.swing.JPanel();
        jButtonFile = new javax.swing.JButton();
        jButtonConfig = new javax.swing.JButton();
        jButtonNew = new javax.swing.JButton();
        jButtonDelete = new javax.swing.JButton();
        jButtonPrevious = new javax.swing.JButton();
        jTextFielRecordNo = new javax.swing.JTextField();
        jButtonNext = new javax.swing.JButton();
        jButtonBrowser = new javax.swing.JButton();
        jButtonStandalone = new javax.swing.JButton();
        releveEditor = new ancestris.modules.releve.editor.ReleveEditor();

        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setResizeWeight(1.0);

        tablePanel.setPreferredSize(new java.awt.Dimension(0, 0));
        tablePanel.setLayout(new java.awt.BorderLayout());

        jScrollPaneTable.setViewportView(releveTable);

        tablePanel.add(jScrollPaneTable, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(tablePanel);

        editorPanel.setPreferredSize(new java.awt.Dimension(270, 100));
        editorPanel.setLayout(new java.awt.BorderLayout(4, 4));

        editorBar.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        editorBar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jButtonFile.setText(org.openide.util.NbBundle.getMessage(RelevePanel.class, "RelevePanel.jButtonFile.text")); // NOI18N
        jButtonFile.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFileActionPerformed(evt);
            }
        });
        editorBar.add(jButtonFile);

        jButtonConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Settings.png"))); // NOI18N
        jButtonConfig.setToolTipText(org.openide.util.NbBundle.getMessage(RelevePanel.class, "RelevePanel.jButtonConfig.toolTipText")); // NOI18N
        jButtonConfig.setPreferredSize(new java.awt.Dimension(44, 31));
        jButtonConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConfigActionPerformed(evt);
            }
        });
        editorBar.add(jButtonConfig);

        jButtonNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/NewRecord.png"))); // NOI18N
        jButtonNew.setToolTipText(org.openide.util.NbBundle.getMessage(RelevePanel.class, "RelevePanel.jButtonNew.toolTipText")); // NOI18N
        jButtonNew.setActionCommand("CreateRecord"); // NOI18N
        jButtonNew.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonNew.setPreferredSize(new java.awt.Dimension(44, 31));
        jButtonNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNewActionPerformed(evt);
            }
        });
        editorBar.add(jButtonNew);

        jButtonDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/DeleteRecord.png"))); // NOI18N
        jButtonDelete.setToolTipText(org.openide.util.NbBundle.getMessage(RelevePanel.class, "RelevePanel.jButtonDelete.toolTipText")); // NOI18N
        jButtonDelete.setActionCommand("RemoveRecord"); // NOI18N
        jButtonDelete.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButtonDelete.setPreferredSize(new java.awt.Dimension(44, 31));
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });
        editorBar.add(jButtonDelete);

        jButtonPrevious.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Back.png"))); // NOI18N
        jButtonPrevious.setToolTipText(org.openide.util.NbBundle.getMessage(RelevePanel.class, "RelevePanel.jButtonPrevious.toolTipText")); // NOI18N
        jButtonPrevious.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonPrevious.setPreferredSize(new java.awt.Dimension(44, 31));
        jButtonPrevious.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPreviousActionPerformed(evt);
            }
        });
        editorBar.add(jButtonPrevious);

        jTextFielRecordNo.setEditable(false);
        jTextFielRecordNo.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextFielRecordNo.setPreferredSize(new java.awt.Dimension(30, 31));
        editorBar.add(jTextFielRecordNo);

        jButtonNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Forward.png"))); // NOI18N
        jButtonNext.setToolTipText(org.openide.util.NbBundle.getMessage(RelevePanel.class, "RelevePanel.jButtonNext.toolTipText")); // NOI18N
        jButtonNext.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonNext.setPreferredSize(new java.awt.Dimension(44, 31));
        jButtonNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNextActionPerformed(evt);
            }
        });
        editorBar.add(jButtonNext);

        jButtonBrowser.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/Camera.png"))); // NOI18N
        jButtonBrowser.setToolTipText(org.openide.util.NbBundle.getMessage(RelevePanel.class, "RelevePanel.jButtonBrowser.toolTipText")); // NOI18N
        jButtonBrowser.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonBrowser.setPreferredSize(new java.awt.Dimension(44, 31));
        jButtonBrowser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBrowserActionPerformed(evt);
            }
        });
        editorBar.add(jButtonBrowser);

        jButtonStandalone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/editor.png"))); // NOI18N
        jButtonStandalone.setToolTipText(org.openide.util.NbBundle.getMessage(RelevePanel.class, "RelevePanel.jButtonStandalone.toolTipText")); // NOI18N
        jButtonStandalone.setMargin(new java.awt.Insets(2, 4, 2, 4));
        jButtonStandalone.setPreferredSize(new java.awt.Dimension(44, 31));
        jButtonStandalone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStandaloneActionPerformed(evt);
            }
        });
        editorBar.add(jButtonStandalone);

        editorPanel.add(editorBar, java.awt.BorderLayout.NORTH);

        releveEditor.setFont(new java.awt.Font("Arial", 2, 11)); // NOI18N
        releveEditor.setMinimumSize(new java.awt.Dimension(100, 300));
        editorPanel.add(releveEditor, java.awt.BorderLayout.CENTER);

        jSplitPane1.setRightComponent(editorPanel);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFileActionPerformed
        // j'affiche le menu Fichier
        menuCommandProvider.showPopupMenu(jButtonFile, 0, jButtonFile.getHeight());
}//GEN-LAST:event_jButtonFileActionPerformed

    private void jButtonConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConfigActionPerformed
        // J'affiche le panneau des options
        menuCommandProvider.showOptionPanel();
}//GEN-LAST:event_jButtonConfigActionPerformed

    private void jButtonNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNewActionPerformed
        createRecord();
}//GEN-LAST:event_jButtonNewActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        removeRecord();
}//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jButtonPreviousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPreviousActionPerformed
        // je recherche l'index de la ligne precedente
        if (releveTable.getRowCount() > 0) {
            int rowIndex = releveTable.convertRowIndexToView(currentRecordIndex);

            if (rowIndex > 0) {
                // je selection la ligne precedente
                selectRow(rowIndex - 1);
            } else {
                // j'affiche la derniere ligne
                Toolkit.getDefaultToolkit().beep();
                selectRow(releveTable.getRowCount() - 1);
            }
        } else {
            // la table est vide
            selectRow(-1);
        }
        menuCommandProvider.showImage();
    }//GEN-LAST:event_jButtonPreviousActionPerformed

    private void jButtonNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNextActionPerformed
        // je recherche l'index de la ligne suivante
        if (releveTable.getRowCount() > 0) {
            int rowIndex = releveTable.convertRowIndexToView(currentRecordIndex);
            if (rowIndex < releveTable.getRowCount() - 1) {
                selectRow(rowIndex + 1);
            } else {
                // j'affiche la premiere ligne
                Toolkit.getDefaultToolkit().beep();
                selectRow(0);
            }
        } else {
            // la table est vide
            selectRow(-1);
        }
        menuCommandProvider.showImage();
}//GEN-LAST:event_jButtonNextActionPerformed

    private void jButtonStandaloneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStandaloneActionPerformed
        if (standaloneMode == false) {
            // j'affiche l'editeur standalone
            menuCommandProvider.showStandalone();
        } else {
            // j'affiche l'editeur de la fenetre principale au premier plan
            menuCommandProvider.showToFront();
        }
}//GEN-LAST:event_jButtonStandaloneActionPerformed

    private void jButtonBrowserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBrowserActionPerformed
        if (standaloneMode == true) {
            // j'inverse la vibilite de la visionneuse d'image 
            menuCommandProvider.toggleBrowserVisible();
            menuCommandProvider.showImage();
        }
    }//GEN-LAST:event_jButtonBrowserActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel editorBar;
    private javax.swing.JPanel editorPanel;
    private javax.swing.JButton jButtonBrowser;
    private javax.swing.JButton jButtonConfig;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonFile;
    private javax.swing.JButton jButtonNew;
    private javax.swing.JButton jButtonNext;
    private javax.swing.JButton jButtonPrevious;
    private javax.swing.JButton jButtonStandalone;
    private javax.swing.JScrollPane jScrollPaneTable;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField jTextFielRecordNo;
    private ancestris.modules.releve.editor.ReleveEditor releveEditor;
    private ancestris.modules.releve.table.ReleveTable releveTable;
    private javax.swing.JPanel tablePanel;
    // End of variables declaration//GEN-END:variables

    void selectField(Record.FieldType fieldType) {
        releveEditor.selectField(fieldType);
    }

    public void createRecord() {
        // avant de creer le nouveau releve , je verifie la coherence du releve courant
        if (verifyCurrentRecord()) {
            Record record;
            if (panelType != RelevePanel.PanelType.all) {
                // je cree un nouveau releve
                record = dataManager.createRecord(recordType);
                currentRecordIndex = dataManager.addRecord(record);
            } else {
                // je demande de choisir le type du releve
                String title = NbBundle.getMessage(ReleveTopComponent.class, "RelevePanel.title");
                String message = NbBundle.getMessage(ReleveTopComponent.class, "RelevePanel.createRecord.message");
                String[] options = {
                    NbBundle.getMessage(ReleveTopComponent.class, "RelevePanel.birth"),
                    NbBundle.getMessage(ReleveTopComponent.class, "RelevePanel.marriage"),
                    NbBundle.getMessage(ReleveTopComponent.class, "RelevePanel.death"),
                    NbBundle.getMessage(ReleveTopComponent.class, "RelevePanel.misc"),
                    NbBundle.getMessage(ReleveTopComponent.class, "RelevePanel.cancel")};
                Toolkit.getDefaultToolkit().beep();
                int result = JOptionPane.showOptionDialog(this,
                        message,
                        title,
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        (javax.swing.Icon) null,
                        options,
                        options[4]);
                switch (result) {
                    case 0:
                        record = dataManager.createRecord(RecordType.BIRTH);
                        break;
                    case 1:
                        record = dataManager.createRecord(RecordType.MARRIAGE);
                        break;
                    case 2:
                        record = dataManager.createRecord(RecordType.DEATH);
                        break;
                    case 3:
                        record = dataManager.createRecord(RecordType.MISC);
                        break;
                    default:
                        // j'abandonne la creation du releve
                        return;
                }
                currentRecordIndex = dataManager.addRecord(record);
            }

            // je copie les champs du relevé précédent           
            copyFieldsfromPreviousRecord(record);

            // je mets à jour la toolbar
            updateToolBar();
            // je selectionne le nouveau releve dans la table
            releveTable.selectRecord(currentRecordIndex);
            // j'affiche le relevé dans l'editeur et je sélectionne le premier champ par defaut
            releveEditor.selectRecord(currentRecordIndex);
            releveEditor.selectFirstField();
        }
    }

    public void insertRecord() {
        // avant de creer le nouveau releve , je verifie la coherence du releve courant
        if (verifyCurrentRecord()) {
            // je cree un nouveau releve
            if (panelType != RelevePanel.PanelType.all) {
                dataManager.insertRecord(recordType, currentRecordIndex);
            } else {
                String title = NbBundle.getMessage(ReleveTopComponent.class, "RelevePanel.title");
                String message = NbBundle.getMessage(ReleveTopComponent.class, "RelevePanel.createRecord.message");
                String[] options = {
                    NbBundle.getMessage(ReleveTopComponent.class, "RelevePanel.birth"),
                    NbBundle.getMessage(ReleveTopComponent.class, "RelevePanel.marriage"),
                    NbBundle.getMessage(ReleveTopComponent.class, "RelevePanel.death"),
                    NbBundle.getMessage(ReleveTopComponent.class, "RelevePanel.misc"),
                    NbBundle.getMessage(ReleveTopComponent.class, "RelevePanel.cancel")};
                Toolkit.getDefaultToolkit().beep();
                int result = JOptionPane.showOptionDialog(this,
                        message,
                        title,
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        (javax.swing.Icon) null,
                        options,
                        options[4]);
                switch (result) {
                    case 0:
                        dataManager.insertRecord(RecordType.BIRTH, currentRecordIndex);
                        break;
                    case 1:
                        dataManager.insertRecord(RecordType.MARRIAGE, currentRecordIndex);
                        break;
                    case 2:
                        dataManager.insertRecord(RecordType.DEATH, currentRecordIndex);
                        break;
                    case 3:
                        dataManager.insertRecord(RecordType.MISC, currentRecordIndex);
                        break;
                    default:
                        // j'abandonne la creation du releve
                        return;
                }
            }

            // je copie les champs du relevé précédent     
            copyFieldsfromPreviousRecord(dataManager.getRecord(currentRecordIndex));
            
            // je mets à jour la toolbar
            updateToolBar();
            // je selectionne le nouveau releve dans la table
            releveTable.selectRecord(currentRecordIndex);
            // j'affiche le relevé dans l'editeur et je sélectionne le premier champ par defaut
            releveEditor.selectRecord(currentRecordIndex);
            releveEditor.selectFirstField();
        }
    }

    /**
     * copie les champs du relevé précedent du même type 
     * si le relevé précédent du même type n'existe pas , copie les champs 
     * du dernier relevé créé
     * @param record
     */
    private void copyFieldsfromPreviousRecord(Record record) {
        // je recupère l'index du relevé affiché dans l'editeur 
        int previousRecordIndex = dataManager.getPreviousRecordIndex();
        Record previousRecord = dataManager.getDataModel().getRecord(previousRecordIndex);
        if (previousRecord == null) {
            // le relevé précédent du même type n'existe pas 
            // je recupère le dernier relevé créé
            previousRecordIndex = dataManager.getDataModel().getRowCount() - 1;
            previousRecord = dataManager.getDataModel().getRecord(previousRecordIndex);
            if (previousRecord == null) {
                return;
            }
        }

        if (DataManager.getCopyCoteEnabled()) {
            record.setFieldValue(FieldType.cote, previousRecord.getFieldValue(Record.FieldType.cote));
        }

        if (DataManager.getCopyEventDateEnabled()) {
            record.setFieldValue(FieldType.eventDate, previousRecord.getFieldValue(FieldType.eventDate));
            //record.setEventCalendar(previousRecord.getEventDateCalendar());
        }

        if (DataManager.getCopySecondDateEnabled() && record.getType() == RecordType.MISC) {
            // le champ secondDate est nul si le releve precedent n'est pas du type misc
            if (previousRecord.getType() == RecordType.MISC) {
                record.setFieldValue(FieldType.secondDate, previousRecord.getFieldValue(FieldType.secondDate));
               // record.setEventCalendar(previousRecord.getEventSecondDateCalendar());
            }
        }

        if (DataManager.getCopyFreeCommentEnabled()) {
            record.setFieldValue(FieldType.freeComment,  previousRecord.getFieldValue(FieldType.freeComment));
        }

        if (DataManager.getCopyNotaryEnabled() && record.getType() == RecordType.MISC) {
            // le champ notary est nul si le releve  precedent n'est pas du type misc
            if (previousRecord.getType() == RecordType.MISC) {
                record.setFieldValue(FieldType.notary, previousRecord.getFieldValue(FieldType.notary));
            }
        }

        if (DataManager.getCopyParishEnabled()) {
            record.setFieldValue(FieldType.parish, previousRecord.getFieldValue(FieldType.parish));
        }
    }

    public void renumberRecords() {
        if (panelType == PanelType.all) {
            // avant de renuméroter les relevés , je vérifie la coherence du releve courant
            if (verifyCurrentRecord()) {

                int[] tableIndexList = new int[releveTable.getRowCount()];
                for (int i = 0; i < tableIndexList.length; i++) {
                    tableIndexList[i] = releveTable.convertRowIndexToView(i);
                    //tableIndexList[i] = releveTable.convertRowIndexToModel(i);
                }
                dataManager.renumberRecords(dataManager.getRecord(currentRecordIndex), tableIndexList);
            }
        }
    }

    public void showGedcomLink(boolean state) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            dataManager.showGedcomLink(state, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        setCursor(Cursor.getDefaultCursor());
        // je notify lea autres panels
        menuCommandProvider.setGedcomLinkSelected(state);
    }

    public void removeRecord() {

        Toolkit.getDefaultToolkit().beep();
        int choice = JOptionPane.showConfirmDialog(this,
                NbBundle.getMessage(ReleveTopComponent.class, "RelevePanel.removeRecord.message"),
                dialogTitle,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        switch (choice) {
            case 0: // YES
                //removeRecord();
                break;
            default: // CANCEL
                //rien à faire
                return;
        }
        // je memorise l'index de la ligne qui va être supprimée
        int rowIndex = releveTable.convertRowIndexToView(currentRecordIndex);
        dataManager.removeRecord(dataManager.getRecord(currentRecordIndex));

        if (rowIndex < releveTable.getRowCount()) {
            // je selectionne la ligne suivante
            selectRow(rowIndex);
        } else {
            // il n'y a pas de ligne suivante
            if (rowIndex > 0) {
                // je selectionne la ligne precedente
                selectRow(rowIndex - 1);
            } else {
                // il n'y a plus de lignes
                selectRow(-1);
            }
        }
    }

    private int getEditorWidth() {
        int width = Integer.valueOf(NbPreferences.forModule(RelevePanel.class).get(
                panelType.name() + "Width", "270"));
        return width;
    }

    private void putEditorWidth(int width) {
        NbPreferences.forModule(RelevePanel.class).put(
                panelType.name() + "Width", String.valueOf(width));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implemente ReleveTableListener methods
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public void swapRecordNext() {
        dataManager.swapRecordNext(dataManager.getRecord(currentRecordIndex));
        releveTable.selectRecord(currentRecordIndex + 1);
    }

    @Override
    public void swapRecordPrevious() {
        dataManager.swapRecordPrevious(dataManager.getRecord(currentRecordIndex));
        releveTable.selectRecord(currentRecordIndex - 1);
    }

    /**
     * un releve est selectionné dans la table => met à jour la toolbar du panel
     * => met à jour l'éditeur
     *
     * @param recordIndex
     * @param isNew
     */
    @Override
    public void tableRecordSelected(int recordIndex, boolean isNew) {
        currentRecordIndex = recordIndex;
        updateToolBar();
        // j'affiche le relevé dans l'éditeur
        releveEditor.selectRecord(recordIndex);
    }

    @Override
    public boolean verifyCurrentRecord() {
        return releveEditor.verifyCurrentRecord(currentRecordIndex);
    }

}
