package ancestris.modules.editors.genealogyeditor;

import ancestris.api.editor.Editor;
import ancestris.core.beans.ConfirmChangeWidget;
import ancestris.modules.editors.genealogyeditor.editors.FamilyEditor;
import ancestris.modules.editors.genealogyeditor.editors.IndividualEditor;
import ancestris.modules.editors.genealogyeditor.editors.MultiMediaObjectEditor;
import ancestris.modules.editors.genealogyeditor.editors.NoteEditor;
import ancestris.modules.editors.genealogyeditor.editors.RepositoryEditor;
import ancestris.modules.editors.genealogyeditor.editors.SourceEditor;
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
import genj.gedcom.UnitOfWork;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.RetainLocation;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "EditorTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@RetainLocation(AncestrisDockModes.EDITOR)
@ServiceProvider(service = AncestrisViewInterface.class)
public final class GenealogyEditorTopComponent
        extends AncestrisTopComponent
        implements TopComponent.Cloneable, ConfirmChangeWidget.ConfirmChangeCallBack {

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
            /*
             // foreign change while we're looking?
             if (editor != null && !isChangeSource) {
             Context ctx = editor.getContext();
             editor.setContext(new Context());
             editor.setContext(ctx);
             }
             */
        }
    }

    private final static Logger mLogger = Logger.getLogger("ancestris.edit");
    private final Map<Class<? extends Property>, Editor> panels = new HashMap<Class<? extends Property>, Editor>();
    private Editor editor;
    private ConfirmChangeWidget confirmPanel;
    private JPanel editorContainer;
    private JLabel titleLabel;
    private final Callback callback = new Callback();
    private Gedcom gedcom;
    private static GenealogyEditorTopComponent factory;
    private boolean isChangeSource = false;
    private boolean isIgnoreSetContext = false;

    public static synchronized GenealogyEditorTopComponent getFactory() {
        if (factory == null) {
            factory = new GenealogyEditorTopComponent();
        }
        return factory;
    }

    @Override
    public boolean createPanel() {
        panels.put(Indi.class, new IndividualEditor());
        panels.put(Fam.class, new FamilyEditor());
        panels.put(Note.class, new NoteEditor());
        panels.put(Source.class, new SourceEditor());
        panels.put(Repository.class, new RepositoryEditor());
        panels.put(Media.class, new MultiMediaObjectEditor());

        setContext(getContext());

        confirmPanel = new ConfirmChangeWidget(this);
        confirmPanel.setChanged(false);

        titleLabel = new JLabel("");
        titleLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titleLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N

        editorContainer = new JPanel(new BorderLayout());
        editorContainer.add(titleLabel, BorderLayout.NORTH);
        editorContainer.add(confirmPanel, BorderLayout.SOUTH);

        return true;
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    @Override
    public TopComponent cloneComponent() {
        if (getContext() == null) {
            return null;
        }

        AncestrisTopComponent topComponent = new GenealogyEditorTopComponent();
        topComponent.init(getContext());
        return topComponent;
    }

    @Override
    public void setContextImpl(Context context) {
        // see also EditView
        if (context == null) {
            return;
        }

        // commit if necessary
        commit();

        // disconnect from last gedcom ?
        if (context.getGedcom() != gedcom && gedcom != null) {
            gedcom.removeGedcomListener(callback);
            gedcom = null;
        }
        if (context.getGedcom() == null) {
            setEditor(null);
            confirmPanel.setChanged(false);
            return;
        }

        // connect to gedcom?
        if (context.getGedcom() != gedcom) {
            gedcom = context.getGedcom();
            gedcom.addGedcomListener(callback);
        }

        if (context.getEntity() == null) {
            return;
        }

        Editor panel = panels.get(context.getEntity().getClass());
        if (panel != null) {
            panel.setContext(context);
            setEditor(panel);
        }

        setPanel(editorContainer);
        repaint();
    }

    @Override
    public void okCallBack(ActionEvent event) {
        commit(false);
    }

    @Override
    public void cancelCallBack(ActionEvent event) {
        Context ctx = editor.getContext();
        editor.setContext(new Context());
        editor.setContext(ctx);
    }
    
    @Override
    public void setName() {
        if (editor != null && editor.getName() != null) {
            setName(editor.getName());
        } else {
            super.setName();
        }
    }

    /**
     * Set editor to use
     */
    private void setEditor(Editor set) {
        // commit old editor unless set==null
        Context old = null;
        if (set != null) {
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
            editorContainer.remove(editor);
            editor = null;
        }

//        editorContainer.removeAll();

        // set new and restore context
        editor = set;
        if (editor != null) {
            if (old != null) {
                editor.setContext(old);
            }
            editorContainer.add(editor, BorderLayout.CENTER);
            titleLabel.setText(editor.getTitle());
            editor.addChangeListener(confirmPanel);
        }
//        editorContainer.add(titleLabel, BorderLayout.NORTH);
//        editorContainer.add(confirmPanel, BorderLayout.SOUTH);

        // show
        revalidate();
        repaint();
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
            confirmPanel.setChanged(false);
            return;
        }

        try {

            isChangeSource = true;
            isIgnoreSetContext = true;

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
            mLogger.log(Level.WARNING, "error committing editor", t);
        } finally {
            isChangeSource = false;
            isIgnoreSetContext = false;
            confirmPanel.setChanged(false);
        }
    }

}
