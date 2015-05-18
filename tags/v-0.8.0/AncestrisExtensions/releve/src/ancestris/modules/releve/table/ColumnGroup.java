package ancestris.modules.releve.table;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * ColumnGroup
 *
 * @version 1.0 10/20/98
 * @author Nobuo Tamemasa
 */
public class ColumnGroup extends TableColumn {
    /**
     * Cell renderer for group header.
     */
    protected TableCellRenderer renderer;
    /**
     * Holds the TableColumn or ColumnGroup objects contained
     * within this ColumnGroup instance.
     */
    protected ArrayList<TableColumn> v;
    /**
     * The ColumnGroup instance name.
     */
    protected String text;
    /**
     * The margin to use for renderering.
     */
    protected int margin=0;

    /**
     * Standard ColumnGroup constructor.
     * @param text Name of the ColumnGroup which will be displayed
     * when the ColumnGroup is renderered.
     */
    public ColumnGroup(String text) {
        this(null,text);
    }

    /**
     * Standard ColumnGroup constructor.
     * @param renderer a TableCellRenderer for the group.
     * @param text Name of the ColumnGroup which will be displayed
     * when the ColumnGroup is renderered.
     */
    public ColumnGroup(TableCellRenderer renderer,String text) {
        if (renderer == null) {
            this.renderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
                    JTableHeader header = table.getTableHeader();
                    if (header != null) {
                        setForeground(header.getForeground());
                        setBackground(header.getBackground());
                        setFont(header.getFont());
                    }
                    setHorizontalAlignment(JLabel.CENTER);
                    setText((value == null) ? "" : value.toString());
                    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                    return this;
                }
            };
        } else {
            this.renderer = renderer;
        }
        this.text = text;
        v = new ArrayList<TableColumn>();
    }


    /**
     * Add a TableColumn or ColumnGroup object to the
     * ColumnGroup instance.
     * @param obj TableColumn or ColumnGroup
     */
    public void add(TableColumn obj) {
        if (obj == null) { return; }
        v.add(obj);
    }


    /**
     * Get the ColumnGroup list containing the required table
     * column.
     * @param g ArrayList to populate with the ColumnGroup/s
     * @param c TableColumn
     * @return ArrayList containing the ColumnGroup/s
     */
    public ArrayList<TableColumn> getColumnGroups(TableColumn c, ArrayList<TableColumn> g) {
        g.add(this);
        if (v.contains(c)) return g;
        Iterator<TableColumn> iter = v.iterator();
        while (iter.hasNext()) {
            TableColumn obj = iter.next();
            if (obj instanceof ColumnGroup) {
                ArrayList<TableColumn> clone = new ArrayList<TableColumn>(g);
                ArrayList<TableColumn> groups = ((ColumnGroup)obj).getColumnGroups(c,clone);
                if (groups != null) return groups;
            }
        }
        return null;
    }

    /**
     * Returns the TableCellRenderer for the ColumnGroup.
     * @return the TableCellRenderer
     */
    @Override
    public TableCellRenderer getHeaderRenderer() {
        return renderer;
    }

    /**
     * Set the TableCellRenderer for this ColumnGroup.
     * @param renderer the renderer to use
     */
    @Override
    public void setHeaderRenderer(TableCellRenderer renderer) {
        if (renderer != null) {
            this.renderer = renderer;
        }
    }

    /**
     * Get the ColumnGroup header value.
     * @return the value.
     */
    @Override
    public Object getHeaderValue() {
        return text;
    }

    /**
     * Get the dimension of this ColumnGroup.
     * @param table the table the header is being rendered in
     * @return the dimension of the ColumnGroup
     */
    public Dimension getSize(JTable table) {
        Component comp = renderer.getTableCellRendererComponent(
        table, getHeaderValue(), false, false,-1, -1);
        int columnHeight = comp.getPreferredSize().height;
        int columnWidth  = 0;
//        Iterator iter = v.iterator();
//        while (iter.hasNext()) {
//            Object obj = iter.next();
//            if (obj instanceof TableColumn) {
//                TableColumn aColumn = (TableColumn)obj;
//                columnWidth += aColumn.getWidth();
//            } else {
//                columnWidth += ((ColumnGroup)obj).getSize(table).width;
//            }
//        }
        Iterator<TableColumn> iter = v.iterator();
        while (iter.hasNext()) {
            TableColumn obj = iter.next();
            if (obj instanceof ColumnGroup) {
                columnWidth += ((ColumnGroup)obj).getSize(table).width;
            } else {
                columnWidth += obj.getWidth();
            }
        }
        return new Dimension(columnWidth, columnHeight);
    }

    /**
     * Sets the margin that ColumnGroup instance will use and all
     * held TableColumns and/or ColumnGroups.
     * @param margin the margin
     */
    public void setColumnMargin(int margin) {
        this.margin = margin;
        Iterator<TableColumn> iter = v.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            if (obj instanceof ColumnGroup) {
                ((ColumnGroup)obj).setColumnMargin(margin);
            }
        }
    }
}
