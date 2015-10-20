/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Copyright 2015 Ancestris
 * Author: Frederic Lapeyre (frederic@ancestris.org).
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.standard;

import ancestris.api.editor.Editor;
import ancestris.core.beans.ConfirmChangeWidget;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Property;
import genj.gedcom.UnitOfWork;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.openide.awt.UndoRedo;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.RetainLocation;
import org.openide.windows.TopComponent;

/**
 *
 * @author daniel & frederic
 */
@ServiceProvider(service = AncestrisViewInterface.class)
@RetainLocation(AncestrisDockModes.EDITOR)
public class EditorTopComponent extends AncestrisTopComponent implements TopComponent.Cloneable, ConfirmChangeWidget.ConfirmChangeCallBack {

    private static final String PREFERRED_ID = "AncestrisEditor";  // NOI18N
    private static EditorTopComponent factory;

    /* package */ final static Logger LOG = Logger.getLogger("ancestris.editor");
    
    // Main elements
    private Gedcom gedcom;
    private Editor editor;
    
    // Update control
    private final Callback callback = new Callback();
    private boolean isChangeSource = false;
    private ConfirmChangeWidget confirmPanel;

    // Redo elements
    private int undoNb;
    UndoRedoListener undoRedoListener;
    
    // panel elements (to be reviewedd)
    private JScrollPane editorContainer;
    private JLabel titleLabel;
    private static final Map<Class<? extends Property>, Editor> panels;
    static {
        panels = new HashMap<Class<? extends Property>, Editor>();
        //panels.put(Fam.class, new EntityEditor());
    }

    
    public static synchronized EditorTopComponent getFactory() {
        LOG.fine("getFactory");
        if (factory == null) {
            factory = new EditorTopComponent();
        }
        return factory;
    }
    
    
    /**
     * Initializers (#1)
     */
    @Override
    public void setName() {
        LOG.fine("setName");
        if (true) return;
        
        if (editor != null && editor.getName() != null) {
            setName(editor.getName());
        } else {
            super.setName();
        }
    }

    /**
     * Initializers (#2)
     */
    @Override
    public void setToolTipText() {
        LOG.fine("setToolTipText");
        if (true) return;

        if (editor != null && editor.getToolTipText() != null) {
            setToolTipText(editor.getToolTipText());
        } else {
            super.setToolTipText();
        }
    }


    /**
     * Initializers (#3)
     * 
     * - Called by AncestrisTopComponent.init
     * - Called by FireSelection after call to componentOpened (from open gedcom)
     */
    @Override
    public void setContextImpl(Context context) {
        LOG.fine("setContextImpl context = " + context.toString());
        if (true) return;
        
        // see also EditView
        if (context == null) {
            return;
        }
        // disconnect from last gedcom?
        if (context.getGedcom() != gedcom && gedcom != null) {
            gedcom.removeGedcomListener(callback);
            gedcom = null;
        }
        // clear?
        if (context.getGedcom() == null) {
            //sticky.setSelected(false);
            setEditor(null);
            //populate(toolbar);
            confirmPanel.setChanged(false);
            return;
        }

        // connect to gedcom?
        if (context.getGedcom() != gedcom) {
            gedcom = context.getGedcom();
            gedcom.addGedcomListener(callback);
        }

        // commit if necessary
        commit();
        if (context.getEntity() == null) {
            return;
        }
        Editor panel = panels.get(context.getEntity().getClass());
        if (panel != null) {
            panel.setContext(context);
            setEditor(panel);
        }

        // save undo index for use in cancel
        undoNb = gedcom.getUndoNb();
        //setPanel(editorContainer);
        repaint();
    }

    /**
     * Set editor to use (called from setContextImpl)
     */
    private void setEditor(Editor set) {
        LOG.fine("setEditor set = " + set.getName());
        if (true) return;
        
        // editor not yet initialized 
        if (editorContainer == null) {
            return;
        }
//        // commit old editor unless set==null
        Context old = null;
        if (set != null) {
//
//            // preserve old context
//            old = editor != null ? editor.getContext() : null;
            old = set.getContext();
//
//            // force commit
//            commit();
//
        }
//
        // clear old editor
        if (editor != null) {
            editor.removeChangeListener(confirmPanel);
            editor.setContext(new Context());
            editor = null;
        }

        // set new and restore context
        editor = set;
        if (editor != null) {
            if (old != null) {
                editor.setContext(old);
            }
            editorContainer.setViewportView(editor);
            titleLabel.setText(editor.getTitle());
            editor.addChangeListener(confirmPanel);
        }

        // show
        revalidate();
        repaint();
    }

    
    
