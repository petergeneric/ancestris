package ancestris.modules.editors.genealogyeditor.models;

import genj.gedcom.Note;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dominique
 */
public class NotesTableModel extends AbstractTableModel {

    List<Note> notesList = new ArrayList<Note>();
    String[] columnsName = {"ID", "text"};

    public NotesTableModel() {
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
            Note note = notesList.get(row);
            if (column == 0) {
                return note.getId();
            } else {
                return note.getDisplayValue();
            }
        } else {
            return "";
        }
    }

    @Override
    public String getColumnName(int col) {
        return columnsName[col];
    }

    public void add(Note notes) {
        this.notesList.add(notes);
        fireTableDataChanged();
    }

    public void addAll(List<Note> notesList) {
        this.notesList.addAll(notesList);
        fireTableDataChanged();
    }

    public void update(List<Note> notesList) {
        this.notesList.clear();
        this.notesList.addAll(notesList);
    }

    public Note getValueAt(int row) {
        return notesList.get(row);
    }

    public Note remove(int row) {
        Note note = notesList.remove(row);
        fireTableDataChanged();
        return note;
    }
}
