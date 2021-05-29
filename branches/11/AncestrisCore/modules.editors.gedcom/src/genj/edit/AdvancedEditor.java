/**
 * Ancestris - http://www.ancestris.org (Formerly GenJ - GenealogyJ)
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2010 - 2013 Ancestris Author: Daniel Andre
 * <daniel@ancestris.org>
 *
 * This piece of code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package genj.edit;

import ancestris.api.editor.Editor;
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.actions.AncestrisActionProvider;
import ancestris.core.actions.SubMenuAction;
import ancestris.core.resources.Images;
import ancestris.modules.gedcom.searchduplicates.IndiDuplicatesFinder;
import ancestris.util.swing.DialogManager;
import ancestris.view.SelectionDispatcher;
import genj.edit.beans.PropertyBean;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomOptions;
import genj.gedcom.Indi;
import genj.gedcom.MetaProperty;
import genj.gedcom.Property;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyXRef;
import genj.gedcom.TagPath;
import genj.gedcom.UnitOfWork;
import genj.io.PropertyReader;
import genj.io.PropertyTransferable;
import genj.util.Registry;
import genj.util.Resources;
import genj.util.WordBuffer;
import genj.view.ViewContext;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.LayoutFocusTraversalPolicy;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

/**
 * Our advanced version of the editor allowing low-level access at the Gedcom
 * record-structure
 */
