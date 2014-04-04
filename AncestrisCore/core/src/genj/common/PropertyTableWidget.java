/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.common;

import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.util.swing.TableFilterWidget;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyName;
import genj.io.BasicTransferable;
import genj.util.WordBuffer;
import genj.util.swing.HeadlessLabel;
import genj.util.swing.LinkWidget;
import genj.view.ViewContext;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * A widget that shows entities in rows and columns
 */
public class PropertyTableWidget extends JPanel {

    private final static Logger LOG = Logger.getLogger("genj.common");
    private JPanel panelShortcuts;
    private Table table;
    private boolean ignoreSelection = false;
    private int visibleRowCount = -1;
    private TransferHandler transferer;

    /**
     * Constructor
     */
    public PropertyTableWidget() {
        this(null);
    }

    /**
     * Constructor
     */
    public PropertyTableWidget(PropertyTableModel propertyModel) {

        // create panel for shortcuts
        panelShortcuts = new JPanel();
        panelShortcuts.setMinimumSize(new Dimension());
        panelShortcuts.setLayout(new BoxLayout(panelShortcuts, BoxLayout.Y_AXIS));

        // create table comp
        table = new Table();
        setModel(propertyModel);
        setRowSelection(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setColSelection(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // setup layout
        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, new JScrollPane(table));
        add(BorderLayout.EAST, panelShortcuts);

        // done
    }

    /**
     * Get underlying table component.
     * Mainly used for ExplorerHelper to properly set MouseListener to
     * handle popup menus
     *
     * @return
     */
    //FIXME: we must change this dependency
    public Component getTableComponent() {
        return table;
    }

    public void setFilterWidget(TableFilterWidget filter) {
        table.setFilterWidget(filter);
    }

    /**
     * Column selection
     * @set one of
     * ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
     * ListSelectionModel.SINGLE_SELECTION
     * ListSelectionModel.SINGLE_INTERVAL_SELECTION
     * -1
     */
    public void setColSelection(int set) {
        table.setColSelection(set);
    }

    /**
     * Column selection
     * @set one of
     * ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
     * ListSelectionModel.SINGLE_SELECTION
     * ListSelectionModel.SINGLE_INTERVAL_SELECTION
     */
    public void setRowSelection(int set) {
        table.setRowSelection(set);
    }

    /**
     * Accessor for table model being shown
     */
    public TableModel getTableModel() {
        return table.getModel();
    }

    /**
     * Setter for current model
     */
    public void setModel(PropertyTableModel set) {
        table.setPropertyTableModel(set);
    }

    public void setVisibleRowCount(int rows) {
        visibleRowCount = rows;
        revalidate();
        repaint();
    }

    /**
     * Getter for current model
     */
    public PropertyTableModel getModel() {
        return table.getPropertyTableModel();
    }

    /**
     * Set column resize behavior
     */
    public void setAutoResize(boolean on) {
        table.setAutoResizeMode(on ? JTable.AUTO_RESIZE_ALL_COLUMNS : JTable.AUTO_RESIZE_OFF);
    }

    public int[] getSelectedRows() {
        return table.getSelectedRows();
    }

    public Property getSelectedRow() {
        int i = table.getSelectedRow();
        return i < 0 ? null : table.getRowRoot(i);
    }

    /**
     * Select a cell
     */
    public void select(Context context) {

        if (ignoreSelection) {
            return;
        }

        if (context.getGedcom() != getModel().getGedcom()) {
            throw new IllegalArgumentException("select on wrong gedcom");
        }

        // set selection
        try {
            ignoreSelection = true;

            // loop over selected properties
            List<? extends Property> props = context.getProperties();

            // use all of selected entities properties if there are no property selections
            if (props.isEmpty()) {
                List<Property> ps = new ArrayList<Property>(context.getProperties());
                for (Entity ent : context.getEntities()) {
                    if (!ps.contains(ent)) {
                        ps.add(ent);
                    }
                }
                props = ps;
            }

            ListSelectionModel rows = table.getSelectionModel();
            ListSelectionModel cols = table.getColumnModel().getSelectionModel();
            table.clearSelection();

            Point cell = new Point();
            for (Property prop : props) {

                // add cell selection
                cell = table.getCell(prop);
                if (cell.y >= 0) {
                    cell.y = table.convertRowIndexToView(cell.y);
                    rows.addSelectionInterval(cell.y, cell.y);
                    if (cell.x >= 0) {
                        cell.x = table.convertColumnIndexToView(cell.x);
                        cols.addSelectionInterval(cell.x, cell.x);
                    }
                } else {
                    int row = table.getRow(prop);
                    if (row >= 0) {
                        row = table.convertRowIndexToView(row);
                        rows.addSelectionInterval(row, row);
                        cols.addSelectionInterval(0, table.getColumnCount() - 1);
                        cell.y = row;
                    }
                    continue;
                }

            }

            // scroll to last selection
            if (cell.y >= 0) {
                Rectangle visible = table.getVisibleRect();
                Rectangle scrollto = table.getCellRect(cell.y, cell.x, true);
                if (cell.x < 0) {
                    scrollto.x = visible.x;
                }
                table.scrollRectToVisible(scrollto);
            }

        } finally {
            ignoreSelection = false;
        }

    }

    /**
     * add listener
     */
    public void addListSelectionListener(ListSelectionListener listener) {
        table.getSelectionModel().addListSelectionListener(listener);
    }

    /**
     * remove listener
     */
    public void removeListSelectionListener(ListSelectionListener listener) {
        table.getSelectionModel().removeListSelectionListener(listener);
    }

    /**
     * Return column layout - a string that can be used to return column widths and sorting
     */
    public String getColumnLayout() {

        // e.g. 4, 40, 60, 70, 48, 0, -1, 1, 1 
        // for a table with 4 columns and two sort directives

        TableColumnModel columns = table.getColumnModel();

        WordBuffer result = new WordBuffer(",");
        result.append(columns.getColumnCount());

        for (int c = 0; c < columns.getColumnCount(); c++) {
            result.append(columns.getColumn(c).getWidth());
        }


        return result.toString();
    }

    /**
     * Set column layout
     */
    public void setColumnLayout(String layout) {

        TableColumnModel columns = table.getColumnModel();

        try {
            StringTokenizer tokens = new StringTokenizer(layout, ",");
            int n = Integer.parseInt(tokens.nextToken());

            for (int i = 0; i < n && i < columns.getColumnCount(); i++) {
                TableColumn col = columns.getColumn(i);
                int w = Integer.parseInt(tokens.nextToken());
                col.setWidth(w);
                col.setPreferredWidth(w);
            }

        } catch (Throwable t) {
            // ignore
        }
    }

    /**
     * Table Content
     */
    private class Table extends JTable {

        private PropertyTableModel propertyModel;
        private int defaultRowHeight;
        private TableRowSorter<Model> sorter;
        private TableFilterWidget filterText;

        /**
         * Constructor
         */
        Table() {

            setPropertyTableModel(null);
            Renderer r = new Renderer();
            r.setFont(getFont());
            defaultRowHeight = r.getPreferredSize().height;
            setDefaultRenderer(Object.class, r);
            getTableHeader().setReorderingAllowed(false);

            getColumnModel().getSelectionModel().addListSelectionListener(this);
            // 20091208 JTable already implements and add itself as listener
            //getSelectionModel().addListSelectionListener(this);

            // 20050721 we want the same focus forward/backwards keys as everyone else
            setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null);
            setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null);

            // patch selecting
            addMouseListener(new MouseAdapter() {

                @Override
                public void mousePressed(MouseEvent e) {
                    // make sure something is selected but don't screw current multi-selection
                    int row = rowAtPoint(e.getPoint());
                    int col = columnAtPoint(e.getPoint());
                    if (row < 0 || col < 0) {
                        clearSelection();
                    } else {
                        if (!isCellSelected(row, col)) {
                            getSelectionModel().setSelectionInterval(row, row);
                            getColumnModel().getSelectionModel().setSelectionInterval(col, col);
                        }
                    }
                    // FIXME: action is handled here and selection is handled in changeSelection
                    Object cell = getValueAt(row, col);
                    if (cell != null && cell instanceof Property) {
//XXX:                        SelectionDispatcher.fireAction(e,new Context((Property)cell));
                        SelectionDispatcher.fireSelection(e, new Context((Property) cell));
                    }
                }
            });

            panelShortcuts.addComponentListener(new ComponentAdapter() {

                @Override
                public void componentResized(ComponentEvent e) {
                    createShortcuts();
                }
            });

            // done
        }

