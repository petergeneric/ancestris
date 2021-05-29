package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Property;
import genj.gedcom.PropertyRepository;
import genj.gedcom.Source;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 *
 * SOURCE_RECORD:= n @<XREF:SOUR>@ SOUR +1 DATA +2 EVEN <EVENTS_RECORDED>
 * +3 DATE <DATE_PERIOD>
 * +3 PLAC <SOURCE_JURISDICTION_PLACE>
 * +2 AGNC <RESPONSIBLE_AGENCY>
 * +2 <<NOTE_STRUCTURE>> +1 AUTH <SOURCE_ORIGINATOR>
 * +2 [CONC|CONT] <SOURCE_ORIGINATOR>
 * +1 TITL <SOURCE_DESCRIPTIVE_TITLE>
 * +2 [CONC|CONT] <SOURCE_DESCRIPTIVE_TITLE>
 * +1 ABBR <SOURCE_FILED_BY_ENTRY>
 * +1 PUBL <SOURCE_PUBLICATION_FACTS>
 * +2 [CONC|CONT] <SOURCE_PUBLICATION_FACTS>
 * +1 TEXT <TEXT_FROM_SOURCE>
 * +2 [CONC|CONT] <TEXT_FROM_SOURCE>
 * +1 <<SOURCE_REPOSITORY_CITATION>> +1 REFN <USER_REFERENCE_NUMBER>
 * +2 TYPE <USER_REFERENCE_TYPE>
 * +1 RIN <AUTOMATED_RECORD_ID>
 * +1 <<CHANGE_DATE>> +1 <<NOTE_STRUCTURE>> +1 <<MULTIMEDIA_LINK>>
 */
public class SourcesTableModel extends AbstractTableModel {

    List<Source> mSourcesList = new ArrayList<>();
    private final String[] columnsName = {
        NbBundle.getMessage(SourcesTableModel.class, "SourcesTableModel.column.ID.title"),
        NbBundle.getMessage(SourcesTableModel.class, "SourcesTableModel.column.description.title"),
        NbBundle.getMessage(SourcesTableModel.class, "SourcesTableModel.column.events.title"),
        NbBundle.getMessage(SourcesTableModel.class, "SourcesTableModel.column.date.title"),
        NbBundle.getMessage(SourcesTableModel.class, "SourcesTableModel.column.repository.title"),
        NbBundle.getMessage(SourcesTableModel.class, "SourcesTableModel.column.repository_caln.title")
    };

    public SourcesTableModel() {
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
        Source source = mSourcesList.get(row);
        if (source != null) {
            switch (column) {
                case 0:
                    return source.getId();
                case 1:
                    return source.getTitle();
                case 2:
                {
                    Property propertyByPath = source.getPropertyByPath(".:DATA:EVEN");
                    if (propertyByPath != null) {
                        return propertyByPath.getDisplayValue();
                    } else {
                        return "";
                    }
                }
                case 3:
                {
                    Property propertyByPath = source.getPropertyByPath(".:DATA:EVEN:DATE");
                    if (propertyByPath != null) {
                        return propertyByPath.getDisplayValue();
                    } else {
                        return "";
                    }
                }
                case 4:
                {
                    // FIXME: same fix as in r6314. To be improved
                    final Property p =source.getProperty("REPO");
                    PropertyRepository repositoryXref = (PropertyRepository) (p instanceof PropertyRepository?p:null);
                    if (repositoryXref != null) {
                        return repositoryXref.getDisplayValue();
                    } else {
                        return "";
                    }
                }
                case 5:
                {
                    // FIXME: same fix as in r6314. To be improved
                    final Property p =source.getProperty("REPO");
                    PropertyRepository repositoryXref = (PropertyRepository) (p instanceof PropertyRepository?p:null);
                    if (repositoryXref != null) {
                        Property caln = repositoryXref.getProperty("CALN");
                        if (caln != null) {
                            return caln.getDisplayValue();
                        } else {
                            return "";
                        }
                    } else {
                        return "";
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
    public String getColumnName(int col) {
        return columnsName[col];
    }
    
    public String[] getColumnsName() {
        return columnsName;
    }

    public void add(Source source) {
        mSourcesList.add(source);
        fireTableDataChanged();
    }

    public void addAll(Collection<Source> sourcesList) {
        mSourcesList.addAll(sourcesList);
        fireTableDataChanged();
    }

    public void clear() {
        this.mSourcesList.clear();
    }

    public Source getValueAt(int row) {
        return mSourcesList.get(row);
    }

    public Source remove(int row) {
        Source source = mSourcesList.remove(row);
        fireTableDataChanged();
        return source;
    }
}
