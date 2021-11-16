/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2014 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.swing.atable;

import ancestris.core.actions.AbstractAncestrisAction;
import genj.gedcom.Property;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author daniel
 */
public class ATable extends JTable {

    /** cached collator */
    private Collator cachedCollator = null;

    private Map<TableModel, ATableRowSorter<TableModel>> sorters;
    private ATableRowSorter<TableModel> currentSorter;
    private ATableFilterWidget filterText;
    private JPanel shortcuts;
    private List<ShortCut> shortcutsList;

    public ATable() {
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setDefaultRenderer(new ATableHeaderRenderer(getTableHeader().getDefaultRenderer()));
        getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent aEvent) {
                if (currentSorter != null) {
                    int columnIdx = getColumnModel().getColumnIndexAtX(aEvent.getX());
                    if (!aEvent.isControlDown()) {
                        currentSorter.toggleSortOrder(columnIdx, true);
                    } else {
                        currentSorter.toggleSortOrder(columnIdx, false);
                    }
                }
            }
        });
    }

    @Override
    public void setModel(final TableModel tableModel) {
        super.setModel(tableModel);
        if (tableModel instanceof DefaultTableModel) {
            return;
        }
        if (sorters == null) {
            sorters = new HashMap<TableModel, ATableRowSorter<TableModel>>();
        }
        currentSorter = sorters.get(tableModel);
        if (currentSorter == null) {                                            
            currentSorter = new ATableRowSorter<TableModel>(tableModel);
            currentSorter.addRowSorterListener(new RowSorterListener() {
                @Override
                public void sorterChanged(RowSorterEvent e) {
                    // only pocess SORTED
                    if (e.getType().toString().equals("SORTED")) {
                        createShortcuts();
                    }
                }
            });
            sorters.put(tableModel, currentSorter);
            
            // 2021-06-20 FL : apparently this listener generates indexoutofbound and the createShortcuts for this lastRow does not does not appear to be of any use... (I cannot find how anyway).
            // listen to changes for generating shortcuts
//            tableModel.addTableModelListener(new TableModelListener() {
//                @Override
//                public void tableChanged(TableModelEvent e) {
//                    if (e.getLastRow() == Integer.MAX_VALUE) {
//                        createShortcuts();
//                    }
//                }
//            });
        }
        
        setRowSorter(currentSorter);

        if (filterText != null) {
            filterText.setSorter(currentSorter);
        }
    }

    public void setFilterWidget(ATableFilterWidget filter) {
        filterText = filter;
        if (filterText != null) {
            filterText.setSorter(currentSorter);
        }
    }

    public void createTableShortcut(ATable.ShortCut sc) {
        this.getInputMap(JComponent.WHEN_FOCUSED).remove(sc.key);
        this.getInputMap(JComponent.WHEN_FOCUSED).put(sc.key, sc.action);
        this.getActionMap().put(sc.action, sc.action);
    }
    
    
    public void setShortCut(JPanel panelShortcuts, List<ShortCut> shortcutsList) {
        this.shortcuts = panelShortcuts;
        this.shortcutsList = shortcutsList;
        shortcuts.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                createShortcuts();
            }
        });
    }

    /**
     * In fact csv export with ; and systematic double-quote
     * @param file File to create
     * @throws IOException If there is a trouble with the file
     */
    public void csvExport(File file) throws IOException {
        TableModel model = getModel();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            for (int i = 0; i < model.getColumnCount(); i++) {
                writer.write("\""+ model.getColumnName(i) + "\";");
            }
            
            writer.write("\n");
            
            for (int r = 0; r < currentSorter.getViewRowCount(); r++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    writer.write("\"");
                    writer.write(exportCellValue(model.getValueAt(convertRowIndexToModel(r), col), r, col));
                    writer.write("\";");
                }
                writer.write("\n");
            }
        }
    }

    /**
     * convert a cell content to a string for export functionnality.
     * Default convert object to string using toString.
     *
     * @param object
     * @param row
     * @param col
     *
     * @return
     */
    public String exportCellValue(Object object, int row, int col) {
        if (object == null) {
            return "";
        }
        return object.toString();
    }

    /** create a shortcut */
    AbstractAncestrisAction createShortcut(String txt, final int y) {
        return new AbstractAncestrisAction(txt.toUpperCase()) {

            @Override
            @SuppressWarnings("empty-statement")
            public void actionPerformed(ActionEvent event) {
                int x = 0;
                try {
                    x = ((JViewport) getParent()).getViewPosition().x;
                } catch (Throwable t) {
                };
                scrollRectToVisible(new Rectangle(x, y, 1, getParent().getHeight()));
            }
        };
    }

    /**
     * Return an appropriate Collator instance
     */
    // From Gedcom class
    private Collator getCollator() {

        // not known?
        if (cachedCollator == null) {
            cachedCollator = Collator.getInstance(getLocale());

            // 20050505 when comparing gedcom values we really don't want it to be
            // done case sensitive. It surfaces in many places (namely for example
            // in prefix matching in PropertyTableWidget) so I'm restricting comparison
            // criterias to PRIMARY from now on
            cachedCollator.setStrength(Collator.PRIMARY);
        }

        // done
        return cachedCollator;
    }

    /** generate */
    void createShortcuts(int col, SortOrder dir, JComponent container) {

//            if (propertyModel == null || container.getHeight() == 0) {
//                return;
//            }
//
        TableModel model = getModel();
//            Collator collator = propertyModel.getGedcom().getCollator();
        Collator collator = getCollator();

        // loop over rows and create actions
        List<AbstractAncestrisAction> actions = new ArrayList<AbstractAncestrisAction>(3);

        InputMap imap = container.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap amap = container.getActionMap();
        
        String cursor = "";
        for (int r = 0; r < currentSorter.getViewRowCount(); r++) {
            int vr = (dir == SortOrder.ASCENDING ? r : currentSorter.getViewRowCount() - r - 1);
            // FIXME: try not to use Property here
            Property prop = (Property) model.getValueAt(convertRowIndexToModel(vr), col);
            if (prop == null) {
                continue;
            }
            String value = prop.getComparator().getSortGroup(prop);
            if (value.length() == 0) {
                continue;
            }

            if (collator.compare(cursor, value) >= 0) {
                continue;
            }
            cursor = value;

            // action
            AbstractAncestrisAction action = createShortcut(value, getCellRect(vr, col, true).y);
            actions.add(action);

            // key binding
            imap.put(KeyStroke.getKeyStroke(value.charAt(0)), action);
            imap.put(KeyStroke.getKeyStroke(value.toLowerCase().charAt(0)), action);
            amap.put(action, action);
        }
        
        for (ShortCut sc : shortcutsList) {
            imap.put(sc.key, sc.action);
            amap.put(sc.action, sc.action);
        }

        // generate buttons
        if (actions.isEmpty()) {
            return;
        }

        int w = LinkWidget.sampleDimension(actions.get(actions.size() / 2).getText().length()).width;
        int n = Math.min(actions.size(), (container.getSize().width) / w - 1);
        for (int i = 0; i < n; i++) {
            LinkWidget link = new LinkWidget(actions.get(i * actions.size() / n));
            container.add(link);
        }

        if (n < actions.size()) {
            LinkWidget link = new LinkWidget(actions.get(actions.size() - 1));
            container.add(link);
        }
        // done
    }

    /**
     * Create shortcuts
     */
    public void createShortcuts() {

        if (shortcuts == null) {
            return;
        }
        // remove old shortcuts
        shortcuts.removeAll();
        shortcuts.getInputMap(WHEN_IN_FOCUSED_WINDOW).clear();
        shortcuts.getActionMap().clear();
        shortcuts.revalidate();
        shortcuts.repaint();

        // anything we can offer? need ascending sorted column and at least 10 rows
        if (currentSorter != null && !currentSorter.getSortKeys().isEmpty()) {
            SortKey prim = currentSorter.getSortKeys().get(0);

            if (prim.getSortOrder() == SortOrder.UNSORTED) {
                return;
            }
            createShortcuts(prim.getColumn(), prim.getSortOrder(), shortcuts);
        }
        // done
    }

    public ATableRowSorter<TableModel> getSorter() {
        return currentSorter;
    }

    public static class ShortCut {
        private KeyStroke key;
        private AbstractAncestrisAction action;
        
        public ShortCut(KeyStroke key, AbstractAncestrisAction action) {
            this.key = key;
            this.action = action;
        }
    }

    
    private static class LinkWidget extends JButton {

        private static Map<String, Dimension> sd = new HashMap<String, Dimension>(3);

        /**
         * Creates a button with initial text and an icon.
         *
         * @param text the text of the button
         * @param icon the Icon image to display on the button
         */
        public LinkWidget(String text, Icon icon) {
            super(text, icon);
            init();
        }

        public LinkWidget(String text) {
            this(text, null);
        }

        public LinkWidget(Action a) {
            super(a);
            init();
        }

        private void init() {
            setBorderPainted(false);
            //setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
            setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8));
        }

        public static Dimension sampleDimension() {
            return sampleDimension("W");
        }

        public static Dimension sampleDimension(int size) {
            StringBuilder outputBuffer = new StringBuilder(size);
            outputBuffer.append("W");
            for (int i = 1; i < size; i++) {
                outputBuffer.append("0");
            }
            return sampleDimension(outputBuffer.toString());
        }

        public static Dimension sampleDimension(String label) {
            if (!sd.containsKey(label)) {
                JButton sample = new JButton(label);
                sample.setBorderPainted(false);
                //sample.setFont(new java.awt.Font("DejaVu Sans", 0, 11)); // NOI18N
                sample.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 8, 6, 8));
                sd.put(label, sample.getPreferredSize());
            }
            return sd.get(label);
        }

    }
}
