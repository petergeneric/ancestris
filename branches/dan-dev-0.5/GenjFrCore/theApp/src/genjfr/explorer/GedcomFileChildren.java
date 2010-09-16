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

    private GedcomTableModel model;

    public GedcomFileChildren() {
        model = new GedcomTableModel(this);
    }

     protected Node[] createNodes(Object key) {
        Gedcom obj = (Gedcom) key;
        return new Node[] { new GedcomFileNode( obj ) };
    }

    protected void addNotify() {
        super.addNotify();
        GedcomDirectory.getInstance().addListener(model);
        updateGedcoms();
    }

    protected void removeNotify(){
        GedcomDirectory.getInstance().removeListener(model);
        super.removeNotify();
    }

    void updateGedcoms(){
        List<Context> contexts = GedcomDirectory.getInstance().getGedcoms();
        List<Gedcom> gedcoms = new ArrayList<Gedcom>();
        for (Context context:contexts){
            gedcoms.add(context.getGedcom());
        }
        setKeys(gedcoms);
    }

  /**
   * our model
   */
  private class GedcomTableModel  implements GedcomDirectory.Listener, GedcomMetaListener {

      GedcomFileChildren children;

      public GedcomTableModel(GedcomFileChildren gfc){
          this.children=gfc;
      }


    public void gedcomRegistered(int pos, Context context) {
      context.getGedcom().addGedcomListener(this);
      children.updateGedcoms();
    }

    public void gedcomUnregistered(int pos, Context context) {
      context.getGedcom().removeGedcomListener(this);
      children.updateGedcoms();
    }



        public void gedcomHeaderChanged(Gedcom gedcom) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void gedcomWriteLockAcquired(Gedcom gedcom) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void gedcomAfterUnitOfWork(Gedcom gedcom) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void gedcomWriteLockReleased(Gedcom gedcom) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }



}
