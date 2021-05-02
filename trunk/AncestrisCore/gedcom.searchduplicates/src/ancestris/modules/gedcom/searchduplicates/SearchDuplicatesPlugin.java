package ancestris.modules.gedcom.searchduplicates;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.gedcom.matchers.*;
import genj.gedcom.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lemovice left and right entities could be the same.
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
@NbBundle.Messages({"SearchDuplicatesPlugin.firstButton=Go to first duplicate (Ctrl + Up)",
    "SearchDuplicatesPlugin.previousButton=Go to previous duplicate (Ctrl + Left)",
    "SearchDuplicatesPlugin.swapButton=Swap left and right entities (Ctrl + Backspace)",
    "SearchDuplicatesPlugin.nextButton=Go to next duplicate (Ctrl + Right)",
    "SearchDuplicatesPlugin.lastButton=Go to last duplicate (Ctrl + Down)",
    "SearchDuplicatesPlugin.mergeButton=<html>Merge checked properties of the right<br>into the entity of the left,<br>and deletes the entity on the right (Ctrl + Enter)</html>",
    "SearchDuplicatesPlugin.cleanButton=<html>Remove duplicates including<br>the entity on the right (Ctrl + Space)</html>",
    "SearchDuplicatesPlugin.closeButton=<html>Stop the duplicates merge<br>and close the window (Esc)</html>",
    "SearchDuplicatesPlugin.noSelectedProperties=Nothing is checked on the entity on the right.\nThis will only delete it.\nOK to delete it ?"})
public class SearchDuplicatesPlugin extends AncestrisPlugin implements Runnable {

    private static final Logger LOG = Logger.getLogger(SearchDuplicatesPlugin.class.getName());
    private final Gedcom gedcom;
    private final TreeMap<String, EntityMatcher> entitiesMatchers = new TreeMap<String, EntityMatcher>() {
        {
            put(Gedcom.INDI, new IndiMatcher());
            put(Gedcom.FAM, new FamMatcher());
            put(Gedcom.NOTE, new NoteMatcher());
            put(Gedcom.SOUR, new SourceMatcher());
            put(Gedcom.REPO, new RepositoryMatcher());
            put(Gedcom.SUBM, new SubmitterMatcher());
            put(Gedcom.OBJE, new MediaMatcher());
        }
    };
    private final List<String> entities2Ckeck;
    private final Map<String, ? extends MatcherOptions> selectedOptions;
    private ProgressHandle progressHandle;

    public SearchDuplicatesPlugin() {
        this.gedcom = null;
        this.entities2Ckeck = null;
        this.selectedOptions = null;
    }

    public SearchDuplicatesPlugin(Gedcom leftGedcom, List<String> entities2Ckeck, Map<String, ? extends MatcherOptions> selectedOptions, ProgressHandle progressHandle) {
        this.gedcom = leftGedcom;
        this.entities2Ckeck = entities2Ckeck;
        this.selectedOptions = selectedOptions;
        this.progressHandle = progressHandle;
    }

    @Override
    public void run() {
        final List<PotentialMatch<? extends Entity>> matchesLinkedList = new LinkedList<>();
        final Map<String, Integer> duplicatesHashMap = new HashMap<>();
        if (gedcom == null) {
            return;
        }
        
        // set progress max
        int num = 0;
        for (String tag : entities2Ckeck) {
            num += gedcom.getEntities(tag).size();
        }
        progressHandle.start(); //we must start the PH before we switch to determinate
        progressHandle.switchToDeterminate(num);
        
        try {
            
            // Get matches by block of entity type
            num = 0;
            for (String tag : entities2Ckeck) {
                List<? extends Entity> entities = new ArrayList<>(gedcom.getEntities(tag));

                LOG.log(Level.FINE, "Checking: {0}", tag);
                switch (tag) {
                    case Gedcom.INDI:
                        (entitiesMatchers.get(tag)).setOptions((IndiMatcherOptions) selectedOptions.get(Gedcom.INDI));
                        break;
                    case Gedcom.FAM:
                        (entitiesMatchers.get(tag)).setOptions((FamMatcherOptions) selectedOptions.get(Gedcom.FAM));
                        break;
                    case Gedcom.NOTE:
                        (entitiesMatchers.get(tag)).setOptions((NoteMatcherOptions) selectedOptions.get(Gedcom.NOTE));
                        break;
                    case Gedcom.REPO:
                        (entitiesMatchers.get(tag)).setOptions((RepositoryMatcherOptions) selectedOptions.get(Gedcom.REPO));
                        break;
                    case Gedcom.SOUR:
                        (entitiesMatchers.get(tag)).setOptions((SourceMatcherOptions) selectedOptions.get(Gedcom.SOUR));
                        break;
                    case Gedcom.SUBM:
                        (entitiesMatchers.get(tag)).setOptions((SubmitterMatcherOptions) selectedOptions.get(Gedcom.SUBM));
                        break;
                    case Gedcom.OBJE:
                        (entitiesMatchers.get(tag)).setOptions((MediaMatcherOptions) selectedOptions.get(Gedcom.OBJE));
                        break;
                    default:
                        break;
                }
                // Get block
                List<PotentialMatch<? extends Entity>> potentialMatches = (entitiesMatchers.get(tag)).getPotentialMatches(entities, progressHandle, num);
                num += entities.size();
                
                // Swap matches so that left entity is with smaller id
                for (PotentialMatch<? extends Entity> e : potentialMatches) {
                    String idLeft = e.getLeft().getId();
                    String idRight = e.getRight().getId();
                    if (idLeft.compareToIgnoreCase(idRight) > 0) {
                        e.swap();
                    }
                }
                
                matchesLinkedList.addAll(potentialMatches);
                duplicatesHashMap.put(tag, potentialMatches.size());
            }
            
                // Sort matches by certainty then entity name
            Collections.sort(matchesLinkedList, new Comparator<PotentialMatch<? extends Entity>>() {
                @Override
                public int compare(PotentialMatch<? extends Entity> e1, PotentialMatch<? extends Entity> e2) {
                    if (e2.getCertainty() - e1.getCertainty() != 0) {
                        return e2.getCertainty() - e1.getCertainty();
                    }
                    return e1.getLeft().toString(true).toLowerCase().compareTo(e2.getLeft().toString(true).toLowerCase());
                }
            });

            // Display them
            SwingUtilities.invokeLater(new DuplicateResultCreator(gedcom, matchesLinkedList));
        } catch (InterruptedException ex) {
            LOG.log(Level.FINE, "the task was CANCELLED");
        }
    }
    
    
    
}
