package ancestris.modules.viewers.entityviewer.models;

import genj.gedcom.Gedcom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 *
 * @author lemovice
 */
public class EntityTypeComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {

    String selectedEntityType;
    List<String> entityTypeList = new ArrayList<String>(Arrays.asList(Gedcom.ENTITIES));

    public EntityTypeComboBoxModel() {
    }

    @Override
    public int getSize() {
        return entityTypeList.size();
    }

    @Override
    public String getElementAt(int i) {
        return entityTypeList.get(i);
    }

    @Override
    public void setSelectedItem(Object o) {
        selectedEntityType = (String) o;
    }

    @Override
    public Object getSelectedItem() {
        return selectedEntityType;
    }
}
