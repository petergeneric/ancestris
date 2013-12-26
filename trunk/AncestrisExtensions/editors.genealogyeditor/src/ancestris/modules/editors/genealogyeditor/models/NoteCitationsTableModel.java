package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyNote;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.openide.util.NbBundle;

/**
 *
 * @author dominique
 *
 * NOTE_STRUCTURE:=
 * [
 * n NOTE @<XREF:NOTE>@
 * |
 * n NOTE [<SUBMITTER_TEXT> | <NULL>]
 * +1 [CONC|CONT] <SUBMITTER_TEXT>
 * ]
 */
public class NoteCitationsTableModel extends AbstractTableModel {

    List<Property> notesList = new ArrayList<Property>();
    private String[] columnsName = {
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "NoteCitationsTableModel.column.ID.title"),
        NbBundle.getMessage(MultiMediaObjectsTableModel.class, "NoteCitationsTableModel.column.noteText.title")
    };

    public NoteCitationsTableModel() {
    }

    @Override
    public int getRowCount() {
        return notesList.size();
    }

    @Override
    public int getColumnCount() {
        return columnsName.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row < notesList.size()) {
            Property note = notesList.get(row);
            if (note instanceof PropertyNote) {
                if (((PropertyNote) note).getTargetEntity() != null) {
                    if (column == 0) {
                        return ((Note) ((PropertyNote) note).getTargetEntity()).getId();
                    } else {
                        return ((Note) ((PropertyNote) note).getTargetEntity()).getValue();
                    }
                } else {
                    return "";
                }
            } else {
                if (column == 0) {
                    return "";
                } else {
                    return note.getValue();
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

    public void add(Property notes) {
        this.notesList.add(notes);
        fireTableDataChanged();
    }

    public void addAll(List<Property> notesList) {
        this.notesList.addAll(notesList);
        fireTableDataChanged();
    }

    public void update(List<Property> notesList) {
        this.notesList.clear();
        this.notesList.addAll(notesList);
    }

    public Property getValueAt(int row) {
        return notesList.get(row);
    }

    public Property remove(int row) {
        Property note = notesList.remove(row);
        fireTableDataChanged();
        return note;
    }
}
