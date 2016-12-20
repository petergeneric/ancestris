package ancestris.modules.gedcom.searchduplicates;

import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.TagPath;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author lemovice
 */
public class EntityDiffTableModel extends AbstractTableModel {

    public static boolean DISPLAY_PROPERTY_NAME = true;
    ArrayList<TagPath> entityTagArray = new ArrayList<TagPath>();
    ArrayList<Property> leftEntityArray = new ArrayList<Property>();
    ArrayList<Property> rightEntityArray = new ArrayList<Property>();
    Entity leftEntity = null;
    Entity rightEntity = null;

    public void setEntities(Entity leftEntity, Entity rightEntity) {
        this.leftEntity = leftEntity;
        this.rightEntity = rightEntity;

        entityTagArray.clear();
        leftEntityArray.clear();
        rightEntityArray.clear();

        for (Property property : leftEntity.getProperties(Property.class)) {
            entityTagArray.add(property.getPath());
        }

        for (Property property : rightEntity.getProperties(Property.class)) {
            if (!entityTagArray.contains(property.getPath())) {
                entityTagArray.add(property.getPath());
            }
        }

        for (Iterator<TagPath> it = entityTagArray.iterator(); it.hasNext();) {
            TagPath tagPath = it.next();
            leftEntityArray.add(entityTagArray.indexOf(tagPath), leftEntity.getProperty(tagPath));
            rightEntityArray.add(entityTagArray.indexOf(tagPath), rightEntity.getProperty(tagPath));
        }

        fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
        fireTableChanged(new TableModelEvent(this));
    }

    @Override
    public int getRowCount() {
        return Math.max(leftEntityArray.size(), rightEntityArray.size());
    }

    @Override
    public int getColumnCount() {

        return DISPLAY_PROPERTY_NAME == true ? 3 : 2;
    }

    @Override
    public String getColumnName(int i) {
        switch (i) {
            default:
            case 0:
                return DISPLAY_PROPERTY_NAME == true ? " " : leftEntity != null ? leftEntity.getId() : " ";
            case 1:
                return DISPLAY_PROPERTY_NAME == true ? leftEntity != null ? leftEntity.getId() : " " : rightEntity != null ? rightEntity.getId() : " ";
            case 2:
                return rightEntity != null ? rightEntity.getId() : " ";
        }
    }

    @Override
    public Class<?> getColumnClass(int i) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
        return false;
    }

    @Override
    public Object getValueAt(int i, int i1) {
        switch (i1) {
            case 0:
            default:
                return DISPLAY_PROPERTY_NAME == true ? entityTagArray.get(i).toString() : leftEntityArray.get(i).getDisplayValue();

            case 1:
                return DISPLAY_PROPERTY_NAME == true ? leftEntityArray.get(i) != null ? leftEntityArray.get(i).getDisplayValue() : "" : rightEntityArray.get(i) != null ? rightEntityArray.get(i).getDisplayValue() : "";

            case 2:
                return rightEntityArray.get(i) != null ? rightEntityArray.get(i).getDisplayValue() : "";

        }
    }

    @Override
    public void setValueAt(Object o, int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
