/*
 * Ancestris - http://www.ancestris.org
 *
 * Copyright 2019 Ancestris
 *
 * Author: Zurga.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package ancestris.modules.gedcom.searchduplicates;

import ancestris.modules.gedcom.matchers.IndiMatcher;
import ancestris.modules.gedcom.matchers.PotentialMatch;
import genj.gedcom.Entity;
import genj.gedcom.Gedcom;
import genj.gedcom.Indi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Get Duplicates of one Indi.
 *
 * @author Zurga
 */
public class IndiDuplicatesFinder implements Runnable {

    private final Gedcom gedcom;
    private final Indi cujus;

    public IndiDuplicatesFinder(Indi indi) {
        cujus = indi;
        gedcom = indi.getGedcom();
    }

    @Override
    public void run() {
        final List<PotentialMatch<? extends Entity>> matchesLinkedList = new LinkedList<>();
        final Map<String, Integer> duplicatesHashMap = new HashMap<>();
        if (gedcom == null) {
            return;
        }
        final IndiMatcher matcher = new IndiMatcher();
        List<Indi> entities = new ArrayList<>();
        for (Entity e : gedcom.getEntities(Gedcom.INDI)) {
            if (e instanceof Indi) {
                entities.add((Indi) e);
            }
        }
        List<PotentialMatch<Indi>> potentialMatches = matcher.getDuplicates(cujus, entities);
        
        if (potentialMatches.isEmpty()) {
            return;
        }

        // Swap matches so that left entity is with smaller id
        for (PotentialMatch<Indi> e : potentialMatches) {
            String idLeft = e.getLeft().getId();
            String idRight = e.getRight().getId();
            if (idLeft.compareToIgnoreCase(idRight) > 0) {
                e.swap();
            }
        }

        matchesLinkedList.addAll(potentialMatches);
        duplicatesHashMap.put(Gedcom.INDI, potentialMatches.size());

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
        Runnable drc = new DuplicateResultCreator(gedcom, matchesLinkedList);
        drc.run();
    }
}
