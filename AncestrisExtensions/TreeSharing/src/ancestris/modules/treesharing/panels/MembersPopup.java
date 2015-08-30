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
import java.util.List;
import javax.swing.ImageIcon;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.openide.util.NbBundle;

/**
 *
 * @author frederic
 */
public class MembersPopup extends JPopupMenu implements TableModelListener {

    private final TreeSharingTopComponent owner;
    
    private final ImageIcon ALLOWED_ICON  = new ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/allowed.png"));
    private final ImageIcon MEMBER_ICON = new ImageIcon(getClass().getResource("/ancestris/modules/treesharing/resources/friend16.png"));
    
    private JTable table = null;
    
    /**
     * Creates new form MembersPopup
     */
    public MembersPopup(TreeSharingTopComponent tstc, List<AncestrisMember> ancestrisMembers) {
        this.owner = tstc;
        initComponents();
        table = new JTable();
        initTable(ancestrisMembers);
    }

    private void initTable(List<AncestrisMember> ancestrisMembers) {
        
        // return if ancestris members is empty
        if (ancestrisMembers == null || ancestrisMembers.isEmpty()) {
            return;
        }
        if (table.getColumnModel().getColumnCount() < 2) {
            return;
        }
        
        // Set Table
        setLayout(new BorderLayout());
        table.setModel(new MyTableModel(ancestrisMembers));
        
        // Sortable columns
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setToolTipText(NbBundle.getMessage(MembersPopup.class, "TIP_SortHeader"));
        
        // Editable Table
        table.getModel().addTableModelListener(this);
        
        // Resize first column
        table.getColumnModel().getColumn(0).setPreferredWidth(20);
        
        // Set tooltip text for name column (is string and does not loose its format)
        DefaultTableCellRenderer rendererCol1 = new DefaultTableCellRenderer();
        rendererCol1.setToolTipText(NbBundle.getMessage(MembersPopup.class, "TIP_Allowed"));
        table.getColumnModel().getColumn(1).setCellRenderer(rendererCol1);        

        // Remove grid lines
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);

        // Set header icons
        Border headerBorder = UIManager.getBorder("TableHeader.cellBorder");
        JLabel allowedLabel = new JLabel("", ALLOWED_ICON, JLabel.CENTER);
        allowedLabel.setBorder(headerBorder);
        JLabel nameLabel = new JLabel("", MEMBER_ICON, JLabel.CENTER);
        nameLabel.setBorder(headerBorder);
        TableCellRenderer renderer = new JComponentTableCellRenderer();
        table.getColumnModel().getColumn(0).setHeaderRenderer(renderer);
        table.getColumnModel().getColumn(1).setHeaderRenderer(renderer);
        table.getColumnModel().getColumn(0).setHeaderValue(allowedLabel);
        table.getColumnModel().getColumn(1).setHeaderValue(nameLabel);
        
        // Set font bold for my pseudo in the pseudo column
        table.getColumnModel().getColumn(1).setCellRenderer(new BoldCellRenderer(owner.getPreferredPseudo()));
        
        // Resize table based on its number of lines (max 15 lines)
        add(new JScrollPane(table));
        Dimension preferredSize = table.getPreferredSize();
        preferredSize.width += 30;
        preferredSize.height = table.getRowHeight()*15;
        table.setPreferredScrollableViewportSize(preferredSize);
    }

    public void updateTable(List<AncestrisMember> ancestrisMembers) {
        this.removeAll();
        initTable(ancestrisMembers);
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
        int row = e.getFirstRow();
        int column = e.getColumn();
        MyTableModel model = (MyTableModel)e.getSource();
        Object data = model.getValueAt(row, column);
        if (column == 0) {
            model.getAncestrisMember(row).setAllowed((Boolean)data);
            // Member is activated or desactivated. If something else needs to be done, do it here.
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables


    class MyTableModel extends AbstractTableModel {

        List<AncestrisMember> ancestrisMembers = null;
        String[] columnNames = { "", "" };
        Object[][] data;
        
        private MyTableModel(List<AncestrisMember> ancestrisMembers) {
            this.ancestrisMembers = ancestrisMembers;
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
                return null;
            }
            return data[row][col];
        }

        @Override
        public Class getColumnClass(int c) {
            if (data.length == 0) {
                return String.class;
            }
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
            for (AncestrisMember member : ancestrisMembers) {
                String name = (String) getValueAt(row, 1);
                if (member.getMemberName().equals(name)) {
                    return member;
                }
            }
            return null;
        }

    }
    
    class JComponentTableCellRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            return (JComponent) value;
        }
    }
    
    class BoldCellRenderer extends JLabel implements TableCellRenderer {

        String myPseudo = "";
        
        public BoldCellRenderer(String myPseudo) {
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
            return this;

            
        }
    }
   
    
}