    /**
     * Initializers (#4)
     */
    @Override
    public Image getImageIcon() {
        LOG.fine("getImageIcon");
        if (true) return null;
        
        Image icon = null;
        if (editor != null) {
            icon = editor.getImageIcon();
        }
        if (icon == null) {
            icon = getImageIcon("ancestris/modules/editors/standard/editeur_standard.png");
        }
        if (icon == null) {
            icon = super.getImageIcon();
        }
        return icon;
    }


    
    /**
     * Initializers (#5)
     */
    @Override
    public boolean createPanel() {
        LOG.fine("createPanel");
        if (true) return true;
                
        JPanel editorPanel;
        
        editorPanel = new JPanel(new MigLayout(new LC().fill().hideMode(3), new AC().grow().fill()));
        editorContainer = new JScrollPane();
        editorPanel.add(editorContainer, new CC().grow());
        titleLabel = new JLabel("");

        confirmPanel = new ConfirmChangeWidget(this);
        confirmPanel.setChanged(false);

        titleLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        editorPanel.add(titleLabel, new CC().dockNorth());
        editorPanel.add(confirmPanel, new CC().dockSouth());

        // retrigger a context change
        // FIXME: we need that because createpanel is called after setcontext
        setPanel(editorPanel);
        Context ctx = getContext();
        setContext(new Context(ctx.getGedcom()));
        setContext(ctx);

        return true;
    }

    

    /**
     * Initializers (#6)
     */
    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    /**
     * Initializers (#7)
     */
    @Override
    public void componentOpened() {
        LOG.fine("componentOpened");
        
        undoRedoListener = new UndoRedoListener();
        UndoRedo undoRedo = getUndoRedo();
        undoRedo.addChangeListener(undoRedoListener);
    }


    
    
    
    /**
     * Updates
     */
    
    
    public void commit() {
        commit(true);
    }

    
    private void commit(boolean ask) {
        LOG.fine("commit ask = " + ask);
        if (true) return;
        
        // changes?
        if (confirmPanel == null || !confirmPanel.hasChanged()) {
            return;
        }

        // we only consider committing IF we're still in a visible top level ancestor (window) - otherwise we assume
        // that the containing window was closed and we're not going to throw a dialog out there or do a change
        // behind the covers - we really would need a about-to-close hook for contained components here :(
        if (!isOpened()) {
            return;
        }

        // check for auto commit
        if (ask && !confirmPanel.isCommitChanges()) {
            // Don't commit
            cancel();
            confirmPanel.setChanged(false);
            return;
        }

        try {

            isChangeSource = true;

            if (gedcom.isWriteLocked()) {
                editor.commit();
            } else {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        editor.commit();
                    }
                });
            }

        } catch (Throwable t) {
            LOG.log(Level.WARNING, "error committing editor", t);
        } finally {
            isChangeSource = false;
            confirmPanel.setChanged(false);
        }
    }

    private void cancel() {
        LOG.fine("cancel");
        if (true) return;
        
        // we only consider committing IF we're still in a visible top level ancestor (window) - otherwise we assume
        // that the containing window was closed and we're not going to throw a dialog out there or do a change
        // behind the covers - we really would need a about-to-close hook for contained components here :(
        if (!isOpened()) {
            return;
        }

        if (gedcom.isWriteLocked()) {
            //FIXME: do we need a cancel here? editor.cancel();
        } else {
            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                gedcom.undoUnitOfWork(false);
            }
        }

    }

    public void okCallBack(ActionEvent event) {
        LOG.fine("okCallBack event = " + event.toString());
        if (true) return;
        
        commit(false);
    }

    public void cancelCallBack(ActionEvent event) {
        LOG.fine("cancelCallBack event = " + event.toString());
        if (true) return;
        
        cancel();
        // re-set for cancel
        Context ctx = editor.getContext();
//        editor.setContext(new Context());
        editor.setContext(ctx);
//        populate(toolbar);
    }

    public TopComponent cloneComponent() {
        LOG.fine("cloneComponent");
        if (true) return null;

        if (getContext() == null) {
            return null;
        }

        AncestrisTopComponent topComponent = new EditorTopComponent();
        topComponent.init(getContext());
        return topComponent;
    }

    
    
    
    /**
     * Classes
     */
    
    private class Callback extends GedcomListenerAdapter {

        @Override
        public void gedcomWriteLockAcquired(Gedcom gedcom) {
            LOG.fine("Callback - gedcomWriteLockAcquired");
            if (true) return;

            // changes we have to commit?
            if (!isChangeSource) {
                commit(false);
            }

        }
    }

    
    
    private class UndoRedoListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            LOG.fine("UndoRedoListener - stateChanged");
            if (true) return;

            Context ctx = editor.getContext();
            editor.setContext(ctx);
        }
    }
}
