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
package ancestris.util.swing;

/**
 * Copied for netbeans plateforme ETabelHeader and modified to support TableRowFilter.
 * See also: http://www.jroller.com/nweber/entry/multi_column_sorting_w_mustang
 * @author daniel
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.LabelUI;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * The ETable header renderer.
 * Delegating to the original default renderer should assure L&F compliance.
 * The rendering logic moved from {@link ETableColumn}.ATableHeaderRenderer.
 * 
 * @author Martin Entlicher
 */
            
    /**
     * Special renderer painting sorting icons and also special icon
     * for the QuickFilter columns.
     */
    public class ATableHeaderRenderer extends DefaultTableCellRenderer implements UIResource {
        
        private TableCellRenderer headerRendererUI;
//        private Map<ETableColumn, TableCellRenderer> defaultColumnHeaderRenderers = new HashMap<ETableColumn, TableCellRenderer>();

    public ATableHeaderRenderer() {
        this(null);
    }

        
        public ATableHeaderRenderer(TableCellRenderer headerRenderer) {
            this.headerRendererUI = headerRenderer;
        }

        @Override
        public void setBorder(Border border) {
            super.setBorder(border);
            if (headerRendererUI instanceof JComponent) {
                ((JComponent) headerRendererUI).setBorder(border);
            }
        }
        
        /**
         * Get the table header renderer for the particular column.
         * If the column is {@link ETableColumn}, check createDefaultHeaderRenderer()
         * method. If it's not overridden, use the current header renderer.
         * If it is overridden, set the current header renderer to the base
         * column's renderer. If the overridden renderer delegates to the original,
         * it will get this header renderer ({@link #headerRendererUI}).
         * @param tc The column
         * @return the renderer
         */
//        private TableCellRenderer getColumnHeaderRenderer(TableColumn tc) {
//            if (tc instanceof ETableColumn) {
//                ETableColumn eColumn = (ETableColumn) tc;
//                TableCellRenderer columnHeaderRenderer;
//                if (!defaultColumnHeaderRenderers.containsKey(eColumn)) {
//                    TableCellRenderer tcr = eColumn.createDefaultHeaderRenderer();
//                    if (tcr instanceof ETableColumn.ETableColumnHeaderRendererDelegate) {
//                        defaultColumnHeaderRenderers.put(eColumn, null);
//                        columnHeaderRenderer = null;
//                    } else {
//                        eColumn.setTableHeaderRendererDelegate(headerRendererUI);
//                        columnHeaderRenderer = tcr;
//                    }
//                } else {
//                    columnHeaderRenderer = defaultColumnHeaderRenderers.get(eColumn);
//                }
//                if (columnHeaderRenderer != null) {
//                    return columnHeaderRenderer;
//                } else {
//                    return headerRendererUI;
//                }
//            } else {
//                return headerRendererUI;
//            }
//        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
//            TableColumn tcm = table.getColumnModel().getColumn(table.convertColumnIndexToModel(column));
            
            Component resUI;
            if (headerRendererUI==null) 
                resUI = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            else
                resUI = headerRendererUI.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (resUI instanceof JLabel) {
                JLabel label = (JLabel)resUI;
                LabelUI lui = label.getUI();
                lui.installUI(label);

                String valueString = "";
                if (value != null) {
                    valueString = value.toString();
                }
//                if (table instanceof ETable) {
//                    ETable et = (ETable)table;
//                    valueString = et.getColumnDisplayName(valueString);
//                }
                Icon sortIcon = null;

                List<TableColumn> sortedColumns = getSortedColumns(table);
                
                SortDesc sort = getSortOrder(table, column);
                int sortRank = sort==null?0:sort.rank;
                boolean ascending = true;
                if (sort != null && sort.order == SortOrder.DESCENDING)
                    ascending = false;
                Icon customIcon = null;
//                if (tcm instanceof ETableColumn) {
//                    ETableColumn eColumn = (ETableColumn) tcm;
//                    sortRank = eColumn.getSortRank();
//                    ascending = eColumn.isAscending();
//                    customIcon = eColumn.getCustomIcon();
//                }

                if (sortRank != 0) {
                    if (sortedColumns.size () > 1) {
                        valueString = (valueString == null || valueString.isEmpty()) ?
                            Integer.toString(sortRank) :
                            sortRank+" "+valueString;
                    }
                    // don't use deriveFont() - see #49973 for details
                    label.setFont (new Font (getFont ().getName (), Font.BOLD, getFont ().getSize ()));

                    if (ascending) {
                        sortIcon = UIManager.getIcon("TableHeaderUI.ascendingIcon");
                        if (sortIcon == null) {
                            sortIcon = new SortUpIcon();
                        }
                    } else {
                        sortIcon = UIManager.getIcon("TableHeaderUI.descendingIcon");
                        if (sortIcon == null) {
                            sortIcon = new SortDownIcon();
                        }
                    }
                }
                label.setText(valueString);
                if (sortIcon == null) {
                    if (customIcon == null) {
                        Icon dummy = new Icon() {
                                @Override
                            public void paintIcon(Component c, Graphics g, int x, int y) {
                            }
                                @Override
                            public int getIconWidth() {
                                return 0;
                            }
                                @Override
                            public int getIconHeight() {
                                return 0;
                            }
                        };
                        label.setIcon(dummy);
                    } else {
                        label.setIcon(customIcon);
                    }
                } else {
                    if (customIcon == null) {
                        label.setIcon(sortIcon);
                    } else {
                        label.setIcon(mergeIcons(customIcon, sortIcon, 16, 0, this));
                    }
                }
            }
            return resUI;
        }
        
        private List<TableColumn> getSortedColumns(JTable table){
            List<TableColumn> result = new ArrayList<TableColumn>(3);
            List<? extends RowSorter.SortKey> sortKeys = null;
            if (table.getRowSorter()!=null)
                sortKeys = table.getRowSorter().getSortKeys();

            if (sortKeys == null || sortKeys.isEmpty())
                return result;

            for (RowSorter.SortKey sortKey : sortKeys)
            {
                result.add(table.getColumnModel().getColumn(sortKey.getColumn()));
            }

            return result;
        }
        
    private SortDesc getSortOrder(JTable table, int column)
    {
            List<? extends RowSorter.SortKey> sortKeys = null;
            if (table.getRowSorter()!=null)
                sortKeys = table.getRowSorter().getSortKeys();
        if (sortKeys == null || sortKeys.isEmpty())
            return null;

        int rank = 1;
        for (RowSorter.SortKey sortKey : sortKeys)
        {
            if (sortKey.getColumn() == column)
            {
                return new SortDesc(rank, sortKey.getSortOrder());
            }

            rank++;
        }

        return null;
    }
