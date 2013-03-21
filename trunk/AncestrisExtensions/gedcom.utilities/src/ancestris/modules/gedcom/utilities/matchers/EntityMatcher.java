package ancestris.modules.gedcom.utilities.matchers;

import genj.gedcom.Entity;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lemovice
 */
public abstract class EntityMatcher<E extends Entity, O extends MatcherOptions> implements Comparator<E>, Options<O> {

    private static final Logger log = Logger.getLogger(EntityMatcher.class.getName());
    protected O options = null;

    public List<PotentialMatch<E>> getPotentialMatches(List<E> entities) throws InterruptedException {
        Map<String, List<E>> sortedEntities;
        List<PotentialMatch<E>> matches = new ArrayList<PotentialMatch<E>>();
        Map<String, List<String>> compareDone = new HashMap<String, List<String>>();

        // Sorting Entities
        sortedEntities = sort(entities);

        log.log(Level.FINE, "sorted entities {0} ...", sortedEntities.size());
        for (String key : sortedEntities.keySet()) {
            List<E> entityList = sortedEntities.get(key);

            log.log(Level.FINE, "entities to compare {0} ...", entityList.size());

            if (entityList.size() > 1) {
                for (E leftEntity : entityList) {
                    List<String> idCompared = new ArrayList<String>();
                    compareDone.put(leftEntity.getId(), idCompared);

                    log.log(Level.FINE, "comparing {0} with {1} entities ...", new Object[]{leftEntity.getId(), entityList.size()});

                    int numberOfCompares = 0;
                    for (E rightEntity : entityList) {
                        // This two entities have not been compared
                        idCompared.add(rightEntity.getId());
                        if (compareDone.get(rightEntity.getId()) == null || compareDone.get(rightEntity.getId()) != null && compareDone.get(rightEntity.getId()).contains(leftEntity.getId()) == false) {
                            numberOfCompares++;
                            int diff = compare(leftEntity, rightEntity);
                            if (diff > 50) {
                                matches.add(new PotentialMatch<E>(leftEntity, rightEntity, diff));
                            }
                        }
                    }
                    log.log(Level.FINE, "... {0} comparisons done", numberOfCompares);
                    Thread.sleep(0); //throws InterruptedException is the task was cancelled
                }
            }
        }

        log.log(Level.INFO, "Potential matches: {0}", matches.size());

        return matches;
    }

    private Map<String, List<E>> sort(List<E> entities) {
        Map<String, List<E>> map = new HashMap<String, List<E>>();
        for (E entity : entities) {
            String[] keys = getKeys(entity);
            for (String key : keys) {
                if (map.containsKey(key)) {
                    List<E> arrayList = map.get(key);
                    arrayList.add(entity);
                } else {
                    List<E> arrayList = new ArrayList<E>();
                    arrayList.add(entity);
                    map.put(key, arrayList);
                }
            }
        }

        return map;
    }

    @Override
    public O getOptions() {
        return options;
    }

    @Override
    public void setOptions(O options) {
        this.options = options;
    }

    protected abstract String[] getKeys(E entity);
}
