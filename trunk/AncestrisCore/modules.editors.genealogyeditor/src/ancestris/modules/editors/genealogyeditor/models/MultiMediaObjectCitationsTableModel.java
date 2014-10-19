package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;
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
public class MultiMediaObjectCitationsTableModel extends AbstractTableModel {

    List<Property> multimediaObjectsRefList = new ArrayList<Property>();
    private final String[] columnsName = {
        "",
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultiMediaObjectCitationsTableModel.column.ID.title"),
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultiMediaObjectCitationsTableModel.column.title.title"),
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultiMediaObjectCitationsTableModel.column.fileName.title"),
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultiMediaObjectCitationsTableModel.column.note.title")
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
                        Property propertyfile = ((PropertyMedia) multimediaObject).getTargetEntity().getProperty("FILE", true);
                        if (propertyfile != null && propertyfile instanceof PropertyFile) {
                            File multimediaFile = ((PropertyFile) propertyfile).getFile();
                            ImageIcon imageIcon = new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Media.png"));
                            if (multimediaFile != null && multimediaFile.exists()) {
                                try {
                                    Image image;
                                    try {
                                        image = ImageIO.read(multimediaFile);
                                        if (image != null) {
                                            image = image.getScaledInstance(16, 16, image.SCALE_DEFAULT);
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
                        } else {
                            return new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"));
                        }
                    }
                    case 1:
                        return ((Media) ((PropertyMedia) multimediaObject).getTargetEntity()).getId();

                    case 2: {
                        return ((Media) ((PropertyMedia) multimediaObject).getTargetEntity()).getTitle();
                    }
                    case 3: {
                        Property propertyFile = ((PropertyMedia) multimediaObject).getTargetEntity().getProperty("FILE", true);
                        if (propertyFile != null && propertyFile instanceof PropertyFile) {
                            File file = ((PropertyFile) propertyFile).getFile();
                            if (file != null) {
                                return file.getAbsolutePath();
                            } else {
                                return "";
                            }
                        } else {
                            return "";
                        }
                    }
                    case 4: {
                        if (((PropertyMedia) multimediaObject).getTargetEntity().getProperty("NOTE") != null) {
                            return NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultiMediaObjectCitationsTableModel.column.note.value.yes");
                        } else {
                            return NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultiMediaObjectCitationsTableModel.column.note.value.no");
                        }
                    }
                    default:
                        return "";
                }
            } else {
                switch (column) {
                    case 0: {
                        Property propertyfile = multimediaObject.getProperty("FILE", true);
                        if (propertyfile != null && propertyfile instanceof PropertyFile) {
                            File multimediaFile = ((PropertyFile) propertyfile).getFile();
                            ImageIcon imageIcon = new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/Media.png"));
                            if (multimediaFile != null && multimediaFile.exists()) {
                                try {
                                    Image image;
                                    try {
                                        image = ImageIO.read(multimediaFile);
                                        if (image != null) {
                                            image = image.getScaledInstance(16, 16, image.SCALE_DEFAULT);
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
                        } else {
                            return new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"));
                        }
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
                        Property propertyFile = multimediaObject.getProperty("FILE", true);
                        if (propertyFile != null && propertyFile instanceof PropertyFile) {
                            File file = ((PropertyFile) propertyFile).getFile();
                            if ( file != null) {
                                return file.getAbsolutePath();
                            } else {
                                return "";
                            }
                        } else {
                            return "";
                        }
                    }
                    case 4: {
                        if (multimediaObject.getProperty("NOTE") != null) {
                            return NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultiMediaObjectCitationsTableModel.column.note.value.yes");
                        } else {
                            return NbBundle.getMessage(MultiMediaObjectsTableModel.class, "MultiMediaObjectCitationsTableModel.column.note.value.no");
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
