package ancestris.modules.releve;

import ancestris.modules.releve.model.Record;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.releve.editor.EditorBeanField;
import ancestris.modules.releve.editor.EditorBeanGroup;
import ancestris.modules.releve.model.Field.FieldType;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.RecordModel;
import java.util.regex.Pattern;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;


/**
 *
 * @author Michel
 */
public class ReleveQuickSearch implements SearchProvider {
    /**
     * Method is called by infrastructure when search operation was requested.
     * Implementors should evaluate given request and fill response object with
     * apropriate results
     *
     * @param request Search request object that contains information what to search for
     * @param response Search response object that stores search results. Note that it's important to react to return value of SearchResponse.addResult(...) method and stop computation if false value is returned.
     */
    
    private static final FieldType lastNameFieldTypes[] = {
        FieldType.indiLastName , FieldType.indiMarriedLastName,  FieldType.indiFatherLastName , FieldType.indiMotherLastName,
        FieldType.wifeLastName , FieldType.wifeMarriedLastName,  FieldType.wifeFatherLastName , FieldType.wifeMotherLastName,
        FieldType.witness1LastName, FieldType.witness2LastName, FieldType.witness3LastName, FieldType.witness4LastName,
    };
            
    private static final FieldType firstNameFieldTypes[] = {    
        FieldType.indiFirstName , FieldType.indiMarriedFirstName,  FieldType.indiFatherFirstName , FieldType.indiMotherFirstName,
        FieldType.wifeFirstName , FieldType.wifeMarriedFirstName,  FieldType.wifeFatherFirstName , FieldType.wifeMotherFirstName,
        FieldType.witness1FirstName, FieldType.witness2FirstName, FieldType.witness3FirstName, FieldType.witness4FirstName,
    } ;
    
    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        synchronized (this) {

            // recherche avec la meme methode que Utilities.wordsMatch
            // Utilities.wordsMatch(String text, String resquestPattern) {
            //  resquestPattern = resquestPattern.replaceAll(" +", ".+");
            //  return text.matches(".*" + resquestPattern + ".*");
            //}            
            //String  pattern = request.getText().toLowerCase().replaceAll(" +", ".+");

            Pattern espacePattern = Pattern.compile(" +");
            String resquestPattern = espacePattern.matcher(request.getText().toLowerCase()).replaceAll(".+");

            // je cherche dans toutes les instances de ReleveTopComponent
            for (ReleveTopComponent tc : AncestrisPlugin.lookupAll(ReleveTopComponent.class)) {
                searchInModel( tc, resquestPattern, response);
            }
        }
    }


    private void searchInModel(ReleveTopComponent tc, String resquestPattern, SearchResponse response ) {
        RecordModel model = tc.getDataManager().getDataModel();
        for (int indexRecord=0; indexRecord < model.getRowCount(); indexRecord++) {
            Record record = model.getRecord(indexRecord);
            
            for (int i =0 ; i < lastNameFieldTypes.length ; i++) {
                Field lastName  = record.getField(lastNameFieldTypes[i]);
                Field firstName = record.getField(firstNameFieldTypes[i]);             
                if (lastName != null && firstName != null ) {
                    String resultDisplay = lastName.toString() + " " + firstName.toString();
                                               
                    if (resultDisplay.toLowerCase().matches(".*" + resquestPattern + ".*") ) {
                        if (!response.addResult(createAction(tc, record, lastNameFieldTypes[i]), tc.getDataManager().getCityName() + " " + resultDisplay + " , " + EditorBeanGroup.getGroup(record.getType(), lastNameFieldTypes[i]).getTitle() + " , " + record.getEventDateString() )) {
                            return;
                        }
                    }
                }
            }
        }
    }

     private Runnable createAction(final ReleveTopComponent tc, final Record record, final Field.FieldType fieldType) {
        return new Runnable() {

            @Override
            public void run() {
                //System.out.println("Found record "+ record.getIndiFirstName().toString());
                tc.requestVisible();
                tc.showToFront();
                tc.selectField(record, fieldType);
            }
        };
    }
}

