package ancestris.modules.releve;

import ancestris.modules.releve.model.Record;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.releve.model.Field.FieldType;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.ModelAbstract;
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

    static private FieldType fieldTypes[] = { 
        FieldType.indiLastName , FieldType.indiMarriedLastName,  FieldType.indiFatherLastName , FieldType.indiMotherLastName, 
        FieldType.wifeLastName , FieldType.wifeMarriedLastName,  FieldType.wifeFatherLastName , FieldType.wifeMotherLastName, 
        FieldType.witness1LastName, FieldType.witness2LastName, FieldType.witness3LastName, FieldType.witness4LastName,
        
        FieldType.indiFirstName , FieldType.indiMarriedFirstName,  FieldType.indiFatherFirstName , FieldType.indiMotherFirstName,
        FieldType.wifeFirstName , FieldType.wifeMarriedFirstName,  FieldType.wifeFatherFirstName , FieldType.wifeMotherFirstName,
        FieldType.witness1FirstName, FieldType.witness2FirstName, FieldType.witness3FirstName, FieldType.witness4FirstName,

    } ;

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        synchronized (this) {
            for (ReleveTopComponent tc : AncestrisPlugin.lookupAll(ReleveTopComponent.class)) {
                searchInModel( tc, tc.getDataManager().getReleveBirthModel(), request, response);
                searchInModel( tc, tc.getDataManager().getReleveMarriageModel(), request, response);
                searchInModel( tc, tc.getDataManager().getReleveDeathModel(), request, response);
                searchInModel( tc, tc.getDataManager().getReleveMiscModel(), request, response);
            }
        }
    }

    private void searchInModel(ReleveTopComponent tc, ModelAbstract model, SearchRequest request, SearchResponse response ) {
        for (int indexRecord=0; indexRecord < model.getRowCount(); indexRecord++) {
            Record record = model.getRecord(indexRecord);


            for (FieldType fieldType : fieldTypes) {
                Field field = record.getField(fieldType);
                if (field != null && !field.isEmpty() ) {
                    if (field.toString().toLowerCase().contains(request.getText().toLowerCase())) {
                        if (!response.addResult(createAction(tc, record,fieldType), record.getIndiFirstName().toString() + " indi " + record.getEventDateField().getStart().getYear())) {
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
                tc.selectField(record, fieldType);
            }
        };
    }
}

