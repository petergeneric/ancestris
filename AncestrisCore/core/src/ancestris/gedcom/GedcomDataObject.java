/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.gedcom;

import ancestris.core.pluginservice.AncestrisPlugin;
import static ancestris.gedcom.GedcomMgr.LOG;
import ancestris.util.TimingUtility;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Property;
import genj.util.Resources;
import genj.view.SelectionListener;
import java.io.IOException;
import java.util.logging.Level;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

//XXX: change GedcomListener to pcl?
//XXX: create a callback that extends gedcomadapter?
public class GedcomDataObject extends MultiDataObject implements SelectionListener, GedcomMetaListener {

    final static Resources RES = Resources.get(GedcomMgr.class);
    private Context context;
    private GedcomUndoRedo undoredo;
    private final Lookup lookup;
    private final InstanceContent lookupContents = new InstanceContent();
    private final Lookup.Result<Context> result;
    private FileObject fileObject = null;
    private boolean isCancelled = false;
//    private GedcomMgr gedcomMgr;
    /**
     * SaveCookie for this support instance. The cookie is adding/removing data
     * object's cookie set depending on if modification flag was set/unset.
     */
    private final SaveCookie saveCookie;

    //XXX: will have to rework on this
    //XXX: FileObject are saved in a pool by DataOject loader. So if the same fo is loaded twice
    // gedcomMgr is not called again. Progress bar is not shown and may be other dysfunction
    /*
     * Note: we use this faq: http://wiki.netbeans.org/DevFaqNodesCustomLookup
     * to have a custom lookup updated using an InstanceContent. That way this lookup may contain
     * selected context use for SelectionSink replacement logic
     */
    public GedcomDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        fileObject = pf;
        this.saveCookie = new SaveCookie() {

            /**
             * Implements
             * <code>SaveCookie</code> interface.
             */
            @Override
            public void save() throws IOException {
                GedcomDataObject.this.saveDocument();
            }
        };
        // associatelookup is 1 so super.getlookup is getCookieSet().getLookup()
        //lookup = new ProxyLookup(getCookieSet().getLookup(), new AbstractLookup(lookupContents));
        lookup = new ProxyLookup(super.getLookup(), new AbstractLookup(lookupContents));

        // register listener
        result = lookup.lookupResult(Context.class);
        result.addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                // notify
                //XXX: we must put selected nodes in global selection lookup (in fact use Explorer API)
                Context context = lookup.lookup(Context.class);
                if (context != null) {
                    for (SelectionListener listener : AncestrisPlugin.lookupAll(SelectionListener.class)) {
                        listener.setContext(context);
                    }
                }
            }
        });

        registerEditor("text/x-gedcom", true);
        load();
    }

    public boolean load() {
            this.context = GedcomMgr.getDefault().openGedcom(fileObject);
            if (context != null) {
                context.getGedcom().addGedcomListener(this);
                GedcomDirectory.getDefault().registerGedcom(this);
                undoredo = new GedcomUndoRedo((context.getGedcom()));
                AncestrisPlugin.register(this);
            } else {
                LOG.log(Level.SEVERE, "{0}: gedcomOpened", TimingUtility.getInstance().getTime());
                LOG.log(Level.SEVERE, "Unable to open file {0}", fileObject.getPath());
                return false;
            }
        return true;
    }

    public void setCancelled(boolean set) {
        isCancelled = set;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    protected Node createNodeDelegate() {
//        return super.createNodeDelegate();
        //FIXME: overidden as stated in http://wiki.netbeans.org/DevFaqNodesCustomLookup
        // FIXME: is this the same as super.createNodeDelegate()?
        return new DataNode(this, Children.LEAF, getLookup());
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    public InstanceContent getLookupContents() {
        return lookupContents;
    }

    /**
     * replace all instances of type clazz in lookup by new instances.
     * FIXME: There may be some optimization to do here...
     *
     * @param <T>
     *            param clazz
     *            param instances
     */
    public <T> void assign(Class<? extends T> clazz, T... instances) {
        for (T ic : lookup.lookupAll(clazz)) {
            lookupContents.remove(ic);
        }
        for (T ic : instances) {
            lookupContents.add(ic);
        }
    }

    public GedcomUndoRedo getUndoRedo() {
        return undoredo;
    }

    @MultiViewElement.Registration(displayName = "#LBL_Gedcom_EDITOR",
            iconBase = "ancestris/gedcom/Gedcom.png",
            mimeType = "text/x-gedcom",
            persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
            preferredID = "Gedcom",
            position = 1000)
    @Messages("LBL_Gedcom_EDITOR=Source")
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }

    @Override
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        updateModified();
    }

    @Override
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        updateModified();
    }

    @Override
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        updateModified();
    }

    @Override
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        updateModified();
    }

    @Override
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        updateModified();
    }

    @Override
    public void gedcomHeaderChanged(Gedcom gedcom) {
        updateModified();
    }

    @Override
    public void gedcomWriteLockAcquired(Gedcom gedcom) {
    }

    @Override
    public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
    }

    @Override
    public void gedcomAfterUnitOfWork(Gedcom gedcom) {
    }

    @Override
    public void gedcomWriteLockReleased(Gedcom gedcom) {
        try {
            getUndoRedo().gedcomUpdated(gedcom);
            updateModified();
        } catch (NullPointerException e) {
        }
    }

    /**
     * Helper method. Adds save cookie to the data object.
     */
//    or use cookieset.assign:
//    if (modified) {
//        getCookieSet().assign(SaveCookie.class, saveImpl);
//    } else {
//        getCookieSet().assign(SaveCookie.class);
//    }
    private void updateModified() {
        if (context.getGedcom().hasChanged()) {
            if (getLookup().lookup(SaveCookie.class) == null) {
                getCookieSet().add(saveCookie);
            }
        } else {
            // Remove save cookie from the data object.
            Cookie cookie = getLookup().lookup(SaveCookie.class);

            if (cookie != null && cookie.equals(saveCookie)) {
                getCookieSet().remove(saveCookie);
            }
        }
    }

    private void saveDocument() {
        GedcomMgr.getDefault().saveGedcom(context, getPrimaryFile());
        updateModified();
    }

    // Remember context
    @Override
    public void setContext(Context context) {
        if (this.context != null && this.context.sameGedcom(context)) {
            this.context = context;
        }
    }

    public Context getContext() {
        return context;
    }
}
