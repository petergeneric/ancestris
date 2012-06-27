package ancestris.modules.gedcom.utilities;

import genj.gedcom.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lemovice
 */
public abstract class EntityMatcher<T extends Entity> implements Matcher<T> {

    private static final Logger log = Logger.getLogger(EntityMatcher.class.getName());

    @Override
    public List<PotentialMatch<T>> getPotentialMatches(List<T> left, List<T> right) {
        List<PotentialMatch<T>> matches = new ArrayList<PotentialMatch<T>>();

        int numberOfCompares = 0;
        for (T leftEntity : left) {
            for (T rightEntity : right) {
                if (leftEntity.getId().equals(rightEntity.getId()) == false) {
                    numberOfCompares++;
                    int diff = compareEntities(leftEntity, rightEntity);
                    if (diff > 50) {
                        matches.add(new PotentialMatch<T>(leftEntity, rightEntity, diff));
                    }
                }
            }
        }

        log.log(Level.INFO, "Total number of compares: {0}", numberOfCompares);
        log.log(Level.INFO, "Potential matches: {0}", matches.size());

        return matches;
    }
}
