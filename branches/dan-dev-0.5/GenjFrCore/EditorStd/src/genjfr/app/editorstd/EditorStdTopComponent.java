/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.app.editorstd;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomException;
import genj.gedcom.UnitOfWork;
import genjfr.app.AncestrisTopComponent;
import genjfr.app.App;
import genjfr.app.GenjViewInterface;
import genjfr.app.pluginservice.GenjFrPlugin;
import genjfr.explorer.ExplorerNode;
import java.awt.Image;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.ActionMap;
import javax.swing.GroupLayout;
import javax.swing.ToolTipManager;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultEditorKit.CopyAction;
import javax.swing.text.DefaultEditorKit.CutAction;
import javax.swing.text.DefaultEditorKit.PasteAction;
import org.openide.util.Exceptions;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;
import org.openide.awt.UndoRedo;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//genjfr.app.editorstd//EditorStd//EN",
autostore = false)
@ServiceProvider(service = GenjViewInterface.class)
public final class EditorStdTopComponent extends AncestrisTopComponent implements LookupListener {

    // Path to the icon used by the component and its open action */
    static final String ICON_PATH = "genjfr/app/editorstd/images/editorStd.png";
    private static final String PREFERRED_ID = "EditorStdTopComponent";
    //
    // Keep track of opened panels for each editor
    private HashSet<EntityPanel> panels = new HashSet<EntityPanel>();
    //
    // Variables per TopComponent instance
    private Lookup.Result result = null;
    private EntityPanel panelOn = null;
    private Entity previousSelectedEntity = null;
    //
    // Undo/Redo manager
    private UndoRedo.Manager URmanager = new UndoRedo.Manager();
    //
    // Save cookie
    private DummyNode dummyNode;

    public EditorStdTopComponent() {
        super();
    }

    public EditorStdTopComponent(Context context) {
        super();
        init(context);
    }

    @Override
    public Image getImageIcon() {
        return ImageUtilities.loadImage(ICON_PATH, true);
    }

    @Override
    public void setName() {
        setName(NbBundle.getMessage(EditorStdTopComponent.class, "CTL_EditorStdTopComponent"));
    }

    @Override
    public void setToolTipText() {
        setToolTipText(NbBundle.getMessage(EditorStdTopComponent.class, "HINT_EditorStdTopComponent"));
    }

    @Override
    public void init(Context context) {
        super.init(context);
        ToolTipManager.sharedInstance().setDismissDelay(10000);
    }

    @Override
    public void refreshPanel(Context context) {
        setPanel(context.getEntity());
    }

    @Override
    public boolean createPanel() {
        // TopComponent window parameters
        initComponents();

        // Create a dummy node for the save button
        setActivatedNodes(new Node[]{dummyNode = new DummyNode()});

        // Set Panel with entity
        if (getContext() != null && getContext().getEntity() != null) {
            setPanel(getContext().getEntity());
        }
        return true;
    }

