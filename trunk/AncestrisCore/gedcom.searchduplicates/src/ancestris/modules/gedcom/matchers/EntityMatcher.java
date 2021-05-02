package ancestris.modules.gedcom.matchers;

import genj.gedcom.Entity;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;

/**
 *
 * @author lemovice
 */
public abstract class EntityMatcher<E extends Entity, O extends MatcherOptions> implements Comparator<E>, Options<O> {

    private static final Logger log = Logger.getLogger(EntityMatcher.class.getName());
    protected O options = null;

    public List<PotentialMatch<E>> getPotentialMatches(List<E> entities, ProgressHandle progressHandle, int counter) throws InterruptedException {
        Map<String, List<E>> sortedEntities;
        List<PotentialMatch<E>> matches = new ArrayList<>();
        Map<String, List<String>> compareDone = new HashMap<>();

        // Sorting Entities
        sortedEntities = sort(entities);

        log.log(Level.FINE, "sorted entities {0} ...", sortedEntities.size());
        for (String key : sortedEntities.keySet()) {
            List<E> entityList = sortedEntities.get(key);

            log.log(Level.FINE, "entities to compare {0} ...", entityList.size());

            if (entityList.size() > 1) {
                for (E leftEntity : entityList) {
                    progressHandle.progress("Recherche de doublons (" + matches.size() + " doublons trouvés)", counter++);
                    getDuplicates(compareDone, leftEntity, entityList, matches);
                    Thread.sleep(0); //throws InterruptedException is the task was cancelled
                    if (matches.size() >= 1000) {
                        break;
                    }
                }
            }
        }

        log.log(Level.FINE, "Potential matches: {0}", matches.size());

        return matches;
    }
    
    /**
     * Get Duplicates of one entity.
     * @param leftEntity Entity to compare
     * @param entityList List of entities
     * @return List of potential Matches.
     */
    public List<PotentialMatch<E>> getDuplicates(E leftEntity, List<E> entityList) {
        List<PotentialMatch<E>> matches = new ArrayList<>();
        Map<String, List<String>> compareDone = new HashMap<>();
        getDuplicates(compareDone, leftEntity, entityList, matches);
        return matches;
    }

    private void getDuplicates(Map<String, List<String>> compareDone, E leftEntity, List<E> entityList, List<PotentialMatch<E>> matches) {
        List<String> idCompared = new ArrayList<>();
        compareDone.put(leftEntity.getId(), idCompared);
        
        log.log(Level.FINE, "comparing {0} with {1} entities ...", new Object[]{leftEntity.getId(), entityList.size()});
        
        int numberOfCompares = 0;
        for (E rightEntity : entityList) {
            // This two entities have not been compared
            idCompared.add(rightEntity.getId());
            if (compareDone.get(rightEntity.getId()) == null || compareDone.get(rightEntity.getId()) != null && compareDone.get(rightEntity.getId()).contains(leftEntity.getId()) == false) {
                numberOfCompares++;
                int diff = compare(leftEntity, rightEntity);
                if (diff < 0) {
                    continue;
                }
                if (diff > 50 && matches.size() < 1000) {
                    matches.add(new PotentialMatch<E>(leftEntity, rightEntity, diff));
                    if (matches.size() >= 1000) {
                        break;
                    }
                }
            }
        }
        log.log(Level.FINE, "... {0} comparisons done", numberOfCompares);
    }

    private Map<String, List<E>> sort(List<E> entities) {
        Map<String, List<E>> map = new HashMap<>();
        for (E entity : entities) {
            String[] keys = getKeys(entity);
            for (String key : keys) {
                if (map.containsKey(key)) {
                    List<E> arrayList = map.get(key);
                    arrayList.add(entity);
                } else {
                    List<E> arrayList = new ArrayList<>();
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
