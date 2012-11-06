package ancestris.modules.gedcom.checkduplicates;

import ancestris.core.pluginservice.AncestrisPlugin;
import ancestris.modules.gedcom.utilities.EntityMatcher;
import ancestris.modules.gedcom.utilities.IndiMatcher;
import ancestris.modules.gedcom.utilities.PotentialMatch;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
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
public class CheckDuplicates extends AncestrisPlugin implements Runnable {

    private static final Logger log = Logger.getLogger(CheckDuplicates.class.getName());
    private Gedcom gedcom;
    private TreeMap<String, EntityMatcher> entitiesMatchers = new TreeMap<String, EntityMatcher>() {

        {
            put(Gedcom.INDI, new IndiMatcher());
//            put(Gedcom.FAM, new FamMatcher());
//            put(Gedcom.NOTE, new NoteMatcher());
//            put(Gedcom.SOUR, new SourceMatcher());
//            put(Gedcom.REPO, new RepositoryMatcher());
//            put(Gedcom.SUBM, new SubmitterMatcher());
        }
    };
    
    public CheckDuplicates() {
        this.gedcom = null;
    }

    public CheckDuplicates(Gedcom leftGedcom) {
        this.gedcom = leftGedcom;
    }

    @Override
    public void run() {
        final LinkedList<PotentialMatch<? extends Entity>> matchesLinkedList = new LinkedList<PotentialMatch<? extends Entity>>();
        if (gedcom == null) {
            return;
        }
        try {
            for (String tag : entitiesMatchers.keySet()) {
                List<? extends Entity> entities = new ArrayList(gedcom.getEntities(tag));

                log.log(Level.INFO, "Checking: {0}", tag);

                if (entities != null) {
                    List<PotentialMatch<? extends Entity>> potentialMatches = (entitiesMatchers.get(tag)).getPotentialMatches(entities);
                    matchesLinkedList.addAll(potentialMatches);
                }
            }

            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    // There is duplicates let displaying them
                    if (matchesLinkedList.size() > 0) {
                        CheckDuplicatesPanel entityViewPanel = new CheckDuplicatesPanel(matchesLinkedList);
                        DialogDescriptor checkDuplicatePanelDescriptor = new DialogDescriptor(
                                entityViewPanel,
                                NbBundle.getMessage(CheckDuplicates.class, "CheckDuplicatePanelDescriptor.title"),
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
                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(CheckDuplicates.class, "CheckDuplicates.noDuplicates"), NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);

                    }
                }
            });
        } catch (InterruptedException ex) {
            log.log(Level.INFO, "the task was CANCELLED");
        }
    }
}
