package ancestris.modules.gedcom.searchduplicates;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.gedcom.utilities.matchers.*;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Property;
import genj.gedcom.PropertyXRef;
import java.awt.Dialog;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lemovice left and right entities could be the same.
 */
@ServiceProvider(service = ancestris.core.pluginservice.PluginInterface.class)
public class SearchDuplicatesPlugin extends AncestrisPlugin implements Runnable {

    private static final Logger log = Logger.getLogger(SearchDuplicatesPlugin.class.getName());
    private Gedcom gedcom;
    private TreeMap<String, EntityMatcher> entitiesMatchers = new TreeMap<String, EntityMatcher>() {

        {
            put(Gedcom.INDI, new IndiMatcher());
            put(Gedcom.FAM, new FamMatcher());
            put(Gedcom.NOTE, new NoteMatcher());
            put(Gedcom.SOUR, new SourceMatcher());
            put(Gedcom.REPO, new RepositoryMatcher());
            put(Gedcom.SUBM, new SubmitterMatcher());
        }
    };
    private final List<String> entities2Ckeck;
    Map<String, ? extends Options> selectedOptions;

    public SearchDuplicatesPlugin() {
        this.gedcom = null;
        this.entities2Ckeck = null;
        this.selectedOptions = null;
    }

    public SearchDuplicatesPlugin(Gedcom leftGedcom, List<String> entities2Ckeck, Map<String, ? extends Options> selectedOptions) {
        this.gedcom = leftGedcom;
        this.entities2Ckeck = entities2Ckeck;
        this.selectedOptions = selectedOptions;
    }

    @Override
    public void run() {
        final LinkedList<PotentialMatch<? extends Entity>> matchesLinkedList = new LinkedList<PotentialMatch<? extends Entity>>();
        final HashMap<String, Integer> duplicatesHashMap = new HashMap<String, Integer>();
        if (gedcom == null) {
            return;
        }
        try {
            for (String tag : entities2Ckeck) {
                List<? extends Entity> entities = new ArrayList(gedcom.getEntities(tag));

                log.log(Level.INFO, "Checking: {0}", tag);
                if (entities != null) {
                    if (tag.equals(Gedcom.INDI)) {
                        (entitiesMatchers.get(tag)).setOptions((IndiMatcherOptions) selectedOptions.get(Gedcom.INDI));
                    } else if (tag.equals(Gedcom.FAM)) {
                        (entitiesMatchers.get(tag)).setOptions((FamMatcherOptions) selectedOptions.get(Gedcom.FAM));
                    } else if (tag.equals(Gedcom.NOTE)) {
                        (entitiesMatchers.get(tag)).setOptions((NoteMatcherOptions) selectedOptions.get(Gedcom.NOTE));
                    } else if (tag.equals(Gedcom.REPO)) {
                        (entitiesMatchers.get(tag)).setOptions((RepositoryMatcherOptions) selectedOptions.get(Gedcom.REPO));
                    } else if (tag.equals(Gedcom.SOUR)) {
                        (entitiesMatchers.get(tag)).setOptions((SourceMatcherOptions) selectedOptions.get(Gedcom.SOUR));
                    } else if (tag.equals(Gedcom.SUBM)) {
                        (entitiesMatchers.get(tag)).setOptions((SubmitterMatcherOptions) selectedOptions.get(Gedcom.SUBM));
                    }
                    List<PotentialMatch<? extends Entity>> potentialMatches = (entitiesMatchers.get(tag)).getPotentialMatches(entities);
                    Collections.sort(potentialMatches, new Comparator<PotentialMatch>() {

                        @Override
                        public int compare(PotentialMatch e1, PotentialMatch e2) {
                            return e2.getCertainty() - e1.getCertainty();
                        }
                    });
                    matchesLinkedList.addAll(potentialMatches);
                    duplicatesHashMap.put(tag, potentialMatches.size());
                }
            }

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    // There is duplicates let displaying them
                    if (matchesLinkedList.size() > 0) {
                        SearchDuplicatesResultPanel entityViewPanel = new SearchDuplicatesResultPanel(matchesLinkedList);
                        DialogDescriptor checkDuplicatePanelDescriptor = new DialogDescriptor(
                                entityViewPanel,
                                NbBundle.getMessage(SearchDuplicatesPlugin.class, "CheckDuplicatePanelDescriptor.title"),
                                true,
                                new Object[]{DialogDescriptor.CLOSED_OPTION},
                                DialogDescriptor.CLOSED_OPTION,
                                DialogDescriptor.DEFAULT_ALIGN,
                                null,
                                null);

                        Dialog dialog = DialogDisplayer.getDefault().createDialog(checkDuplicatePanelDescriptor);
                        dialog.setVisible(true);
                        dialog.setModal(false);
                        dialog.toFront();
                    } else {
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SearchDuplicatesPlugin.class, "CheckDuplicates.noDuplicates"), NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                }
            });
        } catch (InterruptedException ex) {
            log.log(Level.INFO, "the task was CANCELLED");
        }
    }

    /**
     * Copy properties beneath a property to another property (copy a cluster)
     */
    private void MergePropertiesRecursively(Property srcProperty, Property destProperty) {

        if (srcProperty == null || destProperty == null) {
            return;
        }

        Property[] srcProperties = srcProperty.getProperties();

        for (Property property : srcProperties) {
            // Xref properties shall not be copy
            if (!(property instanceof PropertyXRef)) {
                MergePropertiesRecursively(property, destProperty.addProperty(property.getTag(), property.getValue()));
            } else {
                // Update Xref
                PropertyXRef propertyXRef = (PropertyXRef) property;
                PropertyXRef target = propertyXRef.getTarget();
                if (target != null) {
                    MergePropertiesRecursively(property, destProperty.addProperty(property.getTag(), property.getValue()));
                } else {
                }
            }
        }
    }
}
