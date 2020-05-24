package ancestris.modules.editors.genealogyeditor.models;

import ancestris.modules.editors.genealogyeditor.utilities.PropertyTag2Name;
import genj.gedcom.Property;
import genj.gedcom.PropertySource;
import genj.gedcom.Source;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;
/*
 * SOURCE_CITATION:=
 * [ pointer to source record (preferred)
 * n SOUR @<XREF:SOUR>@
 * +1 PAGE <WHERE_WITHIN_SOURCE>
 * +1 EVEN <EVENT_TYPE_CITED_FROM>
 * +2 ROLE <ROLE_IN_EVENT>
 * +1 DATA
 * +2 DATE <ENTRY_RECORDING_DATE>
 * +2 TEXT <TEXT_FROM_SOURCE>
 * +3 [CONC|CONT] <TEXT_FROM_SOURCE>
 * +1 <<MULTIMEDIA_LINK>>
 * +1 <<NOTE_STRUCTURE>>
 * +1 QUAY <CERTAINTY_ASSESSMENT>
 * | Systems not using source records
 * n SOUR <SOURCE_DESCRIPTION>
 * +1 [CONC|CONT]
 * <SOURCE_DESCRIPTION>
 * +1 TEXT <TEXT_FROM_SOURCE>
 * +2 [CONC|CONT] <TEXT_FROM_SOURCE>
 * +1 <<MULTIMEDIA_LINK>>
 * +1 <<NOTE_STRUCTURE>>
 * +1 QUAY <CERTAINTY_ASSESSMENT>
 * ]
 */

/**
 *
 * @author dominique
 */
public class SourceCitationsTableModel extends AbstractTableModel {

    ArrayList<Property> mSourcesList = new ArrayList<>();
    private final String[] columnsName = {
        NbBundle.getMessage(SourceCitationsTableModel.class, "SourceCitationsTableModel.column.events.title"),
        NbBundle.getMessage(SourceCitationsTableModel.class, "SourceCitationsTableModel.column.description.title"),
        NbBundle.getMessage(SourceCitationsTableModel.class, "SourceCitationsTableModel.column.page.title"),
        NbBundle.getMessage(SourceCitationsTableModel.class, "SourceCitationsTableModel.column.multimedia.title"),
        NbBundle.getMessage(SourceCitationsTableModel.class, "SourceCitationsTableModel.column.note.title")
    };

    public SourceCitationsTableModel() {
    }

    @Override
    public int getRowCount() {
        return mSourcesList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Property source = mSourcesList.get(row);
        if (source != null) {
            switch (column) {
                case 0: {
                    if (source instanceof PropertySource) {
                        Property event = source.getProperty("EVEN");
                        if (event != null) {
                            return PropertyTag2Name.getTagName(event.getValue());
                        } else {
                            return "";
                        }
                    } else {
                        return "";
                    }
                }
                case 1: {
                    if (source instanceof PropertySource) {
                        Source targetEntity = (Source) ((PropertySource) source).getTargetEntity();
                        if (targetEntity != null) {
                            return targetEntity.getTitle();
                        } else {
                            return "";
                        }
                    } else {
                        return source.getValue();
                    }
                }
                case 2: {
                    if (source instanceof PropertySource) {
                        Property page = source.getProperty("PAGE");
                        if (page != null) {
                            return page.getValue();
                        } else {
                            return "";
                        }
                    } else {
                        return "";
                    }
                }
                case 3: {
                    ArrayList<Property> multimediaObjects = new ArrayList<>(Arrays.asList(source.getProperties("OBJE")));
                    if (source instanceof PropertySource) {
                        Source targetEntity = (Source) ((PropertySource) source).getTargetEntity();
                        if (targetEntity != null) {
                            multimediaObjects.addAll(Arrays.asList(targetEntity.getProperties("OBJE")));
                        }
                    }
                    if (multimediaObjects.size() > 0) {
                        return NbBundle.getMessage(SourceCitationsTableModel.class, "SourceCitationsTableModel.column.multimedia.value.yes");
                    } else {
                        return NbBundle.getMessage(SourceCitationsTableModel.class, "SourceCitationsTableModel.column.multimedia.value.no");
                    }
                }
                case 4: {
                    ArrayList<Property> notes = new ArrayList<>(Arrays.asList(source.getProperties("NOTE")));
                    if (source instanceof PropertySource) {
                        Source targetEntity = (Source) ((PropertySource) source).getTargetEntity();
                        if (targetEntity != null) {
                            notes.addAll(Arrays.asList(targetEntity.getProperties("NOTE")));
                        }
                    }
                    if (notes.size() > 0) {
                        return NbBundle.getMessage(SourceCitationsTableModel.class, "SourceCitationsTableModel.column.note.value.yes");
                    } else {
                        return NbBundle.getMessage(SourceCitationsTableModel.class, "SourceCitationsTableModel.column.note.value.no");
                    }
                }
                default: {
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
    
    public String[] getColumnsName() {
        return columnsName;
    }

    public void add(Property source) {
        mSourcesList.add(source);
        fireTableDataChanged();
    }

    public void addAll(List<Property> sourcesList) {
        mSourcesList.addAll(sourcesList);
        fireTableDataChanged();
    }

    public void clear() {
        mSourcesList.clear();
        fireTableDataChanged();
    }

    public Property getValueAt(int row) {
        return mSourcesList.get(row);
    }

    public Property remove(int row) {
        Property source = mSourcesList.remove(row);
        fireTableDataChanged();
        return source;
    }

    public int indexOf(Object o) {
        return this.mSourcesList.indexOf(o);
    }
}
