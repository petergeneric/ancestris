package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 * 
 * MULTIMEDIA_LINK:=
 * [
 * n OBJE @<XREF:OBJE>@
 * |
 * n OBJE
 * +1 FILE <MULTIMEDIA_FILE_REFN>
 * +2 FORM <MULTIMEDIA_FORMAT>
 * +3 MEDI <SOURCE_MEDIA_TYPE>
 * +1 TITL <DESCRIPTIVE_TITLE>
 * ]
 */

public class MultiMediaObjectCitationsTableModel extends AbstractTableModel {

    List<Property> multimediaObjectsRefList = new ArrayList<Property>();
    private String[] columnsName = {
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultiMediaObjectCitationsTableModel.column.ID.title"),
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultiMediaObjectCitationsTableModel.column.fileName.title")
    };

    public MultiMediaObjectCitationsTableModel() {
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
        if (row < multimediaObjectsRefList.size()) {
            Property multimediaObject = multimediaObjectsRefList.get(row);
            if (multimediaObject instanceof PropertyMedia) {
                if (column == 0) {
                    return ((Media) ((PropertyMedia) multimediaObject).getTargetEntity()).getId();
                } else {
                    Property file = multimediaObject.getProperty("FILE", true);
                    if (file != null && file instanceof PropertyFile) {
                        return ((PropertyFile) file).getFile().getAbsolutePath();
                    } else {
                        return "";
                    }
                }
            } else {
                if (column == 0) {
                    return "";
                } else {
                    Property file = multimediaObject.getProperty("FILE", true);
                    if (file != null && file instanceof PropertyFile) {
                        return ((PropertyFile) file).getFile().getAbsolutePath();
                    } else {
                        return "";
                    }
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

    public void add(Property multimediaObject) {
        multimediaObjectsRefList.add(multimediaObject);
        fireTableDataChanged();
    }

    public void addAll(List<Property> multimediaObjectsList) {
        multimediaObjectsRefList.addAll(multimediaObjectsList);
        fireTableDataChanged();
    }

    public void update(List<Property> multimediaObjectsList) {
        multimediaObjectsRefList.clear();
        addAll(multimediaObjectsList);
    }

    public Property getValueAt(int row) {
        return multimediaObjectsRefList.get(row);
    }
}
