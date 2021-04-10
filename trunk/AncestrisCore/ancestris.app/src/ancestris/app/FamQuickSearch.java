/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.app;

import ancestris.gedcom.GedcomDirectory;
import ancestris.util.Utilities;
import ancestris.view.SelectionDispatcher;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.gedcom.Indi;
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
                    String str1 = Normalizer.normalize(getStringFromFam(fam), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");  
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
            
    private String getStringFromFam(Fam fam) {
        String ret = "";
        
        Indi husb = fam.getHusband();
        if (husb != null) {
            String[] names = husb.getLastNames();
            for (String name : names) {
                ret += name + " ";
            }
            String[] firstnames = husb.getFirstNames();
            for (String firstname : firstnames) {
                ret += firstname + " ";
            }
        }
        Indi wife = fam.getWife();
        if (wife != null) {
            String[] names = wife.getLastNames();
            for (String name : names) {
                ret += name + " ";
            }
            String[] firstnames = wife.getFirstNames();
            for (String firstname : firstnames) {
                ret += firstname + " ";
            }
        }
        ret += fam.toString(true);
        return ret.toLowerCase();
    }
    
}
