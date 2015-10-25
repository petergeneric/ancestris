/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2011 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.editors.genealogyeditor;

import ancestris.api.editor.Editor;
import ancestris.core.beans.ConfirmChangeWidget;
import ancestris.modules.editors.genealogyeditor.editors.FamilyEditor;
import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
import ancestris.modules.editors.genealogyeditor.editors.MultiMediaObjectEditor;
import ancestris.modules.editors.genealogyeditor.editors.NoteEditor;
import ancestris.modules.editors.genealogyeditor.editors.RepositoryEditor;
import ancestris.modules.editors.genealogyeditor.editors.SourceEditor;
import ancestris.modules.editors.genealogyeditor.editors.SubmitterEditor;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import genj.gedcom.Context;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Indi;
import genj.gedcom.Media;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import genj.gedcom.Submitter;
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
 * @author daniel
 */
@ServiceProvider(service = AncestrisViewInterface.class)
@RetainLocation(AncestrisDockModes.EDITOR)
public class EditorTopComponent extends AncestrisTopComponent
        implements TopComponent.Cloneable, ConfirmChangeWidget.ConfirmChangeCallBack {

    private static final String PREFERRED_ID = "GenealogyEditor";  // NOI18N
    private static EditorTopComponent factory;

    /* package */ final static Logger LOG = Logger.getLogger("ancestris.genealogyedit");
    private final Callback callback = new Callback();
    private boolean isChangeSource = false;
    private Editor editor;
    private ConfirmChangeWidget confirmPanel;
    private Gedcom gedcom;
    private JScrollPane editorContainer;
    private JLabel titleLabel;
    private int undoNb;
    UndoRedoListener undoRedoListener;
    
    private static final Map<Class<? extends Property>, Editor> panels;

    static {
        panels = new HashMap<Class<? extends Property>, Editor>();
        panels.put(Fam.class, new FamilyEditor());
        panels.put(Indi.class, new IndividualEditor());
        panels.put(Note.class, new NoteEditor());
        panels.put(Repository.class, new RepositoryEditor());
        panels.put(Source.class, new SourceEditor());
        panels.put(Submitter.class, new SubmitterEditor());
        panels.put(Media.class, new MultiMediaObjectEditor());
    }

    @Override
    public boolean createPanel() {

        JPanel editorPanel;
        editorPanel = new JPanel(
                new MigLayout(new LC().fill().hideMode(3),
                        new AC().grow().fill()));
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

    public static synchronized EditorTopComponent getFactory() {
        if (factory == null) {
            factory = new EditorTopComponent();
        }
        return factory;
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    /**
     * Set editor to use
     */
    private void setEditor(Editor set) {
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

    @Override
    public void componentOpened() {
        undoRedoListener = new UndoRedoListener();
        UndoRedo undoRedo = getUndoRedo();
        undoRedo.addChangeListener(undoRedoListener);
    }

    public void commit() {
        commit(true);
    }

    private void commit(boolean ask) {
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
        // we only consider committing IF we're still in a visible top level ancestor (window) - otherwise we assume
        // that the containing window was closed and we're not going to throw a dialog out there or do a change
        // behind the covers - we really would need a about-to-close hook for contained components here :(
        if (!isOpened()) {
            return;
        }

        if (gedcom.isWriteLocked()) {
            //FIXME: do we need a canclel here? editor.cancel();
        } else {
            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                gedcom.undoUnitOfWork(false);
            }
        }

    }

    @Override
    public void setContextImpl(Context context) {
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
//            sticky.setSelected(false);
            setEditor(null);
//            populate(toolbar);
            confirmPanel.setChanged(false);
            return;
        }

        // connect to gedcom?
        if (context.getGedcom() != gedcom) {
            gedcom = context.getGedcom();
            gedcom.addGedcomListener(callback);
        }

        // commit if necessary
        commit(false);
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
//        setPanel(editorContainer);
        repaint();
    }

    public void okCallBack(ActionEvent event) {
        commit(false);
    }

    public void cancelCallBack(ActionEvent event) {
        cancel();
        // re-set for cancel
        Context ctx = editor.getContext();
//        editor.setContext(new Context());
        editor.setContext(ctx);
//        populate(toolbar);
    }

    @Override
    public Image getImageIcon() {
        Image icon = null;
        if (editor != null) {
            icon = editor.getImageIcon();
        }
        if (icon == null) {
            icon = getImageIcon("ancestris/modules/editors/genealogyeditor/resources/Editor.png");
        }
        if (icon == null) {
            icon = super.getImageIcon();
        }
        return icon;
    }

    @Override
    public void setName() {
        if (editor != null && editor.getName() != null) {
            setName(editor.getName());
        } else {
            super.setName();
        }
    }

    @Override
    public void setToolTipText() {
        if (editor != null && editor.getToolTipText() != null) {
            setToolTipText(editor.getToolTipText());
        } else {
            super.setToolTipText();
        }
    }

    public TopComponent cloneComponent() {
        if (getContext() == null) {
            return null;
        }

        AncestrisTopComponent topComponent = new EditorTopComponent();
        topComponent.init(getContext());
        return topComponent;
    }

    private class Callback extends GedcomListenerAdapter {

        @Override
        public void gedcomWriteLockAcquired(Gedcom gedcom) {

            // changes we have to commit?
            if (!isChangeSource) {
                commit(false);
            }

        }
//        @Override
//        //XXX: this is commented out to help finding a race condition in toolbox
//        public void gedcomWriteLockReleased(Gedcom gedcom) {
//
//            // foreign change while we're looking?
//            if (editor != null && !isChangeSource) {
//                Context ctx = editor.getContext();
//                editor.setContext(new Context());
//                editor.setContext(ctx);
////                populate(toolbar);
//            }
//        }
    }

    private class UndoRedoListener implements ChangeListener {


        @Override
        public void stateChanged(ChangeEvent e) {
            Context ctx = editor.getContext();
            editor.setContext(ctx);
        }
    }
}
