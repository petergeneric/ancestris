/*
 * Ancestris - http://www.ancestris.org
 * 
 * Copyright 2012 Ancestris
 * 
 * Author: Daniel Andre (daniel@ancestris.org).
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.explorer;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.gedcom.PropertyNode;
import ancestris.view.AncestrisTopComponent;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
import genj.gedcom.Note;
import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 *
 * @author daniel
 */
public class EntityNode extends PropertyNode/* AbstractNode */ implements Comparable<EntityNode>, ExplorerNode {

    Entity entity;

    public EntityNode(Entity e) {
        super(new Context(e));
        entity = e;
    }

    @Override
    public String getDisplayName() {
        if (entity instanceof Indi) {
            Indi i = (Indi) entity;
            return i.toString(true); 
        } else if (entity instanceof Fam) {
            Fam f = (Fam) entity;
            return f.toString(true);
        } else if (entity instanceof Note) {
            Note n = (Note) entity;
            return n.getDelegate().getLines()[0] + " (" + entity.getId() + ")";
        } else {
            return entity.toString(true);
        }
    }

    void fireChanges() {
        fireDisplayNameChange(null, getDisplayName());
        fireIconChange();
    }

    @Override
    public String getHtmlDisplayName() {
        return null;
    }

    @Override
    public Action getPreferredAction() {
        // netbeans way - fire setContext to all Ancestris components
        for (AncestrisTopComponent atc : AncestrisPlugin.lookupAll(AncestrisTopComponent.class)) {
            if ((getContext().getGedcom().equals(atc.getGedcom()))) {
                atc.refreshPanel(getContext());
            }
        }

        // ancestris way
        return new FireNodeSelection();
    }

    @Override
    public Image getIcon(int type) {
        return entity.getImage().getImage();
    }

    Entity getEntity() {
        return entity;
    }

    @Override
    public int compareTo(EntityNode that) {
        if (entity instanceof Indi) {
            Indi i = (Indi) entity;
            Indi o = (Indi) that.getEntity();
            return i.getDisplayComparator().compare(i, o);
        } else if (entity instanceof Fam) {
            Fam i = (Fam) entity;
            Fam o = (Fam) that.getEntity();
            return i.getDisplayComparator().compare(i, o);
        }
        return entity.getDisplayComparator().compare(entity, that.getEntity());
    }

    @Override
    public Context getContext() {
        return new Context(entity);
    }

    private class FireNodeSelection extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            // ancestris way
            SelectionDispatcher.fireSelection(getContext());
        }
    }
}
