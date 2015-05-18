package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.PropertyFile;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import org.openide.util.Exceptions;
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

    List<PropertyFile> multimediaFilesList = new ArrayList<PropertyFile>();
    private final String[] columnsName = {
        "",
        NbBundle.getMessage(MultimediaFilesTableModel.class, "MultimediaFilesTableModel.column.fileName.title"), //        "Image"
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
            File multimediaFile = multimediaFilesList.get(row).getFile();
            if (multimediaFile != null) {
                switch (column) {
                    case 0: {
                        if (multimediaFile.exists()) {
                            ImageIcon imageIcon = new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Media.png"));
                            try {
                                Image image;
                                try {
                                    image = ImageIO.read(multimediaFile);
                                    if (image != null) {
                                        image = image.getScaledInstance(-1, 32, image.SCALE_DEFAULT);
                                    }
                                } catch (IOException ex) {
                                    image = sun.awt.shell.ShellFolder.getShellFolder(multimediaFile).getIcon(true);
                                }
                                if (image != null) {
                                    imageIcon = new ImageIcon(image);
                                }
                            } catch (FileNotFoundException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            return imageIcon;
                        } else {
                            return new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"));
                        }
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
        if (column == 0) {
            return ImageIcon.class;
        } else {
            return String.class;
        }
    }

    @Override
    public String getColumnName(int col) {
        return columnsName[col];
    }

    public void add(PropertyFile multimediaObject) {
        multimediaFilesList.add(multimediaObject);
        fireTableDataChanged();
    }

    public void addAll(List<PropertyFile> multimediaObjectsList) {
        multimediaFilesList.addAll(multimediaObjectsList);
        fireTableDataChanged();
    }

    public PropertyFile getValueAt(int row) {
        return multimediaFilesList.get(row);
    }

    public PropertyFile remove(int rowIndex) {
        PropertyFile remove = multimediaFilesList.remove(rowIndex);
        fireTableDataChanged();
        return remove;
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