/* package */ class AdvancedEditor extends Editor {

    private final static String ACC_CUT = "ctrl X",
            ACC_COPY = "ctrl C",
            ACC_PASTE = "ctrl V";
    private final static Clipboard clipboard = initClipboard();
    private final static Registry REGISTRY = Registry.get(AdvancedEditor.class);
    // Doit etre trie car une sequence du type IND:BIRT:OBJE,IND:BIRT ne deplie pas le obje sous le birt
    private Set<TagPath> expands = new TreeSet<TagPath>();
    private boolean ignoreTreeSelection = false;

    /**
     * Initialize clipboard - trying system falling back to private
     */
    private static Clipboard initClipboard() {
        try {
            return Toolkit.getDefaultToolkit().getSystemClipboard();
        } catch (Throwable t) {
            return new Clipboard("Ancestris");
        }

    }
    /**
     * resources
     */
    private static Resources resources = Resources.get(AdvancedEditor.class);
    /**
     * gedcom
     */
    private Gedcom gedcom;
    /**
     * tree for record structure
     */
    private PropertyTreeWidget tree = null;
    /**
     * everything for the bean
     */
    private JPanel editPane;
    private PropertyBean bean = null;
    /**
     * splitpane for tree/bean
     */
    private JSplitPane splitPane = null;
    /**
     * view
     */
    private EditView view;
    /**
     * interaction callback
     */
    private Callback callback;

    /**
     * Initialize
     */
    public AdvancedEditor(Gedcom gedcom, EditView view) {

        // remember
        this.gedcom = gedcom;
        this.view = view;

        // TREE Component's 
        tree = new Tree();
        callback = new Callback();
        tree.addTreeSelectionListener(callback);
        tree.addTreeWillExpandListener(callback);

        JScrollPane treePane = new JScrollPane(tree);
        treePane.setMinimumSize(new Dimension(160, 128));
        treePane.setPreferredSize(new Dimension(160, 128));
        treePane.getHorizontalScrollBar().setFocusable(false); // dont allow focus on scroll bars
        treePane.getVerticalScrollBar().setFocusable(false);

        // EDIT Component
        editPane = new JPanel(new BorderLayout());
        JScrollPane editScroll = new JScrollPane(editPane);
        // .. don't want scrollbars to get focus
        editScroll.getVerticalScrollBar().setFocusable(false);
        editScroll.getHorizontalScrollBar().setFocusable(false);

        // SplitPane with tree/edit
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treePane, editScroll);
        splitPane.setDividerLocation(REGISTRY.get("divider", 300));
        splitPane.setContinuousLayout(true);
        splitPane.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (JSplitPane.DIVIDER_LOCATION_PROPERTY.equals(evt.getPropertyName())) {
                    REGISTRY.put("divider", splitPane.getDividerLocation());
                }
            }
        });

        // layout
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);

        // setup focus policy
        setFocusTraversalPolicy(new FocusPolicy());
        setFocusCycleRoot(true);

        // re-read expand settings
        String paths = REGISTRY.get("expand", "INDI:BIRT,INDI:RESI,INDI:OBJE,FAM:MARR");
        for (String path : paths.split(",")) {
            try {
                expands.add(new TagPath(path));
            } catch (IllegalArgumentException iae) {
                // ignored
            }
        }

        // done    
    }

    @Override
    public Component getEditorComponent() {
        return tree;
    }

    @Override
    public void removeNotify() {
        // store some settings on remove
        WordBuffer paths = new WordBuffer(",");
        for (TagPath path : expands) {
            paths.append(path);
        }
        REGISTRY.put("expand", paths.toString());
        // continue
        super.removeNotify();
    }

    /**
     * Provider current context
     */
    @Override
    public ViewContext getContext() {
        return tree.getContext();
    }

    /**
     * Accessor - current context
     *
     * @param context context to switch to
     */
    @Override
    protected void setContextImpl(Context context) {
        setContextImpl(context, false);
    }

    private void setContextImpl(Context context, boolean pickFirstProperty) {

        // Clean up
        if (bean != null) {
            bean.removeChangeListener(changes);
        }
        bean = null;
        editPane.removeAll();
        editPane.revalidate();
        editPane.repaint();
        changes.setChanged(false);

        // clear?
        if (context.getGedcom() == null/* ||context.getEntities().isEmpty() */) {
            try {
                ignoreTreeSelection = true;
                tree.setRoot(null);
            } finally {
                ignoreTreeSelection = false;
            }
            return;
        }
        if (context.getEntities().isEmpty()) {
            context = new Context(context.getGedcom().getFirstEntity("HEAD"));

        }

        ignoreTreeSelection = true;

        // clear current selection
        tree.clearSelection();

        // change root if necessary
        Entity entity = context.getEntity();
        if (entity != tree.getRoot()) {
            tree.setRoot(entity);
            for (TagPath path : expands) {
                expand(path);
            }
        }

        // current root
        Property root = tree.getRoot();
        if (root != null) {
            Gedcom gedcom = root.getGedcom();
            // commit
            view.commit();
        }

        // set selection
        List<? extends Property> props = context.getProperties();
        if (props.isEmpty()) {
            if (pickFirstProperty && entity.getNoOfProperties() > 0) {
                props = Collections.singletonList(entity.getProperty(0));
            } else {
                props = Collections.singletonList(entity);
            }
        }
        tree.setSelection(props);

        ignoreTreeSelection = false;

        // show bean for single selection
        if (props.isEmpty()) {
            return;
        }

        Property prop = props.get(props.size() - 1);
        try {
            expand(prop.getPath());

            // get a bean for property
            bean = PropertyBean.getBean(prop.getClass()).setContext(prop);

            // add bean to center of editPane 
            editPane.add(bean, BorderLayout.CENTER);

            // and a label/button for the top
            JToolBar header = new JToolBar();
            header.setFloatable(false);
            if (prop instanceof PropertyXRef) {
                JButton follow = new JButton((Action) new Follow((PropertyXRef) prop));
                header.add(follow);
            } else {
                JLabel label = new JLabel(Gedcom.getName(prop.getTag()), prop.getImage(false), SwingConstants.LEFT);
                label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
                header.add(label);
            }
            editPane.add(header, BorderLayout.NORTH);

            // listen to it
            changes.setChanged(false);
            bean.addChangeListener(changes);

            // focus?
            if (view.isGrabFocus()) {
                bean.requestFocus();
            }

        } catch (Throwable t) {
            EditView.LOG.log(Level.WARNING, "Property bean " + bean, t);
        }

        // Done
    }

    /**
     * Expand a path
     */
    public void expand(TagPath path) {
        tree.expand(path);
    }

    @Override
    public void commit() throws GedcomException {
        Property root = tree.getRoot();
        if (root == null) {
            return;
        }

        boolean nouveau = false;
        Indi individual = null;
        if (root instanceof Indi) {
            individual = (Indi) root;
            nouveau = individual.isNew();
            individual.setOld();
        }

        if (bean != null) {
            bean.commit();
        }

        if (individual != null) {
            // Detect if ask for it and new or any time.
            if ((GedcomOptions.getInstance().getDetectDuplicate() && nouveau) || GedcomOptions.getInstance().getDuplicateAnyTime()) {
                SwingUtilities.invokeLater(new IndiDuplicatesFinder(individual));
            }
        }
    }

    /* Editor API */
    @Override
    public Image getImageIcon() {
        return new EditViewFactory().getImage().getImage();
    }

    @Override
    public String getName() {
        return new EditViewFactory().getTitle();
    }

    @Override
    public String getToolTipText() {
        return getName();
    }

    @Override
    public Entity getEditedEntity() {
        return view.getEntity();
    }

    /**
     * Action - propagate properties
     */
    private class Propagate extends AbstractAncestrisAction {

        /**
         * selection to propagate
         */
        private Entity entity;
        private List<Property> properties;
        private String what;

        /**
         * constructor
         */
        private Propagate(List<Property> selection) {
            // remember
            this.entity = (Entity) tree.getRoot();
            properties = Property.normalize(selection);
            setImage(Images.imgPropagate);
            // something there?
            if (properties.isEmpty()) {
                setText(resources.getString("action.propagate", ""));
                setEnabled(false);
                return;
            }
            // setup looks
            this.what = "'" + Property.getPropertyNames(properties, 5) + "'";
            setText(resources.getString("action.propagate") + "...");
            setTip(resources.getString("action.propagate.tip", what));
        }

        /**
         * apply it
         */
        @Override
        public void actionPerformed(ActionEvent event) {

            // Selection box
            final SelectEntityToPropagatePanel select = new SelectEntityToPropagatePanel(gedcom, entity.getTag(), what,
                    gedcom.getEntity(REGISTRY.get("select." + entity.getTag(), (String) null)), resources.getString("action.propagate.toall"));
            if (DialogManager.OK_OPTION != DialogManager.create(resources.getString("action.propagate", ""), select).setMessageType(DialogManager.WARNING_MESSAGE).setOptionType(DialogManager.OK_CANCEL_OPTION)
                    .setDialogId("propagate.entityselected").show()) {
                return;
            }

            final Entity selection = select.getSelection();

            // remember selection
            REGISTRY.put("select." + entity.getTag(), selection != null ? selection.getId() : null);

            // change it
            try {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        for (Entity to : selection != null ? Collections.singletonList(selection) : gedcom.getEntities(entity.getTag())) {
                            Propagate.this.copy(properties, entity, to, select.isSelected());
                        }
                    }
                });
            } catch (GedcomException e) {
                DialogManager.createError(null, e.getMessage()).show();
            }

            // done
        }

        private void copy(List<Property> selection, Entity from, Entity to, boolean values) throws GedcomException {
            // make sure we're not propagating to self
            if (from == to) {
                return;
            }
            // loop over selection
            for (Property property : selection) {
                TagPath path = property.getParent().getPath();
                Property root = to.getProperty(path);
                if (root == null) {
                    root = to.setValue(path, "");
                }
                root.copyProperties(property, values);
            }
            // done
        }
    } //Propagate

    /**
     * Action - cut
     */
    private class Cut extends AbstractAncestrisAction {

        /**
         * selection
         */
        protected List<Property> presetSelection;

        /**
         * constructor
         */
        private Cut(List<Property> preset) {
            presetSelection = Property.normalize(preset);
            setImage(Images.imgCut);
            setText(resources.getString("action.cut"));
            setTip(resources.getString("action.cut.tip"));
        }

        private Cut() {
        }

        /**
         * run
         */
        @Override
        public void actionPerformed(ActionEvent event) {

            // available
            final List<Property> selection = presetSelection != null ? presetSelection : Property.normalize(tree.getSelection());
            if (selection.isEmpty()) {
                return;
            }

            // contains entity?
            if (selection.contains(tree.getRoot())) {
                selection.clear();
                selection.addAll(Arrays.asList(tree.getRoot().getProperties()));
            }

            // warn about cut
            String veto = getVeto(selection);
            if (veto.length() > 0) {
                String cut = resources.getString("action.cut");
                if (DialogManager.create(resources.getString("action.cut"), veto)
                        .setMessageType(DialogManager.WARNING_MESSAGE)
                        .setOptions(new Object[]{cut, DialogManager.CANCEL_OPTION})
                        .setDialogId("action.cut")
                        .show() != cut) {
                    return;
                }
            }

            // copy first
            try {
                clipboard.setContents(new PropertyTransferable(selection).getStringTransferable(), null);
            } catch (Throwable t) {
                EditView.LOG.log(Level.WARNING, "Couldn't copy properties", t);
                return;
            }

            // now cut
            gedcom.doMuteUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) {
                    Property parent = null;
                    for (Property prop : selection) {
                        parent = prop.getParent();
                        parent.delProperty(prop);
                    }
                    if (parent != null) {
                        SelectionDispatcher.fireSelection(new Context(parent));
                        // FIXME: probablement pas necessaire puisque le expande est deja fait par le setContext
                        // Mais on assure au cas ou...
                        expand(parent.getPath());
                    }
                }
            });
            // done
        }

        /**
         * assemble a list of vetos for cutting properties
         */
        private String getVeto(List<Property> properties) {

            boolean found = false;
            String msg = resources.getString("del.warning.message") + "\n\n";
            StringBuilder result = new StringBuilder();
            for (Property p : properties) {

                String veto = p.getDeleteVeto();
                if (veto != null) {
                    found = true;
                    // Removing property {0} from {1} leads to:\n{2}
                    result.append(resources.getString("del.warning", p.getPropertyName(), p.getParent().getPropertyName(), veto));
                    result.append("\n\n");
                }
            }

            if (found) {
                return msg + result.toString();
            }

            return "";
        }
    } //Cut

    /**
     * Action - copy
     */
    private class Copy extends AbstractAncestrisAction {

        /**
         * selection
         */
        protected List<Property> presetSelection;

        /**
         * constructor
         */
        protected Copy(List<Property> preset) {
            presetSelection = Property.normalize(preset);
            setText(resources.getString("action.copy"));
            setTip(resources.getString("action.copy.tip"));
            setImage(Images.imgCopy);
        }

        /**
         * constructor
         */
        protected Copy() {
        }

        /**
         * run
         */
        @Override
        public void actionPerformed(ActionEvent event) {

            // check selection
            List<Property> selection = presetSelection;
            if (selection == null) {
                selection = Property.normalize(tree.getSelection());
            }

            // contains entity?
            if (selection.contains(tree.getRoot())) {
                selection = Arrays.asList(tree.getRoot().getProperties());
            }

            try {
                clipboard.setContents(new PropertyTransferable(selection).getStringTransferable(), null);
            } catch (Throwable t) {
                EditView.LOG.log(Level.WARNING, "Couldn't copy properties", t);
            }
        }
    } //ActionCopy

    /**
     * Action - paste
     */
    private class Paste extends AbstractAncestrisAction {

        /**
         * selection
         */
        private Property presetParent;

        /**
         * constructor
         */
        protected Paste(Property property) {
            presetParent = property;
            setText(resources.getString("action.paste"));
            setTip(resources.getString("action.paste.tip"));
            setImage(Images.imgPaste);

            // 20060404 isPasteAvail() apparently is VERY costly - depending on what's in the system clipboard
            // so let's not do this anymore and check on execute() instead
            // setEnabled(isPasteAvail());
        }

        /**
         * constructor
         */
        protected Paste() {
        }

        /**
         * run
         */
        @Override
        public void actionPerformed(ActionEvent event) {

            // grab the clipboard content now
            final String content;
            try {
                content = clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor).toString();
            } catch (Throwable t) {
                EditView.LOG.log(Level.INFO, "Accessing system clipboard as stringFlavor failed ({0})", t.getMessage());
                return;
            }

            // select
            final Property parent;
            if (presetParent != null) {
                parent = presetParent;
            } else if (tree.getSelectionCount() == 1) {
                parent = tree.getSelection().get(0);
            } else {
                return;
            }

            // grab from clipboard
            gedcom.doMuteUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) throws GedcomException {
                    PropertyReader reader = new PropertyReader(new StringReader(content), null, true);
                    reader.setMerge(true);
                    try {
                        reader.read(parent);
                    } catch (IOException e) {
                        throw new GedcomException("IO during read()");
                    }
                }
            });

            // done
        }
    } //Paste

    /**
     * Action - follow
     */
    private class Follow extends AbstractAncestrisAction {

        private PropertyXRef xref;

        public Follow(PropertyXRef xref) {
            this.xref = xref;
            setText(Gedcom.getName(xref.getTarget() != null ? xref.getTarget().getTag() : xref.getValue()));
            setImage(xref.getImage(false));
            setTip(resources.getString("action.follow.tip", ""));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SelectionDispatcher.fireSelection(new Context(xref.getTarget()));
        }
    }

    /**
     * Action - add
     */
    private class Add extends AbstractAncestrisAction {

        /**
         * parent
         */
        private Property parent;
        private String[] tags;
        private boolean addDefaults = true;

        /**
         * constructor
         */
        protected Add(Property parent, MetaProperty meta) {
            this.parent = parent;
            String txt = meta.getName();
            if (!txt.equals(meta.getTag())) {
                txt += " (" + meta.getTag() + ")";
            }
            setText(txt);
            setImage(meta.getImage());
            tags = new String[]{meta.getTag()};
        }

        /**
         * constructor
         */
        protected Add(Property parent) {
            this.parent = parent;
            setText(resources.getString("action.list.add") + "...");
            setTip(resources.getString("action.list.add.tip"));
            setImage(Images.imgAdd);
        }

        /**
         * run
         */
        @Override
        public void actionPerformed(ActionEvent event) {

            // need to let user select tags to add?
            if (tags == null) {
                JLabel label = new JLabel(resources.getString("add.choose"));
                label.setBorder(BorderFactory.createEmptyBorder(6, 0, 6, 0));
                ChoosePropertyBean choose = new ChoosePropertyBean(parent);
                JCheckBox check = new JCheckBox(resources.getString("add.default_too"), addDefaults);
                if (DialogManager.create(resources.getString("add.title"), new JComponent[]{label, choose, check})
                        .setMessageType(DialogManager.QUESTION_MESSAGE)
                        .setOptionType(DialogManager.OK_CANCEL_OPTION)
                        .setDialogId("add.title")
                        .show() != DialogManager.OK_OPTION) {
                    return;
                }
                // .. calculate chosen tags
                tags = choose.getSelectedTags();
                addDefaults = check.isSelected();
                if (tags.length == 0) {
                    DialogManager.createError(null, resources.getString("add.must_enter")).show();
                    return;
                }
            }

            // .. stop current 
            tree.clearSelection();

            // .. add properties
            final List<Property> newProps = new ArrayList<Property>();
            gedcom.doMuteUnitOfWork(new UnitOfWork() {

                @Override
                public void perform(Gedcom gedcom) {
                    for (int i = 0; i < tags.length; i++) {
                        Property prop = parent.addProperty(tags[i], "");
                        newProps.add(prop);
                        if (addDefaults) {
                            prop.addDefaultProperties();
                        }
                    }
                }
            ;
            });
    
      // .. select added
      Property newProp = newProps.isEmpty() ? null : newProps.get(0);
            if (newProp instanceof PropertyEvent) {
                Property pdate = ((PropertyEvent) newProp).getDate(false);
                if (pdate != null) {
                    newProp = pdate;
                }
            }
            // Capture IllegalArgumentException : no selection can be done
            // DAN: I don't understand how a null or empty path can be found here
            try {
                tree.setSelectionPath(new TreePath(tree.getPathFor(newProp)));
            } catch (IllegalArgumentException e) {
            }

            // done
        }
    } //Add

    /**
     * Handling selection of properties
     */
    private class Callback implements TreeSelectionListener, TreeWillExpandListener {

        /**
         * callback - selection in tree has changed
         */
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            // ignore override + model change check
            if (ignoreTreeSelection || tree.getRoot() == null) {
                return;
            }
            List<Property> selection = tree.getSelection();
            Context ctx = new Context(gedcom, Collections.singletonList((Entity) tree.getRoot()), selection);
            if (!selection.isEmpty()) {
                SelectionDispatcher.fireSelection(ctx);
                if (ctx.getProperties().size() != 1) {
                    setContextImpl(ctx, false);
                }
            }
        }

        @Override
        public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
            TreePath path = event.getPath();
            int len = path.getPathCount();
            if (len == 1) {
                throw new ExpandVetoException(event);
            }
            String[] tags = new String[len];
            for (int i = 0; i < len; i++) {
                tags[i] = ((Property) path.getPathComponent(i)).getTag();
            }
            expands.remove(new TagPath(tags, null));
        }

        @Override
        public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
            TreePath path = event.getPath();
            int len = path.getPathCount();
            if (len == 1) {
                return;
            }
            String[] tags = new String[len];
            for (int i = 0; i < len; i++) {
                tags[i] = ((Property) path.getPathComponent(i)).getTag();
            }
            expands.add(new TagPath(tags, null));
        }
    } //InteractionListener

    /**
     * Intercept focus policy requests to automate tree node traversal on TAB.
     *
     * FL: 2015-11-21 : Fix : Pressing TAB key continuously on the editor used
     * to freeze the keyboard With the fix (do the selection in a
     * invokewheUiReady), I cannot reproduce it when pressing TAB continuously
     * for nearly a minute or two Same for Shift+TAB although I stop it on first
     * property. Indeed, if Shift+TAB is pressed continuously going backwards
     * AND looping around, it does still freeze the keyboard after a few seconds
     * (I do not know why !?!?)
     */
    private class FocusPolicy extends LayoutFocusTraversalPolicy {

        @Override
        public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
            Component result = super.getComponentAfter(focusCycleRoot, aComponent);
            if (result == null) {
                return null;
            }
            // choose next row in tree IF
            //  - a bean is still displayed at the moment
            //  - next component is not part of that bean
            if (bean != null && !SwingUtilities.isDescendingFrom(result, bean)) {
                final int[] selection = tree.getSelectionRows();
                if (selection != null && selection.length > 0) {
                    WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                        public void run() {
                            tree.setSelectionRow((selection[0] + 1) % tree.getRowCount());
                        }
                    });
                }
            }
            // done for me
            return result;
        }

        @Override
        public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
            // let super find out who's getting focus - this might be null!
            Component result = super.getComponentBefore(focusCycleRoot, aComponent);
            if (result == null) {
                return null;
            }
            // choose previous row in tree IF
            //  - a bean is still displayed at the moment
            //  - prev component is not part of that bean
            if (bean != null && !SwingUtilities.isDescendingFrom(result, bean)) {
                final int[] selection = tree.getSelectionRows();
                if (selection != null && selection.length > 0) {
                    WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                        public void run() {
                            int i = selection[0] - 1;
                            if (i >= 0) {
                                tree.setSelectionRow(i % tree.getRowCount());
                            }
                        }
                    });
                }
            }
            // done for me
            return result;
        }
    } //FocusPolicy

    /**
     * our patched up PropertyTreeWidget
     */
    private class Tree extends PropertyTreeWidget implements AncestrisActionProvider {

        /**
         * constructor
         */
        private Tree() {
            super(gedcom);
            // this makes the tree not grab focus on selection changes with mouse
            // thus not killing our grabFocus functionality
            setRequestFocusEnabled(false);
            // shortcuts
            //XXX: in layer 
//            new Cut().install(this, ACC_CUT, JComponent.WHEN_FOCUSED);
//            new Copy().install(this, ACC_COPY, JComponent.WHEN_FOCUSED);
//            new Paste().install(this, ACC_PASTE, JComponent.WHEN_FOCUSED);
        }

        @Override
        public List<Action> getActions(boolean hasFocus, Node[] nodes) {
            if (!hasFocus) {
                return new ArrayList<Action>();
            }
            List<Property> selection = tree.getSelection();
            List<Action> result = new ArrayList<Action>();

            // cut copy paste
            if (nodes.length != 0) {
                result.add(new Cut(selection));
                result.add(new Copy(selection));
            }
            if (selection.size() == 1) {
                result.add(new Paste(selection.get(0)));

                // add
                result.add(null);
                Property prop = selection.get(0);
                if (!prop.isTransient()) {
                    SubMenuAction menu = new SubMenuAction(resources.getString("action.add"));
                    menu.setTip(resources.getString("action.add.tip"));
                    MetaProperty[] metas = prop.getNestedMetaProperties(MetaProperty.WHERE_NOT_HIDDEN | MetaProperty.WHERE_CARDINALITY_ALLOWS);
                    Arrays.sort(metas);
                    for (int i = 0; i < metas.length; i++) {
                        if (metas[i].isInstantiated() || true) {
                            menu.addAction(new Add(prop, metas[i]));
                        }
                    }
                    result.add(menu);
                    result.add(new Add(prop));
                }
            } else {
                result.add(null);
            }

            if (!selection.isEmpty() && !selection.contains(tree.getRoot())) {
                result.add(new Propagate(selection));
            }

            return result;
        }
    } //Tree
} //AdvancedEditor
