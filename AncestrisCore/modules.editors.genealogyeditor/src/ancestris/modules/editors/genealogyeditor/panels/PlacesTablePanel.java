package ancestris.modules.editors.genealogyeditor.panels;

import ancestris.modules.editors.genealogyeditor.models.GedcomPlaceTableModel;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.RowFilter;
import javax.swing.event.MouseInputListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 *
 * @author dominique
 */
public class PlacesTablePanel extends javax.swing.JPanel {

    String[] mPlaceFormat = null;
    private final GedcomPlaceTableModel mPlacesListTableModel;
    private final TableRowSorter<TableModel> mPlaceTableSorter;
    private final Preferences modulePreferences = NbPreferences.forModule(PlacesTablePanel.class);
    private Gedcom mGedcom = null;

    /**
     * Creates new form PlacesTablePanel
     */
    public PlacesTablePanel(final Gedcom gedcom) {
        this.mGedcom = gedcom;
        mPlaceFormat = PropertyPlace.getFormat(gedcom);

        mPlacesListTableModel = new GedcomPlaceTableModel(mPlaceFormat);
        initComponents();

        this.mGedcom = gedcom;
        mPlacesListTableModel.update(gedcom.getReferenceSet("PLAC"));
        placesListTable.setID(PlacesTablePanel.class.getName());

        mPlaceTableSorter = new TableRowSorter<TableModel>(placesListTable.getModel());
        placesListTable.setRowSorter(mPlaceTableSorter);

//        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
//        if (mPlaceTableSorter.getModelRowCount() > 0) {
//            sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
//        }
//        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
//        mPlaceTableSorter.setSortKeys(sortKeys);
//        mPlaceTableSorter.sort();
        
        try {
            if (!modulePreferences.nodeExists(gedcom.getName())) {
                searchPlaceComboBox.setSelectedIndex(0);
            } else {
                Preferences node = modulePreferences.node(gedcom.getName());
                searchPlaceComboBox.setSelectedIndex(node.getInt("PlacesTablePanel.searchPlaceComboBox.selectedIndex", 0));
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
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

        searchPlaceToolBar = new javax.swing.JToolBar();
        jLabel1 = new javax.swing.JLabel();
        searchPlaceLabel = new javax.swing.JLabel();
        searchPlaceComboBox = new javax.swing.JComboBox<String>();
        filterGedcomPlaceTextField = new javax.swing.JTextField();
        filterGedcomPlaceButton = new javax.swing.JButton();
        clearFilterGedcomPlaceButton = new javax.swing.JButton();
        placesListTableScrollPane = new javax.swing.JScrollPane();
        placesListTable = new ancestris.modules.editors.genealogyeditor.table.EditorTable();

        searchPlaceToolBar.setFloatable(false);
        searchPlaceToolBar.setRollover(true);

        jLabel1.setText(org.openide.util.NbBundle.getMessage(PlacesTablePanel.class, "PlacesTablePanel.jLabel1.text")); // NOI18N
        searchPlaceToolBar.add(jLabel1);

        searchPlaceLabel.setText(org.openide.util.NbBundle.getMessage(PlacesTablePanel.class, "PlacesTablePanel.searchPlaceLabel.text")); // NOI18N
        searchPlaceToolBar.add(searchPlaceLabel);

        searchPlaceComboBox.setModel(new DefaultComboBoxModel<String>(mPlaceFormat));
        searchPlaceComboBox.setMinimumSize(new java.awt.Dimension(20, 28));
        searchPlaceComboBox.setPreferredSize(new java.awt.Dimension(120, 28));
        searchPlaceToolBar.add(searchPlaceComboBox);

        filterGedcomPlaceTextField.setText(org.openide.util.NbBundle.getMessage(PlacesTablePanel.class, "PlacesTablePanel.filterGedcomPlaceTextField.text")); // NOI18N
        filterGedcomPlaceTextField.setMinimumSize(new java.awt.Dimension(80, 28));
        filterGedcomPlaceTextField.setPreferredSize(new java.awt.Dimension(220, 28));
        filterGedcomPlaceTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterGedcomPlaceButtonActionPerformed(evt);
            }
        });
        filterGedcomPlaceTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                filterGedcomPlaceTextFieldKeyTyped(evt);
            }
        });
        searchPlaceToolBar.add(filterGedcomPlaceTextField);

        filterGedcomPlaceButton.setText(org.openide.util.NbBundle.getMessage(PlacesTablePanel.class, "PlacesTablePanel.filterGedcomPlaceButton.text")); // NOI18N
        filterGedcomPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterGedcomPlaceButtonActionPerformed(evt);
            }
        });
        searchPlaceToolBar.add(filterGedcomPlaceButton);

        clearFilterGedcomPlaceButton.setText(org.openide.util.NbBundle.getMessage(PlacesTablePanel.class, "PlacesTablePanel.clearFilterGedcomPlaceButton.text")); // NOI18N
        clearFilterGedcomPlaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearFilterGedcomPlaceButtonActionPerformed(evt);
            }
        });
        searchPlaceToolBar.add(clearFilterGedcomPlaceButton);

        placesListTable.setModel(mPlacesListTableModel);
        placesListTableScrollPane.setViewportView(placesListTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(placesListTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 633, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(searchPlaceToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(searchPlaceToolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(placesListTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void filterGedcomPlaceTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterGedcomPlaceTextFieldKeyTyped
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            newFilter(filterGedcomPlaceTextField.getText());
        }
    }//GEN-LAST:event_filterGedcomPlaceTextFieldKeyTyped

    private void filterGedcomPlaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterGedcomPlaceButtonActionPerformed
        newFilter(filterGedcomPlaceTextField.getText());
    }//GEN-LAST:event_filterGedcomPlaceButtonActionPerformed

    private void clearFilterGedcomPlaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFilterGedcomPlaceButtonActionPerformed
        filterGedcomPlaceTextField.setText("");
        newFilter(filterGedcomPlaceTextField.getText());
    }//GEN-LAST:event_clearFilterGedcomPlaceButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearFilterGedcomPlaceButton;
    private javax.swing.JButton filterGedcomPlaceButton;
    private javax.swing.JTextField filterGedcomPlaceTextField;
    private javax.swing.JLabel jLabel1;
    private ancestris.modules.editors.genealogyeditor.table.EditorTable placesListTable;
    private javax.swing.JScrollPane placesListTableScrollPane;
    private javax.swing.JComboBox<String> searchPlaceComboBox;
    private javax.swing.JLabel searchPlaceLabel;
    private javax.swing.JToolBar searchPlaceToolBar;
    // End of variables declaration//GEN-END:variables

    private void newFilter(String filter) {
        RowFilter<TableModel, Integer> rf;
        //If current expression doesn't parse, don't update.
        try {
            rf = RowFilter.regexFilter("(?i)" + filter, searchPlaceComboBox.getSelectedIndex());
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }

        mPlaceTableSorter.setRowFilter(rf);
    }

    public PropertyPlace getSelectedPlace() {
        Preferences node = modulePreferences.node(mGedcom.getName());
        node.putInt("PlacesTablePanel.searchPlaceComboBox.selectedIndex", searchPlaceComboBox.getSelectedIndex());

        int selectedRow = placesListTable.getSelectedRow();
        if (selectedRow != -1) {
            int rowIndex = placesListTable.convertRowIndexToModel(selectedRow);
            Set<Property> valueAt = mPlacesListTableModel.getValueAt(rowIndex);
            return (PropertyPlace) mPlacesListTableModel.getValueAt(rowIndex).toArray()[0];
        } else {
            return null;
        }
    }

    public void setFilter(String strFilter) {
        filterGedcomPlaceTextField.setText(strFilter);
    }
}
