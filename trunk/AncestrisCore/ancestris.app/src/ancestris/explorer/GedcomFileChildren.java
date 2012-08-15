/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.explorer;

import ancestris.gedcom.GedcomDirectory;
import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.util.ArrayList;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author daniel
 */
class GedcomFileChildren extends Children.Keys<Gedcom> {

//    private GedcomFileListener model;
//
//    public GedcomFileChildren() {
//        model = new GedcomFileListener(this);
//    }

    protected Node[] createNodes(Gedcom key) {
        return new Node[]{new GedcomFileNode(key)};
    }

    protected void addNotify() {
        super.addNotify();
//XXX:        GedcomMgr.getDefault().addListener(model);
        updateGedcoms();
    }

    protected void removeNotify() {
//XXX:        GedcomMgr.getDefault().removeListener(model);
        super.removeNotify();
    }

    void updateGedcoms() {
        List<Context> contexts = GedcomDirectory.getDefault().getContexts();
        List<Gedcom> gedcoms = new ArrayList<Gedcom>();
        for (Context context : contexts) {
            gedcoms.add(context.getGedcom());
        }
        setKeys(gedcoms);
    }

    //XXX:
    /**
     * our model
     */
//    private static class GedcomFileListener implements GedcomDirectory.Listener {
//
//        GedcomFileChildren children;
//
//        public GedcomFileListener(GedcomFileChildren gfc) {
//            this.children = gfc;
//        }
//
//        public void gedcomRegistered(Context context) {
//            children.updateGedcoms();
//        }
//
//        public void gedcomUnregistered(Context context) {
//            children.updateGedcoms();
//        }
//
//    }
}
