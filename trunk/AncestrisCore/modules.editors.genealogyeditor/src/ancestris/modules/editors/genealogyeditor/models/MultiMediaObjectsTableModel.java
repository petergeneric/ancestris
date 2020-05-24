package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Media;
import genj.io.InputSource;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 */
public class MultiMediaObjectsTableModel extends AbstractTableModel {

    List<Media> multimediaObjectsRefList = new ArrayList<>();
    private final String[] columnsName = {
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultiMediaObjectsTableModel.column.ID.title"),
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultiMediaObjectsTableModel.column.fileName.title")
    };

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
            InputSource file = media.getFile();
            if (file != null) {
                return file.getLocation();
            } else {
                return "";
            }
        }
    }

    @Override
    public String getColumnName(int col) {
        return columnsName[col];
    }
    
    public String[] getColumnsName() {
        return columnsName;
    }

    public void add(Media multimediaObject) {
        multimediaObjectsRefList.add(multimediaObject);
        fireTableDataChanged();
    }

    public void addAll(List<Media> multimediaObjectsList) {
        multimediaObjectsRefList.addAll(multimediaObjectsList);
        fireTableDataChanged();
    }

    public void clear() {
        multimediaObjectsRefList.clear();
    }

    public Media getValueAt(int row) {
        return multimediaObjectsRefList.get(row);
    }
}
