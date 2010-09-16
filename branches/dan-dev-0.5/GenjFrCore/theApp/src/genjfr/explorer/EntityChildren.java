/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.explorer;

import genj.gedcom.Entity;
import java.util.ArrayList;
import java.util.Collection;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author daniel
 */
class EntityChildren extends Children.SortedArray{

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

}