        private void setFilterWidget(TableFilterWidget filter) {
            filterText = filter;
            if (filterText != null) {
                filterText.setSorter(sorter);
            }
        }

        @Override
        public TransferHandler getTransferHandler() {
            if (transferer == null) {
                transferer = new Transferer();
            }
            return transferer;
        }

        @Override
        public int[] getSelectedRows() {
            int[] rows = super.getSelectedRows();
            for (int r = 0; r < rows.length; r++) {
                rows[r] = convertRowIndexToModel(rows[r]);
            }
            return rows;
        }

        void setRowSelection(int set) {
            getSelectionModel().setSelectionMode(set);
        }

        void setColSelection(int set) {
            getColumnModel().setColumnSelectionAllowed(set >= 0);
            if (set >= 0) {
                getColumnModel().getSelectionModel().setSelectionMode(set);
            }
        }

        /** create a shortcut */
        AbstractAncestrisAction createShortcut(String txt, final int y) {
            return new AbstractAncestrisAction(txt.toUpperCase()) {

                @Override
                public void actionPerformed(ActionEvent event) {
                    int x = 0;
                    try {
                        x = ((JViewport) table.getParent()).getViewPosition().x;
                    } catch (Throwable t) {
                    };
                    table.scrollRectToVisible(new Rectangle(x, y, 1, getParent().getHeight()));
                }
            };
        }

