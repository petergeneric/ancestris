/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package genjfr.explorer;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomMetaListener;
import genj.gedcom.Property;
import genjfr.util.GedcomDirectory;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author daniel
 */
class GedcomFileChildren extends Children.Keys {

    private GedcomFileListener model;

    public GedcomFileChildren() {
        model = new GedcomFileListener(this);
    }

    protected Node[] createNodes(Object key) {
        Gedcom obj = (Gedcom) key;
        return new Node[]{new GedcomFileNode(obj)};
    }

    protected void addNotify() {
        super.addNotify();
        GedcomDirectory.getInstance().addListener(model);
        updateGedcoms();
    }

    protected void removeNotify() {
        GedcomDirectory.getInstance().removeListener(model);
        super.removeNotify();
    }

    void updateGedcoms() {
        List<Context> contexts = GedcomDirectory.getInstance().getGedcoms();
        List<Gedcom> gedcoms = new ArrayList<Gedcom>();
        for (Context context : contexts) {
            gedcoms.add(context.getGedcom());
        }
        setKeys(gedcoms);
    }

    /**
     * our model
     */
    private class GedcomFileListener implements GedcomDirectory.Listener, GedcomMetaListener {

        GedcomFileChildren children;

        public GedcomFileListener(GedcomFileChildren gfc) {
            this.children = gfc;
        }

        public void gedcomRegistered(Context context) {
            context.getGedcom().addGedcomListener(this);
            children.updateGedcoms();
        }

        public void gedcomUnregistered(Context context) {
            context.getGedcom().removeGedcomListener(this);
            children.updateGedcoms();
        }

        public void gedcomHeaderChanged(Gedcom gedcom) {
        }

        public void gedcomWriteLockAcquired(Gedcom gedcom) {
        }

        public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
        }

        public void gedcomAfterUnitOfWork(Gedcom gedcom) {
        }

        public void gedcomWriteLockReleased(Gedcom gedcom) {
        }

        public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        }

        public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        }

        public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        }

        public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        }

        public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        }
    }
}
