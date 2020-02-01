package ancestris.modules.releve.table;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.plaf.basic.*;

/**
 * GroupableTableHeader
 *
 * @version 1.0 10/20/98
 * @author Nobuo Tamemasa
 */
public class GroupableTableHeader extends JTableHeader {

    /**
     * Identifies the UI class which draws the header.
     */
    private static final String uiClassID = "GroupableTableHeaderUI";

    /**
     * Constructs a GroupableTableHeader which is initialized with cm as the
     * column model. If cm is null this method will initialize the table header
     * with a default TableColumnModel.
     * @param model the column model for the table
     */
    public GroupableTableHeader(GroupableTableColumnModel model) {
        super(model);
        setUI(new GroupableTableHeaderUI());
        setReorderingAllowed(false);
    }

    /**
     * Sets the margins correctly for all groups within
     * the header.
     */
    public void setColumnMargin() {
        int columnMargin = getColumnModel().getColumnMargin();
        Iterator<ColumnGroup>  iter = ((GroupableTableColumnModel) columnModel).columnGroupIterator();
        while (iter.hasNext()) {
            ColumnGroup cGroup = iter.next();
            cGroup.setColumnMargin(columnMargin);
        }
    }
    // Steve Webb 16/09/04 swebb99_uk@hotmail.com

    

    /**
     * This class paints groupable header cells. These can be a combination of
     * normal header cells and groupable cells.
     */
    public class GroupableTableHeaderUI extends BasicTableHeaderUI {

        /**
         * Contains a list of ColumnGroups that have already been painted
         * in the current paint request.
         */
        protected Vector<ColumnGroup> paintedGroups = new Vector<ColumnGroup>();

        /**
         * Paint a representation of the table header.
         * @param g the Graphics context in which to paint
         * @param c the component being painted; this argument is often ignored
         */
        @Override
        public void paint(Graphics g, JComponent c) {
            Rectangle clipBounds = g.getClipBounds();
            GroupableTableColumnModel cm = (GroupableTableColumnModel) header.getColumnModel();
            if (cm == null) {
                return;
            }
            ((GroupableTableHeader) header).setColumnMargin();
            int column = 0;
            Dimension size = header.getSize();
            Rectangle cellRect = new Rectangle(0, 0, size.width, size.height);
            Hashtable<ColumnGroup,Rectangle> h = new Hashtable<ColumnGroup,Rectangle>();
            int columnMargin = cm.getColumnMargin();

            Enumeration<TableColumn>  columns = cm.getColumns();
            while (columns.hasMoreElements()) {
                cellRect.height = size.height;
                cellRect.y = 0;
                TableColumn aColumn = columns.nextElement();
                Iterator<TableColumn>  colGrpIter = cm.getColumnGroups(aColumn);
                if (colGrpIter != null) {
                    int groupHeight = 0;
                    while (colGrpIter.hasNext()) {
                        ColumnGroup cGroup = (ColumnGroup) colGrpIter.next();
                        Rectangle groupRect = h.get(cGroup);
                        if (groupRect == null) {
                            groupRect = new Rectangle(cellRect);
                            Dimension d = cGroup.getSize(header.getTable());
                            groupRect.width = d.width;
                            groupRect.height = d.height;
                            h.put(cGroup, groupRect);
                        }
                        if (!paintedGroups.contains(cGroup)) {
                            paintCell(g, groupRect, cGroup);
                            paintedGroups.add(cGroup);
                        }
                        groupHeight += groupRect.height;
                        cellRect.height = size.height - groupHeight;
                        cellRect.y = groupHeight;
                    }
                }
                cellRect.width = aColumn.getWidth();
                if (cellRect.intersects(clipBounds)) {
                    paintCell(g, cellRect, column);
                }
                cellRect.x += cellRect.width;
                column++;
            }
            paintedGroups.clear();
        }

        /**
         * Paints a header column cell.
         * @param g Graphics context
         * @param cellRect The rectangle to contain the cell
         * @param columnIndex The header column to be painted
         */
        private void paintCell(Graphics g, Rectangle cellRect, int columnIndex) {
            TableColumn aColumn = header.getColumnModel().getColumn(columnIndex);
            TableCellRenderer renderer = aColumn.getHeaderRenderer();
            if (renderer == null) {
                renderer = header.getDefaultRenderer();
            }
            Component component = renderer.getTableCellRendererComponent(
                    header.getTable(), aColumn.getHeaderValue(), false, false, -1, columnIndex);
            rendererPane.add(component);
            rendererPane.paintComponent(g, component, header, cellRect.x, cellRect.y,
                    cellRect.width, cellRect.height, true);
        }

        /**
         * Paint group column cell.
         * @param g Graphics context
         * @param cellRect Rectangle that the cell with be painted in
         * @param cGroup Current column group
         */
        private void paintCell(Graphics g, Rectangle cellRect, ColumnGroup cGroup) {
            TableCellRenderer renderer = cGroup.getHeaderRenderer();
            Component component = renderer.getTableCellRendererComponent(
                    header.getTable(), cGroup.getHeaderValue(), false, false, -1, -1);
            rendererPane.add(component);
            rendererPane.paintComponent(g, component, header, cellRect.x, cellRect.y,
                    cellRect.width, cellRect.height, true);
        }

        /**
         * Calculate and return the height of the header.
         * @return Header Height
         */
        private int getHeaderHeight() {
            int height = 0;
            GroupableTableColumnModel columnModel = (GroupableTableColumnModel) header.getColumnModel();
            for (int column = 0; column < columnModel.getColumnCount(); column++) {
                TableColumn aColumn = columnModel.getColumn(column);
                TableCellRenderer renderer = aColumn.getHeaderRenderer();
                if (renderer == null) {
                    renderer = header.getDefaultRenderer();
                }
                Component comp = renderer.getTableCellRendererComponent(
                        header.getTable(), aColumn.getHeaderValue(), false, false, -1, column);
                int cHeight = comp.getPreferredSize().height;
                Iterator<TableColumn>  iter = columnModel.getColumnGroups(aColumn);
                if (iter != null) {
                    while (iter.hasNext()) {
                        ColumnGroup cGroup = (ColumnGroup) iter.next();
                        cHeight += cGroup.getSize(header.getTable()).height;
                    }
                }
                height = Math.max(height, cHeight);
            }
            return height;
        }

        /**
         * Calculate and return the dimension of the header.
         * @param width Starting width to be used.
         * @return Dimension of the header
         */
        private Dimension createHeaderSize(long width) {
            TableColumnModel columnModel = header.getColumnModel();
            width += columnModel.getColumnMargin() * columnModel.getColumnCount();
            if (width > Integer.MAX_VALUE) {
                width = Integer.MAX_VALUE;
            }
            return new Dimension((int) width, getHeaderHeight());
        }

        /**
         * Invokes the getPreferredSize method on each UI handled by this object.
         * @param c the component whose preferred size is being queried; this argument is ignored.
         * @return the dimension of the whole header
         */
        @Override
        public Dimension getPreferredSize(JComponent c) {
            long width = 0;
            Enumeration<TableColumn> columns = header.getColumnModel().getColumns();
            while (columns.hasMoreElements()) {
                TableColumn aColumn = columns.nextElement();
                width = width + aColumn.getPreferredWidth();
            }
            return createHeaderSize(width);
        }
    }
}
