/**
 * GenJ - GenealogyJ
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 *
 * This piece of code is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.option;

import genj.util.swing.ImageIcon;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import swingx.tree.AbstractTreeModel;

/**
 * A widget for displaying options in tabular way
 */
public class OptionsWidget extends JPanel {

    /**
     * an image for options
     */
    public final static ImageIcon IMAGE = new ImageIcon(OptionsWidget.class, "images/Options");

    /**
     * tree we're using
     */
    private JTree tree;

    /**
     * tree model
     */
    private Model model = new Model();

    /**
     * first column width
     */
    private int widthOf1stColumn = 32;

    /**
     * a title for the options we're looking at - is used as default category
     */
    private String title;

    /**
     * a default renderer we keep around for colors
     */
    private DefaultTreeCellRenderer defaultRenderer;

    /**
     * Constructor
     */
    public OptionsWidget(String title) {
        this(title, null);
    }

    /**
     * Constructor
     */
    public OptionsWidget(String title, List<? extends Option> options) {

        this.title = title;

        // setup
        tree = new JTree(model) {
            public boolean isPathEditable(TreePath path) {
                return path.getLastPathComponent() instanceof Option;
            }
        };
        tree.setShowsRootHandles(false);
        tree.setRootVisible(false);
        tree.setCellRenderer(new Cell());
        tree.setCellEditor(new Cell());
        tree.setEditable(true);
        tree.setInvokesStopCellEditing(true);

        ToolTipManager.sharedInstance().registerComponent(tree);

        // layout
        setLayout(new BorderLayout());
        add(BorderLayout.CENTER, new JScrollPane(tree));

        // options?
        if (options != null) {
            setOptions(options);
        }

        // done
    }

    /**
     * callback - lifecycle remove
     */
    public void removeNotify() {
        // make sure any edit is stopped
        stopEditing();
        // continue
        super.removeNotify();
    }

    /**
     * Stop editing if currently ongoing
     */
    public void stopEditing() {
        tree.stopEditing();
    }

    /**
     * Set options to display
     */
    public void setOptions(List<? extends Option> set) {

        // stop editing
        stopEditing();

        // clear current selection
        tree.clearSelection();

        // check options - we don't keep any without ui
        List<Option> options = new ArrayList<Option>();
        for (Option option : set) {
            if (option.getUI(this) != null) {
                options.add(option);
            }
        }

        // calculate longest width of option name
        FontRenderContext ctx = new FontRenderContext(null, false, false);
        Font font = tree.getFont();
        widthOf1stColumn = 0;
        for (Option option : options) {
            widthOf1stColumn = Math.max(widthOf1stColumn, 4 + (int) Math.ceil(font.getStringBounds(option.getName(), ctx).getWidth()));
        }

        // tell to model
        model.setOptions(options);

        // unfold all
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

        // layout
        doLayout();
    }

    /**
     * Intercept new ui to get default renderer that provides us with colors
     */
    public void setUI(TreeUI ui) {
        // continue
        super.setUI(ui);
        // grab the default renderer now
        defaultRenderer = new DefaultTreeCellRenderer();
    }

    /**
     * A cell user either temporarily as renderer or editor
     */
    private class Cell extends AbstractCellEditor implements TreeCellRenderer, TreeCellEditor {

        /**
         * current ui
         */
        private OptionUI optionUi;

        /**
         * panel container
         */
        private JPanel panel = new JPanel() {
            // override key binding process - intercept enter as end of edit
            protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
                if (ks.getKeyCode() == KeyEvent.VK_ENTER) {
                    stopCellEditing();
                }
                if (ks.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelCellEditing();
                }
                return true;
            }
        };

        /**
         * label for option name
         */
        private JLabel labelForName = new JLabel();

        /**
         * label for option value
         */
        private JLabel labelForValue = new JLabel();

        /**
         * constructor
         */
        private Cell() {
            panel.setOpaque(false);
            panel.setLayout(new BorderLayout());
            panel.add(labelForName, BorderLayout.WEST);
        }

        /**
         * callback - component generation
         */
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            // prepare color
            if (defaultRenderer != null) {
                if (selected) {
                    labelForName.setForeground(defaultRenderer.getTextSelectionColor());
                    labelForName.setBackground(defaultRenderer.getBackgroundSelectionColor());
                    labelForValue.setForeground(defaultRenderer.getTextSelectionColor());
                    labelForValue.setBackground(defaultRenderer.getBackgroundSelectionColor());
                } else {
                    labelForName.setForeground(defaultRenderer.getTextNonSelectionColor());
                    labelForName.setBackground(defaultRenderer.getBackgroundNonSelectionColor());
                    labelForValue.setForeground(defaultRenderer.getTextNonSelectionColor());
                    labelForValue.setBackground(defaultRenderer.getBackgroundNonSelectionColor());
                }
            }
            // option?
            if (value instanceof Option) {
                return assemblePanel((Option) value, false);
            }
            // must be string
            // Here, we are not leaf because value is not an option
            Font font = labelForValue.getFont(); // font displayed for values (and not font displayed for names)
            labelForName.setFont(new Font(font.getFamily(), Font.BOLD, font.getSize()+2));
            labelForName.setBorder(new EmptyBorder(14,6,2,0));//top,left,bottom,right

