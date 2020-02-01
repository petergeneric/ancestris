/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.explorer;

import genj.gedcom.Context;
import genj.gedcom.Gedcom;
import java.awt.Image;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author daniel
 */
class EntitiesNode extends AbstractNode implements ExplorerNode {

    private GedcomEntities entities;
    private Gedcom gedcom;

    /**
     * Creates a new instance of InstrumentNode
     */
    public EntitiesNode(Gedcom gedcom, GedcomEntities entities) {
        super(new EntityChildren(entities), Lookups.singleton(entities));
        this.entities = entities;
        this.gedcom = gedcom;
        updateDisplay();
        setIconBaseWithExtension(entities.getImage().toString());
    }

    @Override
    public Image getIcon(int type) {
        return entities.getImage();
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public Action[] getActions(boolean isContext) {
        return new Action[]{};
    }

    public Context getContext() {
        return gedcom == null ? null : new Context(gedcom);
    }

    public void updateDisplay() {
        setDisplayName(entities.getTitle());
    }
}
