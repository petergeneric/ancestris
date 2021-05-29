/*
 * ResultDialog.java
 *
 */
package ancestris.modules.releve.table;

import ancestris.modules.releve.MenuCommandProvider;
import ancestris.modules.releve.editor.EditorBeanField;
import ancestris.modules.releve.editor.EditorBeanGroup;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.FieldDate;
import ancestris.modules.releve.model.FieldSex;
import ancestris.modules.releve.model.FieldSimpleValue;
import ancestris.modules.releve.model.Record.RecordType;
import ancestris.modules.releve.table.ErrorBuffer.CheckError;
import genj.util.WordBuffer;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.Iterator;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.RowSorterEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.util.NbPreferences;
import org.oxbow.swingbits.table.filter.TableRowFilterSupport;

/**
 *
 * @author Michel
 */
public class ResultDialog extends javax.swing.JFrame {

    static ResultDialog dialog = null;
    private MenuCommandProvider menuCommandeProvider;
    private AbstractTableModel outputFileBuffer;
    private ErrorBuffer errorBuffer;
    javax.swing.JScrollPane jScrollPane1;
    JTableRowHeader jTableRowHeader;
    JTableResult jTableResult;
    OutputTableModel outputModel = new OutputTableModel();
    RowHeaderModel rowHeaderModel = new RowHeaderModel();
    private int[] columnWidth;

    /**
     * affiche la fenetre
     *
     * @param parent
     * @param dataManager
     */
    public static ResultDialog show(java.awt.Frame parent, MenuCommandProvider menuCommandeProvider, AbstractTableModel fileBuffer, ErrorBuffer errorBuffer, File outputFile) {
        if (dialog == null) {
            dialog = new ResultDialog(parent);
            // centre la fenetre sur l'ecran
            dialog.setLocationRelativeTo(null);

            // je configure la taille de la fenetre
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            String size = NbPreferences.forModule(ResultDialog.class).get("ResultDialogBounds", "600,400,0,0");
            String[] dimensions = size.split(",");
            if (dimensions.length >= 4) {
                int width = Integer.parseInt(dimensions[0]);
                int height = Integer.parseInt(dimensions[1]);
                int x = Integer.parseInt(dimensions[2]);
                int y = Integer.parseInt(dimensions[3]);
                if (width < 100) {
                    width = 100;
                }
                if (height < 100) {
                    height = 100;
                }
                if (x < 10 || x > screen.width - 10) {
                    x = (screen.width / 2) - (width / 2);
                }
                if (y < 10 || y > screen.height - 10) {
                    y = (screen.height / 2) - (height / 2);
                }
                dialog.setBounds(x, y, width, height);
            } else {
                dialog.setBounds(screen.width / 2 - 100, screen.height / 2 - 100, 300, 450);
            }

            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {

                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    ResultDialog.closeComponent();
                }
            });
        } else {
            dialog.toFront();
        }

        dialog.initData(menuCommandeProvider, fileBuffer, errorBuffer, outputFile);
        dialog.setVisible(true);
        return dialog;
    }

    public ResultDialog(java.awt.Frame parent) {
        //super(parent, false);
        initComponents();
        ImageIcon icon = new ImageIcon(ResultDialog.class.getResource("/ancestris/modules/releve/images/check16.png"));
        setIconImage(icon.getImage());
    }

    static protected void closeComponent() {
        if (dialog.getExtendedState() != JFrame.NORMAL) {
            dialog.setExtendedState(JFrame.NORMAL);
        }
        // j'enregistre la taille dans les preferences
        java.awt.Rectangle bounds = dialog.getBounds();
        String size;
        size = String.valueOf((int) bounds.getWidth()) + ","
                + String.valueOf((int) bounds.getHeight()) + ","
                + String.valueOf(bounds.getLocation().x + ","
                        + String.valueOf(bounds.getLocation().y));

        NbPreferences.forModule(ResultDialog.class).put("ResultDialogBounds", size);

        // j'enregistre la largeur de colonnes
        dialog.saveColumnLayout();

        dialog.setVisible(false);
        dialog.dispose();

    }

    private void initData(MenuCommandProvider menuCommandeProvider, AbstractTableModel fileBuffer, final ErrorBuffer errorBuffer, File outputFile) {
        this.menuCommandeProvider = menuCommandeProvider;
        this.outputFileBuffer = fileBuffer;
        this.errorBuffer = errorBuffer;

        if (outputFile != null) {
            setTitle(outputFile.getName());
        }

        if (jScrollPane1 != null) {
            jPanelResult.remove(jScrollPane1);
        }

        columnWidth = new int[outputModel.getColumnCount()];

        RowFilter<OutputTableModel, Integer> checkFilter = new RowFilter<OutputTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends OutputTableModel, ? extends Integer> entry) {
                if (jCheckBoxMenuMaskValidRow.getState()) {
                    return errorBuffer.hasError(entry.getIdentifier());
                } else {
                    return true;
                }
            }
        };

        TableRowSorter<OutputTableModel> checkSorter = new TableRowSorter<OutputTableModel>(outputModel);
        checkSorter.setRowFilter(checkFilter);

        ///////////////////////////////////////////////////////////////////////
        // addField table Result
        ///////////////////////////////////////////////////////////////////////
        jTableResult = new JTableResult();
        jTableResult.setColumnModel(new GroupableTableColumnModel());
        jTableResult.setTableHeader(new GroupableTableHeader((GroupableTableColumnModel) jTableResult.getColumnModel()));
        jTableResult.setModel(outputModel);
        // j'active les filtres par colonne
        TableRowFilterSupport tableRowFilterSupport = TableRowFilterSupport.forTable(jTableResult);
        tableRowFilterSupport.apply();
        // j'active le filtre de recherche
        jTableResult.setRowSorter(checkSorter);
        // je configure la largeur des colonnes
        loadColumnLayout();

        // je configure l'entete des colonnes
        GroupableTableColumnModel cm = (GroupableTableColumnModel) jTableResult.getColumnModel();

        int col = 1;
        for (EditorBeanGroup beanGroup : EditorBeanGroup.getGroups(RecordType.MISC)) {
            ColumnGroup columnGroup = new ColumnGroup(beanGroup.getTitle());
            for (EditorBeanField beanField : beanGroup.getFields()) {
                columnGroup.add(cm.getColumn(col));
                col++;
            }
            cm.addColumnGroup(columnGroup);
        }

