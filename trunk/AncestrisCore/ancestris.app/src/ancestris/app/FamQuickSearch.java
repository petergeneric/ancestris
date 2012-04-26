/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ancestris.app;

import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Fam;
import genj.view.SelectionSink;
import ancestris.gedcom.GedcomDirectory;
import ancestris.util.Utilities;
import java.awt.Component;
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
        synchronized (this) {
            for (Context context : GedcomDirectory.getInstance().getContexts()) {
                for (Fam fam : context.getGedcom().getFamilies()) {
                    if (Utilities.wordsMatch(fam.toString(false).toLowerCase(),request.getText().toLowerCase())) {
                        if (!response.addResult(createAction(fam), fam.toString(false))) {
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
                SelectionSink.Dispatcher.fireSelection((Component) null, new Context(entity), true);
            }
        };
    }
}
