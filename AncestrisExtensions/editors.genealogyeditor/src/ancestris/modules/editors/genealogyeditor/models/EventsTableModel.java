package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.gedcom.utilities.PropertyTag2Name;
import genj.gedcom.PropertyEvent;
import genj.gedcom.PropertyPlace;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class EventsTableModel extends AbstractTableModel {

    List<PropertyEvent> eventsList = new ArrayList<PropertyEvent>();
    String[] columnsName = {
        NbBundle.getMessage(EventsTableModel.class, "EventsTableModel.column.ID.eventType"),
        NbBundle.getMessage(EventsTableModel.class, "EventsTableModel.column.ID.date"),
        NbBundle.getMessage(EventsTableModel.class, "EventsTableModel.column.ID.place")
    };

    public EventsTableModel() {
    }

    @Override
    public int getRowCount() {
        return eventsList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row < eventsList.size()) {
            PropertyEvent propertyEvent = eventsList.get(row);
            if (column == 0) {
                return PropertyTag2Name.getTagName(propertyEvent.getTag());
            } else if (column == 1) {
                return propertyEvent.getDate() != null ? propertyEvent.getDate().getDisplayValue() : "";
            } else {
                PropertyPlace place = (PropertyPlace) propertyEvent.getProperty("PLAC");
                return place.format("all");
            }
        } else {
            return "";
        }
    }

    @Override
    public String getColumnName(int col) {
        return columnsName[col];
    }

    public void addAll(List<PropertyEvent> eventsList) {
        this.eventsList.addAll(eventsList);
        fireTableDataChanged();
    }

    public void add(PropertyEvent event) {
        this.eventsList.add(event);
        fireTableDataChanged();
    }

    public PropertyEvent getValueAt(int row) {
        return eventsList.get(row);
    }

    public PropertyEvent remove(int row) {
        PropertyEvent event = eventsList.remove(row);
        fireTableDataChanged();
        return event;
    }

    public void update(List<PropertyEvent> eventsList) {
        this.eventsList.clear();
        addAll(eventsList);
    }
}
