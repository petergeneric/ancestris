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

import ancestris.util.swing.DialogManager;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.PropertyAssociation;
import genj.gedcom.PropertySex;
import genj.util.ReferenceSet;
import genj.util.Registry;
import genj.util.WordBuffer;
import genj.util.swing.ImageIcon;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class AssoManager extends javax.swing.JPanel implements TableModelListener  {

    private Registry registry = null;
    private boolean hasChanged = false;
    private JButton okButton = null;
    private JButton cancelButton = null;
    private boolean isValid = true;
    private boolean isBusy = false;
    
    private Gedcom gedcom = null;
    private Indi indi = null;
    private List<EventWrapper> eventSet = null;
    
    // Associations With indi
    private List<AssoWrapper> assoWithSet = null;
    private AssoWithTableModel awtm = null;
    private int rowHeight = 18;
    private Indi inditobecreated = null;

    // ComboBox Indi
    private Entity[] arrayIndis = null;
    private JComboBox comboBoxIndis = null;
    private JTextField comboIndiFilter = null;
    private String oldEnteredIndiText = "";

    // ComboBox Rela
    private String[] arrayRelas = null;
    private JComboBox comboBoxRelas = null;
    private JTextField comboRelaFilter = null;
    private String oldEnteredRelaText = "";
    
    // ComboBox Occupations
    private String[] arrayOccus = null;
    private JComboBox comboBoxOccus = null;
    private JTextField comboOccuFilter = null;
    private String oldEnteredOccuText = "";
    
            
    // Associations Of indi
    private DefaultListModel assoOfSet = null;
    
    /**
     * Creates new form AssoManager
     */
    public AssoManager(Indi indi, List<EventWrapper> list, List<AssoWrapper> assoSet, AssoWrapper selectedAsso, JButton okButton, JButton cancelButton) {
        
        this.eventSet = list;
        this.indi = indi;
        this.gedcom = indi.getGedcom();
        this.okButton = okButton;
        this.cancelButton = cancelButton;

        registry = Registry.get(getClass());

        // Pre-create an indi for display in table
        inditobecreated = new Indi("INDI", NbBundle.getMessage(getClass(), "AssoManager.inditobecreated"));
        
        // Get associations of both types
        assoWithSet = clone(assoSet);
        assoOfSet = getAssociationOf(indi);

        initComponents();

        this.setPreferredSize(new Dimension(registry.get("assoWindowWidth", this.getPreferredSize().width), registry.get("assoWindowHeight", this.getPreferredSize().height)));
        assoSplitPanel.setDividerLocation(registry.get("assoSplitDividerLocation", assoSplitPanel.getDividerLocation()));

        
        // Titles
        assoWithIndiTitle.setText(NbBundle.getMessage(getClass(), "AssoManager.assoWithIndiTitle.text", getIndi()));
        assoOfIndoTitle.setText(NbBundle.getMessage(getClass(), "AssoManager.assoOfIndoTitle.text", getIndi()));
        
        // Build table
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
        arrayRelas = relaKeys.toArray(new String[relaKeys.size()]);
        Arrays.sort(arrayRelas);
        comboBoxRelas = new JComboBox(new DefaultComboBoxModel(arrayRelas));
        comboBoxRelas.setMaximumRowCount(20);
        assoWithTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBoxRelas));
        assoWithTable.getColumnModel().getColumn(1).setCellRenderer(new OtherCellRenderer());
        comboBoxRelas.setEditable(true);
        comboRelaFilter = (JTextField) comboBoxRelas.getEditor().getEditorComponent();
        comboRelaFilter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (comboRelaFilter.getSelectedText() == null && !oldEnteredRelaText.equals(comboRelaFilter.getText())) {
                            oldEnteredRelaText = comboRelaFilter.getText();
                            filterComboRela(oldEnteredRelaText);
                        }
                    }
                });
            }
        });
        
        // Set indi column as combobox
        arrayIndis = gedcom.getEntities("INDI", "INDI:NAME");
        comboBoxIndis = new JComboBox(arrayIndis);
        comboBoxIndis.setMaximumRowCount(20);
        assoWithTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(comboBoxIndis));
        assoWithTable.getColumnModel().getColumn(2).setCellRenderer(new OtherCellRenderer());
        comboBoxIndis.setEditable(true);
        comboIndiFilter = (JTextField) comboBoxIndis.getEditor().getEditorComponent();
        comboIndiFilter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (comboIndiFilter.getSelectedText() == null && !oldEnteredIndiText.equals(comboIndiFilter.getText())) {
                            oldEnteredIndiText = comboIndiFilter.getText();
                            filterComboIndi(oldEnteredIndiText);
                        }
                    }
                });
            }
        });

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
        arrayOccus = occuKeys.toArray(new String[occuKeys.size()]);
        Arrays.sort(arrayOccus);
        comboBoxOccus = new JComboBox(new DefaultComboBoxModel(arrayOccus));
        comboBoxOccus.setMaximumRowCount(20);
        assoWithTable.getColumnModel().getColumn(6).setCellEditor(new DefaultCellEditor(comboBoxOccus));
        comboBoxOccus.setEditable(true);
        comboOccuFilter = (JTextField) comboBoxOccus.getEditor().getEditorComponent();
        comboOccuFilter.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent ke) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (comboOccuFilter.getSelectedText() == null && !oldEnteredOccuText.equals(comboOccuFilter.getText())) {
                            oldEnteredOccuText = comboOccuFilter.getText();
                            filterComboOccu(oldEnteredOccuText);
                        }
                    }
                });
            }
        });

        // Resize columns
        resizeColumns();
        
        // Rowheight to fit in comboboxes
        rowHeight = comboBoxOccus.getPreferredSize().height;
        assoWithTable.setRowHeight(rowHeight);

        // Resize table based on its number of lines
        resizeTable();
        
        // Select appropriate association
        int row = 0;
        boolean selected = false;
        for (AssoWrapper asso : assoSet) {   // do not replace with cloned set
            if (asso == selectedAsso) {
                assoWithTable.setRowSelectionInterval(row, row);
                selected = true;
            }
            row++;
        }
        if (!selected && assoWithTable.getRowCount() != 0) {
            assoWithTable.setRowSelectionInterval(0, 0);
        }
        
        // Update button
        updateOK();

        // Detect data changes
        awtm.addTableModelListener(this);
        
    }

    private void resizeTable() {
        Dimension preferredSize = assoWithTable.getPreferredSize();
        preferredSize.height = rowHeight * awtm.getRowCount() + 1;
        assoWithTable.setPreferredSize(preferredSize);
        assoWithTable.revalidate();
        assoWithTable.repaint();
    }

    private void resizeColumns() {
        FontMetrics fm = getFontMetrics(getFont());
        for (int i = 0; i < assoWithTable.getColumnCount(); i++) {
            assoWithTable.getColumnModel().getColumn(i).setPreferredWidth(awtm.getMaxWidth(fm, i));
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

        assoSplitPanel = new javax.swing.JSplitPane();
        assoWithIndiPanel = new javax.swing.JPanel();
        assoWithIndiTitle = new javax.swing.JLabel();
        assoListScrollPane = new javax.swing.JScrollPane();
        assoWithTable = new javax.swing.JTable();
        addLineButton = new javax.swing.JButton();
        removeLineButton = new javax.swing.JButton();
        AssoOfIndiPanel = new javax.swing.JPanel();
        assoOfIndoTitle = new javax.swing.JLabel();
        assoOfScrollPane = new javax.swing.JScrollPane();
        assoOfList = new javax.swing.JList();

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
        assoWithTable.setToolTipText(org.openide.util.NbBundle.getMessage(AssoManager.class, "AssoManager.assoWithTable.toolTipText")); // NOI18N
        assoWithTable.setPreferredSize(new java.awt.Dimension(300, 10));
        assoWithTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        assoWithTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                assoWithTableMousePressed(evt);
            }
        });
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

        assoOfList.setModel(assoOfSet);
        assoOfList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        assoOfList.setToolTipText(org.openide.util.NbBundle.getMessage(AssoManager.class, "AssoManager.assoOfList.toolTipText")); // NOI18N
        assoOfList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                assoOfListMousePressed(evt);
            }
        });
        assoOfScrollPane.setViewportView(assoOfList);
        assoOfList.setCellRenderer(new ListRenderer());

        javax.swing.GroupLayout AssoOfIndiPanelLayout = new javax.swing.GroupLayout(AssoOfIndiPanel);
        AssoOfIndiPanel.setLayout(AssoOfIndiPanelLayout);
        AssoOfIndiPanelLayout.setHorizontalGroup(
            AssoOfIndiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AssoOfIndiPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AssoOfIndiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(assoOfScrollPane)
                    .addComponent(assoOfIndoTitle, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        AssoOfIndiPanelLayout.setVerticalGroup(
            AssoOfIndiPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AssoOfIndiPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(assoOfIndoTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(assoOfScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
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
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = evt.getComponent().getWidth();
        if (w > dim.width*8/10) {
            w = dim.width*8/10;
        }
        int h = evt.getComponent().getHeight();
        if (h > dim.height*8/10) {
            h = dim.height*8/10;
        }
        registry.put("assoWindowWidth", w);
        registry.put("assoWindowHeight", h);
    }//GEN-LAST:event_formComponentResized

    private void assoSplitPanelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_assoSplitPanelPropertyChange
        registry.put("assoSplitDividerLocation", assoSplitPanel.getDividerLocation());
    }//GEN-LAST:event_assoSplitPanelPropertyChange

    private void addLineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLineButtonActionPerformed
        int index = assoWithTable.getSelectedRow();   // selected line
        int row = assoWithTable.getRowSorter().convertRowIndexToModel(index);  // row in model
        awtm.addRow(row); 
        if (assoWithTable.getRowCount() > 1) {
            index = assoWithTable.getRowSorter().convertRowIndexToView(row+1); // because rows could be sorted, added row will not be at index+1
        } else {
            index = 0;
        }
        
        // wrap up
        assoWithTable.setRowSelectionInterval(index, index);  
        resizeTable();
        assoListScrollPane.repaint();
        hasChanged = true;
        resizeColumns();
        updateOK();        
    }//GEN-LAST:event_addLineButtonActionPerformed

    private void removeLineButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeLineButtonActionPerformed
        int index = assoWithTable.getSelectedRow();
        int row = assoWithTable.getRowSorter().convertRowIndexToModel(index);
        awtm.removeRow(row);
        index--;
        if (index < 0) {
            if (assoWithTable.getRowCount() == 0) {
                awtm.addRow(index);
            }
            index = 0;
        } 

        // wrap up
        assoWithTable.setRowSelectionInterval(index, index);
        resizeTable();
        assoListScrollPane.repaint();
        hasChanged = true;
        resizeColumns();
        updateOK();
    }//GEN-LAST:event_removeLineButtonActionPerformed

    private void assoWithTableMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_assoWithTableMousePressed
        if (evt.getButton() == MouseEvent.BUTTON3) {
            int row = assoWithTable.rowAtPoint(evt.getPoint());
            AssoWrapper asso = (AssoWrapper) assoWithSet.get(row);
            if (asso != null) {
                final Entity ent = asso.assoIndi;
                if (ent != null) {
                    JPopupMenu menu = new JPopupMenu();
                    JMenuItem menuItem = new JMenuItem(NbBundle.getMessage(getClass(), hasChanged ? "AssoManager.SaveFirst" : "AssoManager.ShowEntity", ent.toString(true)));
                    menu.add(menuItem);
                    if (!hasChanged) {
                        menuItem.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent ae) {
                                cancelButton.doClick();
                                SelectionDispatcher.fireSelection(new Context(ent));
                            }
                        });
                    }
                    menu.show(assoWithTable, evt.getX(), evt.getY());
                }
            }
        }
    }//GEN-LAST:event_assoWithTableMousePressed

    private void assoOfListMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_assoOfListMousePressed
        if (evt.getButton() == MouseEvent.BUTTON3) {
            int index = assoOfList.locationToIndex(evt.getPoint());
            AssoWrapper asso = (AssoWrapper) assoOfList.getModel().getElementAt(index);
            if (asso != null) {
                final Property targetProperty = asso.targetEvent != null ? asso.targetEvent.eventProperty : null;
                if (targetProperty == null) {
                    return;
                }
                JPopupMenu menu = new JPopupMenu();
                JMenuItem menuItem = new JMenuItem(NbBundle.getMessage(getClass(), hasChanged ? "AssoManager.SaveFirst" : "AssoManager.ShowEntity", targetProperty.getEntity().toString(true)));
                menu.add(menuItem);
                if (!hasChanged) {
                    menuItem.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ae) {
                            cancelButton.doClick();
                            SelectionDispatcher.fireSelection(new Context(targetProperty));
                        }
                    });
                }
                menu.show(assoOfList, evt.getX(), evt.getY());
            }
        }
    }//GEN-LAST:event_assoOfListMousePressed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AssoOfIndiPanel;
    private javax.swing.JButton addLineButton;
    private javax.swing.JScrollPane assoListScrollPane;
    private javax.swing.JLabel assoOfIndoTitle;
    private javax.swing.JList assoOfList;
    private javax.swing.JScrollPane assoOfScrollPane;
    private javax.swing.JSplitPane assoSplitPanel;
    private javax.swing.JPanel assoWithIndiPanel;
    private javax.swing.JLabel assoWithIndiTitle;
    private javax.swing.JTable assoWithTable;
    private javax.swing.JButton removeLineButton;
    // End of variables declaration//GEN-END:variables


    private DefaultListModel getAssociationOf(Indi indi) {
        DefaultListModel ret = new DefaultListModel();

        // Get ASSO tags from indi (can only be attached to root)
        List<PropertyAssociation> assoList = indi.getProperties(PropertyAssociation.class);
        for (PropertyAssociation assoProp : assoList) {
            ret.addElement(new AssoWrapper(assoProp));
        }
        
        return ret;
    }

    
    

    
    
    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        int column = e.getColumn();
        if (row >= 0 && row < awtm.getRowCount() && column >= 0 && column < awtm.getColumnCount()) {
            Object data = awtm.getValueAt(row, column);
            if (data != null && !isBusy) {
                if (column == 2) {
                    if (data instanceof Indi) {
                    isBusy = true;
                    awtm.setIndiValues((Indi) data, row);
                    isBusy = false;
                    } else {
                        // Indi not selected, delete area.
                        data = null;
                    }
                }
                if (column >=3 && column <= 6) {
                    isBusy = true;
                    Object data2 = awtm.getValueAt(row, 2);
                    if (awtm.isChanged(data, row, column) && data2 instanceof Indi) {
                        Indi changedIndi = (Indi) data2;
                        if ((changedIndi != null) && (changedIndi != inditobecreated) && (DialogManager.YES_OPTION == DialogManager.createYesNo(
                                NbBundle.getMessage(getClass(), "TITL_AssoChangeIndi"),
                                NbBundle.getMessage(getClass(), "MSG_AssoChangedIndi", awtm.getColumnName(column), changedIndi)).
                                setMessageType(DialogManager.QUESTION_MESSAGE).show())) {
                            awtm.setValueAt(inditobecreated, row, 2);
                        }
                    }
                    
                    isBusy = false;
                }
                if (column == 1) { // add rela value to combobox if different
                    if ((((DefaultComboBoxModel) comboBoxRelas.getModel()).getIndexOf((String) data)) == -1) {
                        comboBoxRelas.addItem(data);
                    }
                }
                if (column == 6) { // add occu value to combobox if different
                    if ((((DefaultComboBoxModel) comboBoxOccus.getModel()).getIndexOf((String) data)) == -1) {
                        comboBoxOccus.addItem(data);
                    }
                }
            }
            awtm.updateList(data, row, column);
            updateOK();
            hasChanged = true;
        }
    }

    private void updateOK() {
        isValid = true;
        if (hasChanged()) {
            isValid = true;
        } else {
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
            if (asso.assoIndi != null) {
                ret.add(AssoWrapper.clone(asso));
            }
        }
        return ret;
    }

    public boolean contains(AssoWrapper element) {
        for (AssoWrapper asso : assoWithSet) {
            if (asso.equals(element)) {
                return true;
            }
        }
        return false;
    }

    private void filterComboRela(String enteredText) {

        List<String> filterArray= new ArrayList<String>();
        for (String arrayRela : arrayRelas) {
            if (arrayRela.toLowerCase().contains(enteredText.toLowerCase())) {
                filterArray.add(arrayRela);
            }
        }

        if (filterArray.isEmpty()) {
            filterArray.add(enteredText);
        }
        comboBoxRelas.setModel(new DefaultComboBoxModel(filterArray.toArray()));
        comboRelaFilter.setText(enteredText);
        comboRelaFilter.setCaretPosition(enteredText.length());

        if (!comboBoxRelas.isPopupVisible()) {
            comboBoxRelas.showPopup();
        }
        
    }
    
    private void filterComboIndi(String enteredText) {

        List<Entity> filterArray= new ArrayList<Entity>();
        for (Entity arrayIndi : arrayIndis) {
            if (arrayIndi.toString().toLowerCase().contains(enteredText.toLowerCase())) {
                filterArray.add(arrayIndi);
            }
        }

        if (filterArray.size() > 0) {
            comboBoxIndis.setModel(new DefaultComboBoxModel(filterArray.toArray()));
            comboIndiFilter.setText(enteredText);
            comboIndiFilter.setCaretPosition(enteredText.length());
        }

        if (!comboBoxIndis.isPopupVisible()) {
            comboBoxIndis.showPopup();
        }
        
    }

    private void filterComboOccu(String enteredText) {

        List<String> filterArray= new ArrayList<String>();
        for (String arrayOccu : arrayOccus) {
            if (arrayOccu.toLowerCase().contains(enteredText.toLowerCase())) {
                filterArray.add(arrayOccu);
            }
        }

        if (filterArray.isEmpty()) {
            filterArray.add(enteredText);
        }
        comboBoxOccus.setModel(new DefaultComboBoxModel(filterArray.toArray()));
        comboOccuFilter.setText(enteredText);
        comboOccuFilter.setCaretPosition(enteredText.length());

        if (!comboBoxOccus.isPopupVisible()) {
            comboBoxOccus.showPopup();
        }
        
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
            
            int cRow = assoWithTable.getRowSorter().convertRowIndexToModel(row);
            if (column == 0 && value == null) {  
                setBorder(BorderFactory.createLineBorder(Color.red));
            }
            if (column == 1) { // rela
                updateEmptyBorder(value);
            }
            if (column == 2) { // indi : red if indi is null && ln and fn are null
                Object o = awtm.getValueAt(cRow, 2);
                if (o == null) {
                    updateIndiBorder(cRow);
                }
            }
            if (column == 3 || column == 4) { 
                Object o = awtm.getValueAt(cRow, 2);
                if (o == null) {
                    updateEmptyBorder(value);
                }
            }
        return this;
        }

        private void updateEmptyBorder(Object value) {
            String str = (String) value;
            if (str == null) {
                str = "";
            }
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

        private Color backSelectedColor = null;
        private Color foreSelectedColor = null;
        
        public ComboBoxEventsRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
            setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            
            // For some reason, selected colors have to be instantiated in order to work when item is selected.
            Color c = new JList().getSelectionBackground();
            backSelectedColor = new Color(c.getRed(), c.getGreen(), c.getBlue());
            c = new JList().getSelectionForeground();
            foreSelectedColor = new Color(c.getRed(), c.getGreen(), c.getBlue());
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                EventWrapper event = (EventWrapper) value;
                if (isSelected) {
                    setBackground(backSelectedColor);
                    setForeground(foreSelectedColor);
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


    private class ComboBoxSexsRenderer extends JLabel implements ListCellRenderer {

        private Color backSelectedColor = null;
        private Color foreSelectedColor = null;
        
        public ComboBoxSexsRenderer() {
            setOpaque(true);
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
            setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            
            // For some reason, selected colors have to be instantiated in order to work when item is selected.
            Color c = new JList().getSelectionBackground();
            backSelectedColor = new Color(c.getRed(), c.getGreen(), c.getBlue());
            c = new JList().getSelectionForeground();
            foreSelectedColor = new Color(c.getRed(), c.getGreen(), c.getBlue());
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                ImageIcon icon = (ImageIcon) value;
                if (isSelected) {
                    setBackground(backSelectedColor);
                    setForeground(foreSelectedColor);
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                setHorizontalAlignment(JLabel.LEFT);
                setIcon(icon);
                setText(icon == PropertySex.getImage(PropertySex.MALE) ? PropertySex.TXT_MALE : icon == PropertySex.getImage(PropertySex.FEMALE) ? PropertySex.TXT_FEMALE : PropertySex.TXT_UNKNOWN);
            }
            return this;
        }
    }

    
    
    private class ListRenderer extends JLabel implements ListCellRenderer {

        private Color backSelectedColor = null;
        private Color foreSelectedColor = null;
        
        public ListRenderer() {
            setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            
            // For some reason, selected colors have to be instantiated in order to work when item is selected.
            Color c = new JList().getSelectionBackground();
            backSelectedColor = new Color(c.getRed(), c.getGreen(), c.getBlue());
            c = new JList().getSelectionForeground();
            foreSelectedColor = new Color(c.getRed(), c.getGreen(), c.getBlue());
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                AssoWrapper asso = (AssoWrapper) value;
          
                setHorizontalAlignment(JLabel.LEFT);
                setVerticalAlignment(JLabel.CENTER);
                setIcon(asso.assoProp.getImage());
                WordBuffer sb = new WordBuffer(" ");
                sb.append(asso.assoTxt + ", ");
                sb.append(NbBundle.getMessage(getClass(), "AssoManager.label_event"));
                sb.append(asso.targetEvent.eventLabel.getLongLabel() + ", ");
                sb.append(NbBundle.getMessage(getClass(), "AssoManager.label_of"));
                sb.append(asso.targetEvent.eventProperty.getEntity());
                setText(sb.toString());

                if (isSelected) {
                    setBackground(backSelectedColor);
                    setForeground(foreSelectedColor);
                } else {
                    setBackground(list.getBackground());
                    setForeground(list.getForeground());
                }
                setEnabled(list.isEnabled());
                setFont(list.getFont());
                setOpaque(true);
            
            }
            return this;
        }

    }

    
}
