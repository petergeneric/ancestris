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
import ancestris.gedcom.PropertyNode;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
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
import genj.gedcom.PropertyXRef;
import genj.gedcom.Repository;
import genj.gedcom.Source;
import genj.gedcom.Submitter;
import genj.gedcom.UnitOfWork;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.awt.UndoRedo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 *
 * @author frederic
 */
@ServiceProvider(service = AncestrisViewInterface.class)
public class CygnusTopComponent extends AncestrisTopComponent implements TopComponent.Cloneable, ConfirmChangeWidget.ConfirmChangeCallBack {

    private static final String PREFERRED_ID = "CygnusTopComponent";  // NOI18N
    final static Logger LOG = Logger.getLogger("ancestris.cygnuseditor");
    
    // Main elements
    private Gedcom gedcom = null;
    private Editor editor = null;
    private Context currentContext = null;
    
    // Update control
    private boolean isBusyCommitting = false;
    private final Callback callback = new Callback();
    private boolean isChangeSource = false;

    // Redo elements
    private int undoNb;
    private UndoRedoListener undoRedoListener;
    
    // Panel elements
    private JScrollPane editorContainer;
    private ConfirmChangeWidget confirmPanel;

    @Override
    public String getAncestrisDockMode() {
        return AncestrisDockModes.EDITOR;
    }

