/**
 * Ancestris - http://www.ancestris.org (Formerly GenJ - GenealogyJ)
 *
 * Copyright (C) 1997 - 2002 Nils Meier <nils@meiers.net>
 * Copyright (C) 2010 - 2013 Ancestris
 * Author: Daniel Andre <daniel@ancestris.org>
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
package genj.edit;

import ancestris.api.editor.Editor;
import ancestris.core.actions.AbstractAncestrisAction;
import ancestris.core.beans.ConfirmChangeWidget;
import ancestris.core.resources.Images;
import ancestris.swing.ToolBar;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomListenerAdapter;
import genj.util.Registry;
import genj.util.Resources;
import genj.view.View;
import genj.view.ViewContext;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * Component for editing genealogic entity properties
 */
public class EditView extends View implements ConfirmChangeWidget.ConfirmChangeCallBack {

    /* package */ final static Logger LOG = Logger.getLogger("ancestris.edit");
    private final static Registry REGISTRY = Registry.get(EditView.class);
    static final Resources RESOURCES = Resources.get(EditView.class);
    private final Mode mode = new Mode();
    private final Sticky sticky = new Sticky();
    private final Focus focus = new Focus();
    private final Callback callback = new Callback();
    private boolean isIgnoreSetContext = false;
    private boolean isChangeSource = false;
    private Editor editor;
    private final ConfirmChangeWidget confirmPanel;
    private ToolBar toolbar;
    private Gedcom gedcom;
    private TitlePanel titlePanel;


    /**
     * Constructor
     */
    public EditView() {

        super(new BorderLayout());

        confirmPanel = new ConfirmChangeWidget(this);

        setLayout(new BorderLayout());
        add(BorderLayout.SOUTH, confirmPanel);

        // check for current modes
        mode.setSelected(true);
        focus.setSelected(REGISTRY.get("focus", false));
        
        // Done
    }

    /**
     * Set editor to use
     */
    private void setEditor(Editor set) {

        // commit old editor unless set==null
        Context old = null;
        if (set != null) {

            // preserve old context
            old = editor != null ? editor.getContext() : null;

            // force commit
            commit();

        }

        // clear old editor
        if (editor != null) {
            editor.removeChangeListener(confirmPanel);
            editor.setContext(new Context());
            remove(editor);
            editor.getExplorerHelper().setPopupAllowed(false);
            editor = null;
        }

        // set new and restore context
        editor = set;
        if (editor != null) {
            add(editor, BorderLayout.CENTER);
            setExplorerHelper(editor.getExplorerHelper());
            if (old != null) {
                editor.setContext(old);
            }
            editor.addChangeListener(confirmPanel);
            //FIXME:explorerHelper.setPopupEnalbled() is called in addNotify(). 
            // but it is not set before this notification so no popup menu is showne
            // so must setup the correct helper at each editor switch
            editor.getExplorerHelper().setPopupAllowed(true);
        }

        // show
        revalidate();
        repaint();
    }

    /**
     * Check whether editor should grab focus or not
     */
    /* package */ boolean isGrabFocus() {
        return focus.isSelected();
    }

    // FIXME: change this
    /**
     * ContextProvider callback
     */
    private ViewContext getContext() {
        return editor != null ? editor.getContext() : null;
    }

    @Override
    public void commit() {
        commit(true);
    }

    @Override
    public void commit(boolean ask) {

        // changes?
        if (!confirmPanel.hasChanged()) {
            return;
        }

        // we only consider committing IF we're still in a visible top level ancestor (window) - otherwise we assume
        // that the containing window was closed and we're not going to throw a dialog out there or do a change
        // behind the covers - we really would need a about-to-close hook for contained components here :(
        // FIXME: How can getTopLevelAncestor() be null? probably an Editor TC closed and the view still alive. 
        // Must be fixed
        if (getTopLevelAncestor() == null || !getTopLevelAncestor().isVisible()) {
            return;
        }

        // check for auto commit
        if (ask && !confirmPanel.isCommitChanges()) {
            // Don't commit
            confirmPanel.setChanged(false);
            return;
        }

        try {

            isChangeSource = true;
            isIgnoreSetContext = true;

            if (gedcom.isWriteLocked()) {
                editor.commit();
            } else {
                gedcom.doUnitOfWork((Gedcom gedcom1) -> {
                    editor.commit();
                });
            }

        } catch (GedcomException t) {
            LOG.log(Level.WARNING, "error committing editor", t);
        } finally {
            isChangeSource = false;
            isIgnoreSetContext = false;
            confirmPanel.setChanged(false);
        }
    }

