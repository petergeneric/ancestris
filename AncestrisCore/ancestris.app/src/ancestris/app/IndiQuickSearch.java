/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ancestris.app;

import ancestris.gedcom.GedcomDirectory;
import genj.gedcom.Context;
import genj.gedcom.Entity;
import genj.gedcom.Indi;
import ancestris.view.SelectionSink;
import ancestris.util.Utilities;
import java.awt.Component;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

public class IndiQuickSearch implements SearchProvider {
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
            for (Context context : GedcomDirectory.getDefault().getContexts()) {
                for (Indi indi : context.getGedcom().getIndis()) {
                    if (Utilities.wordsMatch(indi.getName().toLowerCase(),request.getText().toLowerCase())) {
                        if (!response.addResult(createAction(indi), indi.getName())) {
                            return;
                        }
                    }
                }
                ;
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
