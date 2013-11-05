package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Media;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dominique
 */
public class MultiMediaObjectsTableModel extends AbstractTableModel {

    List<Media> multimediaObjectsRefList = new ArrayList<Media>();
    String[] columnsName = {"ID", "date"};

    public MultiMediaObjectsTableModel() {
    }

    @Override
    public int getRowCount() {
        return multimediaObjectsRefList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Media media = multimediaObjectsRefList.get(row);
        if (column == 0) {
            return media.getId();
        } else {
            return media.getDisplayValue();
        }
    }

    @Override
    public String getColumnName(int col) {
        return columnsName[col];
    }

    public void add(Media multimediaObject) {
        multimediaObjectsRefList.add(multimediaObject);
        fireTableDataChanged();
    }

    public void addAll(List<Media> multimediaObjectsList) {
        multimediaObjectsRefList.addAll(multimediaObjectsList);
        fireTableDataChanged();
    }

    public void update(List<Media> multimediaObjectsList) {
        multimediaObjectsRefList.clear();
        addAll(multimediaObjectsList);
    }

    public Media getValueAt(int row) {
        return multimediaObjectsRefList.get(row);
    }
}
