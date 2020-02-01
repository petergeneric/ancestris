/*
 * ReleveCompletionDialog.java
 *
 * Created on 9 déc. 2012, 11:57:25
 */

package ancestris.modules.releve;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.releve.model.CompletionProvider;
import ancestris.modules.releve.model.CompletionProvider.CompletionType;
import ancestris.modules.releve.model.DataManager;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 * permet de decocher les noms ou prenom que l'on ne souhaite pas voir
 * apparaitre dans les listes de completion
 * @author Michel
 */
public class ReleveCompletionDialog extends javax.swing.JFrame {

    CompletionModel model = new CompletionModel() ;
    CompletionType completionType;

    
    /**
     * affiche la fenetre de completion des prénoms
     */
    static public void  showFirstNameCompletionPanel() {
        ReleveCompletionDialog completionDialog = new ReleveCompletionDialog();
        completionDialog.initData(CompletionType.firstName );
        completionDialog.setVisible(true);
    }

    /**
     * affiche la fenetre de completion des noms
     */
    static public void  showLastNameCompletionPanel() {
        ReleveCompletionDialog completionDialog = new ReleveCompletionDialog();
        completionDialog.initData(CompletionType.lastName );
        completionDialog.setVisible(true);
    }

        /**
     * affiche la fenetre de completion des prénoms
     */
    static public void showOccupationCompletionPanel() {
        ReleveCompletionDialog completionDialog = new ReleveCompletionDialog();
        completionDialog.initData(CompletionType.occupation);
        completionDialog.setVisible(true);
    }



