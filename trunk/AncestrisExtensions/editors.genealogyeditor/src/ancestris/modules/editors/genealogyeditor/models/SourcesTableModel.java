package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Property;
import genj.gedcom.Source;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dominique
 */
public class SourcesTableModel extends AbstractTableModel {

    List<Source> sourcesList = new ArrayList<Source>();
    String[] columnsName = {"ID", "Description", "Events"};

    public SourcesTableModel() {
    }

    @Override
    public int getRowCount() {
        return sourcesList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Source source = sourcesList.get(row);
        if (source != null) {
            if (column == 0) {
                return source.getId();
            } else if (column == 1) {
                return source.getTitle();
            } else {
                Property propertyByPath = source.getPropertyByPath("DATA:EVEN");
                if (propertyByPath != null) {
                    return propertyByPath.getDisplayValue();
                } else {
                    return "";
                }
            }
        } else {
            return "";
        }
    }

    @Override
    public String getColumnName(int col) {
        return columnsName[col];
    }

    public void add(Source source) {
        sourcesList.add(source);
        fireTableDataChanged();
    }

    public void addAll(List<Source> sourcesList) {
        sourcesList.addAll(sourcesList);
        fireTableDataChanged();
    }

    public void update(List<Source> sourcesList) {
        this.sourcesList.clear();
        addAll(sourcesList);
    }

    public Source getValueAt(int row) {
        return sourcesList.get(row);
    }
}
