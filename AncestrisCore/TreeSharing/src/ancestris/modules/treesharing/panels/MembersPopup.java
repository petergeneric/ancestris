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
import ancestris.modules.treesharing.communication.AncestrisMember;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class MembersPopup extends JPopupMenu implements TableModelListener {

    private final TreeSharingTopComponent owner;
    
    private final ImageIcon ALLOWED_ICON  = new ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/allowed.png"));
    private final ImageIcon MEMBER_ICON = new ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/friend16.png"));
    
    private MyTableModel model = null;
    private JTable table = null;
    private JScrollPane jscrollpane = null;
    
    /**
     * Creates new form MembersPopup
     */
    public MembersPopup(TreeSharingTopComponent tstc) {
        this.owner = tstc;
        initComponents();
        table = new JTable();

        // Set Table model
        setLayout(new BorderLayout());
        model = new MyTableModel();
        table.setModel(model);

        // Editable Table
        table.getModel().addTableModelListener(this);
        
        // Sortable columns
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setToolTipText(NbBundle.getMessage(MembersPopup.class, "TIP_SortHeader"));
        
        // Resize first column
        table.getTableHeader().setResizingAllowed(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(25);
        
        // Remove grid lines
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);

        // Set header icons
        Border headerBorder = UIManager.getBorder("TableHeader.cellBorder");
        JLabel allowedLabel = new JLabel("", ALLOWED_ICON, JLabel.CENTER);
        allowedLabel.setBorder(headerBorder);
        JLabel nameLabel = new JLabel("", MEMBER_ICON, JLabel.CENTER);
        nameLabel.setBorder(headerBorder);
        TableCellRenderer renderer = new HeaderCellRenderer();
        table.getColumnModel().getColumn(0).setHeaderRenderer(renderer);
        table.getColumnModel().getColumn(1).setHeaderRenderer(renderer);
        table.getColumnModel().getColumn(0).setHeaderValue(allowedLabel);
        table.getColumnModel().getColumn(1).setHeaderValue(nameLabel);

        // Set renderes for both columns, font bold for my pseudo in the pseudo column
        table.getColumnModel().getColumn(0).setCellRenderer(new TickCellRenderer());
        table.getColumnModel().getColumn(1).setCellRenderer(new NameCellRenderer(owner.getPreferredPseudo()));

        // Display table in scrolable dropdown in this component
        jscrollpane = new JScrollPane(table);
        jscrollpane.setBackground(table.getBackground());
        jscrollpane.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
        add(jscrollpane);
        updateTable();
        
    }

    public void updateTable() {

        // Get latest data
        if (model != null) {
            model.refreshData();
            table.setRowSorter(new TableRowSorter<TableModel>(model));
        } else {
            return;
        }

        // Resize table based on its number of lines (max 15 lines)
        table.getColumnModel().getColumn(1).setPreferredWidth(getMaxWidth());
        Dimension preferredSize = table.getPreferredSize();
        // ...Calculate height
        preferredSize.height = table.getRowHeight()*Math.min(15, model.getRowCount()+1) + 5;
        if (model.getRowCount() == 0) {
            preferredSize.height = 20;
        }
        // ...Set updated size
        table.setPreferredScrollableViewportSize(preferredSize);

        // Refresh scrollpane
        jscrollpane.repaint();
    }

    private int getMaxWidth() {
        FontMetrics fm = getFontMetrics(getFont().deriveFont(Font.BOLD));
        int width = 100, w = 0;
        for (int row = 0; row < table.getRowCount(); row++) {
            w = fm.stringWidth((String) model.getValueAt(row, 1)) + 15;
            width = Math.max(width, w);
        }
        return width;
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void tableChanged(TableModelEvent e) {
        if (model == null || model.getRowCount() == 0) {
            return;
        }
        int row = e.getFirstRow();
        int column = e.getColumn();
        if (row >= 0 && row < model.getRowCount() && column >= 0 && column < model.getColumnCount()) {
            Object data = model.getValueAt(row, column);
            if (column == 0) {
                model.getAncestrisMember(row).setAllowed((Boolean) data);
                //table.setRowSorter(new TableRowSorter<TableModel>(model));
                owner.rememberMembers();
                // Member is activated or desactivated. If something else needs to be done, do it here.
            }
        }
    }



    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables


    class MyTableModel extends AbstractTableModel {

        List<AncestrisMember> ancestrisMembers = null;
        String[] columnNames = { "", "" };
        Object[][] data = new Object[0][2];
        
        private MyTableModel() {
            this.ancestrisMembers = owner.getAncestrisMembers();
            refreshData();
        }

        public void refreshData() {
            this.ancestrisMembers = owner.getAncestrisMembers();
            data = new Object[ancestrisMembers.size()][2];
            int i = 0;
            for (AncestrisMember member : ancestrisMembers) {
                data[i][0] = member.isAllowed();
                data[i][1] = member.getMemberName();
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
            if (data.length == 0) {
                return col == 0 ? new Boolean(false) : new String("");
            }
            return data[row][col];
        }

        @Override
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col == 0;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }

        public AncestrisMember getAncestrisMember(int row) {
            for (AncestrisMember member : owner.getAncestrisMembers()) {
                String name = (String) getValueAt(row, 1);
                if (member.getMemberName().equals(name)) {
                    return member;
                }
            }
            return null;
        }


    }
    
    class HeaderCellRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            return (JComponent) value;
        }
    }
    
    
    
    class TickCellRenderer extends JCheckBox implements TableCellRenderer {

        public TickCellRenderer() {
            setToolTipText(NbBundle.getMessage(MembersPopup.class, "TIP_Allowed"));
            setHorizontalAlignment(JLabel.CENTER);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            setSelected((value != null && ((Boolean) value).booleanValue()));
            setOpaque(true);
            return this;
        }
    }
    
    
   
    class NameCellRenderer extends JLabel implements TableCellRenderer {

        String myPseudo = "";
        
        public NameCellRenderer(String myPseudo) {
            this.myPseudo = myPseudo;
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            Font f = getFont();
            String cellText = (String) table.getValueAt(row, column);
            if (cellText != null && cellText.equals(myPseudo)) {
                setFont(f.deriveFont(Font.BOLD));
            } else {
                setFont(f.deriveFont(Font.PLAIN));
            }
            setText(cellText);
            setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(table.getBackground());
                setForeground(table.getForeground());
            }
            setOpaque(true);
            return this;

            
        }
    }
    
}
