/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.explorer;

import genj.gedcom.Gedcom;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author daniel
 */
public class EntitiesChildren extends Children.Keys<GedcomEntities> {

    private final Gedcom gedcom;

    EntitiesChildren(Gedcom gedcom) {
        this.gedcom = gedcom;
    }

    protected Node[] createNodes(GedcomEntities key) {
        return new Node[]{new EntitiesNode(gedcom, key)};
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        GedcomEntities[] objs = new GedcomEntities[Gedcom.ENTITIES.length];
        for (int i = 0; i < objs.length; i++) {
            GedcomEntities cat = new GedcomEntities(gedcom, Gedcom.ENTITIES[i]);
            objs[i] = cat;
        }
        setKeys(objs);
    }
}
