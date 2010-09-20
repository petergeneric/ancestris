/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package genjfr.explorer;

import java.awt.Image;
import javax.swing.Action;
import org.openide.actions.CopyAction;
import org.openide.actions.CutAction;
import org.openide.actions.DeleteAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import sun.misc.Resource;

/**
 *
 * @author daniel
 */
class EntitiesNode extends AbstractNode {

    private GedcomEntities entities;

    /** Creates a new instance of InstrumentNode */
    public EntitiesNode(GedcomEntities entities) {
//        super(Children.LEAF, Lookups.fixed( new Object[] {key} ) );
        super( new EntityChildren(entities), Lookups.singleton(entities) );
        this.entities = entities;
        setDisplayName(entities.getTitle());
        setIconBaseWithExtension(entities.getImage().toString());
//        setIconBaseWithExtension("org/netbeans/myfirstexplorer/marilyn.gif");
    }

    @Override
    public Image getIcon(int type) {
        return entities.getImage();
    }
    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }
    public Action[] getActions(boolean isContext) {
        return new Action[]{};
    }

}