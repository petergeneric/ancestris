package ancestris.modules.releve.file;

import ancestris.modules.releve.model.Record;
import ancestris.modules.releve.model.RecordBirth;
import ancestris.modules.releve.model.RecordDeath;
import ancestris.modules.releve.model.RecordInfoPlace;
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
    private RecordInfoPlace recordsInfoPlace = new RecordInfoPlace();
    private List<Record> records = new ArrayList<Record>();
    private HashMap<String, Integer> places = new HashMap<String, Integer>();
    private int birthCount =0;
    private int marriageCount =0;
    private int deathCount =0;
    private int miscCount =0;

    StringBuilder sb = new StringBuilder();

     /**
     * Cette methode est appelée par les classes qui lisent un fichier
     * @param record
     * @param recordNo
     */
    public void addRecord(Record record) {
        getRecords().add(record);
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

    public void setRegisterInfoPlace(String cityName, String cityCode, String county, String state, String country) {
        recordsInfoPlace.setCityName(cityName);
        recordsInfoPlace.setCityCode(cityCode);
        recordsInfoPlace.setCountyName(county);
        recordsInfoPlace.setStateName(state);
        recordsInfoPlace.setCountryName(country);
        // je met a jour le compter des lieux
        String placeValue = recordsInfoPlace.getValue();
        int count = places.containsKey(placeValue) ? places.get(placeValue) : 0;
        places.put(placeValue, count + 1);
    }

     public RecordInfoPlace getRegisterInfoPlace() {
        return recordsInfoPlace;
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
