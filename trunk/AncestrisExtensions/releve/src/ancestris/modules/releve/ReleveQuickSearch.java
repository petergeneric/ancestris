package ancestris.modules.releve;

import ancestris.modules.releve.model.Record;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.releve.editor.EditorBeanGroup;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.RecordModel;
import java.text.Normalizer;
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
            
            //Pattern espacePattern = Pattern.compile(" +");
            //String resquestPattern = espacePattern.matcher(request.getText().toLowerCase()).replaceAll(".+");
            //resquestPattern = Normalizer.normalize(resquestPattern, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
            String resquestPattern = Normalizer.normalize(request.getText().toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");        
        
            // je cherche dans toutes les instances de ReleveTopComponent
            for (ReleveTopComponent tc : AncestrisPlugin.lookupAll(ReleveTopComponent.class)) {
                searchInModel( tc, resquestPattern, response);
            }
        }
    }

    /**
     * Recherche
     * recherche avec la meme methode ancestris.app.IndiQuickSearch qui utilise 
     * Utilities.wordsMatch(String text, String resquestPattern) 
     * {
     *   resquestPattern = resquestPattern.replaceAll(" +", ".+");
     *   return text.matches(".*" + resquestPattern + ".*");
     * }
     * 
     * Formatage de la réponse :
     * Quicksearch formate la réponse en HTML pour mettre en gras la chaîne trouvée
     * si elle est exactement la mêm que que la chaine cherchée (ce qui n'est pas 
     * le cas quand elle contient des accents) 
     * Mais si la réponse commence par <html> , ce mécanieme est désactivé et 
     * permet de mettre en gras soi meme la chaine de son choix
     * 
     * https://github.com/apache/incubator-netbeans/blob/master/spi.quicksearch/src/org/netbeans/modules/quicksearch/ResultsModel.java
     * 
     * @param tc  topComponent utilisé par l'action de la réponse (pour donner le focus au relevé)
     * @param resquestPattern chaine de caractères recherchée 
     * @param response   réponse à renseigner
     */
    // 

    private void searchInModel(ReleveTopComponent tc, String resquestPattern, SearchResponse response ) {
        RecordModel model = tc.getDataManager().getDataModel();
        
        for (int indexRecord=0; indexRecord < model.getRowCount(); indexRecord++) {
            Record record = model.getRecord(indexRecord);
            
            for (int i =0 ; i < lastNameFieldTypes.length ; i++) {
                Field lastName  = record.getField(lastNameFieldTypes[i]);
                Field firstName = record.getField(firstNameFieldTypes[i]);             
                if (lastName != null && firstName != null ) {
                    String resultDisplay = lastName.toString() + " " + firstName.toString();
                    String resultSearch  = Normalizer.normalize(resultDisplay, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();  
                    if (resultSearch.matches(".*" + resquestPattern + ".*") ) {
                        int start = resultSearch.indexOf(resquestPattern);
                        int end = start+ resquestPattern.length();
                        // je construit la réponse en commençant par <html>
                        StringBuilder sbDisplay = new StringBuilder("<html>");
                        if( start > 0) {
                            sbDisplay.append(resultDisplay.substring(0, start));                            
                        }
                        // je mets en gras la chaine trouvée 
                        sbDisplay.append("<b>").append(resultDisplay.substring(start, end)).append("</b>");
                        if(end < resultDisplay.length() -1) {
                           sbDisplay.append(resultDisplay.substring(end));
                        }
                        // j'ajoute le role de l'individu dans le relevé
                        sbDisplay.append(", ").append(EditorBeanGroup.getGroup(record.getType(), lastNameFieldTypes[i]).getTitle());
                        // j'ajoute la date du relevé
                        sbDisplay.append(", ").append(record.getFieldValue(FieldType.eventDate));
                        // j'ajoute le lieu du relevé (ville)
                        if( !tc.getDataManager().getCityName().isEmpty()) {
                            sbDisplay.append(" ").append(tc.getDataManager().getCityName());
                        }
                        
                        if (!response.addResult(createAction(tc, record, lastNameFieldTypes[i]), sbDisplay.toString() ) ) {
                            // j'arrete la recherche si la dernière réponse n'est pas acceptée
                            return;
                        }
                    }
                }
            }
        }
    }

     private Runnable createAction(final ReleveTopComponent tc, final Record record, final Record.FieldType fieldType) {
        return new Runnable() {

            @Override
            public void run() {
                tc.requestVisible();
                tc.showToFront();
                tc.selectField(record, fieldType);
            }
        };
    }
}

