package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Property;
import genj.gedcom.PropertySource;
import genj.gedcom.Source;
import java.util.ArrayList;
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

    ArrayList<Property> mSourcesList = new ArrayList<Property>();
    private String[] columnsName = {
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "SourceCitationsTableModel.column.ID.title"),
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "SourceCitationsTableModel.column.description.title"),
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "SourceCitationsTableModel.column.events.title")
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
            if (column == 0) {
                if (source instanceof PropertySource) {
                    Source targetEntity = (Source) ((PropertySource) source).getTargetEntity();
                    if (targetEntity != null) {
                        return targetEntity.getId();
                    } else {
                        return "";
                    }
                } else {
                    return "";
                }
            } else if (column == 1) {
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

    public void add(Property source) {
        mSourcesList.add(source);
        fireTableDataChanged();
    }

    public void addAll(List<Property> sourcesList) {
        mSourcesList.addAll(sourcesList);
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
}
