/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.explorer;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import genj.util.swing.Action2;
import genj.util.swing.MenuHelper;
import genj.view.ActionProvider;
import genj.view.ActionProvider.Purpose;
import genj.view.ViewContext;
import genjfr.app.ActionClose;
import genjfr.app.ActionSave;
import genjfr.app.App;
import genjfr.app.pluginservice.GenjFrPlugin;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.MenuSelectionManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Cookie;
import org.openide.nodes.NodeTransfer;
import org.openide.util.actions.Presenter;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author daniel
 */
class GedcomFileNode extends AbstractNode {

    Context context;

    /** Creates a new instance of GedcomFileNode */
    public GedcomFileNode(Gedcom gedcom) {
        super(new EntitiesChildren(gedcom), Lookups.singleton(gedcom));
        context = new Context(gedcom);
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
                        getChildren().add(new Node[]{new EntitiesNode(movie)});
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
        if (context == null) {
            return null;
        }
        ViewContext vcontext = new ViewContext(context);
        if (vcontext == null) {
            return null;
        }
        vcontext.addAction(new ActionSave());
        vcontext.addAction(new ActionClose());
        // make sure context is valid

        List<Action2> nodeactions = new ArrayList<Action2>(8);
        // popup local actions?
        nodeactions.addAll(vcontext.getActions());

        // get and merge all actions
        List<Action2> groups = new ArrayList<Action2>(8);
        List<Action2> singles = new ArrayList<Action2>(8);
        Map<Action2.Group, Action2.Group> lookup = new HashMap<Action2.Group, Action2.Group>();

        for (Action2 action : getProvidedActions(vcontext)) {
            if (action instanceof Action2.Group) {
                Action2.Group group = lookup.get(action);
                if (group != null) {
                    group.add(new ActionProvider.SeparatorAction());
                    group.addAll((Action2.Group) action);
                } else {
                    lookup.put((Action2.Group) action, (Action2.Group) action);
                    groups.add((Action2.Group) action);
                }
            } else {
                singles.add(action);
            }
        }

        // add to menu
        nodeactions.add(null);
        nodeactions.addAll(groups);
        nodeactions.add(null);
        nodeactions.addAll(singles);

        // done
        return nodeactions.toArray(new Action[0]);
    }

    public static Action2.Group getProvidedActions(Context context) {
        Action2.Group group = new Action2.Group("");
        // ask the action providers
        for (ActionProvider provider : (List<ActionProvider>) GenjFrPlugin.lookupAll(ActionProvider.class)) {
            provider.createActions(context, Purpose.CONTEXT, group);
        }
        // done
        return group;
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
}