private static class SortDesc{
    private int rank = 0;
    private Enum<SortOrder> order = null;

        public SortDesc(int rank, Enum<SortOrder> order) {
            this.order = order;
            this.rank = rank;
        }
    
}
            
/**
     * An icon pointing down. It is used if the LAF does not supply
     * special icon.
     */
    private static class SortDownIcon implements Icon {

        public SortDownIcon() {
        }

        @Override
        public int getIconWidth() {
            return 8;
        }

        @Override
        public int getIconHeight() {
            return 8;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.BLACK);
            g.drawLine(x    , y + 2, x + 8, y + 2);
            g.drawLine(x    , y + 2, x + 4, y + 6);
            g.drawLine(x + 8, y + 2, x + 4, y + 6);
        }
    }

    /**
     * An icon pointing up. It is used if the LAF does not supply
     * special icon.
     */
    private static class SortUpIcon implements Icon {

        public SortUpIcon() {
        }

        @Override
        public int getIconWidth() {
            return 8;
        }

        @Override
        public int getIconHeight() {
            return 8;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.BLACK);
            g.drawLine(x    , y + 6, x + 8, y + 6);
            g.drawLine(x    , y + 6, x + 4, y + 2);
            g.drawLine(x + 8, y + 6, x + 4, y + 2);
        }
    }

    /**
     * Utility method merging 2 icons.
     */
    private static Icon mergeIcons(Icon icon1, Icon icon2, int x, int y, Component c) {
        int w = 0, h = 0;
        if (icon1 != null) {
            w = icon1.getIconWidth();
            h = icon1.getIconHeight();
        }
        if (icon2 != null) {
            w = icon2.getIconWidth()  + x > w ? icon2.getIconWidth()   + x : w;
            h = icon2.getIconHeight() + y > h ? icon2.getIconHeight()  + y : h;
        }
        if (w < 1) w = 16;
        if (h < 1) h = 16;
        
        java.awt.image.ColorModel model = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment ().
                                          getDefaultScreenDevice ().getDefaultConfiguration ().
                                          getColorModel (java.awt.Transparency.BITMASK);
        java.awt.image.BufferedImage buffImage = new java.awt.image.BufferedImage (model,
             model.createCompatibleWritableRaster (w, h), model.isAlphaPremultiplied (), null);
        
        java.awt.Graphics g = buffImage.createGraphics ();
        if (icon1 != null) {
            icon1.paintIcon(c, g, 0, 0);
        }
        if (icon2 != null) {
            icon2.paintIcon(c, g, x, y);
        }
        g.dispose();
        
        return new ImageIcon(buffImage);
    }
}