    /**
     * Initializers (#1)
     */
    @Override
    public void setName() {
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
     * @param newContext
     */
    @Override
    public void setContextImpl(Context newContext) {
        
        // Quit if new context is null  
        if (newContext == null || newContext.getEntity() == null) {
            return;
        }

        // Adjust new context
        newContext = adjustContext(newContext);
        setManagerContext(newContext.getEntity());
        
        // Quit if context is the same  
        if (newContext.equals(this.currentContext)) {
            return;
        }

        // Redisplay and Quit if same entity, different context, and if editor already exists
        if (this.currentContext != null && !newContext.equals(this.currentContext) &&  newContext.getEntity().equals(this.currentContext.getEntity()) && editor != null) {
            this.currentContext = newContext;
            editor.setContext(newContext);  
            return;
        }
        this.currentContext = newContext;

        // Reconnect to gedcom if different
        if (newContext.getGedcom() != gedcom) {
            // Disconnect if not null
            if (gedcom != null) {
                gedcom.removeGedcomListener(callback);
            }
            gedcom = newContext.getGedcom();
            gedcom.addGedcomListener(callback);
        }

        // Commit asking for confirmation (in case context changes, user will be asked to confirm)
        commit(true);

        // Prepare confirm panel
        if (confirmPanel == null) {
            confirmPanel = new ConfirmChangeWidget(this);
            confirmPanel.setChanged(false);
        }
        
        // Reset editor if not suitable for new context
        if (editor != null && (!editor.getContext().getEntity().getTag().equals(newContext.getEntity().getTag()))) {
            editor.getExplorerHelper().setPopupAllowed(false);
            editor.removeChangeListener(confirmPanel);
            editor = null;
        }
        
        // Set the right editor panel to display depending on newt context
        if (editor == null) {
            if (newContext != null && newContext.getEntity().getTag().equals(Gedcom.INDI)) {
                editor = new IndiPanel();
            } else {
                editor = new BlankPanel();
            }
            if (editor != null) {
                editor.addChangeListener(confirmPanel);
            }
        }
        
        // Fill in editor with new context
        editor.setContext(newContext);

        // Redisplay editor in panel
        if (editorContainer != null) {
            editorContainer.setViewportView(editor);
        }

        // Save undo index for use in cancel
        undoNb = gedcom.getUndoNb();
        
        // Show
        revalidate();
        repaint();
    }

    
    
    /**
     * Initializers (#4)
     * @return 
     */
    @Override
    public Image getImageIcon() {
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
     * @return 
     */
    @Override
    public boolean createPanel() {
        if (editor == null) {
            return false;
        }
        
        // Display editor
        JPanel panel = new JPanel(new BorderLayout());
        editorContainer = new JScrollPane(editor);
        editorContainer.getVerticalScrollBar().setUnitIncrement(50);
        panel.add(editorContainer, BorderLayout.CENTER);        
        panel.add(confirmPanel, BorderLayout.PAGE_END);        
        setPanel(panel);
        editor.getExplorerHelper().setPopupAllowed(true);
        
        return true;
    }

    

    /**
     * Initializers (#6)
     * @return 
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
        undoRedoListener = new UndoRedoListener();
        UndoRedo undoRedo = getUndoRedo();
        if (undoRedoListener != null && undoRedo != null) {
            undoRedo.addChangeListener(undoRedoListener);
        }
    }


    
    /**
     * Updates
     */
    
    
    public void commit() {
        commit(true);
    }

    
    @Override
    public void commit(boolean ask) {
        // Is busy committing ?
        if (isBusyCommitting) {
            return;
        }

        // Changes?
        if (confirmPanel == null || !confirmPanel.hasChanged()) {
            return;
        }

        // We only consider committing IF we're still in a visible top level ancestor (window)
        if (!isOpen) {
            return;
        }

        // Do not commit for auto commit
        if (ask && !confirmPanel.isCommitChanges()) {
            cancel();
            confirmPanel.setChanged(false);
            return;
        }

        isBusyCommitting = true;
        try {

            isChangeSource = true;

            if (gedcom.isWriteLocked()) {
                if (!confirmPanel.hasChanged()) { // only commit changes from other modules (eg: undo) if confirm is off (do not automatically commit a pending change for the user)
                    editor.commit();
                }
            } else {
                gedcom.doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        editor.commit();
                    }
                });
            }

        } catch (Throwable t) {
            LOG.log(Level.WARNING, "Error committing editor", t);
        } finally {
            isChangeSource = false;
            confirmPanel.setChanged(false);
            isBusyCommitting = false;
        }
    }

    
    private void cancel() {
        // We only consider cancelling IF we're still in a visible top level ancestor (window)
        if (!isOpen) {
            return;
        }

        if (!gedcom.isWriteLocked()) {
            while (gedcom.getUndoNb() > undoNb && gedcom.canUndo()) {
                gedcom.undoUnitOfWork(false);
            }
        }

    }

    public void okCallBack(ActionEvent event) {
        commit(event == null);
    }

    public void cancelCallBack(ActionEvent event) {
        // Force reload and remember intra-entity selections
        editor.setGedcomHasChanged(true);
        // Redisplay context from scratch
        Context ctx = editor.getContext();
        editor.setContext(ctx);
        setManagerContext(ctx.getEntity());
    }

    
    public TopComponent cloneComponent() {
        if (getContext() == null) {
            return null;
        }

        AncestrisTopComponent topComponent = new CygnusTopComponent();
        topComponent.init(getContext());
        return topComponent;
    }

    public Editor getEditor() {
        return editor;
    }
    
    /**
     * Find the indi related to the context's entity
     * @param ctx
     * @return 
     */
    private Context adjustContext(Context ctx) {
        Context newContext = ctx;
        
        String type = ctx.getEntity().getTag();
        
        if (type.equals(Gedcom.FAM)) {
            // For a FAMily, indi displayed is husband, otherwise wife, and event will be the FAM event
            // FAM => Indi
            Fam fam = (Fam) ctx.getEntity();
            newContext = getIndiContextFromFam(fam, ctx);

        // For another entity, indi displayed will be the first indi using that entity, on the corresponding event (general or event)
        // If not used by any indi, display default panel
        } else if (type.equals(Gedcom.SUBM)) {
            // SUBM => Indi
            Submitter subm = (Submitter) ctx.getEntity();
            newContext = getIndiContextFromSubmitter(subm, ctx);
            
        } else if (type.equals(Gedcom.REPO)) {
            // REPO => SOUR 
            Repository repo = (Repository) ctx.getEntity();
            newContext = getIndiContextFromRepository(repo, ctx);
            
        } else if (type.equals(Gedcom.SOUR)) {
            // SOUR => Indi or NOTE or OBJE (but avoid loop coming back to same SOUR, NOTE or OBJE)
            Source source = (Source) ctx.getEntity();
            newContext = getIndiContextFromSource(source, ctx);
            
        } else if (type.equals(Gedcom.OBJE)) {
            // OBJE => Indi or SUBM or SOUR (avoiding loop)
            Media media = (Media) ctx.getEntity();
            newContext = getIndiContextFromMedia(media, ctx);
            
        } else if (type.equals(Gedcom.NOTE)) {
            // NOTE => Indi or SUBM or REPO or SOUR or OBJE (avoiding loop)
            Note note = (Note) ctx.getEntity();
            newContext = getIndiContextFromNote(note, ctx);

        
        } else if (type.equals(Gedcom.NOTE) || type.equals(Gedcom.SOUR) || type.equals(Gedcom.OBJE) || type.equals(Gedcom.REPO) || type.equals(Gedcom.SUBM)) {
            Entity mainEnt = ctx.getEntity();
            Context tmpCtx = getIndiContextFromEntity(mainEnt, ctx);
            if (tmpCtx != null) {
                newContext = tmpCtx;
            }
        }
        
        return newContext;
    }

    
    private Context getIndiContextFromFam(Fam fam, Context ctx) {
        Context retCtx = null;
        Indi indi = fam.getHusband();
        if (indi == null) {
            indi = fam.getWife();
        }
        if (indi == null) {
            try {
                indi = fam.getChild(0);
            } catch (Exception e) {
            }
        }
        if (indi != null) {
            retCtx = new Context(indi);
            List<Entity> entities = new ArrayList<>();
            entities.add(indi);
            List<Property> properties = (List<Property>) ctx.getProperties();
            Property union = fam.getProperty("MARR");
            if ((properties == null || properties.isEmpty())) {
                properties = new ArrayList<>();
                if (union != null) {
                    properties.add(union);
                } else {
                    properties.add(fam.getProperty(0));
                }
            }
            retCtx = new Context(ctx.getGedcom(), entities, properties);
        }
        return retCtx != null ? retCtx : ctx;
    }
    
    private Context getIndiContextFromEntity(Entity entity, Context ctx) {
        Context retCtx = null;
        Entity target = null;
        for (PropertyXRef xref : entity.getProperties(PropertyXRef.class)) {
            target = xref.getTargetEntity();
            if (target instanceof Indi || target instanceof Fam) {
                retCtx = new Context(xref.getTargetParent());
                String type = retCtx.getEntity().getTag();
                if (type.equals(Gedcom.FAM)) {
                    Fam fam = (Fam) retCtx.getEntity();
                    retCtx = getIndiContextFromFam(fam, retCtx);
                }
                break;
            }
        }
        return retCtx;
    }

    private Context getIndiContextFromSubmitter(Submitter subm, Context ctx) {
        Context retCtx = null;
        retCtx = getIndiContextFromEntity(subm, ctx);
        return retCtx != null ? retCtx : ctx;
    }
    
    private Context getIndiContextFromRepository(Repository repo, Context ctx) {
        Context retCtx = null;
        Entity target = null;
        for (PropertyXRef xref : repo.getProperties(PropertyXRef.class)) {
            target = xref.getTargetEntity();
            if (target instanceof Source) {
                retCtx = getIndiContextFromSource((Source)target, ctx);
                if (retCtx != ctx) {
                    break;
                }
            }
        }
        return retCtx != null ? retCtx : ctx;
    }
    
    private Context getIndiContextFromSource(Source source, Context ctx) {
        Context retCtx = null;
        retCtx = getIndiContextFromEntity(source, ctx);
        if (retCtx == null) {  // if source not directly linked to an indi or a fam, find if it is linked to a OBJE or a NOTE...
            Entity target = null;
            for (PropertyXRef xref : source.getProperties(PropertyXRef.class)) {
                target = xref.getTargetEntity();
                if (target instanceof Note) {
                    //retCtx = getIndiContextFromNote(target, ctx); // watch out for infinite loop
                    break;
                } else if (target instanceof Media) {
                    //retCtx = getIndiContextFromMedia(target, ctx); // watch out for infinite loop
                    break;
                }
            }
        }
        return retCtx != null ? retCtx : ctx;
    }
    
    private Context getIndiContextFromMedia(Media media, Context ctx) {
        Context retCtx = null;
        retCtx = getIndiContextFromEntity(media, ctx);
        if (retCtx == null) {  // if media not directly linked to an indi or a fam, find if it is linked to a SUBM or a SOUR...
            Entity target = null;
            for (PropertyXRef xref : media.getProperties(PropertyXRef.class)) {
                target = xref.getTargetEntity();
                if (target instanceof Submitter) {
                    retCtx = getIndiContextFromSubmitter((Submitter) target, ctx);
                    if (retCtx != ctx) {
                        break;
                    }
                } else if (target instanceof Source) {
                    retCtx = getIndiContextFromSource((Source)target, ctx);
                    if (retCtx != ctx) {
                        break;
                    }
                }
            }
        }
        return retCtx != null ? retCtx : ctx;
    }
    
    private Context getIndiContextFromNote(Note note, Context ctx) {
        Context retCtx = null;
        retCtx = getIndiContextFromEntity(note, ctx);
        if (retCtx == null) {  // if note not directly linked to an indi or a fam, find if it is linked to a OBJE, SOUR, SUBM or REPO...
            Entity target = null;
            for (PropertyXRef xref : note.getProperties(PropertyXRef.class)) {
                target = xref.getTargetEntity();
                if (target instanceof Media) {
                    retCtx = getIndiContextFromMedia((Media)target, ctx);
                    if (retCtx != ctx) {
                        break;
                    }
                } else if (target instanceof Source) {
                    retCtx = getIndiContextFromSource((Source)target, ctx);
                    if (retCtx != ctx) {
                        break;
                    }
                } else if (target instanceof Submitter) {
                    retCtx = getIndiContextFromSubmitter((Submitter)target, ctx);
                    if (retCtx != ctx) {
                        break;
                    }
                } else if (target instanceof Repository) {
                    retCtx = getIndiContextFromRepository((Repository)target, ctx);
                    if (retCtx != ctx) {
                        break;
                    }
                }
            }
        }
        return retCtx != null ? retCtx : ctx;
    }

    // Force content of manager lookup to be on the individual, not the families or sub-properties
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
    
    
    
    
    
    
    /**
     * Classes
     */
    
    private class Callback extends GedcomListenerAdapter {

        @Override
        public void gedcomWriteLockAcquired(Gedcom gedcom) {
            // Changes we have to commit?
            if (!isChangeSource) {
                commit(false);
            }
        }
    }

    
    
    private class UndoRedoListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (editor != null) {
                // Set force reload on
                editor.setGedcomHasChanged(true);
                // Reset context
                Context ctx = editor.getContext();
                editor.setContext(adjustContext(ctx));
                setManagerContext(ctx.getEntity());
            }
        }
    }
}
