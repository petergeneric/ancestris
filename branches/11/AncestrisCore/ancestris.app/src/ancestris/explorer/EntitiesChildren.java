/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.explorer;

import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        
        // Reorder entities
        List<String> tagsList = new ArrayList<>();
        for (String e : Gedcom.ENTITIES) {
            tagsList.add(e);
            // Keep only unknown entities
            tagEntities.remove(e);
        }
        // Add unknown entities
        tagsList.addAll(tagEntities);
        
        // Create listes of entities.
        GedcomEntities[] objs = new GedcomEntities[tagsList.size()];
        String[] tags = tagsList.toArray(new String[0]);
        for (int i = 0; i < objs.length; i++) {
            GedcomEntities cat = new GedcomEntities(gedcom, tags[i]);
            objs[i] = cat;
        }
        setKeys(objs);
    }
}
