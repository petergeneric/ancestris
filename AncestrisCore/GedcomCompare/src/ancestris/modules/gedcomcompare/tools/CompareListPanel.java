/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2020 Ancestris
 * 
 * Author: Frédéric Lapeyre (frederic@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcomcompare.tools;

import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Property;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author frederic
 */
public class CompareListPanel extends javax.swing.JPanel {

    /**
     * Creates new form CompareListPanel
     */
    public CompareListPanel() {
        initComponents();
        table.getSelectionModel().addListSelectionListener(new CompareListSelectionListener());
    }

    public void init(STMap map1, STMap map2, STMap intersection) {

        String[] columnNames = new String[]{ "", map1.getName(), map2.getName(), "", "" };
        table.setModel(new tableModel(columnNames, intersection.getData()));
        ((JLabel) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(1).setCellRenderer(new CompareTableCellRenderer());
        table.getColumnModel().getColumn(2).setCellRenderer(new CompareTableCellRenderer());
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(4));
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(3));
        table.getColumnModel().removeColumn(table.getColumnModel().getColumn(0));
        table.setCellSelectionEnabled(true);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();

        setPreferredSize(new java.awt.Dimension(700, 307));

        scrollPane.setPreferredSize(new java.awt.Dimension(700, 300));

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        scrollPane.setViewportView(table);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables

    private class tableModel extends AbstractTableModel {

        private String[] columnNames = null;
        private Object[][] data;

        private tableModel(String[] columnNames, Object[][] data) {
            this.columnNames = columnNames;
            this.data = data;
        }

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < 0 || rowIndex >= getRowCount() || columnIndex < 0 || columnIndex >= getColumnCount()) {
                return null;
            }
            return data[rowIndex][columnIndex];
        }

    }

    private class CompareTableCellRenderer extends JLabel implements TableCellRenderer {

        private final Color bgc;

        public CompareTableCellRenderer() {
            bgc = new Color(table.getBackground().getRGB());
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

            Color fgcs = table.getSelectionForeground();
            Color fgc = table.getForeground();
            String cellText = (String) table.getValueAt(row, column);
            String gap = "";
            Font f = getFont();
            setFont(f.deriveFont(Font.PLAIN));

            int type = Integer.parseInt((String) table.getModel().getValueAt(row, 0));
            switch (type) {
                case 0:  // blanck line
                default:
                    break;
                case 1: // ST title bold left
                    gap = " ";
                    setFont(f.deriveFont(Font.BOLD));
                    break;
                case 2:
                    gap = "    \u2022 ";
                    break;
                case 3:
                    gap = "       \u25E6 ";
                    break;
                case 4:
                    gap = "          \u2219 ";
                    break;
                case 5:
                    gap = "            \u2043 ";
                    break;
                case 6:
                    fgcs = Color.WHITE;
                    fgc = Color.RED;
                    gap = "            \u2043 ";
                    break;

            }

            setText(gap + cellText);

            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(fgcs);
            } else {
                setBackground(bgc);
                setForeground(fgc);
            }
            setOpaque(true);
            return this;

        }
    }

    private class CompareListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()){
                return;
            }
            int row = table.getSelectedRow();
            int column = table.getSelectedColumn();
            if (row < 0 || column < 0) {
                return;
            }
            Object o = table.getModel().getValueAt(row, column+3);
            if (o instanceof Property) {
                SelectionDispatcher.fireSelection(new Context((Property)o));
            }
        }

    }

}
