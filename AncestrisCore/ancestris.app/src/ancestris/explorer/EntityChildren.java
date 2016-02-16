/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.explorer;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.GedcomListener;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import ancestris.core.pluginservice.AncestrisPlugin;
import java.util.ArrayList;
import java.util.Collection;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author daniel
 */
class EntityChildren extends Children.SortedArray implements GedcomListener {

    private final GedcomEntities entities;

    public EntityChildren(GedcomEntities entities) {
        super();
        this.entities = entities;
        entities.getGedcom().addGedcomListener(this);
    }

    @Override
    public Collection<Node> initCollection() {
        ArrayList<Node> result = new ArrayList<Node>();
        for (Entity e : entities.getEntities()) {
            result.add(new EntityNode(e));
        }
        return result;
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        AncestrisPlugin.register(this);
    }

    @Override
    protected void removeNotify() {
        super.removeNotify();
        AncestrisPlugin.unregister(this);
    }

    /**
     * gedcom callback
     */
    @Override
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        // an entity we're not looking at?
        if (!entities.getTag().equals(entity.getTag())) {
            return;
        }
        add(new EntityNode[]{new EntityNode(entity)});

        Node node = this.getNode();
        if (node instanceof EntitiesNode) {
            EntitiesNode esn = (EntitiesNode) node;
            esn.updateDisplay();
        }

        refresh();
    }

    /**
     * gedcom callback
     */
    @Override
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        // an entity we're not looking at?
        if (!entities.getTag().equals(entity.getTag())) {
            return;
        }
        for (Node n : getNodes()) {
            EntityNode en = (EntityNode) n;
            if (en.getEntity().equals(entity)) {
                remove(new EntityNode[]{en});
            }
        }

        Node node = this.getNode();
        if (node instanceof EntitiesNode) {
            EntitiesNode esn = (EntitiesNode) node;
            esn.updateDisplay();
        }

        refresh();
    }

    /**
     * gedcom callback
     */
    @Override
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        invalidate(gedcom, property.getEntity(), added.getPath());
        
    }

    /**
     * gedcom callback
     */
    @Override
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        invalidate(gedcom, property.getEntity(), property.getPath());
    }

    /**
     * gedcom callback
     */
    @Override
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        invalidate(gedcom, property.getEntity(), new TagPath(property.getPath(), deleted.getTag()));
    }

    
    
    
    private void invalidate(Gedcom gedcom, Entity entity, TagPath path) {
        if (entities.getTag().equals(Gedcom.FAM) && entity instanceof Indi) {
            for (Node n : getNodes()) {
                EntityNode en = (EntityNode) n;
                Indi i = (Indi) entity;
                Fam f = (Fam) en.getEntity();
                if (f.getSpouses().contains(i)) {
                    GedcomExplorerTopComponent.getDefault().addToList(en);
                    //en.fireChanges();
                }

            }
        }
        if (!entities.getTag().equals(entity.getTag())) {
            return;
        }
        for (Node n : getNodes()) {
            EntityNode en = (EntityNode) n;
            if (en.getEntity().equals(entity)) {
                GedcomExplorerTopComponent.getDefault().addToList(en);
                //en.fireChanges();
            }
        }
        //refresh();
    }

}
