package ancestris.modules.editors.placeeditor.topcomponents;

import static ancestris.core.beans.ConfirmChangeWidget.getAutoCommit;
import ancestris.swing.atable.ATableHeaderRenderer;
import ancestris.swing.atable.ATableRowSorter;
import genj.gedcom.Gedcom;
import genj.util.ChangeSupport;
import genj.util.Registry;
import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.TextAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.windows.WindowManager;

/**
 *
 * @author dominique
 * @author frederic (performance issues of sort with over 2000 locations, multi columns sorting, editable, DND)
 */
public class EditorTable extends JTable implements FocusListener {

    private Registry registry = null;
    private String mTableId = null;
    private ATableRowSorter<TableModel> sorter;
    private boolean dragingColumnCompleted = false;
    private int fromIndex = -1, toIndex = -1;

    private List<Action> actions = new ArrayList<>();

    protected ChangeSupport changes = new ChangeSupport(this);

    // CCP elements
    private String Copy_or_Move_Action = "";
    private boolean pendingPaste = false;
    private int[] rowsSelectedAtExport = null;
    private int[] colsSelectedAtExport = null;
    

    /**
     * Constructor
     */
    public EditorTable() {
        super();

        // Enable CCP actions
        addAction(new CutAction(), KeyEvent.VK_X, DefaultEditorKit.cutAction, new ImageIcon(ImageUtilities.loadImage("org/openide/resources/actions/cut.gif")));
        addAction(new CopyAction(), KeyEvent.VK_C, DefaultEditorKit.copyAction, new ImageIcon(ImageUtilities.loadImage("org/openide/resources/actions/copy.gif")));
        addAction(new PasteAction(), KeyEvent.VK_V, DefaultEditorKit.pasteAction, new ImageIcon(ImageUtilities.loadImage("org/openide/resources/actions/paste.gif")));

        // Enable DND
        setDragEnabled(true);
        setDropMode(DropMode.ON);
        setTransferHandler(new CellsTransferHandler(this));
        addPropertyChangeListener("dropLocation", new Repainter());
        setDefaultRenderer(Object.class, new MyTableCellRenderer());
    }

