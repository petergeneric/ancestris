package ancestris.modules.gedcom.searchduplicates;

import genj.gedcom.Property;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.AbstractListModel;

/**
 *
 * @author lemovice
 */
public class PropertyListModel extends AbstractListModel {

    ArrayList<Property> propertyArray = new ArrayList<Property>();

    public void addAll(Collection<Property> properties) {
        int oldSize = propertyArray.size();
        propertyArray.addAll(properties);
        fireIntervalAdded(this, oldSize, propertyArray.size());
    }

    public void add(Property property) {
        int oldSize = propertyArray.size();
        propertyArray.add(property);
        fireIntervalAdded(this, oldSize, propertyArray.size());
    }

    public void add(int index, Property property) {
        int oldSize = propertyArray.size();
        propertyArray.add(index, property);
        fireIntervalAdded(this, oldSize, propertyArray.size());
    }

    public void clear() {
        int oldSize = propertyArray.size();
        propertyArray.clear();
        fireIntervalRemoved(this, 0, oldSize);
    }

    @Override
    public int getSize() {
        return propertyArray.size();
    }

    @Override
    public Object getElementAt(int i) {
        return propertyArray.get(i);
    }
}
