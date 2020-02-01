/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.app;

import ancestris.gedcom.GedcomDirectory;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import ancestris.util.Utilities;
import ancestris.view.SelectionDispatcher;
import java.text.Normalizer;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;


public class FamQuickSearch implements SearchProvider {
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
            for (Context context : GedcomDirectory.getDefault().getContexts()) {
                for (Fam fam : context.getGedcom().getFamilies()) {
                    String str1 = Normalizer.normalize(fam.toString(true), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");  
                    String str2 = Normalizer.normalize(req, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");  
                    if (Utilities.wordsMatch(str1.toLowerCase(), str2.toLowerCase())) {
                        if (!response.addResult(createAction(fam), str1)) {
                            return;
                        }
                    }
                }
            }
        }
    }

    private Runnable createAction(final Entity entity) {
        return new Runnable() {

            public void run() {
                SelectionDispatcher.fireSelection(new Context(entity));
            }
        };
    }
}