    @Override
    public void setContext(Context context) {

        // ignore it?
        if (isIgnoreSetContext) {
            return;
        }

        // disconnect from last gedcom?
        if (context.getGedcom() != gedcom && gedcom != null) {
            gedcom.removeGedcomListener(callback);
            gedcom = null;
        }

        // clear?
        if (context.getGedcom() == null) {
            sticky.setSelected(false);
            setEditor(null);
            populate(toolbar);
            confirmPanel.setChanged(false);
            return;
        }

        // connect to gedcom?
        if (context.getGedcom() != gedcom) {
            gedcom = context.getGedcom();
            gedcom.addGedcomListener(callback);
        }

        // commit?
        commit();

        // set
        if (context.getEntities().size() == 1) {

            if (editor == null) {
                sticky.setSelected(false);
                if (mode.isSelected()) {
                    setEditor(new AdvancedEditor(context.getGedcom(), this));
                } else {
                    setEditor(new BasicEditor(context.getGedcom(), this));
                }
            }

            if (!sticky.isSelected()) {
                editor.setContext(context);
            }

        } else {
            if (editor != null) {
                editor.setContext(new Context(context.getGedcom()));
            }
        }

        // start with a fresh edit
        confirmPanel.setChanged(false);

        // done
        populate(toolbar);
    }

    /**
     * @see genj.view.ToolBarSupport#populate(JToolBar)
     */
    @Override
    public void populate(final ToolBar toolbar) {

        this.toolbar = toolbar;
        if (toolbar == null) {
            return;
        }

        toolbar.beginUpdate();

        // editor?
        if (editor != null) {
            for (Action a : editor.getActions()) {
                toolbar.add(a);
            }
            if (editor.getActions().size() > 0) {
                toolbar.addSeparator();
            }
        }

        // add sticky/focus/mode
        toolbar.add(new JToggleButton((Action)sticky));
        toolbar.add(new JToggleButton((Action)focus));
        toolbar.addSeparator();

        titlePanel = new TitlePanel();
        toolbar.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if ("orientation".equals(evt.getPropertyName())) {
                setTitleBar();
            }
        });
        
        editor.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                setTitleBar();
            }
        });
        
        toolbar.add(titlePanel);
        setTitleBar();
        
        // done
        toolbar.endUpdate();
    }

    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(256, 480);
    }

    public void setTitleBar() {
        if (toolbar.getOrientation() == JToolBar.HORIZONTAL) {
            titlePanel.setTitle("<html>" + getEntity().getDisplayTitle() + "</html>");
            titlePanel.setPreferredSize(new java.awt.Dimension(Math.max(editor.getSize().width, 2000), 17));
            titlePanel.setVisible(true);
        } else {
            titlePanel.setTitle(" ");
            titlePanel.setPreferredSize(new java.awt.Dimension(10, 0));
            titlePanel.setVisible(false);

        }
    }
    
    
    /**
     * Current entity
     */
    public Entity getEntity() {
        return editor.getContext().getEntity();
    }

    @Override
    public void okCallBack(ActionEvent event) {
        commit(false);
    }

    @Override
    public void cancelCallBack(ActionEvent event) {
        // re-set for cancel
        Context ctx = editor.getContext();
        editor.setContext(new Context());
        editor.setContext(ctx);
        populate(toolbar);
    }

  /**
     * Current gedcom
     * @return
     */
    public Gedcom getGedcom() {
        return gedcom;
    }    
    
    /**
     * Action - toggle sticky mode
     */
    private static class Sticky extends AbstractAncestrisAction {

        /** constructor */
        protected Sticky() {
            super.setImage(Images.imgStickOff);
            super.setTip(RESOURCES.getString("action.stick.tip"));
            super.setSelected(false);
        }

        /** run */
        @Override
        public void actionPerformed(ActionEvent event) {
            setSelected(isSelected());
        }

        @Override
        public boolean setSelected(boolean selected) {
            super.setImage(selected ? Images.imgStickOn : Images.imgStickOff);
            return super.setSelected(selected);
        }
    } //Sticky

    /**
     * Action - toggle focus mode
     */
    private static class Focus extends AbstractAncestrisAction {

        /** constructor */
        protected Focus() {
            super.setImage(Images.imgFocus);
            super.setTip(RESOURCES.getString("action.focus.tip"));
            super.setSelected(false);
        }

        /** run */
        @Override
        public void actionPerformed(ActionEvent event) {
            setSelected(isSelected());
            REGISTRY.put("focus", isSelected());
        }
    } //Sticky

    /**
     * Action - advanced or basic
     */
    private class Mode extends AbstractAncestrisAction {

        private Mode() {
            setImage(Images.imgView);
            setTip(RESOURCES.getString("action.mode"));
            super.setSelected(false);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            setSelected(isSelected());
        }

        @Override
        public boolean setSelected(boolean selected) {
            REGISTRY.put("advanced", selected);
            if (getContext() != null) {
                setEditor(selected ? new AdvancedEditor(getContext().getGedcom(), EditView.this) : new BasicEditor(getContext().getGedcom(), EditView.this));
            }
            populate(toolbar);
            return super.setSelected(selected);
        }
    } //Advanced

    private class Callback extends GedcomListenerAdapter {

        @Override
        public void gedcomWriteLockAcquired(Gedcom gedcom) {

            // changes we have to commit?
            if (!isChangeSource) {
                commit(false);
            }

        }

        @Override
        public void gedcomWriteLockReleased(Gedcom gedcom) {

            // foreign change while we're looking?
            if (editor != null && !isChangeSource) {
                Context ctx = editor.getContext();
                // Propagate new selected context globally
                SelectionDispatcher.fireSelection(ctx);
                populate(toolbar);
            }
        }
    }
} //EditView

