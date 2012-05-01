package ancestris.modules.releve.file;

import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordMarriage;
import ancestris.modules.releve.model.RecordMisc;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Michel
 */
public class FileBuffer {
    private List<Record> records = new ArrayList<Record>();
    private HashMap<String, Integer> places = new HashMap<String, Integer>();
    private int birthCount =0;
    private int marriageCount =0;
    private int deathCount =0;
    private int miscCount =0;

    StringBuilder sb = new StringBuilder();

     /**
     * Cette methode est appelée par les ckasses qui lisent un fichier
     * @param record
     * @param recordNo
     */
    public void loadRecord(Record record) {
        getRecords().add(record);
        String placeValue = record.getEventPlace().getValue();
        // je met a jour le compter des lieux
        int count = places.containsKey(placeValue) ? places.get(placeValue) : 0;
        places.put(placeValue, count + 1);
        //je mets a jour le compteur des releves
        if (record instanceof RecordBirth) {
            birthCount++;
        } else  if (record instanceof RecordMarriage) {
            marriageCount++;
        } else  if (record instanceof RecordDeath) {
            deathCount++;
        } else  if (record instanceof RecordMisc) {
            miscCount++;
        }
    }

    public StringBuilder append(String message) {
        return sb.append( message );
    }

    public String getError() {
        return sb.toString();
    }

    /**
     * @return the records
     */
    public List<Record> getRecords() {
        return records;
    }

    /**
     * @return the places
     */
    public List<String> getPlaces() {
        return new ArrayList<String>(places.keySet());
    }

    /**
     * @return the birthCount
     */
    public int getBirthCount() {
        return birthCount;
    }

    /**
     * @return the marriageCount
     */
    public int getMarriageCount() {
        return marriageCount;
    }

    /**
     * @return the deathCount
     */
    public int getDeathCount() {
        return deathCount;
    }

    /**
     * @return the miscCount
     */
    public int getMiscCount() {
        return miscCount;
    }
}
