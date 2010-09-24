/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.explorer;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Note;
import genj.util.swing.Action2;
import genj.view.SelectionSink;
import genjfr.util.MyContext;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author daniel
 */
class EntityNode extends AbstractNode implements Comparable<EntityNode>,ExplorerNode{
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
        } else if (entity instanceof Note){
            Note n = (Note) entity;
            setDisplayName(n.getDelegate().getLines()[0]);
        } else {
            setDisplayName(entity.toString(false));
        }
    }

    @Override
    public String getHtmlDisplayName(){
        return null;
    }

    @Override
    public Action getPreferredAction() {
        return new FireNodeSelection();
    }


    @Override
    public Image getIcon(int type) {
        return entity.getImage().getImage();
    }

    Entity getEntity(){
        return entity;
    }

    public int compareTo(EntityNode that) {
        if (entity instanceof Indi) {
            Indi i = (Indi)entity;
            Indi o = (Indi) that.getEntity();
            return i.getLastName().compareToIgnoreCase(o.getLastName());
        } else if (entity instanceof Fam){
            Fam i = (Fam)entity;
            Fam o = (Fam) that.getEntity();
            return i.toString(false).compareToIgnoreCase(o.toString(false));
        }
        return entity.compareTo(that.getEntity());
    }

        public Action[] getActions(boolean isContext) {
        MyContext nodeContext = new MyContext(new Context(entity));
        if (nodeContext == null) {
            return null;
        }
        List<Action2> nodeactions = new ArrayList<Action2>(8);
        nodeactions.addAll(nodeContext.getPopupActions());

        // done
        return nodeactions.toArray(new Action[0]);
    }

    public Context getContext() {
        return new Context(entity);
    }

        private class FireNodeSelection extends AbstractAction{

        public void actionPerformed(ActionEvent e) {
            SelectionSink.Dispatcher.fireSelection((Component)null, getContext(),true);
        }

        }
}
