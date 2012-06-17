package ancestris.modules.commonAncestor;

import genj.gedcom.Indi;
import ancestris.modules.commonAncestor.quicksearch.spi.SearchProvider;
import ancestris.modules.commonAncestor.quicksearch.spi.SearchRequest;
import ancestris.modules.commonAncestor.quicksearch.spi.SearchResponse;
import ancestris.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provider for quicksearch widget
 */
@ServiceProvider(service=SearchProvider.class)
public class QuickSearchProvider implements SearchProvider {
    
    // Panel where selected individu is showns
    SamePanel samePanel;
    
    public QuickSearchProvider() {
    }
    
    public void setSamePanel(SamePanel samePanel) {
        this.samePanel = samePanel;
    }    
    
    /**
     * Method is called by infrastructure when search operation was requested.
     * Implementors should evaluate given request and fill response object with
     * apropriate results
     *
     * @param request Search request object that contains information what to search for
     * @param response Search response object that stores search results. Note that it's important to react to return value of SearchResponse.addResult(...) method and stop computation if false value is returned.
     */
    public void evaluate(final SearchRequest request, final SearchResponse response) {
        synchronized (this) {
            if (samePanel.getContext() != null) {
                if (samePanel.getContext().getGedcom() != null) {
                    for (Indi indi : samePanel.getContext().getGedcom().getIndis()) {
                        if (Utilities.wordsMatch(indi.getName().toLowerCase(),request.getText().toLowerCase())) {
                            String categoryName = response.getCatResult().getCategory().getName();
                            if (!response.addResult(new createAction(indi, categoryName), indi.getName() )) {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * action called when individu is selected 
     * 
     */
    private class createAction implements Runnable {

        private Indi indi;
        String category;

        public createAction( Indi indi,  String category) {
            this.indi = indi;
            this.category = category;
        }

        public void run() {
            if (category.equals("Individu1")) {
                samePanel.setIndividu1(indi);
            }
            if (category.equals("Individu2")) {
                samePanel.setIndividu2(indi);
            }
        }
    }
}
