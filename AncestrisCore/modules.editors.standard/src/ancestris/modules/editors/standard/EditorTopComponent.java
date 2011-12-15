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
package ancestris.modules.editors.standard;

import ancestris.api.editor.Editor;
import ancestris.core.beans.ConfirmChangeWidget;
import ancestris.view.AncestrisDockModes;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.AncestrisViewInterface;
import genj.gedcom.Context;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.GedcomListenerAdapter;
import genj.gedcom.Indi;
import genj.gedcom.UnitOfWork;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.LC;
import net.miginfocom.swing.MigLayout;
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

    private static final String PREFERRED_ID = "AncestrisEditor";  // NOI18N
    private static EditorTopComponent factory;

    /*package*/ final static Logger LOG = Logger.getLogger("ancestris.edit");
    private final Map<Class, Editor> panels = new HashMap<Class, Editor>();
    private Callback callback = new Callback();
    private boolean isIgnoreSetContext = false;
    private boolean isChangeSource = false;
    private Editor editor;
    private ConfirmChangeWidget confirmPanel;
    private Gedcom gedcom;
    private JPanel editorContainer;
    private JLabel titleLabel;

    @Override
    public boolean createPanel() {
        panels.put(Fam.class, new FamPanel());
        panels.put(Indi.class, new IndiPanel());

        editorContainer = new JPanel(
                new MigLayout(new LC().fillX().hideMode(2),
                new AC().grow().fill()));
        titleLabel = new JLabel("");

        confirmPanel = new ConfirmChangeWidget(this);
        confirmPanel.setChanged(false);

        setContext(getContext(), true);

        titleLabel.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        editorContainer.add(titleLabel, new CC().dockNorth());
        editorContainer.add(confirmPanel, new CC().dockSouth());

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
        editorContainer.removeAll();

        // set new and restore context
        editor = set;
        if (editor != null) {
            if (old != null) {
                editor.setContext(old);
            }
            editorContainer.add(editor, new CC().growX());
            titleLabel.setText(editor.getTitle());
            editor.addChangeListener(confirmPanel);
        }
        editorContainer.add(titleLabel, new CC().dockNorth());
        editorContainer.add(confirmPanel, new CC().dockSouth());

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
        if (!isOpened())
            return;

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
            LOG.log(Level.WARNING, "error committing editor", t);
        } finally {
            isChangeSource = false;
            isIgnoreSetContext = false;
            confirmPanel.setChanged(false);
        }
    }

    @Override
    public void setContextImpl(Context context, boolean isActionPerformed) {
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
        commit();
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

    public void okCallBack(ActionEvent event) {
        commit(false);
    }

    public void cancelCallBack(ActionEvent event) {
        // re-set for cancel
        Context ctx = editor.getContext();
        editor.setContext(new Context());
        editor.setContext(ctx);
//        populate(toolbar);
    }

    @Override
    public Image getImageIcon() {
        if (editor == null) {
            return null;
        }
        return editor.getImageIcon();
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

        @Override
        public void gedcomWriteLockReleased(Gedcom gedcom) {

            // foreign change while we're looking?
            if (editor != null && !isChangeSource) {
                Context ctx = editor.getContext();
                editor.setContext(new Context());
                editor.setContext(ctx);
//                populate(toolbar);
            }
        }
    }
}
