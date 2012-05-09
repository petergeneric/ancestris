package ancestris.modules.releve.model;

import ancestris.modules.releve.ReleveEditorListener;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Michel
 */
public abstract class ModelAbstract extends AbstractTableModel  {

    protected List<Record> releveList = new ArrayList<Record>();
    private boolean dirty = false;
    
    // liste des editeurs succeptibles d'être en train de modifier un releve
    private ArrayList<ReleveEditorListener> validationListeners = new ArrayList<ReleveEditorListener>(1);

    public ModelAbstract() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Implement ModelAbstract methods
    ///////////////////////////////////////////////////////////////////////////
    /**
     * appelle le constructeur de releve specifique du modele
     * @return
     */
    public abstract Record createRecord();

    /**
     * constructeur de releve
     * Cette methode doit etre appelee par le constructeur specifique createRecord()
     * de chaque modele
     * @param record
     */
    protected int addRecord(final Record record) {
        releveList.add(record);
      
        // keep undo
        lock.addChange(new Undo() {
        @Override
            Record undo() {
                removeRecord(record);
                return null;
            }
        });

        return releveList.size()-1;
    }

    protected void removeRecord(final Record record) {
        // keep undo
        lock.addChange(new Undo() {

            @Override
            Record undo() {
                addRecord(record);
                return record;
            }
        });

        int recordIndex = releveList.indexOf(record);
        releveList.remove(record);
        fireTableRowsDeleted(recordIndex, recordIndex);
    }

    /**
     * supprime tous les releves du modele
     * et purge Undo
     * et reinitialise Dirty
     */
    protected void removeAll() {
        try {
        // je vide le modele
        int previousRowCount = getRowCount();
        releveList.clear();
        if (previousRowCount > 0) {
            fireTableRowsDeleted(0, previousRowCount - 1);
        }
        lock.clear();
        resetDirty();
        } catch (NullPointerException e ){
            e.printStackTrace();

        }
    }

    public Record getRecord(int index) {
        Record record = null;
        try {
            record = releveList.get(index);
        } catch (IndexOutOfBoundsException e) {
            record = null;
        }
        return record;
    }

    /**
     * retourne le releve precedent par ordre chonologique de creation dans
     * le modele
     * @param record
     * @return
     */
    public Record getPreviousRecord(Record record) {
        Record previousRecord = null;
        try {
            previousRecord = releveList.get(releveList.indexOf(record)-1);
        } catch (IndexOutOfBoundsException e) {
            previousRecord = null;
        }
        return previousRecord;
    }

    public int getIndex(Record record) {
        return releveList.indexOf(record);
    }
    
    public boolean isDirty() {
        return dirty;
    }

    public void resetDirty() {
        dirty = false;
    }

       
    ///////////////////////////////////////////////////////////////////////////
    // Implement AbstractTableModel methods
    ///////////////////////////////////////////////////////////////////////////
    @Override
    public abstract int getColumnCount();

    @Override
    public abstract String getColumnName(int col);

    @Override
    public abstract Object getValueAt(int row, int col);

    @Override
    public int getRowCount() {
        return releveList.size();
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Implement AbstractEditorModel methods
    ///////////////////////////////////////////////////////////////////////////
    public abstract BeanField[] getFieldList( int row );

    public abstract String getColumnLayout();
    public abstract void putColumnLayout(String columnLayout);
    public abstract int getEditorWidth();
    public abstract void putEditorWidth(int width);


    ///////////////////////////////////////////////////////////////////////////
    // Implement Undo methods
    ///////////////////////////////////////////////////////////////////////////
    public void fieldChanged(final Record record, final Field field, final Object oldValue) {

        // keep undo
        lock.addChange(new Undo() {

            @Override
            Record undo() {
                field.setValue(oldValue);
                return record;
            }
        });
    }


    ///////////////////////////////////////////////////////////////////////////
    // Manager VerificationListener
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @param validationListeners the validationListeners to set
     */
    public void addReleveValidationListener(ReleveEditorListener listener) {
        validationListeners.add(listener);
    }

    /**
     * @param validationListeners the validationListeners to set
     */
    public void removeReleveValidationListener(ReleveEditorListener listener) {
        validationListeners.remove(listener);
    }

    public String verifyRecord( ) {
        StringBuilder errorMessage = new StringBuilder();

        for (ReleveEditorListener listener : validationListeners) {
            int recordIndex = listener.getCurrentRecordIndex();

            try {
                Record record = releveList.get(recordIndex);

                if (record.getEventDateString().equals("")) {
                    errorMessage.append("La date de l'évènement est vide").append("\n");
                }
                if (record.getIndiLastName().isEmpty() && record.getIndiFirstName().isEmpty()) {
                    errorMessage.append("Le nom et le prénom sont vides").append("\n");
                }
            } catch (IndexOutOfBoundsException e) {
                // rien a faire
            }
        }

        return errorMessage.toString();
    }

     public String verifyRecord( int recordIndex ) {
        StringBuilder errorMessage = new StringBuilder();

        try {
            Record record = releveList.get(recordIndex);

            if (record.getEventDateString().equals("")) {
                errorMessage.append("La date de l'évènement est vide").append("\n");
            }
            if (record.getIndiLastName().isEmpty() && record.getIndiFirstName().isEmpty()) {
                errorMessage.append("Le nom et le prénom sont vides").append("\n");
            }
        } catch (IndexOutOfBoundsException e) {
            // rien a faire
        }

        return errorMessage.toString();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Undo
    ///////////////////////////////////////////////////////////////////////////

    private static boolean undoEnabled = true;

    //private Object writeSemaphore = new Object();
    private Lock lock = new Lock();

    
    /**
     * @param undoEnabled the undoEnabled to set
     */
    public static void setUndoEnabled(boolean value) {
        undoEnabled = value;
    }

    /**
     * retourne la liste des releves qui ont lea même date, même nom et même prénom
     * de l'individu du releve donné en parametre.
     * @param record
     * @return
     */
    public Record[]  findDuplicateRecord(Record referenceRecord) {
        List<Record> duplicate = new ArrayList<Record>();

        for(Record record : releveList) {
            if ( record.getEventDateField().toString().equals(referenceRecord.getEventDateField().toString())
                 && record.getIndiFirstName().equals(referenceRecord.getIndiFirstName())
                 && record.getIndiLastName().equals(referenceRecord.getIndiLastName())
                 && record != referenceRecord ) {
                 duplicate.add(record);
            }
        }
        return duplicate.toArray(new Record[0]);
    }

    public Record undo() {
        Record record;
        // run through undos
        Undo undo = lock.removeChange();
        if (undo != null) {
            setUndoEnabled(false);
            record = undo.undo();
            setUndoEnabled(true);

        } else {
            // beep
            Toolkit.getDefaultToolkit().beep();
            record = null;
        }
        return record;
    }

    /**
     * Undo
     */
    private abstract class Undo {
        abstract Record undo() ;
    }

    /**
     * Our locking mechanism is based on one writer at a time
     */
    private class Lock {

        List<Undo> undos = new ArrayList<Undo>();

        void clear() {
            undos.clear();
        }
        
        void addChange(Undo run) {

            dirty = true;
            if (!undoEnabled) {
                return;
            } else {
                undos.add(run);

                if (undos.size() > 1000) {
                    undos.remove(0);
                }
            }
        }

        Undo removeChange() {
            if (undos.size() > 0) {
                return undos.remove(undos.size() - 1);
            } else {
                return null;
            }
        }
    }

    
}
