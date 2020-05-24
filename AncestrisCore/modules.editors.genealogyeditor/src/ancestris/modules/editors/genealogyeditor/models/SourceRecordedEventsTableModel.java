package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.editors.genealogyeditor.utilities.PropertyTag2Name;
import genj.gedcom.Property;
import genj.gedcom.PropertyPlace;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class SourceRecordedEventsTableModel extends AbstractTableModel {

    List<Property> mEventTypesList = new ArrayList<>();
    String[] columnsName = {
        NbBundle.getMessage(SourceRecordedEventsTableModel.class, "EventsTableModel.column.ID.eventType"),
        NbBundle.getMessage(SourceRecordedEventsTableModel.class, "EventsTableModel.column.ID.date"),
        NbBundle.getMessage(SourceRecordedEventsTableModel.class, "EventsTableModel.column.ID.place")
    };

    public SourceRecordedEventsTableModel() {
    }

    @Override
    public int getRowCount() {
        return mEventTypesList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    /*
     * +2 EVEN <EVENTS_RECORDED> {0:M}
     * +3 DATE <DATE_PERIOD> {0:1}
     * +3 PLAC <SOURCE_JURISDICTION_PLACE> {0:1}
     */
    @Override
    public Object getValueAt(int row, int column) {
        if (row < mEventTypesList.size()) {
            Property eventType = mEventTypesList.get(row);
            switch (column) {
                case 0:
                    String result = "";
                    for (String tag : eventType.getValue().replaceAll(" ", "").split(",")) {
                        if (result.isEmpty()) {
                            result += PropertyTag2Name.getTagName(tag);
                        } else {
                            result += ", " + PropertyTag2Name.getTagName(tag);
                        }
                    }
                    return result;
                case 1:
                    Property date = eventType.getProperty("DATE");
                    if (date != null) {
                        return date.getDisplayValue();
                    } else {
                        return "";
                    }
                case 2:
                    PropertyPlace place = (PropertyPlace) eventType.getProperty("PLAC");
                    if (place != null) {
                        return place.format("all");
                    } else {
                        return "";
                    }
                default:
                    return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public String getColumnName(int col) {
        return columnsName[col];
    }
    
    public String[] getColumnsName() {
        return columnsName;
    }

    public void addAll(List<Property> eventTypesList) {
        this.mEventTypesList.addAll(eventTypesList);
        fireTableDataChanged();
    }

    public void add(Property eventType) {
        this.mEventTypesList.add(eventType);
        fireTableDataChanged();
    }

    public Property getValueAt(int row) {
        return mEventTypesList.get(row);
    }

    public Property remove(int row) {
        Property event = mEventTypesList.remove(row);
        fireTableDataChanged();
        return event;
    }

    public void clear() {
        this.mEventTypesList.clear();
    }
}
