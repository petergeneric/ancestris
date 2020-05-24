package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.editors.genealogyeditor.utilities.PropertyTag2Name;
import genj.gedcom.Property;
import genj.gedcom.PropertyDate;
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

    /*
     * INDIVIDUAL_ATTRIBUTE
     */
    private final ArrayList<String> mIndividualAttributesTags = new ArrayList<String>() {
        {
            add("CAST");
            add("DSCR");
            add("EDUC");
            add("IDNO");
            add("NATI");
            add("NCHI");
            add("NMR");
            add("OCCU");
            add("PROP");
            add("RELI");
            add("RESI");
            add("SSN");
            add("TITL");
//            add("FACT"); not defined in gedcom xml definition file
        }
    };
    List<Property> eventsList = new ArrayList<>();
    String[] columnsName = {
        NbBundle.getMessage(EventsTableModel.class, "EventsTableModel.column.ID.eventType"),
        NbBundle.getMessage(EventsTableModel.class, "EventsTableModel.column.ID.place"),
        NbBundle.getMessage(EventsTableModel.class, "EventsTableModel.column.ID.date")
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
            final Property propertyEvent = eventsList.get(row);
            switch (column) {
                case 0:
                    if (propertyEvent.getTag().equals("EVEN") || propertyEvent.getTag().equals("FACT")) {
                        Property eventType = propertyEvent.getProperty("TYPE");
                        return eventType != null ? eventType.getValue() : "";
                    } else if (mIndividualAttributesTags.contains(propertyEvent.getTag())) {
                        return PropertyTag2Name.getTagName(propertyEvent.getTag()) + " " + propertyEvent.getValue();
                    } else {
                        return PropertyTag2Name.getTagName(propertyEvent.getTag());
                    }
                case 1:
                    PropertyPlace place = (PropertyPlace) propertyEvent.getProperty("PLAC");
                    if (place != null) {
                        return place.format("all");
                    } else {
                        Property address = propertyEvent.getProperty("ADDR");
                        if (address != null) {
                            return address.getValue();
                        } else {
                            return "";
                        }
                    }
                case 2:
                    Property date =  propertyEvent.getProperty("DATE");
                    return date != null ? date : new PropertyDate();
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

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return getValueAt(0, columnIndex).getClass();
    }

    public void addAll(List<Property> eventsList) {
        this.eventsList.addAll(eventsList);
        fireTableDataChanged();
    }

    public void add(Property event) {
        this.eventsList.add(event);
        fireTableDataChanged();
    }
    
    public int indexOf(Property event) {
        return this.eventsList.indexOf(event);
    }

    public Property getValueAt(int row) {
        return eventsList.get(row);
    }

    public Property remove(int row) {
        Property event = eventsList.remove(row);
        fireTableDataChanged();
        return event;
    }

    public void clear() {
        eventsList.clear();
        fireTableDataChanged();
    }
}