        /** generate */
        void createShortcuts(int col, int dir, JComponent container) {

            if (propertyModel == null || container.getHeight() == 0) {
                return;
            }

            TableModel model = getModel();
            Collator collator = propertyModel.getGedcom().getCollator();

            // loop over rows and create actions
            List<AbstractAncestrisAction> actions = new ArrayList<AbstractAncestrisAction>(26);

            String cursor = "";
            for (int r = 0; r < model.getRowCount(); r++) {
                Property prop = (Property) model.getValueAt(dir > 0 ? r : model.getRowCount() - r - 1, col);
                if (prop instanceof PropertyDate) {
                    break;
                }
                if (prop == null) {
                    continue;
                }

                String value = prop instanceof PropertyName ? ((PropertyName) prop).getLastName().trim() : prop.getDisplayValue().trim();
                if (value.length() == 0) {
                    continue;
                }
                value = value.substring(0, 1).toLowerCase();

                if (collator.compare(cursor, value) >= 0) {
                    continue;
                }
                cursor = value;

                // action
                AbstractAncestrisAction action = createShortcut(value, table.getCellRect(dir > 0 ? r : model.getRowCount() - r - 1, col, true).y);
                actions.add(action);

                // key binding
                InputMap imap = container.getInputMap(WHEN_IN_FOCUSED_WINDOW);
                ActionMap amap = container.getActionMap();
                imap.put(KeyStroke.getKeyStroke(value.charAt(0)), action);
                amap.put(action, action);
            }

            // generate buttons
            if (actions.isEmpty()) {
                return;
            }

            LinkWidget sample = new LinkWidget("Sample", null);
            int h = sample.getPreferredSize().height;
            int n = Math.min(actions.size(), (container.getHeight() - h) / h);
            for (int i = 0; i < n; i++) {
                LinkWidget link = new LinkWidget(actions.get(i * actions.size() / n));
                link.setAlignmentX(0.5F);
                container.add(link);
            }

            if (n < actions.size()) {
                LinkWidget link = new LinkWidget(actions.get(actions.size() - 1));
                link.setAlignmentX(0.5F);
                container.add(link);
            }

            // done
        }

        /**
         * Create shortcuts
         */
        void createShortcuts() {

            // remove old shortcuts
            panelShortcuts.removeAll();
            panelShortcuts.getInputMap(WHEN_IN_FOCUSED_WINDOW).clear();
            panelShortcuts.getActionMap().clear();
            panelShortcuts.revalidate();
            panelShortcuts.repaint();

            // anything we can offer? need ascending sorted column and at least 10 rows
//            if (!sortableModel.isSorting()) {
//                return;
//            }

//            SortableTableModel.Directive directive = sortableModel.getDirectives().get(0);
//      if (directive.getDirection()<=0)
//        return;

//            createShortcuts(directive.getColumn(), directive.getDirection(), panelShortcuts);

            // done
        }

        int getRow(Property prop) {

            PropertyTableModel model = getPropertyTableModel();
            for (int i = 0; i < model.getNumRows(); i++) {
                if (model.getRowRoot(i).contains(prop)) {
                    return convertRowIndexToView(i);
                }
            }
            return -1;
        }

