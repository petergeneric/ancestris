package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 *
 * MULTIMEDIA_LINK:= [ n OBJE @<XREF:OBJE>@ | n OBJE +1 FILE
 * <MULTIMEDIA_FILE_REFN>
 * +2 FORM <MULTIMEDIA_FORMAT>
 * +3 MEDI <SOURCE_MEDIA_TYPE>
 * +1 TITL <DESCRIPTIVE_TITLE> ]
 */
public class MultiMediaObjectCitationsTableModel extends AbstractTableModel {

    List<Property> multimediaObjectsRefList = new ArrayList<Property>();
    private String[] columnsName = {
        "",
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultiMediaObjectCitationsTableModel.column.ID.title"),
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultiMediaObjectCitationsTableModel.column.title.title"),
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultiMediaObjectCitationsTableModel.column.fileName.title"), //        "Image"
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
                switch (column) {
                    case 0: {
                        Property file = ((PropertyMedia) multimediaObject).getTargetEntity().getProperty("FILE", true);
                        if (file != null && file instanceof PropertyFile) {
                            Icon icon = new JFileChooser().getIcon(((PropertyFile) file).getFile());
                            return icon;
                        }
                        return "";
                    }
                    case 1:
                        return ((Media) ((PropertyMedia) multimediaObject).getTargetEntity()).getId();

                    case 2: {
                        return ((Media) ((PropertyMedia) multimediaObject).getTargetEntity()).getTitle();
                    }
                    case 3: {
                        Property file = ((PropertyMedia) multimediaObject).getTargetEntity().getProperty("FILE", true);
                        if (file != null && file instanceof PropertyFile) {
                            return ((PropertyFile) file).getFile().getAbsolutePath();
                        } else {
                            return "";
                        }
                    }
                    default:
                        return "";
                }
            } else {
                switch (column) {
                    case 0: {
                        Property file = multimediaObject.getProperty("FILE", true);
                        if (file != null && file instanceof PropertyFile) {
                            Icon icon = new JFileChooser().getIcon(((PropertyFile) file).getFile());
                            return icon;
                        }
                        return "";
                    }

                    case 1:
                        return "";
                    case 2: {
                        Property title = multimediaObject.getProperty("TITL", true);
                        if (title != null) {
                            return title.getValue();
                        } else {
                            return "";
                        }
                    }
                    case 3: {
                        Property file = multimediaObject.getProperty("FILE", true);
                        if (file != null && file instanceof PropertyFile) {
                            return ((PropertyFile) file).getFile().getAbsolutePath();
                        } else {
                            return "";
                        }
                    }
                    default:
                        return "";
                }
            }
        } else {
            return "";
        }
    }

    @Override
    public Class getColumnClass(int column) {
        if (getValueAt(0, column) != null) {
            return getValueAt(0, column).getClass();
        } else {
            return String.class;
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

    public Property getValueAt(int row) {
        return multimediaObjectsRefList.get(row);
    }

    public void remove(int rowIndex) {
        multimediaObjectsRefList.remove(rowIndex);
        fireTableDataChanged();
    }

    public void clear() {
        multimediaObjectsRefList.clear();
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected ImageIcon createImageIcon(String path, String description) {
        return new ImageIcon(path, description);
    }
}
