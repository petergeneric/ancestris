package ancestris.modules.viewers.entityviewer.models;

import genj.gedcom.Entity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 *
 * @author lemovice
 */
public class EntityComboBoxModel extends AbstractListModel<Entity> implements ComboBoxModel<Entity> {

    Entity selectedEntity;
    List<Entity> entityList = new ArrayList<Entity>();

    public EntityComboBoxModel() {
    }

    @Override
    public int getSize() {
        return entityList.size();
    }

    @Override
    public Entity getElementAt(int i) {
        return entityList.get(i);
    }

    @Override
    public void setSelectedItem(Object o) {
        selectedEntity = (Entity) o;
    }

    public boolean addAll(Collection<? extends Entity> c) {
        int size = entityList.size();
        boolean addAll = entityList.addAll(c);
        fireIntervalAdded(this, size, entityList.size());
        return addAll;
    }

    public void clear() {
        int size = entityList.size();
        entityList.clear();
        fireIntervalRemoved(this, 0, size);
    }

    @Override
    public Object getSelectedItem() {
        return selectedEntity;
    }
}
