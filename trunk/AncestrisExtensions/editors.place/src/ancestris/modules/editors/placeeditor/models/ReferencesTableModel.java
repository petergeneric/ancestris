package ancestris.modules.editors.placeeditor.models;

import ancestris.modules.gedcom.utilities.EntityTag2Name;
import ancestris.modules.gedcom.utilities.PropertyTag2Name;
import genj.gedcom.Entity;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dominique
 */
public class ReferencesTableModel extends AbstractTableModel {

    String[] referencesTablecolumnNames = {"event",
        "description"};
    protected ArrayList<PropertyPlace> referencesTableValues;

    public ReferencesTableModel() {
        referencesTableValues = new ArrayList<PropertyPlace>();
    }

    @Override
    public int getRowCount() {
        return referencesTableValues.size();
    }

    @Override
    public int getColumnCount() {
        return referencesTablecolumnNames.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row < getRowCount()) {
            PropertyPlace place = referencesTableValues.get(row);
            Entity entity = place.getEntity();
            Property parent = place.getParent();

            if (column == 0) {
                return PropertyTag2Name.getTagName(parent.getTag());
            } else {
                return "(" + entity.getId() + ")" + entity.toString(false);
            }
        } else {
            return "";
        }
    }

    public Entity getValueAt(int row) {
        if (row < getRowCount()) {
            PropertyPlace place = referencesTableValues.get(row);
            return place.getEntity();
        } else {
            return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        return referencesTablecolumnNames[column];
    }

    public void addRow(PropertyPlace place) {
        referencesTableValues.add(place);
    }
}
