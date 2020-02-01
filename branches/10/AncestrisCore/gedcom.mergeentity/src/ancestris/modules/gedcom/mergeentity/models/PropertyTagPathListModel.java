package ancestris.modules.gedcom.mergeentity.models;

import genj.gedcom.TagPath;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.AbstractListModel;

/**
 *
 * @author lemovice
 */
public class PropertyTagPathListModel extends AbstractListModel<TagPath> {

    ArrayList<TagPath> propertyTagArray = new ArrayList<TagPath>();

    public void addAll(Collection<TagPath> c) {
        int oldSize = propertyTagArray.size();
        propertyTagArray.addAll(c);
        fireIntervalAdded(this, oldSize, propertyTagArray.size());
    }

    public void add(TagPath c) {
        int oldSize = propertyTagArray.size();
        propertyTagArray.add(c);
        fireIntervalAdded(this, oldSize, propertyTagArray.size());
    }

    public void clear() {
        int oldSize = propertyTagArray.size();
        propertyTagArray.clear();
        fireIntervalRemoved(this, 0, oldSize);
    }

    @Override
    public int getSize() {
        return propertyTagArray.size();
    }

    @Override
    public TagPath getElementAt(int i) {
        return propertyTagArray.get(i);
    }
}
