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
import ancestris.gedcom.PropertyNode;
import ancestris.modules.editors.genealogyeditor.editors.EntityEditor;
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
import ancestris.view.ExplorerHelper;
import genj.gedcom.Context;
import genj.gedcom.Entity;
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
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
import org.openide.awt.UndoRedo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author daniel
 */
@ServiceProvider(service = AncestrisViewInterface.class)
public class AriesTopComponent extends AncestrisTopComponent implements ConfirmChangeWidget.ConfirmChangeCallBack {

    private static final String PREFERRED_ID = "AriesTopComponent";  // NOI18N
    private static AriesTopComponent factory;

    /* package */ final static Logger LOG = Logger.getLogger("ancestris.genealogyedit");
    private final Callback callback = new Callback();
    private boolean isChangeSource = false;
    private Editor editor;
    private ConfirmChangeWidget confirmPanel;
    private Gedcom gedcom;
    private Context context = null;
    private JScrollPane editorContainer;
    private TitlePanel titlePanel;
    private int undoNb;
    UndoRedoListener undoRedoListener;
    private final List<EntityEditor> openEditors = new ArrayList<>();
    
    /* This should be non static and defined for each AriesTopComponent, and not static once for all of them, otherwise 2 AriesTopComponents would share the same editors */
    private final Map<Class<? extends Property>, Editor> PANELS;
    {
        PANELS = new HashMap<>();
        PANELS.put(Fam.class, new FamilyEditor());
        PANELS.put(Indi.class, new IndividualEditor());
        PANELS.put(Note.class, new NoteEditor());
        PANELS.put(Repository.class, new RepositoryEditor());
        PANELS.put(Source.class, new SourceEditor());
        PANELS.put(Submitter.class, new SubmitterEditor());
        PANELS.put(Media.class, new MultiMediaObjectEditor());
    }
    

        
    @Override
    public String getAncestrisDockMode() {
        return AncestrisDockModes.EDITOR;
    }


    @Override
    public boolean createPanel() {

        JPanel editorPanel;
        editorPanel = new JPanel(
                new MigLayout(new LC().fill().hideMode(3),
                        new AC().grow().fill()));
        editorContainer = new JScrollPane();
        editorPanel.add(editorContainer, new CC().grow());

        confirmPanel = new ConfirmChangeWidget(this);
        confirmPanel.setChanged(false);

        titlePanel = new TitlePanel();
        editorPanel.add(titlePanel, new CC().dockNorth());
        editorPanel.add(confirmPanel, new CC().dockSouth());

        // retrigger a context change
        // FIXME: we need that because createpanel is called after setcontext
        setPanel(editorPanel);
        Context ctx = getContext();
        setContext(new Context(ctx.getGedcom()));
        setContext(ctx);
        
        return true;
    }

    public static synchronized AriesTopComponent getFactory() {
        if (factory == null) {
            factory = new AriesTopComponent();
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
            editor.getExplorerHelper().setPopupAllowed(false);
            editor = null;
        }

        // set new and restore context
        editor = set;
        if (editor != null) {
            if (old != null) {
                editor.setContext(old);
            }
            editorContainer.setViewportView(editor);
            String title = editor.getTitle();
            if (!title.isEmpty()) {
                int len = title.length();
                if (len > 120) {
                    int cut = title.indexOf(" ", 120);
                    if (cut != -1) {
                        title = title.substring(0, cut) + "...";
                    }
                }
            }

            titlePanel.setTitle("<html><center>" + title + "</center></html>");
            new ExplorerHelper(this).setPopupAllowed(true);  // allow context menu in the main panel as well (title bar)
            editor.getExplorerHelper().setPopupAllowed(true);
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
    
    /**
     * Accessor to liste of Editors.
     * @return List of editors opened
     */
     public List<EntityEditor> getOpenEditors() {
        return openEditors;
    }
    
    private void commitAllEditors() throws GedcomException {
        for (EntityEditor ee : openEditors){
            ee.commit();
        }
    }

    public void commit() {
        commit(true);
    }

    @Override
    public void commit(boolean ask) {
        // changes?
        if (confirmPanel == null || !confirmPanel.hasChanged()) {
            return;
        }

        // We only consider committing IF we're still in a visible top level ancestor (window)
        if (!isOpen) {
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
                commitAllEditors();
            } else {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        editor.commit();
                        commitAllEditors();
                    }
                });
            }

        } catch (GedcomException t) {
            LOG.log(Level.WARNING, "error committing editor", t);
        } finally {
            isChangeSource = false;
            confirmPanel.setChanged(false);
            undoNb = gedcom.getUndoNb();
        }
    }

    private void cancel() {
        // We only consider committing IF we're still in a visible top level ancestor (window)
        if (!isOpen) {
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
    public void setContextImpl(Context newContext) {
        // see also EditView
        if (newContext == null) {
            return;
        }

        setManagerContext(newContext.getEntity());
        
        // Quit if context is the same  
        if (newContext.equals(this.context)) {
            return;
        }
        this.context = newContext;
        
        // disconnect from last gedcom?
        if (newContext.getGedcom() != gedcom && gedcom != null) {
            gedcom.removeGedcomListener(callback);
            gedcom = null;
        }
        // clear?
        if (newContext.getGedcom() == null) {
//            sticky.setSelected(false);
            setEditor(null);
//            populate(toolbar);
            confirmPanel.setChanged(false);
            return;
        }

        // connect to gedcom?
        if (newContext.getGedcom() != gedcom) {
            gedcom = newContext.getGedcom();
            gedcom.addGedcomListener(callback);
        }

        // Commit asking for confirmation (in case newContext changes, user will be asked to confirm)
        commit(true);
        if (newContext.getEntity() == null) {
            return;
        }
        Editor panel = PANELS.get(newContext.getEntity().getClass());
        if (panel != null) {
            panel.setContext(newContext);
            setEditor(panel);
        }

        // save undo index for use in cancel
        undoNb = gedcom.getUndoNb();
//        setPanel(editorContainer);
        repaint();
    }

    @Override
    public void okCallBack(ActionEvent event) {
        commit(false);
    }

    @Override
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

        AncestrisTopComponent topComponent = new AriesTopComponent();
        topComponent.init(getContext());
        return topComponent;
    }

    
    // Force content of manager lookup to be on the entity level, not sub-properties
    private void setManagerContext(Entity entity) {
        if (entity == null || entity.getGedcom() == null) {
            return;
        }
        try {
            Children children = PropertyNode.getChildren(new Context(entity));
            getExplorerManager().setRootContext(new AbstractNode(children));
            getExplorerManager().setSelectedNodes(children.getNodes());
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
        
    }
    
    
    
    
    private class Callback extends GedcomListenerAdapter {

        @Override
        public void gedcomWriteLockAcquired(Gedcom gedcom) {

            // changes we have to commit?
            if (!isChangeSource) {
                commit(false);
            }

        }
    }

    private class UndoRedoListener implements ChangeListener {


        @Override
        public void stateChanged(ChangeEvent e) {
            Context ctx = editor.getContext();
            editor.setContext(ctx);
            setManagerContext(ctx.getEntity());
        }
    }
    
    public static AriesTopComponent findEditorWindow(Gedcom gedcom) {
        AriesTopComponent editorWindow = null;
        for (TopComponent tc : WindowManager.getDefault().getRegistry().getOpened()) {
            if (tc instanceof AriesTopComponent) {
                AriesTopComponent gltc = (AriesTopComponent) tc;
                if (gltc.getGedcom() == gedcom) {
                    editorWindow = gltc;
                    break;
                }
            }
        }
        return editorWindow;
    }
    
}
