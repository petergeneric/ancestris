/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.explorer;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import genj.gedcom.Property;
import ancestris.core.pluginservice.AncestrisPlugin;
import genj.gedcom.GedcomMetaListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author daniel & frédéric
 */
class EntityChildren extends Children.SortedArray implements GedcomMetaListener {

    private final GedcomEntities entities;   // entities of a given tag
    private final String tag;
    private final boolean tagIsFam;
    
    private TreeSet<Entity> addedEntities;    // entities of this type that were added
    private TreeSet<Entity> deletedEntities;  // entities of this type that were deleted
    private TreeSet<Entity> changedEntities;  // entities of this type that were changed

    public EntityChildren(GedcomEntities entities) {
        super();
        this.entities = entities;
        tag = entities.getTag();
        tagIsFam = tag.equals(Gedcom.FAM);
        entities.getGedcom().addGedcomListener(this);
        addedEntities = new TreeSet<Entity>();
        deletedEntities = new TreeSet<Entity>();
        changedEntities = new TreeSet<Entity>();
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
        if (entities != null) {
            entities.getGedcom().removeGedcomListener(this);
        }
        AncestrisPlugin.unregister(this);
    }

    /**
     * gedcom callback
     */
    @Override
    public void gedcomEntityAdded(Gedcom gedcom, Entity entity) {
        // only entities we're not looking at
        if (tag.equals(entity.getTag())) {
            addedEntities.add(entity);
            return;
        }
    }

    /**
     * gedcom callback
     */
    @Override
    public void gedcomEntityDeleted(Gedcom gedcom, Entity entity) {
        // only entities we're not looking at
        if (tag.equals(entity.getTag())) {
            deletedEntities.add(entity);
            return;
        }
    }

    /**
     * gedcom callback
     */
    @Override
    public void gedcomPropertyAdded(Gedcom gedcom, Property property, int pos, Property added) {
        invalidate(property.getEntity());
    }

    /**
     * gedcom callback
     */
    @Override
    public void gedcomPropertyChanged(Gedcom gedcom, Property property) {
        invalidate(property.getEntity());
    }

    /**
     * gedcom callback
     */
    @Override
    public void gedcomPropertyDeleted(Gedcom gedcom, Property property, int pos, Property deleted) {
        invalidate(property.getEntity());
    }
    
    
    private void invalidate(Entity entity) {
        // only entities we're not looking at
        if (tag.equals(entity.getTag()) || (tagIsFam && entity instanceof Indi)) {
            changedEntities.add(entity);
            return;
        }
    }

    @Override
    public void gedcomHeaderChanged(Gedcom gedcom) {
    }

    @Override
    public void gedcomWriteLockAcquired(Gedcom gedcom) {
        addedEntities.clear();
        deletedEntities.clear();
        changedEntities.clear();
    }

    @Override
    public void gedcomBeforeUnitOfWork(Gedcom gedcom) {
    }

    @Override
    public void gedcomAfterUnitOfWork(Gedcom gedcom) {
    }

    @Override
    public void gedcomWriteLockReleased(Gedcom gedcom) {
        
        // Check Entities Added to be refresh
        if (!addedEntities.isEmpty()) {
            for (Entity entity : addedEntities) {
                add(new EntityNode[]{new EntityNode(entity)});
            }
            Node node = this.getNode();
            if (node instanceof EntitiesNode) {
                EntitiesNode esn = (EntitiesNode) node;
                esn.updateDisplay();
            }
        }
        
        // Check Entities Deleted to be refresh
        if (!deletedEntities.isEmpty()) {
            for (Node n : getNodes()) {
                EntityNode en = (EntityNode) n;
                if (deletedEntities.contains(en.getEntity())) {
                    remove(new EntityNode[]{en});
                }
            }
            Node node = this.getNode();
            if (node instanceof EntitiesNode) {
                EntitiesNode esn = (EntitiesNode) node;
                esn.updateDisplay();
            }
        }
        
        // Check Entities Changed to be refresh
        if (!changedEntities.isEmpty()) {
            // Case of tagIsFam : look if node is a family whose one spouse is in the changed entities
            if (tagIsFam) {
                for (Node n : getNodes()) {
                    EntityNode en = (EntityNode) n;
                    Fam f = (Fam) en.getEntity();
                    Indi i = f.getHusband();
                    if (i != null && changedEntities.contains(i)) {
                        en.fireChanges();
                        continue;
                    }
                    i = f.getWife();
                    if (i != null && changedEntities.contains(i)) {
                        en.fireChanges();
                    }
                }
            } else {
            // General case
                for (Node n : getNodes()) {
                    EntityNode en = (EntityNode) n;
                    if (changedEntities.contains(en.getEntity())) {
                        en.fireChanges();
                    }
                }
            }
        }
        
        // Now Refresh
        refresh();
    }
    

}
