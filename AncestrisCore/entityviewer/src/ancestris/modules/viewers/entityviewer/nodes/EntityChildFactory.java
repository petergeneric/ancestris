package ancestris.modules.viewers.entityviewer.nodes;

import genj.gedcom.Entity;
import genj.gedcom.Property;
import java.util.Arrays;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lemovice
 */
public class EntityChildFactory extends ChildFactory<Property> {

    private final Property property;

    public EntityChildFactory(Entity entity) {
        this.property = entity;
    }

    @Override
    protected boolean createKeys(List<Property> toPopulate) {
        toPopulate.addAll(Arrays.asList(property.getProperties()));
        return true;
    }

    @Override
    protected Node createNodeForKey(Property key) {
        PropertyNode propertyNode;
        if (key.getProperties().length > 0) {
            propertyNode = new PropertyNode(Children.create(new PropertyChildFactory(key), true), Lookups.singleton(key));
        } else {
            propertyNode = new PropertyNode(Children.LEAF, Lookups.singleton(key));
        }
        return propertyNode;
    }
}
