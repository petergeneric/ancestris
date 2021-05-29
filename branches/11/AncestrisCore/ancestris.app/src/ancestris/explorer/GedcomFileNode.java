/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.explorer;

import ancestris.app.ActionClose;
import ancestris.app.ActionProperties;
import ancestris.app.ActionSave;
import ancestris.gedcom.GedcomDirectory;
import ancestris.gedcom.GedcomDirectory.ContextNotFoundException;
import ancestris.util.AUtilities;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.nodes.NodeTransfer;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author daniel
 */
//XXX: must be reworked (see PropertyNode class
class GedcomFileNode extends AbstractNode implements ExplorerNode {

    Context context;
    Action saveAction = null;
    Action propertiesAction = null;
    Action closeAction = null;

    /** Creates a new instance of GedcomFileNode */
    public GedcomFileNode(Gedcom gedcom) {
        super(new EntitiesChildren(gedcom), Lookups.singleton(gedcom));
        context = new Context(gedcom);
        saveAction = new ActionSave(context);
        propertiesAction = new ActionProperties(context);
        closeAction = new ActionClose(context);
        setDisplayName(gedcom.getDisplayName());
    }

    @Override
    public PasteType getDropType(Transferable t, final int action, int index) {
        final Node dropNode = NodeTransfer.node(t,
                DnDConstants.ACTION_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
        if (null != dropNode) {
            final GedcomEntities movie = dropNode.getLookup().lookup(GedcomEntities.class);
            if (null != movie && !this.equals(dropNode.getParentNode())) {
                return new PasteType() {

                    public Transferable paste() throws IOException {
                        getChildren().add(new Node[]{new EntitiesNode(context.getGedcom(),movie)});
                        if ((action & DnDConstants.ACTION_MOVE) != 0) {
                            dropNode.getParentNode().getChildren().remove(new Node[]{dropNode});
                        }
                        return null;
                    }
                };
            }
        }
        return null;
    }

    @Override
    public <T extends Cookie> T getCookie(Class<T>  clazz) {
        try {
            return GedcomDirectory.getDefault().getDataObject(context).getCookie(clazz);
        } catch (ContextNotFoundException ex) {
            return null;
        }
    }

    @Override
    protected void createPasteTypes(Transferable t, List<PasteType> s) {
        super.createPasteTypes(t, s);
        PasteType paste = getDropType(t, DnDConstants.ACTION_COPY, -1);
        if (null != paste) {
            s.add(paste);
        }
    }

    public Action[] getActions(boolean isContext) {
//        MyContext vcontext = new MyContext(context);
//        if (vcontext == null) {
//            return null;
//        }
        List<Action> nodeactions = new ArrayList<Action>(8);
        nodeactions.add(saveAction);
        nodeactions.add(propertiesAction);
        nodeactions.add(closeAction);
//        nodeactions.addAll(vcontext.getPopupActions());
        nodeactions.addAll(AUtilities.actionsForPath("Ancestris/Actions/GedcomProperty"));

        // done
        return nodeactions.toArray(new Action[0]);
    }

    public boolean canDestroy() {
        return true;
    }

    @Override
    public Image getIcon(int type) {
        return Gedcom.getImage().getImage();
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    public Context getContext() {
        return context;
    }

}