    public ReleveCompletionDialog() {
        initComponents();
        
        // je configure la position de la fenetre
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screen.width - getWidth())/ 2, (screen.height -getHeight()) / 2, getWidth(), getHeight());
    }

    public void initData(CompletionType completionType) {
        List<String> valueList;
        List<String> excludeList;
        this.completionType=completionType;

        DataManager dataManager = null;
        // je choisis le premier ReleveTopComponent
        for (ReleveTopComponent tc : AncestrisPlugin.lookupAll(ReleveTopComponent.class)) {
            dataManager = tc.getDataManager();
            break;
        }
        if(dataManager == null) {
            return;
        }

        String columnTitle;
        switch( completionType ) {
            case firstName:
               columnTitle = NbBundle.getMessage(ReleveCompletionDialog.class, "ReleveCompletionDialog.firstName");
               valueList = dataManager.getCompletionProvider().getFirstNames().getAll();
               excludeList = CompletionProvider.loadExcludeCompletion(completionType);
               break;
            case lastName:
               columnTitle = NbBundle.getMessage(ReleveCompletionDialog.class, "ReleveCompletionDialog.lastName");
               valueList = dataManager.getCompletionProvider().getLastNames().getAll();
               excludeList = CompletionProvider.loadExcludeCompletion(completionType);
               break;
            case occupation:
               columnTitle = NbBundle.getMessage(ReleveCompletionDialog.class, "ReleveCompletionDialog.occupation");
               valueList = dataManager.getCompletionProvider().getOccupations().getAll();
               excludeList = CompletionProvider.loadExcludeCompletion(completionType);
               break;
            default:
               columnTitle = "";
               valueList = new ArrayList<String>();
               excludeList = new ArrayList<String>();
        }

        setTitle(String.format(
                NbBundle.getMessage(ReleveCompletionDialog.class, "ReleveCompletionDialog.title"),
                columnTitle));       

        model.setColumnTitle(columnTitle);
        
        // je recupere la liste des valeurs existantes dans le relevé
        HashMap<String,Boolean> lastNameMap= new HashMap<String,Boolean>();
        for (Iterator<String> it = valueList.iterator(); it.hasNext(); ) {
            lastNameMap.put(it.next(), true);
        }
        // j'ajoute les valeurs deja exlues
        for (Iterator<String> it = excludeList.iterator(); it.hasNext(); ) {
            lastNameMap.put(it.next(), false);
        }
        
        // je copie les valeurs dans le modele de la JTable
        for (Entry<String,Boolean> entry : lastNameMap.entrySet()) {
            model.add(entry.getKey(), entry.getValue());
        }
        jTableExclude.setModel(model);
        // je trie la table
        jTableExclude.setAutoCreateRowSorter(true);
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
        jTableExclude.getRowSorter().setSortKeys(sortKeys);
        // je fixe la largeur de la permiere colonne contenant les cases à cocher
        //jTableExclude.getColumnModel().getColumn(0).setPreferredWidth (40);
        //jTableExclude.getColumnModel().getColumn(0).setWidth (40);
        jTableExclude.getColumnModel().getColumn(0).setMaxWidth(40);
    }


    /**
     * enregistre la liste des valeurs exlues
     */
    private void saveExcluded() {
        ArrayList<String> excludeList = new ArrayList<String>();
        int n = model.getRowCount();
        for(int i = 0; i <n ; i++) {
            if (((Boolean)model.getValueAt(i, 0)) == false) {
                excludeList.add(model.getValueAt(i,1).toString());
            }
        }
        
        // j'enregistre les valeurs dans les preferences
        CompletionProvider.saveExcludedCompletion(excludeList, completionType);

        // je notifie les instances de la mise a jour
        for (ReleveTopComponent tc : AncestrisPlugin.lookupAll(ReleveTopComponent.class)) {
            tc.getDataManager().getCompletionProvider().refreshExcludeCompletion(completionType);
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

        jPanelComment = new javax.swing.JPanel();
        comment = new javax.swing.JLabel();
        jPaneTable = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTableExclude = new javax.swing.JTable();
        jPanelButton = new javax.swing.JPanel();
        jButtonCopyToClipboard = new javax.swing.JButton();
        jButtonDelete = new javax.swing.JButton();
        jButtonOk = new javax.swing.JButton();
        jButtonCancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        comment.setText(org.openide.util.NbBundle.getMessage(ReleveCompletionDialog.class, "ReleveCompletionDialog.comment.text")); // NOI18N
        jPanelComment.add(comment);
        comment.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ReleveCompletionDialog.class, "ReleveCompletionDialog.comment.text")); // NOI18N

        getContentPane().add(jPanelComment, java.awt.BorderLayout.NORTH);

        jPaneTable.setPreferredSize(new java.awt.Dimension(300, 400));
        jPaneTable.setLayout(new java.awt.BorderLayout());

        jTableExclude.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        jTableExclude.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_LAST_COLUMN);
        jScrollPane1.setViewportView(jTableExclude);

        jPaneTable.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPaneTable, java.awt.BorderLayout.CENTER);

        jPanelButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jButtonCopyToClipboard.setText(org.openide.util.NbBundle.getMessage(ReleveCompletionDialog.class, "ReleveCompletionDialog.jButtonCopyToClipboard.text")); // NOI18N
        jButtonCopyToClipboard.setToolTipText(org.openide.util.NbBundle.getMessage(ReleveCompletionDialog.class, "ReleveCompletionDialog.jButtonCopyToClipboard.toolTipText")); // NOI18N
        jButtonCopyToClipboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCopyToClipboardActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonCopyToClipboard);

        jButtonDelete.setText(org.openide.util.NbBundle.getMessage(ReleveCompletionDialog.class, "ReleveCompletionDialog.jButtonDelete.text")); // NOI18N
        jButtonDelete.setToolTipText(org.openide.util.NbBundle.getMessage(ReleveCompletionDialog.class, "ReleveCompletionDialog.jButtonDelete.tooltip")); // NOI18N
        jButtonDelete.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonDelete);

        jButtonOk.setText(org.openide.util.NbBundle.getMessage(ReleveCompletionDialog.class, "ReleveCompletionDialog.jButtonOk.text")); // NOI18N
        jButtonOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOkActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonOk);

        jButtonCancel.setText(org.openide.util.NbBundle.getMessage(ReleveCompletionDialog.class, "ReleveCompletionDialog.jButtonCancel.text")); // NOI18N
        jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCancelActionPerformed(evt);
            }
        });
        jPanelButton.add(jButtonCancel);

        getContentPane().add(jPanelButton, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        //rien a faire        
    }//GEN-LAST:event_formWindowClosed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        int[] selectedRows = jTableExclude.getSelectedRows();

        for (int row : selectedRows) {
            model.remove(jTableExclude.convertRowIndexToModel(row));
        }
	model.fireTableDataChanged();
        jTableExclude.clearSelection();

    }//GEN-LAST:event_jButtonDeleteActionPerformed

    private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
        dispose();
    }//GEN-LAST:event_jButtonCancelActionPerformed

    private void jButtonOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOkActionPerformed
        saveExcluded();
        dispose();
    }//GEN-LAST:event_jButtonOkActionPerformed

    private void jButtonCopyToClipboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCopyToClipboardActionPerformed
        final String LINE_BREAK = "\n";
        Clipboard CLIPBOARD = Toolkit.getDefaultToolkit().getSystemClipboard();

        StringBuilder clipboardStr = new StringBuilder();

        // je copie les valeurs dans
        int n = model.getRowCount();
        for(int i = 0; i <n ; i++) {
            clipboardStr.append(model.getValueAt(i,1).toString()).append(LINE_BREAK);
        }
        StringSelection sel = new StringSelection(clipboardStr.toString());
        CLIPBOARD.setContents(sel, sel);
    }//GEN-LAST:event_jButtonCopyToClipboardActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel comment;
    private javax.swing.JButton jButtonCancel;
    private javax.swing.JButton jButtonCopyToClipboard;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonOk;
    private javax.swing.JPanel jPaneTable;
    private javax.swing.JPanel jPanelButton;
    private javax.swing.JPanel jPanelComment;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTableExclude;
    // End of variables declaration//GEN-END:variables


    private class CompletionModel extends AbstractTableModel {
        ArrayList<Boolean> includeList = new ArrayList<Boolean>();
        ArrayList<String> valueList = new ArrayList<String>();
        String itemColumnTitle;
        String includedColumnTitle = NbBundle.getMessage(ReleveCompletionDialog.class, "ReleveCompletionDialog.inludeColumnTitle");
        final Class<?> columnClass[]= {Boolean.class, String.class};
        
        public void setColumnTitle(String columnName) {
            this.itemColumnTitle = columnName;
        }

        public void add(String value, boolean include) {
            valueList.add(value);
            includeList.add(include);
        }
        
        public void remove(int row) {
            valueList.remove(row);
            includeList.remove(row);
        }

        
        @Override
        public int getColumnCount() {
            return columnClass.length;
        }

        @Override
        public int getRowCount() {
            return valueList.size();
        }

        @Override
        public String getColumnName(int col) {
            switch (col) {
                case 0:
                    return includedColumnTitle;
                case 1:
                    return itemColumnTitle;
                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return columnClass[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            switch (col) {
                case 0:
                    return includeList.get(row);
                case 1:
                    return valueList.get(row);
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            switch (col) {
                case 0:
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            switch (col) {
                case 0:
                    includeList.set(row,(Boolean)value);
                    break;
                default:
                    break;
            }
            fireTableCellUpdated(row, col);
        }


    }
}
