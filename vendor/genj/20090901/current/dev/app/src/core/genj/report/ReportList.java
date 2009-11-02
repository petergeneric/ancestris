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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package genj.report;

import genj.util.Registry;
import genj.util.Resources;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Report list capable of displaying the report list in two formats.
 * Either it is a list of all reports sorted alphabetically or it is a tree
 * with reports within their categories.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 */
/*package*/ class ReportList extends JScrollPane
{
    public static final int VIEW_LIST = 0;
    public static final int VIEW_TREE = 1;

    /**
     * All the reports.
     */
    private Report[] reports;

    /**
     * The tree component.
     */
    private JTree tree = null;

    /**
     * View type: VIEW_LIST or VIEW_TREE.
     */
    private int viewType;

    /**
     * Currently selected report. null if none selected.
     */
    private Report selection = null;

    /**
     * Object with callback functions.
     */
    private Callback callback = new Callback();

    /**
     * Listener for changes in the currently selected report.
     */
    private ReportSelectionListener selectionListener = null;

    /**
     * Data model for displaying grouped reports.
     */
    private TreeModel treeModel = null;

    /**
     * Data model for displaying reports in a list (not grouped).
     */
    private TreeModel listModel = null;

    /**
     * Registry for storing configuration.
     */
    private Registry registry;

    /**
     * Language resources.
     */
    private static final Resources RESOURCES = Resources.get(ReportView.class);

    /**
     * Creates the component.
     * @param reports   all reports
     * @param viewType  view type
     * @param registry  configuration registry
     */
    public ReportList(Report[] reports, int viewType, Registry registry) {
        this.reports = reports;
        this.viewType = viewType;
        this.registry = registry;

        tree = new JTree();
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setCellRenderer(callback);
        tree.addTreeSelectionListener(callback);
        tree.addTreeExpansionListener(callback);
        tree.setRootVisible(false);
        setViewportView(tree);

        refreshView();
    }

    /**
     * Sets the view type.
     */
    public void setViewType(int viewType) {
        this.viewType = viewType;
        refreshView();
    }

    /**
     * Returns the current view type.
     */
    public int getViewType() {
        return viewType;
    }

    /**
     * Sets the given report as selected.
     */
    public void setSelection(Report report) {
        selection = report;
        if (selection == null) {
            tree.clearSelection();
        } else {
            for (int i = 0; i < tree.getRowCount(); i++) {
                TreePath path = tree.getPathForRow(i);
                Object v = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
                if (v == selection) {
                    tree.addSelectionPath(path);
                    tree.makeVisible(path);
                    break;
                }
            }
        }
    }

    /**
     * Returns the currently selected report.
     */
    public Report getSelection() {
        return selection;
    }

    /**
     * Sets the selection listener.
     */
    public void setSelectionListener(ReportSelectionListener listener) {
        selectionListener = listener;
    }

    /**
     * Sets a new list of reports.
     */
    public void setReports(Report[] reports) {
        this.reports = reports;
        listModel = null;
        treeModel = null;
        refreshView();
    }

    /**
     * Refreshes the view, possibly changing the view type.
     * Changing view types doesn't change the currently selected report.
     */
    private void refreshView() {
        Report oldSelection = getSelection();
        if (viewType == VIEW_LIST) {
            if (listModel == null)
                listModel = createList();
            tree.setModel(listModel);
        } else {
            if (treeModel == null)
                treeModel = createTree();
            tree.setModel(treeModel);
            refreshExpanded();
        }
        setSelection(oldSelection);
    }

    /**
     * Expands the groups which are configured in the registry to be expanded.
     */
    private void refreshExpanded() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            TreePath path = tree.getPathForRow(i);
            Object v = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
            if (v instanceof Report.Category) {
                Report.Category category = (Report.Category)v;
                if (registry.get("expanded." + category.getName(), true))
                    tree.expandPath(path);
                else
                    tree.collapsePath(path);
            }
        }
    }

    /**
     * Creates the list data model.
     */
    private TreeModel createList() {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode();
        for (int i = 0; i < reports.length; i++)
            top.add(new DefaultMutableTreeNode(reports[i]));
        return new DefaultTreeModel(top);
    }

    /**
     * Creates the group tree data model.
     */
    private TreeModel createTree() {
        SortedMap categories = new TreeMap();
        for (int i = 0; i < reports.length; i++) {
            String name = getCategoryText(reports[i].getCategory());
            CategoryList list = (CategoryList)categories.get(name);
            if (list == null) {
                list = new CategoryList(reports[i].getCategory());
                categories.put(name, list);
            }
            list.add(reports[i]);
        }

        DefaultMutableTreeNode top = new DefaultMutableTreeNode();
        Iterator iterator = categories.entrySet().iterator();
        while (iterator.hasNext()) {
            CategoryList list = (CategoryList)((Map.Entry)iterator.next()).getValue();
            DefaultMutableTreeNode cat = new DefaultMutableTreeNode(list.getCategory());
            Report[] reps = list.getReportsInCategory();
            for (int i = 0; i < reps.length; i++)
                cat.add(new DefaultMutableTreeNode(reps[i]));
            top.add(cat);
        }
        return new DefaultTreeModel(top);
    }

    /**
     * Returns the translated category name.
     */
    private String getCategoryText(Report.Category category) {
        String resourceName = "category." + category.getName();
        String text = RESOURCES.getString(resourceName);
        if (text.equals(resourceName))
            text = category.getName();
        return text;
    }

    /**
     * A private callback for various messages coming in.
     */
    private class Callback implements TreeCellRenderer, TreeSelectionListener,
        TreeExpansionListener
    {
        /**
         * a default renderer for tree
         */
        private DefaultTreeCellRenderer defTreeRenderer = new DefaultTreeCellRenderer();

        /**
         * Return component for rendering tree element
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean isSelected, boolean isExpanded, boolean isLeaf,
                int index, boolean hasFocus) {
            defTreeRenderer.getTreeCellRendererComponent(tree, value, isSelected,
                    isExpanded, isLeaf, index, hasFocus);
            Object v = ((DefaultMutableTreeNode)value).getUserObject();
            if (v instanceof Report) {
                Report report = (Report)v;
                defTreeRenderer.setText(report.getName());
                defTreeRenderer.setIcon(report.getImage());
            } else if (v instanceof Report.Category) {
                Report.Category category = (Report.Category)v;
                defTreeRenderer.setText(getCategoryText(category));
                defTreeRenderer.setIcon(category.getImage());
            }

            return defTreeRenderer;
        }

        /**
         * Monitors changes to selection of reports.
         */
        public void valueChanged(TreeSelectionEvent e) {
            selection = null;
            TreePath path = tree.getSelectionPath();
            if (path != null) {
                Object v = ((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
                if (v instanceof Report)
                    selection = (Report)v;
            }
            if (selectionListener != null)
                selectionListener.valueChanged(selection);
        }

        /**
         * Saves the expansion state of groups in the registry.
         */
        public void treeExpanded(TreeExpansionEvent e) {
            Object v = ((DefaultMutableTreeNode)e.getPath()
                    .getLastPathComponent()).getUserObject();
            if (v instanceof Report.Category) {
                Report.Category category = (Report.Category)v;
                registry.put("expanded." + category.getName(), true);
            }
        }

        /**
         * Saves the expansion state of groups in the registry.
         */
        public void treeCollapsed(TreeExpansionEvent e) {
            Object v = ((DefaultMutableTreeNode)e.getPath()
                    .getLastPathComponent()).getUserObject();
            if (v instanceof Report.Category) {
                Report.Category category = (Report.Category)v;
                registry.put("expanded." + category.getName(), false);
            }
        }
    }

    /**
     * List of reports in a category.
     */
    private class CategoryList
    {
        private Report.Category category;
        private List reportsInCategory = new ArrayList();

        public CategoryList(Report.Category category) {
            this.category = category;
        }

        public Report.Category getCategory() {
            return category;
        }

        public Report[] getReportsInCategory() {
            return (Report[])reportsInCategory.toArray(new Report[reportsInCategory.size()]);
        }

        public void add(Report report) {
            reportsInCategory.add(report);
        }
    }
}
