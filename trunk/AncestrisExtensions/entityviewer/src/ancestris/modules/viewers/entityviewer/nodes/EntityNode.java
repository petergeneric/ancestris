package ancestris.modules.viewers.entityviewer.nodes;

import ancestris.modules.viewers.entityviewer.EntityTag2Icon;
import ancestris.modules.viewers.entityviewer.EntityTag2Name;
import genj.gedcom.Entity;
import java.awt.Image;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lemovice
 */
public class EntityNode extends AbstractNode {

    public EntityNode(Children create, Entity entity) {
        super(create, Lookups.singleton(entity));
    }

    public EntityNode() {
        super(Children.LEAF);
    }

    @Override
    public String getDisplayName() {
        Entity entity = getLookup().lookup(Entity.class);
        if (entity != null) {
            return EntityTag2Name.getTagName(entity.getTag()) + " " + entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public Image getIcon(int type) {
        Entity entity = getLookup().lookup(Entity.class);
        if (entity != null) {
            return EntityTag2Icon.getIcon(entity.getTag());
        } else {
            return null;
        }
    }

    @Override
    public Image getOpenedIcon(int type) {
        Entity entity = getLookup().lookup(Entity.class);
        if (entity != null) {
            return EntityTag2Icon.getIcon(entity.getTag());
        } else {
            return null;
        }
    }

    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        Entity entity = getLookup().lookup(Entity.class);
        try {
            Property valueProp = new PropertySupport.Reflection(entity, String.class, "getValue", null);

            valueProp.setName("value");

            set.put(valueProp);

        } catch (NoSuchMethodException ex) {
            ErrorManager.getDefault();
        }

        sheet.put(set);
        return sheet;
    }

    @Override
    public boolean canDestroy() {
        return true;
    }
}