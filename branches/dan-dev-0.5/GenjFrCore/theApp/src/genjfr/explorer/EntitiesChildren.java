/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.explorer;

import genj.gedcom.Gedcom;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author daniel
 */

public class EntitiesChildren  extends Children.Keys {

    private String[] entityNames = new String[]{
        "INDI",
        "FAM",
        "SOUR",
        "NOTE"};
    private final Gedcom gedcom;

    EntitiesChildren(Gedcom gedcom) {
        this.gedcom=gedcom;
    }

    protected Node[] createNodes(Object key) {
        GedcomEntities obj = (GedcomEntities) key;
        return new Node[] { new EntitiesNode( obj ) };
    }

    protected void addNotify() {
        super.addNotify();
        GedcomEntities[] objs = new GedcomEntities[entityNames.length];
        for (int i = 0; i < objs.length; i++) {
            GedcomEntities cat = new GedcomEntities(gedcom,entityNames[i]);
            objs[i] = cat;
        }
        setKeys(objs);
    }

}

