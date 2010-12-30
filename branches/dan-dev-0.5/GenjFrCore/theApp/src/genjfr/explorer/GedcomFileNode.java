/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.explorer;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.swing.Action2;
import genjfr.app.ActionClose;
import genjfr.app.ActionSave;
import genjfr.util.MyContext;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.nodes.NodeTransfer;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author daniel
 */
class GedcomFileNode extends AbstractNode implements ExplorerNode {

    Context context;
    Action2 closeAction = null;
    Action2 saveAction = null;

    /** Creates a new instance of GedcomFileNode */
    public GedcomFileNode(Gedcom gedcom) {
        super(new EntitiesChildren(gedcom), Lookups.singleton(gedcom));
        context = new Context(gedcom);
        closeAction = new ActionClose(context);
        saveAction = new ActionSave(context);
        setDisplayName(gedcom.getName());
    }

    public PasteType getDropType(Transferable t, final int action, int index) {
        final Node dropNode = NodeTransfer.node(t,
                DnDConstants.ACTION_COPY_OR_MOVE + NodeTransfer.CLIPBOARD_CUT);
        if (null != dropNode) {
            final GedcomEntities movie = (GedcomEntities) dropNode.getLookup().lookup(GedcomEntities.class);
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

    public Cookie getCookie(Class clazz) {
        Children ch = getChildren();

        if (clazz.isInstance(ch)) {
            return (Cookie) ch;
        }

        return super.getCookie(clazz);
    }

    protected void createPasteTypes(Transferable t, List s) {
        super.createPasteTypes(t, s);
        PasteType paste = getDropType(t, DnDConstants.ACTION_COPY, -1);
        if (null != paste) {
            s.add(paste);
        }
    }

    public Action[] getActions(boolean isContext) {
        MyContext vcontext = new MyContext(context);
        if (vcontext == null) {
            return null;
        }
        List<Action2> nodeactions = new ArrayList<Action2>(8);
        nodeactions.add(saveAction);
        nodeactions.add(closeAction);
        nodeactions.addAll(vcontext.getPopupActions());

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
