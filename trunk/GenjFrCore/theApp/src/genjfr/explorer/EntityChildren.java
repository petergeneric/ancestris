/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.explorer;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import genjfr.app.pluginservice.GenjFrPlugin;
import java.util.ArrayList;
import java.util.Collection;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author daniel
 */
class EntityChildren extends Children.SortedArray implements GedcomListener{

    private final GedcomEntities entities;

    public EntityChildren(GedcomEntities entities) {
        super();
        this.entities = entities;
    }

    @Override
    public Collection<Node> initCollection(){
        ArrayList<Node> result = new ArrayList();
        for (Entity e:entities.getEntities())
            result.add(new EntityNode(e));
        return result;
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        GenjFrPlugin.register(this);
    }

    @Override
    protected void removeNotify() {
        super.removeNotify();
        GenjFrPlugin.unregister(this);
    }



    /** gedcom callback */
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
      // an entity we're not looking at?
      if (!entities.getTag().equals(entity.getTag()))
        return;
      add(new EntityNode[]{new EntityNode(entity)});

      refresh();
    }

    /** gedcom callback */
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
      // an entity we're not looking at?
      if (!entities.getTag().equals(entity.getTag()))
        return;

      add(new EntityNode[]{new EntityNode(entity)});

      refresh();
    }

    /** gedcom callback */
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
      invalidate(gedcom, property.getEntity(), added.getPath());
    }

    /** gedcom callback */
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
      invalidate(gedcom, property.getEntity(), property.getPath());
    }

    /** gedcom callback */
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
      invalidate(gedcom, property.getEntity(), new TagPath(property.getPath(), deleted.getTag()));
    }

    private void invalidate(Gedcom gedcom, Entity entity, TagPath path) {
      if (entities.getTag().equals(Gedcom.FAM) && entity instanceof Indi) {
          for (Node n:getNodes()){
              EntityNode en = (EntityNode)n;
              Indi i = (Indi)entity;
              Fam f = (Fam)en.getEntity();
              if (f.getSpouses().contains(i))
                en.fireChanges();

          }
      }
      if (!entities.getTag().equals(entity.getTag()))
            return;
      for (Node n:getNodes()){
          EntityNode en = (EntityNode)n;
          if (en.getEntity().equals(entity)){
              en.fireChanges();
          }
      }
      refresh();
//      // a path we're interested in?
//      TagPath[] paths = mode.getPaths();
//      for (int i=0;i<paths.length;i++) {
//        if (paths[i].equals(path)) {
//          for (int j=0;j<rows.size();j++) {
//            if (rows.get(j)==entity) {
//                fireRowsChanged(j,j,i);
//                return;
//            }
//          }
//        }
//      }
      // done
    }

}
