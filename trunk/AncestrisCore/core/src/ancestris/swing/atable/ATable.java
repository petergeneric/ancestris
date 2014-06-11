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
import genj.gedcom.PropertyDate;
import genj.gedcom.PropertyName;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 *
 * @author daniel
 */
public class ATable extends JTable {

    /** cached collator */
    private Collator cachedCollator = null;

    private ATableRowSorter<TableModel> sorter;
    private ATableFilterWidget filterText;
    private JPanel shortcuts;

    public ATable() {
        getTableHeader().setReorderingAllowed(false);
        getTableHeader().setDefaultRenderer(new ATableHeaderRenderer(getTableHeader().getDefaultRenderer()));
        getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent aEvent) {
                if (sorter != null) {
                    int columnIdx = getColumnModel().getColumnIndexAtX(aEvent.getX());
                    if (!aEvent.isControlDown()) {
                        sorter.toggleSortOrder(columnIdx, true);
                    } else {
                        sorter.toggleSortOrder(columnIdx, false);
                    }
                }
            }
        });
    }

    public void setFilterWidget(ATableFilterWidget filter) {
        filterText = filter;
        if (filterText != null) {
            filterText.setSorter(sorter);
        }
    }

    public void setShortCut(JPanel panelShortcuts) {
        this.shortcuts = panelShortcuts;
        shortcuts.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                createShortcuts();
            }
        });
    }

    @Override
    public void setModel(TableModel tableModel) {
        super.setModel(tableModel); //To change body of generated methods, choose Tools | Templates.
        sorter = new ATableRowSorter<TableModel>(tableModel);
        sorter.addRowSorterListener(new RowSorterListener() {

            @Override
            public void sorterChanged(RowSorterEvent e) {
                createShortcuts();
            }
        });
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
        List<AbstractAncestrisAction> actions = new ArrayList<AbstractAncestrisAction>(26);

        String cursor = "";
        for (int r = 0; r < sorter.getViewRowCount(); r++) {
            int vr = (dir == SortOrder.ASCENDING ? r : sorter.getViewRowCount() - r - 1);
            // FIXME: try not to use Property here
            Property prop = (Property) model.getValueAt(convertRowIndexToModel(vr), col);
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
            AbstractAncestrisAction action = createShortcut(value, getCellRect(vr, col, true).y);
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

        int w = LinkWidget.sampleDimension().width;
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
        if (sorter != null && !sorter.getSortKeys().isEmpty()) {
            SortKey prim = sorter.getSortKeys().get(0);

            if (prim.getSortOrder() == SortOrder.UNSORTED) {
                return;
            }
            createShortcuts(prim.getColumn(), prim.getSortOrder(), shortcuts);
        }
        // done
    }

    public ATableRowSorter<TableModel> getSorter() {
        return sorter;
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
            setAlignmentX(RIGHT_ALIGNMENT);
        }

        public static Dimension sampleDimension() {
            return sampleDimension("W");
        }

        public static Dimension sampleDimension(String label) {
            if (!sd.containsKey(label)) {
                JButton sample = new JButton(label);
                sample.setBorderPainted(false);
                sd.put(label, sample.getPreferredSize());
            }
            return sd.get(label);
        }

    }
}
