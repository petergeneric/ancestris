package ancestris.modules.gedcom.utilities;

import genj.gedcom.Entity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lemovice
 */
public abstract class EntityMatcher<T extends Entity> implements Comparator<T> {

    private static final Logger log = Logger.getLogger(EntityMatcher.class.getName());

    public List<PotentialMatch<T>> getPotentialMatches(List<T> left, List<T> right) throws InterruptedException {
        List<PotentialMatch<T>> matches = new ArrayList<PotentialMatch<T>>();
        List<String> compareDone = new ArrayList<String>();

        for (T leftEntity : left) {
            compareDone.add(leftEntity.getId());
            int numberOfCompares = 0;
            log.log(Level.INFO, "comparing {0} ...", leftEntity.getId());
            for (T rightEntity : right) {
                if (compareDone.contains(rightEntity.getId()) == false) {
                    numberOfCompares++;
                    int diff = compare(leftEntity, rightEntity);
                    if (diff > 50) {
                        matches.add(new PotentialMatch<T>(leftEntity, rightEntity, diff));
                    }
                }
            }
            log.log(Level.INFO, "... done {0} comparisons", numberOfCompares);
            Thread.sleep(0); //throws InterruptedException is the task was cancelled

        }

        log.log(Level.INFO, "Potential matches: {0}", matches.size());

        return matches;
    }
}
