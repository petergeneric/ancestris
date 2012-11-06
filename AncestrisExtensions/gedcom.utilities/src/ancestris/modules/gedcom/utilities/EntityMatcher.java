package ancestris.modules.gedcom.utilities;

import genj.gedcom.Entity;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lemovice
 */
public abstract class EntityMatcher<T extends Entity> implements Comparator<T> {

    private static final Logger log = Logger.getLogger(EntityMatcher.class.getName());

    public List<PotentialMatch<T>> getPotentialMatches(List<T> entities) throws InterruptedException {
        Map<String, List<T>> sortedEntities = new HashMap<String, List<T>>();
        List<PotentialMatch<T>> matches = new ArrayList<PotentialMatch<T>>();
        Map<String, List<String>> compareDone = new HashMap<String, List<String>>();

        // Sorting Entities
        sortedEntities = sort(entities);

        log.log(Level.INFO, "sorted entities {0} ...", sortedEntities.size());
        for (String key : sortedEntities.keySet()) {
            List<T> entityList = sortedEntities.get(key);

            log.log(Level.INFO, "entities to compare {0} ...", entityList.size());

            if (entityList.size() > 1) {
                for (T leftEntity : entityList) {
                    List<String> idCompared = new ArrayList<String>();
                    compareDone.put(leftEntity.getId(), idCompared);

                    log.log(Level.INFO, "comparing {0} with {1} entities ...", new Object [] {leftEntity.getId(), entityList.size()});

                    int numberOfCompares = 0;
                    for (T rightEntity : entityList) {
                        // This two entities have not been compared
                        idCompared.add(rightEntity.getId());
                        if (compareDone.get(rightEntity.getId()) == null || compareDone.get(rightEntity.getId()) != null && compareDone.get(rightEntity.getId()).contains(leftEntity.getId()) == false) {
                            numberOfCompares++;
                            int diff = compare(leftEntity, rightEntity);
                            if (diff > 50) {
                                matches.add(new PotentialMatch<T>(leftEntity, rightEntity, diff));
                            }
                        }
                    }
                    log.log(Level.INFO, "... {0} comparisons done", numberOfCompares);
                    Thread.sleep(0); //throws InterruptedException is the task was cancelled
                }
            }
        }

        log.log(Level.INFO, "Potential matches: {0}", matches.size());

        return matches;
    }

    private Map<String, List<T>> sort(List<T> entities) {
        Map<String, List<T>> map = new HashMap<String, List<T>>();
        for (T entity : entities) {
            String[] keys = getKeys(entity);
            for (String key : keys) {
                if (map.containsKey(key)) {
                    List<T> arrayList = map.get(key);
                    arrayList.add(entity);
                } else {
                    List<T> arrayList = new ArrayList<T>();
                    arrayList.add(entity);
                    map.put(key, arrayList);
                }
            }
        }

        return map;
    }

    protected abstract String[] getKeys(T entity);
}
