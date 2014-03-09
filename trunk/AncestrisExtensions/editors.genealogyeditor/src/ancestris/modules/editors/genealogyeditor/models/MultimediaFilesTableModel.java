package ancestris.modules.editors.genealogyeditor.models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
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
public class MultimediaFilesTableModel extends AbstractTableModel {

    List<File> multimediaFilesList = new ArrayList<File>();
    private String[] columnsName = {
        "",
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultimediaFilesTableModel.column.fileName.title"), //        "Image"
    };

    public MultimediaFilesTableModel() {
    }

    @Override
    public int getRowCount() {
        return multimediaFilesList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row < multimediaFilesList.size()) {
            File multimediaFile = multimediaFilesList.get(row);
            if (multimediaFile.exists()) {
                switch (column) {
                    case 0: {
                        return FileSystemView.getFileSystemView().getSystemIcon(multimediaFile);
                    }

                    case 1: {
                        return multimediaFile.getAbsolutePath();
                    }
                    default:
                        return "";
                }
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public Class getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }

    @Override
    public String getColumnName(int col) {
        return columnsName[col];
    }

    public void add(File multimediaObject) {
        multimediaFilesList.add(multimediaObject);
        fireTableDataChanged();
    }

    public void addAll(List<File> multimediaObjectsList) {
        multimediaFilesList.addAll(multimediaObjectsList);
        fireTableDataChanged();
    }

    public File getValueAt(int row) {
        return multimediaFilesList.get(row);
    }

    public void remove(int rowIndex) {
        multimediaFilesList.remove(rowIndex);
        fireTableDataChanged();
    }

    public void clear() {
        multimediaFilesList.clear();
        fireTableDataChanged();
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected ImageIcon createImageIcon(String path, String description) {
        return new ImageIcon(path, description);
    }
}
