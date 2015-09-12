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
package ancestris.modules.treesharing.panels;

import ancestris.modules.treesharing.TreeSharingTopComponent;
import ancestris.modules.treesharing.communication.MemberProfile;
import ancestris.util.swing.DialogManager;
import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class StatsPanel extends javax.swing.JPanel {

    private static int NBCOLUMNS = 6;
    
    private final TreeSharingTopComponent owner;

    private final Map<String, StatsData> list;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("d-MMM-yyyy HH:mm");
    
    /**
     * Creates new form ListEntitiesPanel
     */
    public StatsPanel(final Map<String, StatsData> list, TreeSharingTopComponent tstc) {
        this.owner = tstc;
        this.list = list;
        initComponents();
        formatTable(jTable1);
        formatTable(jTable2);
        
        // Centered titles of table1
        ((JLabel) jTable1.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        // Size table1
        Dimension preferredSize = jTable1.getPreferredSize();
        preferredSize.height = list.size()*jTable1.getRowHeight();
        jTable1.setPreferredSize(preferredSize);
   
        // Ability to click on image to popup profil
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = jTable1.rowAtPoint(evt.getPoint());
                int col = jTable1.columnAtPoint(evt.getPoint());
                if (row >= 0 && col == 5) {
                    MemberProfile mp = list.get(jTable1.getModel().getValueAt(row, 1)).profile;
                    DialogManager.create(NbBundle.getMessage(StatsPanel.class, "TITL_ProfilePanel"),
                            new ProfilePanel(mp, owner.getMyProfile())).setMessageType(DialogManager.PLAIN_MESSAGE).setOptionType(DialogManager.OK_ONLY_OPTION).show();

                }
            }
        });
        
        // Hide table2 headers
        jTable2.setTableHeader(null);
        
        // Size table2
        Dimension preferredSize2 = jTable2.getPreferredSize();
        preferredSize2.height = jTable2.getRowHeight();
        jTable1.setPreferredSize(preferredSize2);
        
        // Set table2 transparent
        jTable2.setShowGrid(false);
        jTable2.setOpaque(false);
        ((DefaultTableCellRenderer)jTable2.getDefaultRenderer(Object.class)).setOpaque(false);
                
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(700, 212));

        jTable1.setModel(new MyTableModel(list));
        jTable1.setColumnSelectionAllowed(true);
        jTable1.setPreferredSize(new java.awt.Dimension(720, 100));
        jScrollPane2.setViewportView(jTable1);
        jTable1.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        jTable2.setModel(new MyFooterModel(list));
        jScrollPane1.setViewportView(jTable2);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/reset.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButton1, org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.jButton1.text")); // NOI18N
        jButton1.setToolTipText(org.openide.util.NbBundle.getMessage(StatsPanel.class, "StatsPanel.jButton1.toolTipText")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 688, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        owner.setResetStats();
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    // End of variables declaration//GEN-END:variables

    private void formatTable(JTable table) {
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(40);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(160);
        table.getColumnModel().getColumn(5).setPreferredWidth(50);
        table.setFillsViewportHeight(true);

        // Center some of the columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        //table.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        
        if (table == jTable2) {
            table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
            table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        }
    }

    
    
    
    
    class MyTableModel extends AbstractTableModel {

        String[] columnNames = { 
            NbBundle.getMessage(StatsData.class, "COL_connections"), 
            NbBundle.getMessage(StatsData.class, "COL_member"), 
            NbBundle.getMessage(StatsData.class, "COL_match"),  
            NbBundle.getMessage(StatsData.class, "COL_startDate"),
            NbBundle.getMessage(StatsData.class, "COL_endDate"),
            NbBundle.getMessage(StatsData.class, "COL_profile")
        };
        Object[][] data;
        
        private MyTableModel(Map<String, StatsData> list) {
            if (list == null || list.isEmpty()) {
                data = new Object[1][NBCOLUMNS];
                return;
            }
            data = new Object[list.size()][NBCOLUMNS];
            int i = 0;
            for (String member : list.keySet()) {
                data[i][0] = list.get(member).connections;
                data[i][1] = member;
                data[i][2] = list.get(member).match;
                data[i][3] = formatter.format(list.get(member).startDate);
                data[i][4] = formatter.format(list.get(member).endDate);
                data[i][5] = (ImageIcon) ((list.get(member).profile != null) ? list.get(member).profile.getIcon() : new ImageIcon());
                i++;
            }
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }


    }

    
    
    
    
    
    
    class MyFooterModel extends AbstractTableModel {

        String[] columnNames = { "", "", "", "", "", ""};
        Object[][] data;
        
        private MyFooterModel(Map<String, StatsData> list) {
            
            if (list == null || list.isEmpty()) {
                data = new Object[1][NBCOLUMNS];
                return;
            }
            data = new Object[list.size()][NBCOLUMNS];
            int iConnections = 0;
            int iMember = 0;
            Date minDate = null;
            Date maxDate = null;
            for (String member : list.keySet()) {
                StatsData stats = list.get(member);
                iConnections += stats.connections;
                if (stats.match) {
                    iMember++;
                }
                if (minDate == null || list.get(member).startDate.compareTo(minDate) < 0) {
                    minDate = list.get(member).startDate;
                }
                if (maxDate == null || list.get(member).endDate.compareTo(maxDate) > 0) {
                    maxDate = list.get(member).endDate;
                }
            }
            data[0][0] = iConnections;
            data[0][1] = list.size();
            data[0][2] = iMember;
            data[0][3] = formatter.format(minDate);
            data[0][4] = formatter.format(maxDate);
            data[0][5] = "";
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            return data[row][col];
        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }


    }
    
}
