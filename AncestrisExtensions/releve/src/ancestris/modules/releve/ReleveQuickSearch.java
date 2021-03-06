package ancestris.modules.releve;

import ancestris.modules.releve.model.Record;
import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.releve.editor.EditorBeanGroup;
import ancestris.modules.releve.model.Record.FieldType;
import ancestris.modules.releve.model.Field;
import ancestris.modules.releve.model.RecordModel;
import java.text.Normalizer;
import java.util.regex.Matcher;
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
            
            String resquestPattern = request.getText().replace("(", "\\(").replace(")", "\\)").trim();
            resquestPattern = Normalizer.normalize(resquestPattern.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");        
        
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
     * Formatage de la r??ponse :
     * Quicksearch formate la r??ponse en HTML pour mettre en gras la cha??ne trouv??e
     * si elle est exactement la m??me que que la chaine cherch??e (ce qui n'est pas 
     * le cas quand elle contient des accents) 
     * Mais si la r??ponse commence par <html> , ce m??canisme est d??sactiv?? et 
     * permet de mettre en gras soi meme la chaine de son choix
     * 
     * https://github.com/apache/incubator-netbeans/blob/master/spi.quicksearch/src/org/netbeans/modules/quicksearch/ResultsModel.java
     * 
     * @param tc  topComponent utilis?? par l'action de la r??ponse (pour donner le focus au relev??)
     * @param resquestPattern chaine de caract??res recherch??e 
     * @param response   r??ponse ?? renseigner
     */
    // 

    private void searchInModel(ReleveTopComponent tc, String resquest, SearchResponse response ) {
        // S'il y a plusieurs mots s??par??s par des espaces dans la requete, 
        // j'ajoute de parenth??ses autour chaque mots pour que la recherche
        // regexp retourne les positions des mots trouv??s dans le r??sultat
        // qui seront utilis??es pour positionner les caract??res en gras
        String resquestPattern = ".*("+resquest.replaceAll(" +", ").+(")+").*";

        RecordModel model = tc.getDataManager().getDataModel();
        
        for (int indexRecord=0; indexRecord < model.getRowCount(); indexRecord++) {
            Record record = model.getRecord(indexRecord);
            
            for (int i =0 ; i < lastNameFieldTypes.length ; i++) {
                Field lastName  = record.getField(lastNameFieldTypes[i]);
                Field firstName = record.getField(firstNameFieldTypes[i]);             
                if (lastName != null && firstName != null ) {
                    String resultDisplay = lastName.toString() + " " + firstName.toString();
                    String resultSearch  = Normalizer.normalize(resultDisplay, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase(); 
                    
                    Pattern pattern = Pattern.compile(resquestPattern);
                    Matcher matcher = pattern.matcher(resultSearch);
                    if (matcher.matches()) {
                        int nb = matcher.groupCount();
                        if( nb >= 1) {
                            // je construit la r??ponse en commen??ant par <html>
                            // pour desactiver la mise ne gras automatique
                            StringBuilder sbDisplay = new StringBuilder("<html>");
                            if (matcher.start(1) > 0) {
                                // j'ajoute les caract??res situ??s entre le d??but du r??sultat et le premier groupe
                                sbDisplay.append( resultDisplay.substring(0, matcher.start(1)) );
                            }
                            for (int j = 1; j <= nb; j++) {
                                // je mets en gras les caract??res du groupe
                                sbDisplay.append("<b>").append( resultDisplay.substring(matcher.start(j), matcher.end(j)) ).append("</b>");
                                
                                if(j <nb ) {
                                    // j'ajoute les caract??res pr??sents entre le groupe et le groupe suivant
                                    sbDisplay.append( resultDisplay.substring(matcher.end(j), matcher.start(j+1)) );                                    
                                } else {
                                    // c'est le dernier groupe trouv??
                                    if (matcher.end(j) < resultDisplay.length() ) {
                                        // j'ajoute les caract??res pr??sents entre le dernier groupe et la fin du resultat
                                        sbDisplay.append( resultDisplay.substring(matcher.end(j)) );
                                    }
                                }
                            }  
                            // j'ajoute le role de l'individu dans le relev??
                            sbDisplay.append(", ").append(EditorBeanGroup.getGroup(record.getType(), lastNameFieldTypes[i]).getTitle());
                            // j'ajoute la date du relev??
                            sbDisplay.append(", ").append(record.getFieldValue(FieldType.eventDate));
                            if (!tc.getDataManager().getCityName().isEmpty()) {
                                // j'ajoute le lieu du relev?? (ville) s'il n'est pas vide 
                                sbDisplay.append(" ").append(tc.getDataManager().getCityName());
                            }
                            if (!response.addResult(createAction(tc, record, lastNameFieldTypes[i]), sbDisplay.toString() ) ) {
                                // j'arrete la recherche si la derni??re r??ponse n'est pas accept??e
                                return;
                            }
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

