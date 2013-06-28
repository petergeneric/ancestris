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
import genj.gedcom.*;
import genj.view.SelectionListener;
import java.io.IOException;
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
public class GedcomDataObject extends MultiDataObject implements SelectionListener,GedcomMetaListener {
    private Context context;
    private GedcomUndoRedo undoredo;
    
    private final Lookup lookup;
    private final InstanceContent lookupContents = new InstanceContent();

        private static Lookup.Result<Context> result;

//    private GedcomMgr gedcomMgr;
    /**
     * SaveCookie for this support instance. The cookie is adding/removing data
     * object's cookie set depending on if modification flag was set/unset.
     */
    private final SaveCookie saveCookie = new SaveCookie() {

        /**
         * Implements
         * <code>SaveCookie</code> interface.
         */
        public void save() throws IOException {
            GedcomDataObject.this.saveDocument();
        }
    };

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
        // associatelookup is 1 so super.getlookup is getCookieSet().getLookup()
        //lookup = new ProxyLookup(getCookieSet().getLookup(), new AbstractLookup(lookupContents));
        lookup = new ProxyLookup(super.getLookup(), new AbstractLookup(lookupContents));
        
        // register listener
        result = lookup.lookupResult(Context.class);
            result.addLookupListener(new LookupListener() {

            public void resultChanged(LookupEvent ev) {
                // notify
                //XXX: we must put selected nodes in global selection lookup (in fact use Explorer API)
                Context context = lookup.lookup(Context.class);
                if (context != null)
                for (SelectionListener listener : AncestrisPlugin.lookupAll(SelectionListener.class)) {
                    listener.setContext(context, false);
                }
            }
        });
        
        
        
        registerEditor("text/x-gedcom", true);
        //XXX: fix it
        this.context = GedcomMgr.getDefault().openGedcom(pf);
        context.getGedcom().addGedcomListener(this);
        GedcomDirectory.getDefault().registerGedcom(this);
        undoredo = new GedcomUndoRedo((context.getGedcom()));
        //XXX: is this still used?
        AncestrisPlugin.register(this);
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
        return new DataNode (this, Children.LEAF, getLookup());
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
     * @param <T>
     * @param clazz
     * @param instances 
     */
    public <T> void assign(Class<? extends T> clazz, T... instances) {
        for (T ic:lookup.lookupAll(clazz)){
            lookupContents.remove(ic);
        }
        for (T ic : instances){
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

    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        setModified(gedcom);
    }

    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        setModified(gedcom);
    }

    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        setModified(gedcom);
    }

    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        setModified(gedcom);
    }

    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        setModified(gedcom);
    }

    public void gedcomHeaderChanged(Gedcom gedcom) {
        setModified(gedcom);
    }
    
    public void gedcomWriteLockAcquired(Gedcom gedcom) {
    }

    public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
    }

    public void gedcomAfterUnitOfWork(Gedcom gedcom) {
    }

    public void gedcomWriteLockReleased(Gedcom gedcom) {
        try {
            getUndoRedo().gedcomUpdated(gedcom);
            setModified(gedcom);
        } catch (NullPointerException e) {}
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
    private void setModified(Gedcom g) {
        // Adds save cookie to the data object.
        if (getCookie(SaveCookie.class) == null) {
            getCookieSet().add(saveCookie);
        }
    }

    /**
     * Helper method. Removes save cookie from the data object.
     */
    private void clearModified() {

        // Remove save cookie from the data object.
        Cookie cookie = getCookie(SaveCookie.class);

        if (cookie != null && cookie.equals(saveCookie)) {
            getCookieSet().remove(saveCookie);
        }
    }

    private void saveDocument() {
        GedcomMgr.getDefault().saveGedcom(context,getPrimaryFile());
        clearModified();
    }

    // Remember context
    @Override
    public void setContext(Context context, boolean isActionPerformed) {
        if (this.context != null && this.context.sameGedcom(context)) {
            this.context = context;
        }
    }

    public Context getContext() {
        return context;
    }
}
