/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.explorer;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import java.util.HashSet;
import java.util.Set;
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
        Set<String> tagEntities = new HashSet<>();
        // Get tags of entities
        for (String e : Gedcom.ENTITIES) {
            tagEntities.add(e);
        }
        // add tags of unkwown entities present in GEDCOM file
        for (Entity e : gedcom.getEntities()) {
            tagEntities.add(e.getTag());
        }
        // No need to display HEADER
        tagEntities.remove("HEAD");
        GedcomEntities[] objs = new GedcomEntities[tagEntities.size()];
        String[] tags = tagEntities.toArray(new String[0]);
        for (int i = 0; i < objs.length; i++) {
            GedcomEntities cat = new GedcomEntities(gedcom, tags[i]);
            objs[i] = cat;
        }
        setKeys(objs);
    }
}
