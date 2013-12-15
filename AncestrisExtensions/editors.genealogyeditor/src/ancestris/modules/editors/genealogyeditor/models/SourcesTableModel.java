package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Property;
import genj.gedcom.Source;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dominique
 */

/*
 * SOURCE_RECORD:=
 * n @<XREF:SOUR>@ SOUR
 * +1 DATA
 * +2 EVEN <EVENTS_RECORDED>
 * +3 DATE <DATE_PERIOD>
 * +3 PLAC <SOURCE_JURISDICTION_PLACE>
 * +2 AGNC <RESPONSIBLE_AGENCY>
 * +2 <<NOTE_STRUCTURE>>
 * +1 AUTH <SOURCE_ORIGINATOR>
 * +2 [CONC|CONT] <SOURCE_ORIGINATOR>
 * +1 TITL <SOURCE_DESCRIPTIVE_TITLE>
 * +2 [CONC|CONT] <SOURCE_DESCRIPTIVE_TITLE>
 * +1 ABBR <SOURCE_FILED_BY_ENTRY>
 * +1 PUBL <SOURCE_PUBLICATION_FACTS>
 * +2 [CONC|CONT] <SOURCE_PUBLICATION_FACTS>
 * +1 TEXT <TEXT_FROM_SOURCE>
 * +2 [CONC|CONT] <TEXT_FROM_SOURCE>
 * +1 <<SOURCE_REPOSITORY_CITATION>>
 * +1 REFN <USER_REFERENCE_NUMBER>
 * +2 TYPE <USER_REFERENCE_TYPE>
 * +1 RIN <AUTOMATED_RECORD_ID>
 * +1 <<CHANGE_DATE>>
 * +1 <<NOTE_STRUCTURE>>
 * +1 <<MULTIMEDIA_LINK>>
 */
public class SourcesTableModel extends AbstractTableModel {

    List<Source> sourcesList = new ArrayList<Source>();
    String[] columnsName = {"ID", "Description", "Events"};

    public SourcesTableModel() {
    }

    @Override
    public int getRowCount() {
        return sourcesList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Source source = sourcesList.get(row);
        if (source != null) {
            if (column == 0) {
                return source.getId();
            } else if (column == 1) {
                return source.getTitle();
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

    public void add(Source source) {
        sourcesList.add(source);
        fireTableDataChanged();
    }

    public void addAll(List<Source> sourcesList) {
        sourcesList.addAll(sourcesList);
        fireTableDataChanged();
    }

    public void update(List<Source> sourcesList) {
        this.sourcesList.clear();
        addAll(sourcesList);
    }

    public Source getValueAt(int row) {
        return sourcesList.get(row);
    }

    public Source remove(int row) {
        Source source = sourcesList.remove(row);
        fireTableDataChanged();
        return source;
    }
}