            // remove old option if there
            if (panel.getComponentCount() > 1) {
                panel.remove(1);
            }
            labelForName.setText(value.toString());
            Graphics g = getGraphics();
            if (g != null) {
                FontMetrics metrics = getGraphics().getFontMetrics(font);
                int hgt = metrics.getHeight();
                int adv = metrics.stringWidth(labelForName.getText()) * 2;
                Dimension size = new Dimension(adv+6, hgt+16);
                labelForName.setPreferredSize(size);
            } else {
                labelForName.setPreferredSize(null);
            }
            return panel;
        }

        /**
         * assemble the editor/renderer panel
         */
        private JPanel assemblePanel(Option option, boolean forceUI) {
            // remove old option if there
            if (panel.getComponentCount() > 1) {
                panel.remove(1);
            }
            // lookup option and ui
            optionUi = option.getUI(OptionsWidget.this);
            // prepare name
            labelForName.setText(option.getName());
            labelForName.setPreferredSize(new Dimension(widthOf1stColumn, 16));
            // and value (either text or ui)
            JComponent compForValue;
            String text = optionUi.getTextRepresentation();
            if (text != null && !forceUI) {
                labelForValue.setText(text);
                compForValue = labelForValue;
            } else {
                compForValue = optionUi.getComponentRepresentation();
            }
            panel.add(compForValue, BorderLayout.CENTER);
            panel.setToolTipText(option.getToolTip());

            // make sure we have a regular font
            labelForName.setFont(labelForValue.getFont());
            labelForName.setBorder(new EmptyBorder(0,0,0,0));//top,left,bottom,right

            // done
            return panel;
        }

        /**
         * callback - call for editor component
         */
        public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
            return assemblePanel((Option) value, true);
        }

        /**
         * callback - the resulting value
         */
        public Object getCellEditorValue() {
            return null;
        }

        /**
         * callback - cancel editing
         */
        public void cancelCellEditing() {
            optionUi = null;
            super.cancelCellEditing();
        }

        /**
         * callback - stop editing = commit
         */
        public boolean stopCellEditing() {
            if (optionUi != null) {
                optionUi.endRepresentation();
            }
            return super.stopCellEditing();
        }

    } //Cell

    /**
     * Model
     */
    private class Model extends AbstractTreeModel {

        /**
         * top-level children
         */
        private List<String> categories = new ArrayList<String>();
        private Map<String, List<Option>> cat2options = new HashMap<String, List<Option>>();

        /**
         * the parent of options is the root (this)
         */
        protected Object getParent(Object node) {
            throw new IllegalArgumentException();
        }

        private List<Option> getCategory(String cat) {
            if (cat == null) {
                cat = title;
            }
            List<Option> result = cat2options.get(cat);
            if (result == null) {
                result = new ArrayList<Option>();
                cat2options.put(cat, result);
                categories.add(cat);
            }
            return result;
        }

        /**
         * Set options to display
         */
        private void setOptions(List<Option> set) {

            // parse anew
            cat2options.clear();
            categories.clear();

            for (Option option : set) {
                List<Option> cat = getCategory(option.getCategory());
                // 2020-07-03 - FL : do not sort options on their translated labels ! Absolutely not rational !?!? Sort as they appear in the code
                // just add opiton and comment out following sort
                cat.add(option);
//        int i = Collections.binarySearch(cat, option, new Comparator<Option>() {
//          public int compare(Option o1, Option o2) {
//            return o1.getName().compareTo(o2.getName());
//          }
//        });
//        if (i>=0)
//          cat.add(option);
//        else
//          cat.add(-i-1, option);   
            }

            // notify
            fireTreeStructureChanged(this, new Object[]{this}, null, null);
        }

        /**
         * the root is this
         */
        public Object getRoot() {
            return this;
        }

        /**
         * children are all options
         */
        public int getChildCount(Object parent) {
            if (parent == this) {
                return categories.size();
            }
            return getCategory((String) parent).size();
        }

        /**
         * options are leafs
         */
        public boolean isLeaf(Object node) {
            return node instanceof Option;
        }

        /**
         * option by index
         */
        public Object getChild(Object parent, int index) {
            if (parent == this) {
                return categories.get(index);
            }
            return getCategory((String) parent).get(index);
        }

        /**
         * reverse index lookup
         */
        public int getIndexOfChild(Object parent, Object child) {
            throw new IllegalArgumentException();
        }

    } //Model

} //OptionsWidget
