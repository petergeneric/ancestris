package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Media;
import genj.gedcom.Property;
import genj.gedcom.PropertyBlob;
import genj.gedcom.PropertyFile;
import genj.gedcom.PropertyMedia;
import genj.gedcom.PropertyXRef;
import genj.io.InputSource;
import genj.renderer.MediaRenderer;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.ImageIcon;
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

    private final genj.util.swing.ImageIcon MINISTAR;

    List<Property> multimediaObjectsRefList = new ArrayList<>();
    private final String[] columnsName = {
        "",
        NbBundle.getMessage(MultiMediaObjectCitationsTableModel.class, "MultiMediaObjectCitationsTableModel.column.ID.title"),
        NbBundle.getMessage(MultiMediaObjectCitationsTableModel.class, "MultiMediaObjectCitationsTableModel.column.title.title"),
        NbBundle.getMessage(MultiMediaObjectCitationsTableModel.class, "MultiMediaObjectCitationsTableModel.column.fileName.title"),
        NbBundle.getMessage(MultiMediaObjectCitationsTableModel.class, "MultiMediaObjectCitationsTableModel.column.note.title")
    };

    public MultiMediaObjectCitationsTableModel() {
        this.MINISTAR = new genj.util.swing.ImageIcon(MultiMediaObjectCitationsTableModel.class, "/ancestris/modules/editors/genealogyeditor/resources/starmini.png");
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
            switch (column) {
                case 0: {
                    if (multimediaObject instanceof PropertyMedia) {
                        multimediaObject = ((PropertyMedia) multimediaObject).getTargetEntity();
                    }
                    if (multimediaObject instanceof Media && multimediaObject.getGedcom().getGrammar().getVersion().equals("5.5")) {
                        PropertyBlob propertyBlob = (PropertyBlob) multimediaObject.getProperty("BLOB", true);
                        if (propertyBlob != null) {
                            Optional<BufferedImage> obi = MediaRenderer.getImage(propertyBlob.getInput().orElse(null));

                            if (obi.isPresent()) {
                                Image image = obi.get();
                                image = image.getScaledInstance(-1, 16, Image.SCALE_DEFAULT);
                                return new ImageIcon(image);
                            } else {
                                return new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/media.png"));
                            }
                        } else {
                            return new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/media.png"));
                        }
                    } else {
                        Property propertyfile = multimediaObject.getProperty("FILE", true);
                        if (propertyfile != null && propertyfile instanceof PropertyFile) {
                            InputSource multimediaFile = ((PropertyFile) propertyfile).getInput().orElse(null);
                            ImageIcon imageIcon = new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/media.png"));
                            Optional<BufferedImage> obi = MediaRenderer.getImage(multimediaFile);
                            if (obi.isPresent()) {
                                Image image = obi.get();
                                image = image.getScaledInstance(-1, 16, Image.SCALE_DEFAULT);
                                if (image != null) {
                                    imageIcon = new ImageIcon(image);
                                    if (row == 0) {
                                        return new genj.util.swing.ImageIcon(imageIcon).getOverLayed(MINISTAR);
                                    }
                                }

                                return imageIcon;
                            } else {
                                return new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"));
                            }
                        } else {
                            return new ImageIcon(getClass().getResource("/ancestris/modules/editors/genealogyeditor/resources/edit_delete.png"));
                        }
                    }
                }
                case 1:
                    if (multimediaObject instanceof PropertyMedia) {
                        return ((Media) ((PropertyMedia) multimediaObject).getTargetEntity()).getId();
                    } else {
                        return "";
                    }
                case 2: {
                    if (multimediaObject instanceof PropertyMedia) {
                        return ((Media) ((PropertyMedia) multimediaObject).getTargetEntity()).getTitle();
                    } else {
                        Property title = multimediaObject.getProperty("TITL", true);
                        if (title != null) {
                            return title.getValue();
                        } else {
                            return "";
                        }
                    }
                }
                case 3: {
                    if (multimediaObject instanceof PropertyMedia) {
                        Property propertyFile = ((PropertyMedia) multimediaObject).getTargetEntity().getProperty("FILE", true);
                        if (propertyFile != null && propertyFile instanceof PropertyFile) {
                            InputSource file = ((PropertyFile) propertyFile).getInput().orElse(null);
                            if (file != null) {
                                return file.getLocation();
                            } else {
                                return "";
                            }
                        } else {
                            return "";
                        }
                    } else {
                        Property propertyFile = multimediaObject.getProperty("FILE", true);
                        if (propertyFile != null && propertyFile instanceof PropertyFile) {
                            InputSource file = ((PropertyFile) propertyFile).getInput().orElse(null);
                            if (file != null) {
                                return file.getLocation();
                            } else {
                                return "";
                            }
                        } else {
                            return "";
                        }
                    }
                }
                case 4: {
                    if (multimediaObject instanceof PropertyMedia) {
                        if (((PropertyMedia) multimediaObject).getTargetEntity().getProperty("NOTE") != null) {
                            return NbBundle.getMessage(MultiMediaObjectCitationsTableModel.class, "MultiMediaObjectCitationsTableModel.column.note.value.yes");
                        } else {
                            return NbBundle.getMessage(MultiMediaObjectCitationsTableModel.class, "MultiMediaObjectCitationsTableModel.column.note.value.no");
                        }
                    } else {
                        if (multimediaObject.getProperty("NOTE") != null) {
                            return NbBundle.getMessage(MultiMediaObjectCitationsTableModel.class, "MultiMediaObjectCitationsTableModel.column.note.value.yes");
                        } else {
                            return NbBundle.getMessage(MultiMediaObjectCitationsTableModel.class, "MultiMediaObjectCitationsTableModel.column.note.value.no");
                        }
                    }
                }
                default:
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

    public int getRowOf(Property p) {
        for (int row = 0; row < getRowCount(); row++) {
            Property pList = getValueAt(row);
            if (pList instanceof PropertyXRef) {
                pList = ((PropertyXRef) pList).getTargetEntity();
            }
            if (pList == p) {
                return row;
            }
        }
        return -1;
    }
}
