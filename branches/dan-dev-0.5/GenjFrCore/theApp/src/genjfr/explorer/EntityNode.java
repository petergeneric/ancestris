/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.explorer;

import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import java.awt.Image;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author daniel
 */
class EntityNode extends AbstractNode implements Comparable<EntityNode>{
    Entity entity;

    public EntityNode(Entity e) {
        super(Children.LEAF, Lookups.fixed( new Object[] {e} ) );
        entity =  e;
        if (entity instanceof Indi) {
            Indi i = (Indi)entity;
            setDisplayName(i.getName());
        } else if (entity instanceof Fam){
            Fam f = (Fam) entity;
            setDisplayName(f.toString(false));
        } else {
            setDisplayName(entity.toString(false));
        }
    }

    @Override
    public String getHtmlDisplayName(){
        return null;
    }

    @Override
    public Image getIcon(int type) {
        return entity.getImage().getImage();
    }

    Entity getEntity(){
        return entity;
    }

    public int compareTo(EntityNode that) {
        return entity.compareTo(that.getEntity());
    }
}