        /**
         * return model cell coordinate for property.
         *
         * @param property
         *
         * @return
         */
        Point getCell(Property property) {

            Point p = new Point(-1, -1);

            if (propertyModel == null) {
                return p;
            }

            TableModel model = getModel();
            for (int i = 0; i < model.getRowCount(); i++) {

                int r = i;

                for (int j = 0; j < model.getColumnCount(); j++) {
                    if (model.getValueAt(r, j) == property) {
                        return new Point(j, r);
                    }
                }

            }

            return p;
        }

        Property getRowRoot(int index) {
            return propertyModel.getRowRoot(convertRowIndexToModel(index));
        }

        /**
         * setting a property model
         */
        void setPropertyTableModel(PropertyTableModel propertyModel) {
            // remember
            this.propertyModel = propertyModel;
            // pass through 
            if (propertyModel == null) {
                return;
            }

            Model tableModel = new Model(propertyModel);
            setModel(tableModel);
            sorter = new TableRowSorter<Model>(tableModel);
            setRowSorter(sorter);

            // listen to changes for generating shortcuts
            tableModel.addTableModelListener(new TableModelListener() {

                @Override
                public void tableChanged(TableModelEvent e) {
                    if (e.getLastRow() == Integer.MAX_VALUE) {
                        createShortcuts();
                    }
                }
            });

            if (filterText != null) {
                filterText.setSorter(sorter);
            }

            // done
        }

        /**
         * accessing property model
         */
        PropertyTableModel getPropertyTableModel() {
            return propertyModel;
        }

