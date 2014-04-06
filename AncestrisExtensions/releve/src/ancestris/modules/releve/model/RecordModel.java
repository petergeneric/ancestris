package ancestris.modules.releve.model;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Michel
 */
public class RecordModel {

    protected ArrayList<Record> releveList = new ArrayList<Record>();
    private boolean dirty = false;
    
    private ArrayList<RecordModelListener> recordModelListeners = new ArrayList<RecordModelListener>(1);

    public RecordModel() {
    }

    /**
     * constructeur de releve
     * Cette methode doit etre appelee par le constructeur specifique createRecord()
     * de chaque modele
     * @param record
     */
    protected int addRecord(final Record record) {
        releveList.add(record);
        int recordIndex = releveList.size()-1;

        // keep undo
        lock.addChange(new Undo() {
        @Override
            Record undo() {
                removeRecord(record);
                return null;
            }
        });
        fireRecordModelInserted(recordIndex, recordIndex);
        return recordIndex;
    }

    /**
     * ajoute plusieurs releves
     * Cette methode est plus efficace que adRecord() car elle notifie les
     * listeners une seule fois
     * Attention : cette methode ne memorise pas les records ajoutés dans Undo
     * @param record
     */
    protected void addRecords(List<Record> records) {
        // j'ajoute les releves
        for (Record record : records) {
            releveList.add(record);
        }
        fireAllChanged();
    }

    /**
     * insere un relevé à la place de celui qui est a l'index recordIndex
     * @param record
     */
    protected void insertRecord(final Record record, int recordIndex) {
        releveList.add(recordIndex,record);
        
        // keep undo
        lock.addChange(new Undo() {
        @Override
            Record undo() {
                removeRecord(record);
                return null;
            }
        });
        fireRecordModelInserted(recordIndex, recordIndex);
    }

    
    protected void swapRecordNext(final Record record) {
         // keep undo
        lock.addChange(new Undo() {

            @Override
            Record undo() {
                int recordIndex = releveList.indexOf(record);
                Collections.swap(releveList, recordIndex-1, recordIndex);
                return record;
            }
        });
        int recordIndex = releveList.indexOf(record);
        Collections.swap(releveList, recordIndex, recordIndex+1);
        fireRecordModelUpdated(recordIndex, recordIndex+1);
    }
    
    protected void swapRecordPrevious(final Record record) {
        // keep undo
        lock.addChange(new Undo() {

            @Override
            Record undo() {
                int recordIndex = releveList.indexOf(record);
                Collections.swap(releveList, recordIndex, recordIndex+1);
                return record;
            }
        });
        int recordIndex = releveList.indexOf(record);
        Collections.swap(releveList, recordIndex-1, recordIndex);
        fireRecordModelUpdated(recordIndex-1, recordIndex);
    }
    
    void renumberRecords(final Record record, int[] tableIndexList) {
        
        final int[] revertTableIndexList = new int[tableIndexList.length]; 
        for(int i= 0; i < revertTableIndexList.length ; i++) {
            revertTableIndexList[tableIndexList[i]] = i;
        }
        
        lock.addChange(new Undo() {

            @Override
            Record undo() {
                Collections.sort(releveList, new OrderArrayList(revertTableIndexList));
                fireAllChanged();
                return record;
            }
        });
        Collections.sort(releveList, new OrderArrayList(tableIndexList));
        fireAllChanged();
    }
    
    public class OrderArrayList extends ArrayList<Record> implements Comparator<Record> {

        int[] tableIndexList;
        
        private OrderArrayList(int[] tableIndexList) {
            this.tableIndexList = tableIndexList;
        }

        @Override
        public int compare(Record itemO, Record itemT) {
            if (tableIndexList[releveList.indexOf(itemO)] >= tableIndexList[releveList.indexOf(itemT)]) {
                return 1;
            } else {
                return -1;
            }
        }

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
        fireRecordModelDeleted(recordIndex, recordIndex);
    }

    /**
     * supprime tous les releves du modele
     * et purge Undo
     * et reinitialise Dirty
     */
    protected void removeAll() {
        // je vide le modele
        int previousRowCount = releveList.size();
        releveList.clear();
        if (previousRowCount > 0) {
            fireRecordModelDeleted(0, previousRowCount - 1);
        }
        lock.clear();
        resetDirty();
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

    public int getRowCount() {
        return releveList.size();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void resetDirty() {
        dirty = false;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Implement Undo methods
    ///////////////////////////////////////////////////////////////////////////
    public void notiFyFieldChanged(final Record record, final Field.FieldType fieldType, final Field field, final Object oldValue) {
        final int recordIndex = getIndex(record);

        // keep undo
        lock.addChange(new Undo() {

            @Override
            Record undo() {
                field.setValue(oldValue);
                fireRecordModelUpdated(recordIndex, fieldType);
                return record;
            }
        });
        fireRecordModelUpdated(recordIndex, fieldType);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Manager VerificationListener
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @param validationListeners the validationListeners to set
     */
    public void addRecordModelListener(RecordModelListener listener) {
        recordModelListeners.add(listener);
    }

    /**
     * @param validationListeners the validationListeners to set
     */
    public void removeRecordModelListener(RecordModelListener listener) {
        recordModelListeners.remove(listener);
    }

    public void fireRecordModelInserted (int firstIndex, int lastIndex) {
          for (RecordModelListener listener : recordModelListeners) {
            listener.recordInserted(firstIndex, lastIndex);
        }
    }

    public void fireRecordModelDeleted (int firstIndex, int lastIndex) {
          for (RecordModelListener listener : recordModelListeners) {
            listener.recordDeleted(firstIndex, lastIndex);
        }
    }

    public void fireRecordModelUpdated (int firstIndex, int lastIndex) {
          for (RecordModelListener listener : recordModelListeners) {
            listener.recordUpdated(firstIndex, lastIndex);
        }
    }

    public void fireRecordModelUpdated (int recordIndex, Field.FieldType filedType) {
          for (RecordModelListener listener : recordModelListeners) {
            listener.recordUpdated(recordIndex, filedType);
        }
    }

    
    public void fireAllChanged () {
          for (RecordModelListener listener : recordModelListeners) {
            listener.allChanged();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Manager VerificationListener
    ///////////////////////////////////////////////////////////////////////////
   
    /**
     * vérifie si les champs obligatoires sont renseignés
     *  - date de l'évènement
     *  - nom ou prénom de l'individu
     * @param recordIndex
     * @return
     */
    public String verifyRecord( int recordIndex ) {
        StringBuilder errorMessage = new StringBuilder();

        try {
            Record record = releveList.get(recordIndex);

            if (record.getEventDateString().isEmpty()) {
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
            if ( record.getEventDateProperty().toString().equals(referenceRecord.getEventDateProperty().toString())
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