    private void addAction(TextAction action, int key, String text, ImageIcon icon) {
        action.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(key, InputEvent.CTRL_DOWN_MASK));
        action.putValue(AbstractAction.NAME, NbBundle.getMessage(getClass(), text));
        action.putValue(AbstractAction.SMALL_ICON, icon);
        getActionMap().put(text, action);
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(key, InputEvent.CTRL_DOWN_MASK), text);
        actions.add(action);
    }

    public List<Action> getActions() {
        return actions;
    }

    /**
     * Initialize table
     *
     * @param gedcom
     * @param tableId
     * @param selectedColumn
     */
    public void setID(Gedcom gedcom, String tableId, int selectedColumn) {

        // Set columns order
        registry = gedcom.getRegistry();
        mTableId = tableId;
        setColumnOrder();
        
        // Set columns width to past memorisation
        if (selectedColumn < 0 || selectedColumn > columnModel.getColumnCount()) {
            selectedColumn = 0;
        }
        for (int index = 0; index < columnModel.getColumnCount(); index++) {
            int columnSize = registry.get(mTableId + ".column" + index + ".size", 100);
            columnModel.getColumn(index).setPreferredWidth(columnSize);
        }

        // Set sorter, listen and memorize changes
        // - for sorting : FL: 2016-02-28 : for some unknown reason, default row sorter sorts strings excluding spaces... We also need to sort case insensitive
        final Collator collator = gedcom.getCollator(); // case insensitive and local characters accounted for (but spaces ignored ???)
        Comparator strComparator = new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return collator.compare(o1.toString().replace(" ", "!"), o2.toString().replace(" ", "!"));   // fix space comparison ("!" is next to "space" in ASCII table)
            }
        };
        sorter = new ATableRowSorter<>(getModel());
        for (int c = 0; c < getColumnModel().getColumnCount(); c++) {
            sorter.setComparator(c, strComparator);
        }
        setRowSorter(sorter);

        getTableHeader().setDefaultRenderer(new ATableHeaderRenderer(getTableHeader().getDefaultRenderer())); // set renderer for multi column sorting
        getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent aEvent) {
                if (sorter != null) {
                    int columnIdx = convertColumnIndexToModel(getColumnModel().getColumnIndexAtX(aEvent.getX()));
                    if (!aEvent.isControlDown()) {
                        sorter.toggleSortOrder(columnIdx, true);
                    } else {
                        sorter.toggleSortOrder(columnIdx, false);
                    }
                    registry.put("placeTableSortOrder", getSortOrder());
                }
            }

        });

        // Set sort on selectedColumn first, then all other columns, else set to last memorisation
        String sortOrder = registry.get("placeTableSortOrder", "");
        List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
        sorter.setMaxSortKeys(getColumnCount());
        if (sortOrder.isEmpty()) {
            sortKeys.add(new RowSorter.SortKey(selectedColumn, SortOrder.ASCENDING));
            for (int c = 0; c < getColumnCount(); c++) {
                if (c != selectedColumn) {
                    sortKeys.add(new RowSorter.SortKey(c, SortOrder.ASCENDING));
                }
            }
        } else {
            sortKeys = setSortOrder(sortOrder);
        }
        sorter.setSortKeys(sortKeys);
        sorter.sort();

        // Listen to column changes
        getColumnModel().addColumnModelListener(new EditorTableTableColumnModelListener());
        getTableHeader().setReorderingAllowed(true);
        getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {   // catch when move is finished
                if (dragingColumnCompleted && fromIndex != toIndex) {
                    saveColumnOrder();
                    // TODO ? process data move
                    //changes.setChanged(true);
                }
                fromIndex = -1;
                dragingColumnCompleted = false;
            }
        });

        // Set editor
        getModel().addTableModelListener((TableModelEvent e) -> {
            changes.setChanged(true);
            if (getAutoCommit()) {
                WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                    @Override
                    public void run() {
                        changes.stateChanged(new ChangeEvent(true));
                    }
                });
            }
            
        });
        changes.setChanged(false);

    }
    
    public void saveColumnOrder() {
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            registry.put(mTableId+".column"+i+".pos", convertColumnIndexToModel(i));
        }
    }
    
    public void setColumnOrder() {
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.moveColumn(convertColumnIndexToView(registry.get(mTableId+".column"+i+".pos", i)), i);
        }
    }

    private String getSortOrder() {
        List<SortKey> keys = new ArrayList<RowSorter.SortKey>(sorter.getSortKeys());
        String ret = "";
        for (SortKey key : keys) {
            ret += key.getColumn() + "," + key.getSortOrder().name() + ";";
        }
        return ret;
    }

    private List<SortKey> setSortOrder(String sortOrder) {
        List<SortKey> keys = new ArrayList<RowSorter.SortKey>(sorter.getSortKeys());
        String[] keyBits = sortOrder.split(";");
        for (String keyPairs : keyBits) {
            String[] keyBits2 = keyPairs.split(",");
            if (keyBits2.length == 2) {
                keys.add(new RowSorter.SortKey(Integer.valueOf(keyBits2[0]), SortOrder.valueOf(keyBits2[1])));
            }
        }

        return keys;
    }

    public TableRowSorter<TableModel> getSorter() {
        return sorter;
    }

    /**
     * Listen to edit changes for TopComponent
     * @param listener 
     */
    public void addChangeListener(ChangeListener listener) {
        changes.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changes.removeChangeListener(listener);
    }

    
    
    /**
     * Enable menu bar CCP buttons
     *
     * @param e
     */
    @Override
    public void focusGained(FocusEvent e) {
        for (Action a : actions) {
            a.setEnabled(true);
        }
    }

    /**
     * Disable menu bar CCP buttons
     *
     * @param e
     */
    @Override
    public void focusLost(FocusEvent e) {
        for (Action a : actions) {
            a.setEnabled(false);
        }
    }


    
    /**
     * Actions
     */
    private class CutAction extends DefaultEditorKit.CutAction {

        public CutAction() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (getSelectedRow() > -1) {
                Clipboard clipboard = getClipboard();
                clipboard.setContents(new CellsData(exportData()), null);
                setpendingPaste(true);
                Copy_or_Move_Action = DefaultEditorKit.cutAction;
            }
        }
    }

    private class CopyAction extends DefaultEditorKit.CopyAction {

        public CopyAction() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (getSelectedRow() > -1) {
                Clipboard clipboard = getClipboard();
                clipboard.setContents(new CellsData(exportData()), null);
                setpendingPaste(true);
                Copy_or_Move_Action = DefaultEditorKit.copyAction;
            }
        }

    }

    private class PasteAction extends DefaultEditorKit.PasteAction {

        public PasteAction() {
            super();
            
            Clipboard clipboard = getClipboard();
            clipboard.addFlavorListener((FlavorEvent e) -> {
                setEnabled(clipboard.isDataFlavorAvailable(CellsData.CELL_DATA_FLAVOR));
            });
            setEnabled(clipboard.isDataFlavorAvailable(CellsData.CELL_DATA_FLAVOR));
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            Clipboard clipboard = getClipboard();
            if (clipboard.isDataFlavorAvailable(CellsData.CELL_DATA_FLAVOR)) {
                try {
                    if (Copy_or_Move_Action.equals(DefaultEditorKit.cutAction)) {
                        eraseSelection();
                    }
                    Object[][] data = (Object[][]) clipboard.getData(CellsData.CELL_DATA_FLAVOR);
                    importData(data, getSelectedRows(), getSelectedColumns());

                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    static public Clipboard getClipboard() {
        Clipboard c = Lookup.getDefault().lookup(ExClipboard.class);
        if (c == null) {
            c = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        }
        return c;
    }


    
    /**
     * Put selected cells into data array
     * 
     * @return data
     */
    public Object[][] exportData() {
        
        rowsSelectedAtExport = getSelectedRows();
        colsSelectedAtExport = getSelectedColumns();
        
        int rows[] = rowsSelectedAtExport;
        int cols[] = colsSelectedAtExport;
        
        int dimRow = rows[rows.length - 1] - rows[0] + 1;
        int dimCol = cols[cols.length - 1] - cols[0] + 1;
        
        // Init the whole object to null (= no value)
        Object[][] data = new Object[dimRow][dimCol];
        for (int i = 0; i < dimRow ; i++) {
            for (int j = 0; j < dimCol ; j++) {
                data[i][j] = null;
            }
        }
        
        // Replace only with selected values where it should
        for (int i = 0; i < rows.length ; i++) {
            for (int j = 0; j < cols.length ; j++) {
                data[rows[i]-rows[0]][cols[j]-cols[0]] = getValueAt(rows[i], cols[j]);
            }
        }
                
        return data;
    }
    
    /**
     * Paste data values to selected area (for drop, it is just a transalation of the data ; for paste, the selected area might be different)
     * And update selection to reflect pasted area
     * 
     * @param data
     * @param rows, cols : selected rows and cols to paste data into
     */
    public void importData(Object[][] data, int[] rows, int[] cols) {

        int maxRow = data.length;
        int maxCol = data[0].length;

        // Paste policy depends on selected cells (1 or many):
        // If many, content is spread throughout the whole selection and *limited* to the selection
        // - if selected range is greater than content, limit paste to size of selection
        // - if selected range is smaller than content, rollover content to extend to selection
        // If one cell selected, it is assumed to be the top left corner paste and the whole data is pasted in the limit of the table
        int r=0, c=0;
        clearSelection();
        if (rows.length + cols.length <= 2) {
            for (int i = 0; i < maxRow ; i++) {
                for (int j = 0; j < maxCol ; j++) {
                    if (rows[0]+i < getRowCount() && cols[0]+j < getColumnCount()) {
                        r = rows[0]+i;
                        c = cols[0]+j;
                        setValueAt(data[i][j], r, c);
                    }
                }
            }
        } else {
            for (int i = 0; i < rows.length ; i++) {
                for (int j = 0; j < cols.length ; j++) {
                    setValueAt(data[i % maxRow][j % maxCol], rows[i], cols[j]);
                }
            }
        }
        
        // Select translated rows x cols from selection at export
        selectDraggedCells(rows[0], cols[0]);
        resetPendingPaste();
    }
    
    
    public void eraseSelection() {
        
        int[] rows = rowsSelectedAtExport;
        int[] cols = colsSelectedAtExport;
        for (int i = 0; i < rows.length ; i++) {
            for (int j = 0; j < cols.length ; j++) {
                setValueAt("", rows[i], cols[j]);
            }
        }
    }
    
    
    /**
     * DropLocation listener
     */
    private class Repainter implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            DropLocation oldLoc = (DropLocation) evt.getOldValue();
            DropLocation newLoc = (DropLocation) evt.getNewValue();
            if (oldLoc != null && newLoc != null && (newLoc.getRow() != oldLoc.getRow() || newLoc.getColumn() != oldLoc.getColumn())) {
                selectDraggedCells(newLoc.getRow(), newLoc.getColumn());
            }
        }
    }

    private void selectDraggedCells(int row, int column) {
        clearSelection();
        int[] sRows = rowsSelectedAtExport;
        int[] sCols = colsSelectedAtExport;
        if (sRows == null || sCols == null) {
            return;
        }
        int r = 0, c = 0;
        for (int i = 0; i < sRows.length ; i++) {
            for (int j = 0; j < sCols.length ; j++) {
                r = row + sRows[i] - sRows[0];
                c = column + sCols[j] - sCols[0];
                if (r < getRowCount()) {
                    addRowSelectionInterval(r, r);
                }
                if (c < getColumnCount()) {
                    addColumnSelectionInterval(c, c);
                }
            }
        }
    }
    
    public void setpendingPaste(boolean setTimer) {
        pendingPaste = true;
        repaint();
        
        // reset after 12 seconds in case of copy or cut pending
        if (setTimer) {
            Timer timer = new Timer();

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    resetPendingPaste();
                }
            }, 12 * 1000);
        }
    }

    public void resetPendingPaste() {
        pendingPaste = false;
        repaint();
    }

    private boolean isDraggedCell(int row, int column) {
        int[] rows = rowsSelectedAtExport;
        int[] cols = colsSelectedAtExport;
        
        // Fail if not drag in progress
        if (rows == null || cols == null) {
            return false;
        }
        
        // Otherwise check
        for (int i = 0; i < rows.length ; i++) {
            for (int j = 0; j < cols.length ; j++) {
                if (rows[i] == row && cols[j] == column) {
                    return true;
                }
            }
        }
        
        return false;
    }

    /**
     * Renderer for rendering differently Drag origin cells
     */
    private class MyTableCellRenderer extends DefaultTableCellRenderer {

        private Color color;
        
        public MyTableCellRenderer() {
            color = new Color(UIManager.getColor ("Table.background").getRGB());
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (pendingPaste && isDraggedCell(row, column)) {
                c.setBackground(new Color(255, 166, 77));  // light orange
            } else {
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground());
                } else {
                    c.setBackground(color);
                }
            }
            
            return c;
        }
        
        
    }

    
    
    /**
     * Detect column changes
     */
    private class EditorTableTableColumnModelListener implements TableColumnModelListener {

        @Override
        public void columnAdded(TableColumnModelEvent tcme) {
        }

        @Override
        public void columnRemoved(TableColumnModelEvent tcme) {
        }

        @Override
        public void columnMoved(TableColumnModelEvent e) { // called many times during the move
            if (fromIndex == -1) {
                fromIndex = e.getFromIndex();
            }
            toIndex = e.getToIndex();
            dragingColumnCompleted = true;
        }

        @Override
        public void columnMarginChanged(ChangeEvent ce) {
            for (int index = 0; index < columnModel.getColumnCount(); index++) {
                registry.put(mTableId + ".column" + index + ".size", columnModel.getColumn(index).getPreferredWidth());
            }
        }

        @Override
        public void columnSelectionChanged(ListSelectionEvent lse) {
        }
    }

}
