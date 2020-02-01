package ancestris.modules.releve.merge;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.openide.util.NbPreferences;

/**
 * SimilarNameSet  gere les equivalences entre les noms (ou les prenoms)
 * @author Michel
 */
public class SimilarNameSet {
    static final private String SimilarFirstNamePreference = "SimilarFirstNameList";
    static final private String SimilarLastNamePreference = "SimilarLastNameList";
    static private SimilarNameSet similarLastNameSet = null;
    static private SimilarNameSet similarFirstNameSet = null;

    private final HashMap<String,String> similarNames = new HashMap<String,String>();
    private final String similarPreference;


    /**
     * factory
     * @return
     */
    public static SimilarNameSet getSimilarFirstName() {
        if( similarFirstNameSet== null) {
            similarFirstNameSet = new SimilarNameSet(SimilarFirstNamePreference);
            similarFirstNameSet.loadPreferences();
        }
        return similarFirstNameSet;
    }

    /**
     * factory
     * @return
     */
    public static SimilarNameSet getSimilarLastName() {
        if( similarLastNameSet== null) {
            similarLastNameSet = new SimilarNameSet(SimilarLastNamePreference);
            similarLastNameSet.loadPreferences();
        }
        return similarLastNameSet;
    }

    private SimilarNameSet(String similarPreference) {
        this.similarPreference = similarPreference;
    }

    public String getSimilarName(String inputName) {
        String similarName = similarNames.get(inputName);
        if( similarName != null) {
            return similarName;
        } else {
            return inputName;
        }
    }

    public Set<String> getKeys() {
        return similarNames.keySet();
    }

    public Collection<String>  getValues() {
        return similarNames.values();
    }

    public void save(Map<String,String> entries) {
        similarNames.clear();
        similarNames.putAll(entries);
        savePreferences();
    }

    /**
     * reset uniqument pour les tests
     */
    protected void reset() {
        similarNames.clear();
        loadPreferences();
    }


    /**
     * charge les equivalences des prénoms
     */
    private void loadPreferences() {

        // je recupere la liste des valeurs similaires
        String similarString = NbPreferences.forModule(SimilarNameSet.class).get(
                    similarPreference,"");
        String[] values = similarString.split(";");
        for (String value : values) {
            if (!value.isEmpty()) {
                String[] pairValue = value.split("=");
                similarNames.put(pairValue[0], pairValue[1]);
            }
        }
    }


    /**
     * enregistre les equivalences
     */
    private void savePreferences() {
        StringBuilder values = new StringBuilder();

        for (Map.Entry<String, String> entry : similarNames.entrySet()) {
            values.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        NbPreferences.forModule(SimilarNameSet.class).put(
                   similarPreference, values.toString());
    }

}