//        for(int col=0; col < outputModel.getColumnCount(); col++) {
//            //sorter.setSortable(col,true);
//            sorter.setComparator(col, fieldComparator);
//        }
//        Comparator fieldComparator = new Comparator<Field>() {
//            @Override
//            public int compare(Field o1, Field o2) {
//                return o1.compareTo(o2);
//            }
//        };
        ///////////////////////////////////////////////////////////////////////
        // addField table RowHeader
        ///////////////////////////////////////////////////////////////////////
        jTableRowHeader = new JTableRowHeader(rowHeaderModel);

        // j'applique le filtre sur la table
        jTableResult.setRowSorter(checkSorter);
        jTableRowHeader.setRowSorter(jTableResult.getRowSorter());

        jScrollPane1 = new javax.swing.JScrollPane(jTableResult);
        JViewport viewport = new JViewport();
        viewport.setView(jTableRowHeader);
        viewport.setPreferredSize(jTableRowHeader.getPreferredSize());
        jScrollPane1.setRowHeaderView(viewport);
        jScrollPane1.setCorner(JScrollPane.UPPER_LEFT_CORNER, jTableRowHeader.getTableHeader());

        jPanelResult.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        if (jCheckBoxMenuMaskEmptyColumn.getState()) {
            // je masque les colonnes vides
            hideEmptyColumns(false);
            hideEmptyColumns(true);
        }

        // j'affiche les erreurs
        jLabelError.setText(" " + errorBuffer.getErrorCount() + " erreur(s)");
        jListError.setListData(errorBuffer.getErrors());
    }

    /**
     * synchronise la selection entre la table d'entete des lignes et la table
     * princpale
     *
     * @param isHeaderTable
     */
    private void synchronizeSelection(boolean isHeaderTable) {
        int fixedSelectedIndex = jTableRowHeader.getSelectedRow();
        int selectedIndex = jTableResult.getSelectedRow();
        if (fixedSelectedIndex != selectedIndex) {
            if (isHeaderTable) {
                if (fixedSelectedIndex != -1) {
                    jTableResult.setRowSelectionInterval(fixedSelectedIndex, fixedSelectedIndex);
                } else {
                    jTableResult.clearSelection();
                }
            } else {
                if (selectedIndex != -1) {
                    jTableRowHeader.setRowSelectionInterval(selectedIndex, selectedIndex);
                } else {
                    jTableRowHeader.clearSelection();
                }
            }
        }
    }

    /**
     * Set column layout from bundle configuration
     */
    public void loadColumnLayout() {

        if (jTableResult.getModel() != null) {
            // je recupere la largeur des colonnes de la session precedente
            String columnLayout = NbPreferences.forModule(ResultDialog.class).get(
                    "ResultDialogColumnLayout", "0");
            StringTokenizer tokens = new StringTokenizer(columnLayout, ",");
            int n = Integer.parseInt(tokens.nextToken());
            TableColumnModel columns = jTableResult.getColumnModel();
            for (int i = 0; i < n && i < columns.getColumnCount(); i++) {
                TableColumn col = columns.getColumn(i);
                int w = Integer.parseInt(tokens.nextToken());

                if (i == 0) {
                    // la premiere colonne est de largeur nulle
                    // et sa largeur ne peut pas etre changée.
                    col.setMaxWidth(0);
                    col.setMinWidth(0);
                    col.setWidth(0);
                    col.setPreferredWidth(0);
                    col.setResizable(false);
                } else {
                    col.setWidth(w);
                    col.setPreferredWidth(w);
                    col.setResizable(true);
                }

            }
        }
    }

    /**
     * Return column layout - a string that can be used to return column widths
     * and sorting
     */
    public void saveColumnLayout() {

        if (jTableResult.getModel() != null) {
            TableColumnModel columns = jTableResult.getColumnModel();
            //List<Directive> directives = outputModel.getDirectives();

            WordBuffer columnLayout = new WordBuffer(",");
            columnLayout.append(columns.getColumnCount());

            for (int c = 0; c < columns.getColumnCount(); c++) {
                if (columnWidth[c] != 0) {
                    columnLayout.append(columnWidth[c]);
                } else {
                    columnLayout.append(columns.getColumn(c).getWidth());
                }
            }
            NbPreferences.forModule(ResultDialog.class).put(
                    "ResultDialogColumnLayout",
                    columnLayout.toString());
        }

    }

    void hideEmptyColumns(boolean hide) {
        if (hide) {
            // je verifie si chaque colonne est vide (sauf la colonne n°0)
            for (int col = 1; col < jTableResult.getColumnCount(); col++) {
                boolean emptyCol = true;
                for (int row = 0; row < outputModel.getRowCount(); row++) {
                    if (!(outputModel.getValueAt(row, col).toString()).isEmpty()) {
                        emptyCol = false;
                        break;
                    }
                }
                if (emptyCol) {
                    TableColumn column = jTableResult.getColumnModel().getColumn(col);
                    columnWidth[col] = column.getPreferredWidth();
                    column.setMinWidth(0);
                    column.setPreferredWidth(0);
                }
            }
        } else {
            // je restaure la largeur des colonnes sauf la colonne n°0)
            for (int col = 1; col < jTableResult.getColumnCount(); col++) {
                if (columnWidth[col] != 0) {
                    TableColumn column = jTableResult.getColumnModel().getColumn(col);
                    column.setPreferredWidth(columnWidth[col]);
                    column.setMinWidth(15);
                    columnWidth[col] = 0;
                }
            }
        }
    }

    void findString(AbstractTableModel fileBuffer, String findValue, ErrorBuffer errorBuffer) {
        errorBuffer.removeAll();
        GroupableTableColumnModel cm = (GroupableTableColumnModel) jTableResult.getColumnModel();

        for (int row = 0; row < fileBuffer.getRowCount(); row++) {
            for (int col = 0; col < fileBuffer.getColumnCount(); col++) {
                String svalue = fileBuffer.getValueAt(row, col).toString();
                if (svalue.contains(findValue)) {
                    Iterator<TableColumn> gourpIter = cm.getColumnGroups(cm.getColumn(col + 1));
                    String message = "";
                    while (gourpIter.hasNext()) {
                        ColumnGroup columnGroup = (ColumnGroup) gourpIter.next();
                        message += columnGroup.getHeaderValue().toString() + " ";
                    }
                    message += fileBuffer.getColumnName(col);
                    errorBuffer.addError(row, col, message);
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanelResult = new javax.swing.JPanel();
        jPanelMenu = new javax.swing.JPanel();
        jButtonStandalone = new javax.swing.JButton();
        jButtonFind = new javax.swing.JButton();
        jTextFieldFind = new javax.swing.JTextField();
        jButtonClear = new javax.swing.JButton();
        jPanelError = new javax.swing.JPanel();
        jLabelError = new javax.swing.JLabel();
        jScrollPaneError = new javax.swing.JScrollPane();
        jListError = new javax.swing.JList<>();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuDisplay = new javax.swing.JMenu();
        jCheckBoxMenuMaskEmptyColumn = new javax.swing.JCheckBoxMenuItem();
        jCheckBoxMenuMaskValidRow = new javax.swing.JCheckBoxMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanelResult.setLayout(new java.awt.BorderLayout());

        jPanelMenu.setPreferredSize(new java.awt.Dimension(400, 28));
        jPanelMenu.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        jButtonStandalone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/releve/images/editor.png"))); // NOI18N
        jButtonStandalone.setToolTipText(org.openide.util.NbBundle.getMessage(ResultDialog.class, "ResultDialog.jButtonStandalone.toolTipText")); // NOI18N
        jButtonStandalone.setMargin(new java.awt.Insets(0, 4, 0, 4));
        jButtonStandalone.setMaximumSize(new java.awt.Dimension(29, 18));
        jButtonStandalone.setPreferredSize(new java.awt.Dimension(50, 28));
        jButtonStandalone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonStandaloneActionPerformed(evt);
            }
        });
        jPanelMenu.add(jButtonStandalone);

        jButtonFind.setText(org.openide.util.NbBundle.getMessage(ResultDialog.class, "ResultDialog.jButtonFind.text")); // NOI18N
        jButtonFind.setPreferredSize(null);
        jButtonFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFindActionPerformed(evt);
            }
        });
        jPanelMenu.add(jButtonFind);

        jTextFieldFind.setPreferredSize(new java.awt.Dimension(100, 28));
        jPanelMenu.add(jTextFieldFind);

        jButtonClear.setText(org.openide.util.NbBundle.getMessage(ResultDialog.class, "ResultDialog.jButtonClear.text")); // NOI18N
        jButtonClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonClearActionPerformed(evt);
            }
        });
        jPanelMenu.add(jButtonClear);

        jPanelResult.add(jPanelMenu, java.awt.BorderLayout.NORTH);

        jPanelError.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanelError.setLayout(new java.awt.BorderLayout());
        jPanelError.add(jLabelError, java.awt.BorderLayout.NORTH);

        jListError.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListError.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListErrorValueChanged(evt);
            }
        });
        jScrollPaneError.setViewportView(jListError);

        jPanelError.add(jScrollPaneError, java.awt.BorderLayout.CENTER);

        jPanelResult.add(jPanelError, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanelResult, java.awt.BorderLayout.CENTER);

        jMenu1.setText(org.openide.util.NbBundle.getMessage(ResultDialog.class, "ResultDialog.jMenu1.text")); // NOI18N
        jMenuBar1.add(jMenu1);

        jMenuDisplay.setText(org.openide.util.NbBundle.getMessage(ResultDialog.class, "ResultDialog.jMenuDisplay.text")); // NOI18N

        jCheckBoxMenuMaskEmptyColumn.setText(org.openide.util.NbBundle.getMessage(ResultDialog.class, "ResultDialog.jCheckBoxMenuMaskEmptyColumn.text")); // NOI18N
        jCheckBoxMenuMaskEmptyColumn.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxMenuMaskEmptyColumnItemStateChanged(evt);
            }
        });
        jMenuDisplay.add(jCheckBoxMenuMaskEmptyColumn);

        jCheckBoxMenuMaskValidRow.setText(org.openide.util.NbBundle.getMessage(ResultDialog.class, "ResultDialog.jCheckBoxMenuMaskValidRow.text")); // NOI18N
        jCheckBoxMenuMaskValidRow.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jCheckBoxMenuMaskValidRowItemStateChanged(evt);
            }
        });
        jMenuDisplay.add(jCheckBoxMenuMaskValidRow);

        jMenuBar1.add(jMenuDisplay);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jListErrorValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListErrorValueChanged
        // je selectionne la ligne concernée par l'erreur dans la table
        int errorIndex = evt.getFirstIndex();
        if (errorIndex != -1 && jTableResult.getSelectionModel() != null) {
            ErrorBuffer.CheckError error = jListError.getSelectedValue();
            if (error != null) {
                int rowtableIndex = jTableResult.convertRowIndexToView(error.row);
                jTableResult.getSelectionModel().setSelectionInterval(rowtableIndex, rowtableIndex);
                jTableResult.scrollRectToVisible(jTableResult.getCellRect(rowtableIndex, 0, false));
            }
        }
    }//GEN-LAST:event_jListErrorValueChanged

    /**
     * masque/démasque les colonnes vides
     *
     * @param evt
     */
    private void jCheckBoxMenuMaskEmptyColumnItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxMenuMaskEmptyColumnItemStateChanged
        hideEmptyColumns(jCheckBoxMenuMaskEmptyColumn.getState());
    }//GEN-LAST:event_jCheckBoxMenuMaskEmptyColumnItemStateChanged

    /**
     * masque/démasque les lignes valides
     *
     * @param evt
     */
    private void jCheckBoxMenuMaskValidRowItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jCheckBoxMenuMaskValidRowItemStateChanged
        outputModel.fireTableDataChanged();
        rowHeaderModel.fireTableDataChanged();

    }//GEN-LAST:event_jCheckBoxMenuMaskValidRowItemStateChanged

    private void jButtonStandaloneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonStandaloneActionPerformed

        int row = jTableResult.getSelectedRow();
        if (row != -1) {
            int panelIndex = 4;
            menuCommandeProvider.showStandalone(panelIndex, jTableResult.convertRowIndexToModel(row));
        }

}//GEN-LAST:event_jButtonStandaloneActionPerformed

    private void jButtonFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFindActionPerformed
        findString(outputFileBuffer, jTextFieldFind.getText(), errorBuffer);
        if (outputModel.getRowCount() > 0) {
            outputModel.fireTableDataChanged();
            //outputModel.fireTableRowsUpdated(0, outputModel.getRowCount()-1);
            rowHeaderModel.fireTableDataChanged();
        }
        // j'affiche les erreurs
        jLabelError.setText(" " + errorBuffer.getErrorCount() + " resultat(s)");
        jListError.setListData(errorBuffer.getErrors());
    }//GEN-LAST:event_jButtonFindActionPerformed

    private void jButtonClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonClearActionPerformed
        // je purge le buffer de recherche
        errorBuffer.removeAll();
        outputModel.fireTableDataChanged();
        rowHeaderModel.fireTableDataChanged();
        jLabelError.setText(" " + errorBuffer.getErrorCount() + " resultat(s)");
        jListError.setListData(errorBuffer.getErrors());
    }//GEN-LAST:event_jButtonClearActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonClear;
    private javax.swing.JButton jButtonFind;
    private javax.swing.JButton jButtonStandalone;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuMaskEmptyColumn;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuMaskValidRow;
    private javax.swing.JLabel jLabelError;
    private javax.swing.JList<CheckError> jListError;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuDisplay;
    private javax.swing.JPanel jPanelError;
    private javax.swing.JPanel jPanelMenu;
    private javax.swing.JPanel jPanelResult;
    private javax.swing.JScrollPane jScrollPaneError;
    private javax.swing.JTextField jTextFieldFind;
    // End of variables declaration//GEN-END:variables

    /**
     * ************************************************************************
     * Table des noms des resultats
     * ***********************************************************************
     */
    public class JTableResult extends JTable {

        //JTableResult(TableModel model) {
        JTableResult() {
            //super(model);

            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            getTableHeader().setReorderingAllowed(false);
            setAutoCreateRowSorter(false);

            setDefaultRenderer(Field.class, new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focs, int row, int col) {
                    setHorizontalAlignment(SwingConstants.LEFT);
                    if (value != null) {
                        if (value instanceof FieldDate) {
                            setText(((FieldDate) value).getDisplayValue());
                            setHorizontalAlignment(SwingConstants.RIGHT);
                        } else if (value instanceof FieldSex) {
                            setText(((FieldSex) value).toString());
                            setHorizontalAlignment(SwingConstants.CENTER);
                        } else if (value instanceof FieldSimpleValue) {
                            setText(((FieldSimpleValue) value).toString());
                            setHorizontalAlignment(SwingConstants.LEFT);
                        } else {
                            setText(value.toString());
                            setHorizontalAlignment(SwingConstants.LEFT);
                        }
                    } else {
                        setText("");
                        setHorizontalAlignment(SwingConstants.LEFT);
                    }

                    ErrorBuffer.CheckError[] errors = errorBuffer.getError(table.convertRowIndexToModel(row), table.convertColumnIndexToModel(col - 1));
                    String toolTipText;
                    if (errors.length > 0) {
                        toolTipText = "<html>";
                        for (int i = 0; i < errors.length; i++) {
                            if (i >= 1) {
                                toolTipText += "<br>";
                            }
                            toolTipText += errors[i].message;
                        }
                        toolTipText += "</html>";

                    } else {
                        toolTipText = null;
                    }
                    setToolTipText(toolTipText);

                    // je chois la couleur de fond
                    if (selected) {
                        if (errors.length > 0) {
                            // noir sur fond magenta
                            setForeground(table.getSelectionForeground());
                            setBackground(Color.MAGENTA);
                        } else {
                            // blanc sur fond bleu
                            setForeground(table.getSelectionForeground());
                            setBackground(table.getSelectionBackground());
                        }
                        setOpaque(true);
                    } else {
                        if (errors.length > 0) {
                            // noir sur fond rose
                            setForeground(table.getForeground());
                            setBackground(Color.PINK);
                        } else {
                            // noir sur fond blanc
                            setForeground(table.getForeground());
                            setBackground(table.getBackground());
                        }
                        setOpaque(true);
                    }
                    return this;
                }
            });
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            super.valueChanged(e);
            synchronizeSelection(false);
        }

        /**
         *
         * @param e
         */
        @Override
        @SuppressWarnings("unchecked")
        public void sorterChanged(RowSorterEvent e) {
            super.sorterChanged(e);
            // je met a jour le Sorter de la table jTableRowHeader
            // chaque fois que le Sorter de la table jTableResult change
            Object m = e.getSource().getModel();
            if (m instanceof TableModel) {
                RowSorter<TableModel> rs = (RowSorter<TableModel>) e.getSource();
                jTableRowHeader.setRowSorter(rs);
            }
        }

        /**
         * Propage la selection d'un ligne vers les listeners
         *
         * @param rowIndex
         * @param columnIndex
         * @param toggle
         * @param extend
         */
        @Override
        public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {
            // j'execute le traitement par defaut
            super.changeSelection(rowIndex, columnIndex, toggle, extend);
            // j'efface la selection dans la liste des erreurs si la ligne
            // sélectionnée dans la table des resultats est différente de la ligne concernée par l'erreur
            ErrorBuffer.CheckError checkErrors = jListError.getSelectedValue();
            if (checkErrors != null && checkErrors.row != jTableResult.getSelectedRow()) {
                jListError.clearSelection();
            }
        }

    }

    /**
     * ************************************************************************
     * Table des noms des lignes
     * ***********************************************************************
     */
    public class JTableRowHeader extends JTable {

        JTableRowHeader(TableModel model) {
            super(model);
            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            getTableHeader().setReorderingAllowed(false);
            TableColumnModel columns = getColumnModel();
            columns.getColumn(0).setResizable(false);
            columns.getColumn(0).setPreferredWidth(36);

            setDefaultRenderer(String.class, new DefaultTableCellRenderer() {

                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focs, int row, int col) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    if (value != null) {
                        setText(value.toString());
                    } else {
                        setText("");
                    }
                    boolean hasError = errorBuffer.hasError(table.convertRowIndexToModel(row));
                    // je chois la couleur de fond
                    if (selected) {
                        setForeground(table.getSelectionForeground());
                        if (hasError) {
                            // fond mauve
                            setBackground(Color.MAGENTA);
                        } else {
                            // fond bleu
                            setBackground(table.getSelectionBackground());
                        }
                        setOpaque(true);
                    } else {
                        setForeground(table.getForeground());
                        if (hasError) {
                            // fond rouge
                            setBackground(Color.PINK);
                        } else {
                            // fond normal
                            setBackground(table.getParent().getBackground());
                        }
                        setOpaque(true);
                    }
                    return this;
                }
            });

        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            super.valueChanged(e);
            synchronizeSelection(true);
        }

    }

    ////////////////////////////////////////////////////////////////////////////
    // OutputTableModel
    ///////////////////////////////////////////////////////////////////////////
    /**
     * modele de la table jTableResult ce model ne contient qu'uen colonne qui
     * sert a afficher le numéro de ligne
     */
    private class OutputTableModel extends AbstractTableModel {

        @Override
        public int getColumnCount() {
            return outputFileBuffer.getColumnCount() + 1;
        }

        @Override
        public int getRowCount() {
            return outputFileBuffer.getRowCount();
        }

        @Override
        public String getColumnName(int col) {
            //return Transposition.getLetter(col)+"\n"+transposition.getOutputFieldName(col);
            if (col == 0) {
                return "";
            } else {
                return outputFileBuffer.getColumnName(col - 1);
            }
        }

        @Override
        public Class<?> getColumnClass(int col) {
            if (col == 0) {
                return Integer.class;
            } else {
                return outputFileBuffer.getColumnClass(col - 1);
            }
        }

        @Override
        public Object getValueAt(int row, int col) {
            //return outputFileBuffer.getValue(row, col);
            //return outputFileBuffer.getValue(jTableResult.convertRowIndexToModel(row), col);
            if (col == 0) {
                return row + 1;
            } else {
                return outputFileBuffer.getValueAt(row, col - 1);
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col != 0;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (col != 0 && value != null) {
                outputFileBuffer.setValueAt(value, row + 1, col - 1);
                //outputFileBuffer.setValue(jTableResult.convertRowIndexToModel(row), col, value.toString());
            }
        }
    }

    /**
     * modele de la table RowHeader ce modele ne contient qu'une colonne qui
     * sert a afficher le numéro de ligne
     */
    private class RowHeaderModel extends AbstractTableModel {

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public int getRowCount() {
            return outputFileBuffer.getRowCount();
        }

        @Override
        public String getColumnName(int col) {
            return "Ligne";
        }

        @Override
        public Class<?> getColumnClass(int col) {
            return String.class;
        }

        @Override
        public Object getValueAt(int row, int col) {
            // j'affiche le numero de la ligne en partant de 1 pour la premiere
            // (dans le model la premiere ligne porte le numero 0) 
            return row + 1;
        }

    };

}