    public static EditorStdTopComponent updateInstances() {
        Context c = App.center.getSelectedContext(true);
        if (c == null || c.getGedcom() == null) {
            return null;
        }
        for (EditorStdTopComponent editor : GenjFrPlugin.lookupAll(EditorStdTopComponent.class)) {
            if ((c.getGedcom().equals(editor.getContext().getGedcom()))) {
                editor.requestActive();
                return editor;
            }
        }

        EditorStdTopComponent editor = new EditorStdTopComponent(c);
        GenjFrPlugin.register(editor);
        editor.open();
        editor.requestActive();
        return editor;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EditorStdTopComponent.class, "EditorStdTopComponent.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel1)
                .addContainerGap(445, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(352, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(35, 35, 35))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables

    @Override
    @SuppressWarnings("unchecked")
    public void componentOpened() {
        Lookup.Template tpl = new Lookup.Template(ExplorerNode.class);
        result = Utilities.actionsGlobalContext().lookup(tpl);
        result.addLookupListener(this);
        Toolbar tb = ToolbarPool.getDefault().findToolbar("EditorStd");
        if (!tb.isVisible()) {
            tb.setVisible(true);
        }
        tb = ToolbarPool.getDefault().findToolbar("EditorIndi");
        if (!tb.isVisible()) {
            tb.setVisible(true);
        }
        // Pour l'instant, on force les petits icones vue que les grandes ne sont pas disponibles
        ToolbarPool.getDefault().setPreferredIconSize(16);
        ToolbarPool.getDefault().setConfiguration(ToolbarPool.getDefault().getConfiguration());
    }

    @Override
    public void componentClosed() {
        Toolbar tb = ToolbarPool.getDefault().findToolbar("EditorStd");
        if (tb.isVisible()) {
            tb.setVisible(false);
        }
        tb = ToolbarPool.getDefault().findToolbar("EditorIndi");
        if (tb.isVisible()) {
            tb.setVisible(false);
        }
        result.removeLookupListener(this);
        result = null;
        GenjFrPlugin.unregister(this);
    }

    @Override
    protected void componentActivated() {
        ActionMap actionMap = getActionMap();
        actionMap.put(DefaultEditorKit.copyAction, new DefaultEditorKit.CopyAction());
        actionMap.put(DefaultEditorKit.cutAction, new DefaultEditorKit.CutAction());
        actionMap.put(DefaultEditorKit.pasteAction, new DefaultEditorKit.PasteAction());
        super.componentActivated();
    }

    @Override
    protected void componentDeactivated() {
        ActionMap actionMap = getActionMap();
        actionMap.put(DefaultEditorKit.copyAction, SystemAction.get(org.openide.actions.CopyAction.class));
        actionMap.put(DefaultEditorKit.cutAction, SystemAction.get(org.openide.actions.CutAction.class));
        actionMap.put(DefaultEditorKit.pasteAction, SystemAction.get(org.openide.actions.PasteAction.class));
        super.componentDeactivated();
    }

    @Override
    public boolean canClose() {
        boolean canClose = true;
        for (Iterator it = panels.iterator(); it.hasNext();) {
            EntityPanel panel = (EntityPanel) it.next();
            if (panel.isModified()) {
                canClose = false;
            }
        }
        if (!canClose) {
            NotifyDescriptor d = new NotifyDescriptor.Confirmation(NbBundle.getMessage(SubmitterPanel.class, "CTL_EditionUnsaved", getGedcom().getName()),
                    NbBundle.getMessage(SubmitterPanel.class, "CTL_AskConfirmation"),
                    NotifyDescriptor.YES_NO_CANCEL_OPTION);
            Object ret = DialogDisplayer.getDefault().notify(d);
//            Would be used to use cancel as well but too complex to intercept at application closure
            if (ret.equals(NotifyDescriptor.CANCEL_OPTION)) {
                return false;
            }
            if (ret.equals(NotifyDescriptor.OK_OPTION)) {
                saveEditor();
            }
        }
        return super.canClose();
    }

    public void saveEditor() {
        for (Iterator it = panels.iterator(); it.hasNext();) {
            final EntityPanel panel = (EntityPanel) it.next();
            try {
                getContext().getGedcom().doUnitOfWork(new UnitOfWork() {

                    @Override
                    public void perform(Gedcom gedcom) throws GedcomException {
                        panel.saveEntity();
                    }
                });
            } catch (GedcomException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        App.workbenchHelper.saveGedcom(getContext());
    }

    @Override
    public void resultChanged(LookupEvent lookupEvent) {
        // Do not bother if context is null, because then panels are not yet ready
        if (getContext() == null) {
            return;
        }
        // Get selected node
        Lookup.Result r = (Lookup.Result) lookupEvent.getSource();
        Collection c = r.allInstances();
        if (!c.isEmpty()) {
            ExplorerNode o = (ExplorerNode) c.iterator().next();
            if (o != null && getContext().getGedcom().equals(o.getContext().getGedcom())) {
                setContext(o.getContext());
            }
        }
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        if (getGedcom() != null) {
            p.setProperty("gedcom", getGedcom().getOrigin().toString());
        }
    }

    Object readProperties(java.util.Properties p) {
        readPropertiesImpl(p);
        return this;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        final String gedcomProperty = p.getProperty("gedcom");
        if (gedcomProperty == null) {
            close();
        } else {
            waitStartup(gedcomProperty);
        }
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    private void setPanel(Entity selectedEntity) {
        if (selectedEntity == null) {
            return;
        }

        // Get panel corresponding to entity
        EntityPanel jPanelEntity = EntityPanel.findInstance(selectedEntity);
        if (!panels.contains(jPanelEntity)) {
            panels.add(jPanelEntity);
        }

        // Set Undo Redo manager and save cookie (even if might have been done already)
        jPanelEntity.setManagers(URmanager, this);

        // Remove existing panel if any
        if (panelOn != null && panelOn != jPanelEntity) {
            jPanel1.remove(panelOn);
        }

        // Check if entity is different and was in modified state
        if (previousSelectedEntity != selectedEntity) {
            if (previousSelectedEntity != null) {
                panelOn.checkIfModified();
            }
            previousSelectedEntity = selectedEntity;
            jPanelEntity.loadEntity(selectedEntity);
        }

        // Set new panel on (Netbeans requires this lenghty code below apparently)
        GroupLayout mainPanelLayout = new GroupLayout(jPanel1);
        jPanel1.setLayout(mainPanelLayout);
        mainPanelLayout.setAutoCreateContainerGaps(true);
        mainPanelLayout.setAutoCreateGaps(true);
        GroupLayout.SequentialGroup hGroup = mainPanelLayout.createSequentialGroup();
        hGroup.addComponent(jPanelEntity);
        mainPanelLayout.setHorizontalGroup(hGroup);
        GroupLayout.SequentialGroup vGroup = mainPanelLayout.createSequentialGroup();
        vGroup.addComponent(jPanelEntity);
        mainPanelLayout.setVerticalGroup(vGroup);
        jPanelEntity.setVisible(true);

        // Remember displayed panel
        panelOn = jPanelEntity;
    }

    @Override
    public UndoRedo getUndoRedo() {
        return URmanager;
    }

    /*
     * Dummy node class for the save button
     */
    private class DummyNode extends AbstractNode {

        SaveCookieImpl saveImpl;

        public DummyNode() {
            super(Children.LEAF);
            saveImpl = new SaveCookieImpl();
        }

        @Override
        public String getDisplayName() {
            return getGedcom().getName();
        }

        private class SaveCookieImpl implements SaveCookie {

            @Override
            public void save() throws IOException {
                saveEditor();
                fire(false);
            }
        }

        public void fire(boolean modified) {
            if (modified) {
                getCookieSet().assign(SaveCookie.class, saveImpl);
            } else {
                getCookieSet().assign(SaveCookie.class);
            }
        }
    }

    public void setModified(boolean modified) {
        dummyNode.fire(modified);
    }
}
