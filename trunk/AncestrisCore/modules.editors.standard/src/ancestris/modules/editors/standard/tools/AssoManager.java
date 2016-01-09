/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2015 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.standard.tools;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertySex;
import genj.util.ReferenceSet;
import genj.util.Registry;
import genj.util.swing.ImageIcon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class AssoManager extends javax.swing.JPanel implements TableModelListener{

    private Registry registry = null;
    private boolean hasChanged = false;
    private JButton okButton = null;
    private boolean isValid = true;
    private boolean isBusy = false;
    
    private Gedcom gedcom = null;
    private Indi indi = null;
    private List<EventWrapper> eventSet = null;
    
    // Association With indi and Of indi
    private List<AssoWrapper> assoWithSet = null;
    private AssoWithTableModel awtm = null;
    private int rowHeight = 18;
    
    private List<AssoWrapper> assoOfSet = null;
    
    /**
     * Creates new form AssoManager
     */
    public AssoManager(Indi indi, List<EventWrapper> eventSet, List<AssoWrapper> assoSet, AssoWrapper selectedAsso, JButton okButton, JButton cancelButton) {
        
        this.eventSet = eventSet;
        this.indi = indi;
        this.gedcom = indi.getGedcom();
        this.okButton = okButton;

        registry = Registry.get(getClass());
        initComponents();

        this.setPreferredSize(new Dimension(registry.get("assoWindowWidth", this.getPreferredSize().width), registry.get("assoWindowHeight", this.getPreferredSize().height)));
        assoSplitPanel.setDividerLocation(registry.get("assoSplitDividerLocation", assoSplitPanel.getDividerLocation()));

        // Get associations of both types
        assoWithSet = clone(assoSet);
        assoOfSet = new ArrayList<AssoWrapper>();
        getAssociationOf(indi);
        
        // Titles
        assoWithIndiTitle.setText(NbBundle.getMessage(getClass(), "AssoManager.assoWithIndiTitle.text", getIndi()));
        assoOfIndoTitle.setText(NbBundle.getMessage(getClass(), "AssoManager.assoOfIndoTitle.text", getIndi()));
        
        //
        // assoWithTable:
        //
        
        // Build list
        awtm = new AssoWithTableModel(assoWithSet);
        assoWithTable.setModel(awtm);    
        assoWithTable.setAutoCreateRowSorter(true);
        

        
        
        // Set event column as a combobox
        EventWrapper[] arrayEvents = eventSet.toArray(new EventWrapper[eventSet.size()]);
        Arrays.sort(arrayEvents, new Comparator() {
            public int compare(Object e1, Object e2) {
                String s1 = ((EventWrapper)e1).eventLabel.getLongLabel().toLowerCase();
                String s2 = ((EventWrapper)e2).eventLabel.getLongLabel().toLowerCase();
                return s1.compareTo(s2);
            }
        });
        JComboBox comboBoxEvents = new JComboBox(arrayEvents);
        comboBoxEvents.setRenderer(new ComboBoxEventsRenderer());
        comboBoxEvents.setMaximumRowCount(10);
        assoWithTable.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(comboBoxEvents));
        assoWithTable.getColumnModel().getColumn(0).setCellRenderer(new EventCellRenderer());

        // Set rela column as editable combobox
        ReferenceSet<String, Property> relaRefSet = gedcom.getReferenceSet("RELA");
        List<String> relaKeys = relaRefSet.getKeys();
        String[] arrayRelas = relaKeys.toArray(new String[relaKeys.size()]);
        Arrays.sort(arrayRelas);
        JComboBox comboBoxRelas = new JComboBox(arrayRelas);
        comboBoxRelas.setMaximumRowCount(10);
        comboBoxRelas.setEditable(true);
        assoWithTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBoxRelas));
        assoWithTable.getColumnModel().getColumn(1).setCellRenderer(new OtherCellRenderer());
        
        // Set indi column as combobox
        Entity[] arrayIndis = gedcom.getEntities("INDI", "INDI:NAME");
        JComboBox comboBoxIndis = new JComboBox(arrayIndis);
        comboBoxRelas.setMaximumRowCount(20);
        assoWithTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(comboBoxIndis));
        assoWithTable.getColumnModel().getColumn(2).setCellRenderer(new OtherCellRenderer());

        // Last name and FirstName
        assoWithTable.getColumnModel().getColumn(3).setCellRenderer(new OtherCellRenderer());
        assoWithTable.getColumnModel().getColumn(4).setCellRenderer(new OtherCellRenderer());
        
        // Set sex column as combobox
        ImageIcon[] arraySexs = new ImageIcon[] {
            PropertySex.getImage(PropertySex.MALE),
            PropertySex.getImage(PropertySex.FEMALE),
            PropertySex.getImage(PropertySex.UNKNOWN)
        };
        JComboBox comboBoxSexs = new JComboBox(arraySexs);
        comboBoxSexs.setRenderer(new ComboBoxSexsRenderer());
        comboBoxSexs.setMaximumRowCount(3);
        assoWithTable.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(comboBoxSexs));
        
        // Set occu column as editable combobox
        ReferenceSet<String, Property> occuRefSet = gedcom.getReferenceSet("OCCU");
        List<String> occuKeys = occuRefSet.getKeys();
        String[] arrayOccus = occuKeys.toArray(new String[occuKeys.size()]);
        Arrays.sort(arrayOccus);
        JComboBox comboBoxOccus = new JComboBox(arrayOccus);
        comboBoxOccus.setMaximumRowCount(10);
        comboBoxOccus.setEditable(true);
        assoWithTable.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(comboBoxOccus));

        // Resize columns
        FontMetrics fm = getFontMetrics(getFont());
        for (int i = 0; i < assoWithTable.getColumnCount(); i++) {
            assoWithTable.getColumnModel().getColumn(i).setPreferredWidth(awtm.getMaxWidth(fm, i));
        }
        
        // Rowheight to fit in comboboxes
        rowHeight = comboBoxOccus.getPreferredSize().height;
        assoWithTable.setRowHeight(rowHeight);

        // Resize table based on its number of lines
        resizeTable();
        
        // Select appropriate association
        int row = 0;
        for (AssoWrapper asso : assoSet) {   // do not replace with cloned set
            if (asso == selectedAsso) {
                assoWithTable.setRowSelectionInterval(row, row);
            }
            row++;
        }

        awtm.addTableModelListener(this);
        
        //
        // assoOfTable:
        //
        
        ////////////////////////// check displayEventTable in IndiPanel
    }

    private void resizeTable() {
        Dimension preferredSize = assoWithTable.getPreferredSize();
        preferredSize.height = rowHeight * awtm.getRowCount() + 1;
        assoWithTable.setPreferredSize(preferredSize);
        assoWithTable.revalidate();
        assoWithTable.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        assoSplitPanel = new javax.swing.JSplitPane();
        assoWithIndiPanel = new javax.swing.JPanel();
        assoWithIndiTitle = new javax.swing.JLabel();
        assoListScrollPane = new javax.swing.JScrollPane();
        assoWithTable = new javax.swing.JTable();
        addLineButton = new javax.swing.JButton();
        removeLineButton = new javax.swing.JButton();
        AssoOfIndiPanel = new javax.swing.JPanel();
        assoOfIndoTitle = new javax.swing.JLabel();
        relaListScrollPane = new javax.swing.JScrollPane();
        assoOfTable = new javax.swing.JTable();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        assoSplitPanel.setDividerLocation(275);
        assoSplitPanel.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        assoSplitPanel.setResizeWeight(0.5);
        assoSplitPanel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                assoSplitPanelPropertyChange(evt);
            }
        });

        assoWithIndiPanel.setPreferredSize(new java.awt.Dimension(468, 273));

        assoWithIndiTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(assoWithIndiTitle, org.openide.util.NbBundle.getMessage(AssoManager.class, "AssoManager.assoWithIndiTitle.text")); // NOI18N

        assoWithTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        assoWithTable.setPreferredSize(new java.awt.Dimension(300, 10));
        assoWithTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        assoListScrollPane.setViewportView(assoWithTable);

        addLineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(addLineButton, org.openide.util.NbBundle.getMessage(AssoManager.class, "AssoManager.addLineButton.text")); // NOI18N
        addLineButton.setToolTipText(org.openide.util.NbBundle.getMessage(AssoManager.class, "AssoManager.addLineButton.toolTipText")); // NOI18N
        addLineButton.setPreferredSize(new java.awt.Dimension(24, 24));
        addLineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLineButtonActionPerformed(evt);
            }
        });

        removeLineButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/editors/standard/images/remove.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(removeLineButton, org.openide.util.NbBundle.getMessage(AssoManager.class, "AssoManager.removeLineButton.text")); // NOI18N
        removeLineButton.setToolTipText(org.openide.util.NbBundle.getMessage(AssoManager.class, "AssoManager.removeLineButton.toolTipText")); // NOI18N
        removeLineButton.setPreferredSize(new java.awt.Dimension(24, 24));
        removeLineButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeLineButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout assoWithIndiPanelLayout = new javax.swing.GroupLayout(assoWithIndiPanel);
        assoWithIndiPanel.setLayout(assoWithIndiPanelLayout);
        assoWithIndiPanelLayout.setHorizontalGroup(
            assoWithIndiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(assoWithIndiPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(assoWithIndiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, assoWithIndiPanelLayout.createSequentialGroup()
                        .addComponent(assoWithIndiTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(addLineButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeLineButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(assoListScrollPane))
                .addContainerGap())
        );
        assoWithIndiPanelLayout.setVerticalGroup(
            assoWithIndiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(assoWithIndiPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(assoWithIndiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(assoWithIndiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(removeLineButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(addLineButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(assoWithIndiTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(assoListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                .addContainerGap())
        );

        assoSplitPanel.setTopComponent(assoWithIndiPanel);

        AssoOfIndiPanel.setPreferredSize(new java.awt.Dimension(268, 160));

        assoOfIndoTitle.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(assoOfIndoTitle, org.openide.util.NbBundle.getMessage(AssoManager.class, "AssoManager.assoOfIndoTitle.text")); // NOI18N

        relaListScrollPane.setPreferredSize(new java.awt.Dimension(256, 175));

        assoOfTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        relaListScrollPane.setViewportView(assoOfTable);

        javax.swing.GroupLayout AssoOfIndiPanelLayout = new javax.swing.GroupLayout(AssoOfIndiPanel);
        AssoOfIndiPanel.setLayout(AssoOfIndiPanelLayout);
        AssoOfIndiPanelLayout.setHorizontalGroup(
            AssoOfIndiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AssoOfIndiPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AssoOfIndiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(assoOfIndoTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(relaListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        AssoOfIndiPanelLayout.setVerticalGroup(
            AssoOfIndiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AssoOfIndiPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(assoOfIndoTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(relaListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                .addContainerGap())
        );

        assoSplitPanel.setRightComponent(AssoOfIndiPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(assoSplitPanel)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(assoSplitPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 464, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
        registry.put("assoWindowWidth", evt.getComponent().getWidth());
        registry.put("assoWindowHeight", evt.getComponent().getHeight());
    }//GEN-LAST:event_formComponentResized

    private void assoSplitPanelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_assoSplitPanelPropertyChange
        registry.put("assoSplitDividerLocation", assoSplitPanel.getDividerLocation());
    }//GEN-LAST:event_assoSplitPanelPropertyChange

    private void addLineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLineButtonActionPerformed
        int index = assoWithTable.getSelectedRow();   // selected line
        int row = assoWithTable.getRowSorter().convertRowIndexToModel(index);  // row in model
        awtm.addRow(row); 
        index = assoWithTable.getRowSorter().convertRowIndexToView(row+1); // because rows could be sorted, added row will not be at index+1
        assoWithTable.setRowSelectionInterval(index, index);  
        resizeTable();
        assoListScrollPane.repaint();
        updateOK();
        hasChanged = true;
    }//GEN-LAST:event_addLineButtonActionPerformed

    private void removeLineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeLineButtonActionPerformed
        int index = assoWithTable.getSelectedRow();
        int row = assoWithTable.getRowSorter().convertRowIndexToModel(index);
        awtm.removeRow(row);
        if (index >= assoWithTable.getRowCount()) {
            index--;
        }
        if (index < 0) {
            awtm.addRow(index);
            index = 0;
        } 
        assoWithTable.setRowSelectionInterval(index, index);
        resizeTable();
        assoListScrollPane.repaint();
        hasChanged = true;
    }//GEN-LAST:event_removeLineButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AssoOfIndiPanel;
    private javax.swing.JButton addLineButton;
    private javax.swing.JScrollPane assoListScrollPane;
    private javax.swing.JLabel assoOfIndoTitle;
    private javax.swing.JTable assoOfTable;
    private javax.swing.JSplitPane assoSplitPanel;
    private javax.swing.JPanel assoWithIndiPanel;
    private javax.swing.JLabel assoWithIndiTitle;
    private javax.swing.JTable assoWithTable;
    private javax.swing.JScrollPane relaListScrollPane;
    private javax.swing.JButton removeLineButton;
    // End of variables declaration//GEN-END:variables


    private void getAssociationOf(Indi indi) {
        
    }


    
    
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        if (row >= 0 && row < awtm.getRowCount() && column >= 0 && column < awtm.getColumnCount()) {
            Object data = awtm.getValueAt(row, column);
            if (data != null && !isBusy) {
                if (column == 2) {
                    isBusy = true;
                    awtm.setIndiValues((Indi) data, row);
                    isBusy = false;
                }
                if (column >=3 && column <= 6) {
                    isBusy = true;
                    awtm.setValueAt(null, row, 2);
                    isBusy = false;
                }
            }
            awtm.updateList(data, row, column);
            updateOK();
            hasChanged = true;
        }
    }

    private void updateOK() {
        isValid = true;
        for (AssoWrapper asso : assoWithSet) {
            if (asso.targetEvent == null || asso.assoTxt.trim().isEmpty()) {
                isValid = false;
                break;
            }
            if (asso.assoIndi == null && (asso.assoLastname.trim().isEmpty() || asso.assoFirstname.trim().isEmpty())) {
                isValid = false;
                break;
            }
        }
        okButton.setEnabled(isValid);
    }
    
    
    public String getIndi() {
        return indi.toString();
    }

    public List<AssoWrapper> getSet() {
        return awtm.getSet();
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public List<AssoWrapper> clone(List<AssoWrapper> assoSet) {
        List<AssoWrapper> ret = new ArrayList<AssoWrapper>();
        for (AssoWrapper asso : assoSet) {
            ret.add(AssoWrapper.clone(asso));
        }
        return ret;
    }
    

    
    
    
    
    
    
    
    
    
    
    
    

    private class EventCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                EventWrapper event = (EventWrapper) value;
                setIcon(event.eventLabel.getIcon());
                setText(event.eventLabel.getLongLabel());
            } else {
                setBorder(BorderFactory.createLineBorder(Color.red));
            }
        return this;
        }
    }
   
    private class OtherCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (column == 0 && value == null) {  
                setBorder(BorderFactory.createLineBorder(Color.red));
            }
            if (column == 1) { // rela
                updateEmptyBorder(value);
            }
            if (column == 2) { // indi : red if indi is null && ln and fn are null
                Object o = awtm.getValueAt(row, 2);
                if (o == null) {
                    updateIndiBorder(row);
                }
            }
            if (column == 3 || column == 4) { 
                Object o = awtm.getValueAt(row, 2);
                if (o == null) {
                    updateEmptyBorder(value);
                }
            }
        return this;
        }

        private void updateEmptyBorder(Object value) {
            String str = (String) value;
            if (str.trim().isEmpty()) {
                setBorder(BorderFactory.createLineBorder(Color.red));
            }
        }

        private void updateIndiBorder(int row) {
            String ln = (String) awtm.getValueAt(row, 3);
            String fn = (String) awtm.getValueAt(row, 4);
            if (ln.trim().isEmpty() && fn.trim().isEmpty()) {
                setBorder(BorderFactory.createLineBorder(Color.red));
            }
        }

    }
   
    
    private class ComboBoxEventsRenderer extends JLabel implements ListCellRenderer {

        public ComboBoxEventsRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                EventWrapper event = (EventWrapper) value;
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                setIcon(event.eventLabel.getIcon());
                setText(event.eventLabel.getLongLabel());
            }
            return this;
        }
    }


    private class ComboBoxSexsRenderer  extends JLabel implements ListCellRenderer {

        public ComboBoxSexsRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                ImageIcon icon = (ImageIcon) value;
                if (isSelected) {
                    setBackground(list.getSelectionBackground());
                    setForeground(list.getSelectionForeground());
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                setHorizontalAlignment(JLabel.CENTER);
                setIcon(icon);
                setText(icon == PropertySex.getImage(PropertySex.MALE) ? PropertySex.TXT_MALE : icon == PropertySex.getImage(PropertySex.FEMALE) ? PropertySex.TXT_FEMALE : PropertySex.TXT_UNKNOWN);
            }
            return this;
        }
    }

}
