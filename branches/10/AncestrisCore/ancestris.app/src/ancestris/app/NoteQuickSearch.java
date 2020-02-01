/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.gedcom.GedcomDirectory;
import ancestris.util.Utilities;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Gedcom;
import genj.gedcom.Note;
import genj.gedcom.Property;
import genj.gedcom.PropertyNote;
import java.text.Normalizer;
import java.util.Collection;
import java.util.List;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

public class NoteQuickSearch implements SearchProvider {
    
    /**
     * Method is called by infrastructure when search operation was requested.
     * Implementors should evaluate given request and fill response object with
     * apropriate results
     *
     * @param request Search request object that contains information what to search for
     * @param response Search response object that stores search results. Note that it's important to react to return value of SearchResponse.addResult(...) method and stop computation if false value is returned.
     */
    public void evaluate(SearchRequest request, SearchResponse response) {
        String req = request.getText().replace("(", "\\(").replace(")", "\\)");
        synchronized (this) {
            String str2 = Normalizer.normalize(req, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");  
            for (Context context : GedcomDirectory.getDefault().getContexts()) {
                for (Note note : context.getGedcom().getNotes()) {
                    String str1 = Normalizer.normalize(note.getDisplayValue(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");  
                    String rep = Utilities.getPhraseBit(str1 + " (" + note.getId() + ")", str2);
                    if (rep == null) {
                        continue;
                    }
                    if (!response.addResult(createAction(note), rep)) {
                        return;
                    }
                }
                String[] ENTITIES = {Gedcom.INDI, Gedcom.FAM};
                for (String type : ENTITIES) {
                    Collection<Entity> entities = (Collection<Entity>) context.getGedcom().getEntities(type);
                    for (Entity entity : entities) {
                        List<Property> properties = entity.getAllProperties("NOTE");
                        for (Property noteProp : properties) {
                            if (noteProp == null || noteProp instanceof PropertyNote) {
                                continue;
                            }
                            String text = noteProp.getDisplayValue().trim();
                            if (text.isEmpty()) {
                                continue;
                            }
                            String str1 = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");  
                            String rep = Utilities.getPhraseBit(str1, str2);
                            if (rep == null) {
                                continue;
                            }
                            rep += " (" + noteProp.getEntity().getId() + ")";
                            if (!response.addResult(createAction(noteProp), rep)) {
                                return;
                            } else {
                                break; // no need to stay on that entity, it is already added to the results
                            }
                        }
                    }
                }
            }
        }
    }


    private Runnable createAction(final Property property) {
        return new Runnable() {

            public void run() {
                SelectionDispatcher.fireSelection(new Context(property));
            }
        };
    }
    
    
}