        @Override
        public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend) {

            // grab before context
            List<? extends Property> before = getContext().getProperties();

            // let table do its thing
            super.changeSelection(rowIndex, columnIndex, toggle, extend);

            // propagate selection change?
            if (ignoreSelection) {
                return;
            }

            List<Property> properties = new ArrayList<Property>();
            ListSelectionModel rows = getSelectionModel();
            ListSelectionModel cols = getColumnModel().getSelectionModel();

            for (int r = rows.getMinSelectionIndex(); r <= rows.getMaxSelectionIndex(); r++) {
                for (int c = cols.getMinSelectionIndex(); c <= cols.getMaxSelectionIndex(); c++) {
                    // check specific row col
                    if (!rows.isSelectedIndex(r) || !cols.isSelectedIndex(c)) {
                        continue;
                    }
                    // 20050721 check arguments - Swing might not always send something smart here
                    TableModel model = getModel();
                    if (r < 0 || r >= model.getRowCount() || c < 0 || c >= model.getColumnCount()) {
                        continue;
                    }
                    Property prop = (Property) getValueAt(r, c);
                    if (prop == null) {
                        prop = propertyModel.getRowRoot(convertRowIndexToModel(r));
                    }
                    // keep it
                    if (before.contains(prop)) {
                        properties.add(prop);
                    } else {
                        properties.add(0, prop);
                    }
                }
            }

            // tell about it
            if (!properties.isEmpty()) {
                ignoreSelection = true;
                SelectionDispatcher.fireSelection(new Context(properties.get(0).getGedcom(), new ArrayList<Entity>(), properties));
                ignoreSelection = false;
            }

            // done
        }

        /**
         *
         */
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            Dimension d = super.getPreferredScrollableViewportSize();
            if (visibleRowCount > 0) {
                d.height = 0;
                for (int row = 0; row < visibleRowCount; row++) {
                    if (row < getModel().getRowCount()) {
                        d.height += getRowHeight(row);
                    } else {
                        d.height += defaultRowHeight;
                    }
                }
            }
            return d;
        }

        /**
         * ContextProvider - callback
         */
        //XXX: we will have to handle this differently: use nodes (see TableView)
        // FIXME: this is used only in changeSelection... Should we refactor
        private ViewContext getContext() {

            // check gedcom first
            Gedcom ged = propertyModel.getGedcom();
            if (ged == null) {
                return null;
            }
            TableModel model = getModel();

            // one row one col?
            List<Property> properties = new ArrayList<Property>();
            int[] rows = super.getSelectedRows();
            if (rows.length > 0) {
                int[] cols = getSelectedColumns();

                // loop over rows
                for (int r = 0; r < rows.length; r++) {

                    // loop over cols
                    boolean rowRepresented = false;
                    for (int c = 0; c < cols.length; c++) {
                        // add property for each cell
                        Property p = (Property) getValueAt(rows[r], cols[c]);
                        if (p != null) {
                            properties.add(p);
                            rowRepresented = true;
                        }
                        // next selected col
                    }

                    // add representation for each row that wasn't represented by a property
                    if (!rowRepresented) {
                        properties.add(propertyModel.getRowRoot(convertRowIndexToModel(rows[r])));
                    }

                    // next selected row
                }
            }

            // done
            return new ViewContext(ged, new ArrayList<Entity>(), properties);
        }

        /**
         * The logical model
         */
        private class Model extends AbstractTableModel implements PropertyTableModelListener {//, SortableTableModel.RowComparator {

            /** our model */
            private PropertyTableModel model;
            /** cached table content */
            private Property cells[][];

            /** constructor */
            private Model(PropertyTableModel set) {
                // setup state
                model = set;
                // done
            }

            private Gedcom getGedcom() {
                return model != null ? model.getGedcom() : null;
            }

            @Override
            public void handleRowsAdded(PropertyTableModel model, int rowStart, int rowEnd) {

                // flush cell state
                int rows = model.getNumRows(),
                        cols = model.getNumCols();
                cells = new Property[rows][cols];

                // tell about it
                fireTableRowsInserted(rowStart, rowEnd);
            }

            @Override
            public void handleRowsDeleted(PropertyTableModel model, int rowStart, int rowEnd) {
                // flush cell state
                int rows = model.getNumRows(),
                        cols = model.getNumCols();
                cells = new Property[rows][cols];

                // tell about it
                fireTableRowsDeleted(rowStart, rowEnd);
            }

            @Override
            public void handleRowsChanged(PropertyTableModel model, int rowStart, int rowEnd, int col) {
                // flush cell state
                for (int i = rowStart; i <= rowEnd; i++) {
                    cells[i][col] = null;
                }
                // tell about it
                fireTableChanged(new TableModelEvent(this, rowStart, rowEnd, col));
            }

            /** someone interested in us */
            @Override
            public void addTableModelListener(TableModelListener l) {
                super.addTableModelListener(l);
                // start listening ?
                if (model != null && getListeners(TableModelListener.class).length == 1) {
                    model.addListener(this);
                }
            }

            /** someone lost interest */
            @Override
            public void removeTableModelListener(TableModelListener l) {
                super.removeTableModelListener(l);
                // stop listening ?
                if (model != null && getListeners(TableModelListener.class).length == 0) {
                    model.removeListener(this);
                }
            }

            /**
             * patched column name
             */
            @Override
            public String getColumnName(int col) {
                return model != null ? model.getColName(col) : "";
            }

            /** num columns */
            @Override
            public int getColumnCount() {
                return model != null ? model.getNumCols() : 0;
            }

            /** num rows */
            @Override
            public int getRowCount() {
                return model != null ? model.getNumRows() : 0;
            }

            /** property */
            private Property getPropertyAt(int row, int col) {

                // is cache setup?
                if (cells == null) {
                    cells = new Property[model.getNumRows()][model.getNumCols()];
                }

                Property prop = cells[row][col];
                if (prop == null) {
                    prop = model.getRowRoot(row).getProperty(model.getColPath(col));
                    cells[row][col] = prop;
                }
                return prop;
            }

            /** value */
            @Override
            public Object getValueAt(int row, int col) {
                return getPropertyAt(row, col);
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                // getValueAt always return a gedcom property
                return Property.class;
            }
        } //Model

        /**
         * Renderer for properties in cells
         */
        private class Renderer extends HeadlessLabel implements TableCellRenderer {

            Renderer() {
                setPadding(2);
            }

            /**
             * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(JTable, Object, boolean, boolean, int, int)
             */
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focs, int row, int col) {

                setFont(table.getFont());
                if (getRowHeight() != getPreferredSize().height) {
                    setRowHeight(getPreferredSize().height);
                }

                // figure out value and alignment
                if (propertyModel instanceof AbstractPropertyTableModel) {
                    AbstractPropertyTableModel m = (AbstractPropertyTableModel) propertyModel;
                    setText(m.getCellValue((Property) value, row, col));
                    setHorizontalAlignment(m.getCellAlignment((Property) value, row, col));
                } else {
                    setText(AbstractPropertyTableModel.getDefaultCellValue((Property) value, row, col));
                    setHorizontalAlignment(AbstractPropertyTableModel.getDefaultCellAlignment((Property) value, row, col));
                }

                // background?
                if (selected) {
                    setBackground(table.getSelectionBackground());
                    setForeground(table.getSelectionForeground());
                    setOpaque(true);
                } else {
                    setForeground(table.getForeground());
                    setOpaque(false);
                }
                // ready
                return this;
            }
        } //PropertyTableCellRenderer

        private class Transferer extends TransferHandler {

            /**
             * Create a Transferable to use as the source for a data transfer.
             *
             * @param c The component holding the data to be transfered.
             * This argument is provided to enable sharing of TransferHandlers by
             * multiple components.
             *
             * @return The representation of the data to be transfered.
             *
             */
            @Override
            protected Transferable createTransferable(JComponent c) {

                // ourselves?
                if (c != Table.this) {
                    return null;
                }

                // loop
                int[] cols = table.getSelectedColumns();
                int[] rows = table.getSelectedRows();

                if (rows == null || cols == null || rows.length == 0 || cols.length == 0) {
                    return null;
                }

                StringBuilder plainBuf = new StringBuilder();
                StringBuilder htmlBuf = new StringBuilder();

                htmlBuf.append("<html>\n<body>\n<table>\n");

                for (int row = 0; row < rows.length; row++) {
                    htmlBuf.append("<tr>\n");
                    for (int col = 0; col < cols.length; col++) {
                        Property obj = (Property) table.getValueAt(convertRowIndexToView(rows[row]), cols[col]);
                        String val = AbstractPropertyTableModel.getDefaultCellValue(obj, row, col);
                        plainBuf.append(val).append("\t");
                        htmlBuf.append("  <td>").append(val).append("</td>\n");
                    }
                    // we want a newline at the end of each line and not a tab
                    plainBuf.deleteCharAt(plainBuf.length() - 1).append("\n");
                    htmlBuf.append("</tr>\n");
                }

                // remove the last newline
                plainBuf.deleteCharAt(plainBuf.length() - 1);
                htmlBuf.append("</table>\n</body>\n</html>");

                return new BasicTransferable(plainBuf.toString(), htmlBuf.toString());
            }

            @Override
            public int getSourceActions(JComponent c) {
                return c == Table.this ? COPY : NONE;
            }
        }
    } //Table
//  /**
//   * A generator for shortcuts to names
//   */
//  private class NameSG extends ValueSG {
//
//    /** collect first letters of names */
//    Set keys(int col) {
//      
//      // check first letter of lastnames
//      Set letters = new TreeSet();
//      for (Iterator names = PropertyName.getLastNames(table.propertyModel.getGedcom(), false).iterator(); names.hasNext(); ) {
//        String name = names.next().toString();
//        if (name.length()>0) {
//          char c = name.charAt(0);
//          if (Character.isLetter(c))
//            letters.add(String.valueOf(Character.toUpperCase(c)));
//        }
//      }
//      // done
//      return letters;
//    }
//    
//    /** create item */
//    protected Property prop(String key) {
//      return new PropertyName("", key);
//    }
//  } //NameShortcutGenerator
//  
//  /**
//   * A generator for shortcuts to years
//   */
//  private class DateSG extends ShortcutGenerator {
//    /** generate */
//    void generate(int col, JComponent container) {
//      
//      // how many text lines fit on screen?
//      int visibleRows = Math.max(0, getHeight() / new LinkWidget("9999",null).getPreferredSize().height);
//      TableModel model = table.getModel();
//      int rows = model.getRowCount();
//      if (rows>visibleRows) try {
//        
//        // find all applicable years
//        Set years = new TreeSet();
//        for (int row=0;row<rows;row++) {
//          PropertyDate date = (PropertyDate)model.getValueAt(row, col);
//          if (date==null || !date.getStart().isValid())
//            continue;
//          try {
//            years.add(new Integer(date.getStart().getPointInTime(PointInTime.GREGORIAN).getYear()));
//          } catch (Throwable t) {
//          }
//        }
//        
//        // generate shortcuts for all years
//        Object[] ys = years.toArray();
//        for (int y=0; y<visibleRows; y++) {
//          int index = y<visibleRows-1  ?   (int)( y * ys.length / visibleRows)  : ys.length-1;
//          int year = ((Integer)ys[index]).intValue();
//          add(new PropertyDate(year), container);
//        }
//
//      } catch (Throwable t) {
//      }
//      
//      // done
//    }
//  } //DateShortcutGenerator
} //PropertyTableWidget
